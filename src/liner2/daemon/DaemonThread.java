package liner2.daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.chunker.factory.ChunkerManager;
import liner2.features.TokenFeatureGenerator;
import liner2.tools.ParameterException;

import liner2.LinerOptions;
import liner2.Main;

/**
 * Daemon class for NER WebService.
 * @author Maciej Janicki
 */
public class DaemonThread extends Thread {
	private static final int DEFAULT_MAX_THREADS = 5;

	String db_addr, myAddr, ip;
	int port, myId = -1;
	Database db;
	ServerSocket serverSocket;
	int numWorkingThreads, maxThreads;
	Vector<WorkingThread> workingThreads;
    private HashMap<String, TokenFeatureGenerator> featureGenerators;
    private HashMap<String, Chunker> chunkers;

	public DaemonThread() throws ParameterException {
		// setup database address
		String db_host = null, db_port = "3306", db_user = null,
			db_pass = "", db_name = null;

		// getGlobal access data from db_uri parameter
		String db_uri = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_URI);
		if (db_uri != null) {
			Pattern dbUriPattern = Pattern.compile("([^:@]*)(:([^:@]*))?@([^:@]*)(:([^:@]*))?/(.*)");
			Matcher dbUriMatcher = dbUriPattern.matcher(db_uri);
			if (dbUriMatcher.find()) {
				db_user = dbUriMatcher.group(1);
				if (dbUriMatcher.group(3) != null)
					db_pass = dbUriMatcher.group(3);
				db_host = dbUriMatcher.group(4);
				if (dbUriMatcher.group(6) != null)
					db_port = dbUriMatcher.group(6);
				db_name = dbUriMatcher.group(7);
			}
		}

		// overwrite with access data from db_* parameters
		if (LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_HOST) != null)
			db_host = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_HOST);
		if (LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_PORT) != null)
			db_port = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_PORT);
		if (LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_USER) != null)
			db_user = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_USER);
		if (LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_PASSWORD) != null)
			db_pass = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_PASSWORD);
		if (LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_NAME) != null)
			db_name = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_DB_NAME);

		if ((db_host == null) || (db_user == null) || (db_name == null))
			throw new ParameterException("Daemon mode: database access data required!");

		this.db_addr = "jdbc:mysql://" + db_host; 
		if (db_port != null)
			this.db_addr += ":" + db_port;
		this.db_addr += "/" + db_name;
		this.db_addr += "?user=" + db_user;
		if ((db_pass != null) && (!db_pass.isEmpty()))
			this.db_addr += "&password=" + db_pass;
		this.db_addr += "&useUnicode=true&characterEncoding=UTF-8";

		//this.db_addr = "jdbc:mysql://" + db_host + "/" + db_name +
		//	"?user=" + db_user + "&password=" + db_pass +
		//	"&useUnicode=true&characterEncoding=UTF-8";
		this.db = new Database(this.db_addr);

		// setup ip address and port number
		this.ip = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_IP);
		if (this.ip == null)
			throw new ParameterException("Daemon mode: -ip (IP address) option is obligatory!");
		String optPort = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_PORT);
		if (optPort == null)
			throw new ParameterException("Daemon mode: -p (port) option is obligatory!");
    	try {
			this.port = Integer.parseInt(optPort);
		} catch (NumberFormatException ex) {
			throw new ParameterException("Incorrect port number: " + optPort);
		}

		// setup maximum threads number
		this.maxThreads = this.DEFAULT_MAX_THREADS;
		String optMaxThreads = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_MAX_THREADS);
		if (optMaxThreads != null) {
			try {
				this.maxThreads = Integer.parseInt(optMaxThreads);
			} catch (NumberFormatException ex) {
				throw new ParameterException("Incorrect maximum threads number: " + optMaxThreads);
			}
		}

		// setup working threads
		this.numWorkingThreads = 0;
		this.workingThreads = new Vector<WorkingThread>();

        chunkers = new HashMap<String, Chunker>();
        featureGenerators = new HashMap<String, TokenFeatureGenerator>();
        try {
            for (String modelNam: LinerOptions.getGlobal().models.keySet()){
                LinerOptions modelConfig = LinerOptions.getGlobal().models.get(modelNam);
                ChunkerManager cm = ChunkerFactory.loadChunkers(modelConfig);
                this.chunkers.put(modelNam, cm.getChunkerByName(modelConfig.getOptionUse()));
                TokenFeatureGenerator gen = null;
                if (!modelConfig.features.isEmpty()) {
                     gen = new TokenFeatureGenerator(modelConfig.features);
                }
                this.featureGenerators.put(modelNam, gen);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

	}

	@Override
	public void run() {
    
    	Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { shutdown(); }});
    
    	// register daemon
		this.myAddr = this.ip + ":" + this.port;
		Main.log("My address: " + this.myAddr, true);
		Main.log("Registering daemon...", false);
		try {
			this.db.connect();
			this.myId = this.db.registerDaemon(this.myAddr);
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		Main.log("Registered with id: " + this.myId, false);
    	
		// start listening for notifications
		Main.log("Listening on port: " + port, false);
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        System.out.println("max threads"+this.maxThreads);
		for (int i = 0; i < this.maxThreads; i++) 
			startWorkingThread();

        System.out.println("working threads at begining"+this.numWorkingThreads);
        while (!serverSocket.isClosed()) {
            System.out.println("working threads"+this.numWorkingThreads);
			try {
				Socket accepted = this.serverSocket.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					accepted.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					accepted.getOutputStream()));

				String line = reader.readLine();
				if (line != null) {
					if (line.equals("PING")) {
						Main.log("Received PING!", true);
					}
					else if (line.equals("NOTIFY")) {
						Main.log("Received NOTIFY!", true);
						
						// start work in a new thread
						if (this.numWorkingThreads < this.maxThreads)
							startWorkingThread();
					}
					else {
						Main.log("Received something weird: " + line, true);
					}
					// respond to connection
					writer.write("OK");
					writer.newLine();
					writer.flush();
				}
				accepted.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public int getDaemonId() {
		return this.myId;
	}

	public synchronized void finishWorkingThread(WorkingThread callingThread) {
		synchronized (this.db) {
			try {
				this.db.connect();
				this.db.daemonReady(this.myId);
				this.db.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		this.numWorkingThreads--;
		this.workingThreads.remove(callingThread);
		if (this.numWorkingThreads == 0)
			Main.log("Sleeping...", false);
	}

	public void startWorkingThread() {
		synchronized (this.db) {
            try {
                this.db.connect();
                this.db.daemonNotReady(this.myId);
                this.db.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (this.numWorkingThreads == 0) {
            Main.log("Woke up!", false);
        }
        this.numWorkingThreads++;
        WorkingThread newThread = new WorkingThread(this, this.db_addr, this.chunkers, this.featureGenerators);
        this.workingThreads.add(newThread);
		newThread.start();
	}
	
	public void shutdown() {
    	try {
			for (WorkingThread wt : this.workingThreads)
				wt.interrupt();
    		this.interrupt();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	Main.log("Shutting down...", false);
		
		// close server socket
		if (this.serverSocket != null) {
			Main.log("Closing socket...", false);
			try {
				this.serverSocket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// unregister daemon
		if (this.myId > -1) {
			try {
				this.db.connect();
				this.db.unregisterDaemon(this.myId);
				this.db.disconnect();
			} catch (SQLException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}

		Main.log("Done.", false);
	}
}
