package liner2.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

import liner2.LinerOptions;

/**
 * Klasa do generowania cech dla słów.
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 *
 */
public class NerdFeatureGenerator {

	//private static ArrayList<String> features = null;
	private static String configuration = null;
	private static String path_python = null;
	private static String path_nerd = null;
	private static Process p = null;
	private static BufferedReader input = null;
	private static BufferedReader error = null;
	private static BufferedWriter output = null;
	
	public static final Pattern regexFeatureGeneralisation = Pattern.compile("hyp([0-9]+)");
    public static final Pattern regexFeatureDictionary = Pattern.compile("([^:]*)(:(.*))?:([^:]*)");
    public static String docstart_config_features = "-DOCSTART CONFIG FEATURES orth base ctag";
	
	//private static FeatureGenerator generator = null;
	private static boolean initialized = false;
	public static long initTime = 0;
	private static long time = 0;
		
	/**
	 * Prywatny konstruktor, aby zagwarantować singleton.
	 */
	private NerdFeatureGenerator(ArrayList<String> features, String path_python, String path_nerd) throws IOException {
		
	}
	
	public static boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Funkcja inicjalizuje generator.
	 * @param features
	 */
	public synchronized static void initialize() throws IOException {
		//this.generator = new FeatureGenerator(features, path_python, path_nerd);
		
		if (NerdFeatureGenerator.initialized)
			return;
		
		long timeInitStart = System.nanoTime();
		
		//FeatureGenerator.features = features;
		NerdFeatureGenerator.path_python = LinerOptions.getOption(LinerOptions.OPTION_PYTHON);
		NerdFeatureGenerator.path_nerd = LinerOptions.getOption(LinerOptions.OPTION_NERD);
		
		String path_to_nerd = NerdFeatureGenerator.path_nerd + "/nerd.py"; 
		if ( !(new File(path_to_nerd).exists()) ) {
			throw new Error("Incorrect path to NERD: " + path_to_nerd);
		}
		
		String[] envp=new String[1];
		envp[0]="PATH=" + path_nerd;
		String cmd = path_python + " " + path_to_nerd + " --batch";
		NerdFeatureGenerator.p = Runtime.getRuntime().exec(cmd, envp);
		
		// wygeneruj konfigurację
		String featureNames = "";
		String featureOthers = "";
		for (String feature : LinerOptions.get().features) {
			String featureName = feature;
			
			if ( featureName.endsWith(":iob") )
				continue;
			
			if (featureName.equals("syn"))
				featureOthers += " --generalization syn:::syn ";
			
			Matcher m1 = regexFeatureDictionary.matcher(feature);
			if (m1.find()) {
				featureOthers += " -g" + feature;
				
				String[] parts = feature.split(":");
				String filename = parts[parts.length-1];
				File file = new File(filename);
				if (!file.exists())
					System.err.println(String.format("Liner2 ERROR: file %s does not exist.", file.getAbsolutePath()));
				
				featureName = m1.group(1);
			}
			
			Matcher m2 = regexFeatureGeneralisation.matcher(feature);
			if (m2.find())
				featureOthers += " --generalization hyp:" + m2.group(1) + ":full:" + feature + " ";
				
			featureNames += (featureNames.length() > 0 ? "," : "") + featureName;
		}
		NerdFeatureGenerator.configuration = "-f " + featureNames + featureOthers;
		
		NerdFeatureGenerator.input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		NerdFeatureGenerator.output = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		NerdFeatureGenerator.error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		NerdFeatureGenerator.initialized = true;
		
		NerdFeatureGenerator.time += System.nanoTime() - timeInitStart;
	}

	public static void generateFeatures(ParagraphSet ps) throws Exception {
		if (!NerdFeatureGenerator.initialized)
			throw new Exception("generateFeatures: FeatureGenerator not initialized.");
		
		ps.getAttributeIndex().update(LinerOptions.get().featureNames);
		for (Paragraph p : ps.getParagraphs())
			NerdFeatureGenerator.generateFeatures(p, false);
	}
	
	/**
	 * Funkcja generuje cechy i wstawia je do tablic cech tokenów.  
	 * @param p
	 */
	public static void generateFeatures(Paragraph p, boolean updateIndex) throws Exception {
		if (!NerdFeatureGenerator.initialized)
			throw new Exception("generateFeatures: FeatureGenerator not initialized.");
		
		if (updateIndex)
			p.getAttributeIndex().update(LinerOptions.get().featureNames);
		for (Sentence s : p.getSentences())
			NerdFeatureGenerator.generateFeatures(s, false);
	}
	
	/**
	 * Funkcja generuje cechy i wstawia je do tablic cech tokenów.  
	 * @param sentence
	 */
	public synchronized static void generateFeatures(Sentence sentence, boolean updateIndex) throws Exception {
		
		if (!NerdFeatureGenerator.initialized)
			throw new Exception("generateFeatures: FeatureGenerator not initialized.");
		
		long timeGenerateStart = System.nanoTime();
		
		if (updateIndex)
			sentence.getAttributeIndex().update(LinerOptions.get().featureNames);

		int index_from = 0;
		int max_tokens = sentence.getTokenNumber();
		while ( index_from < sentence.getTokenNumber() ){
			int index_to = Math.min(index_from + max_tokens, sentence.getTokenNumber());
			NerdFeatureGenerator.generateAttributesForRange(sentence.getTokens(), index_from, index_to);
			index_from = index_to;
		}

		if (sentence.getAttributeIndex().getIndex("agr1") > -1){
			int cas = sentence.getAttributeIndex().getIndex("case");
			int nmb = sentence.getAttributeIndex().getIndex("number");
			int gnd = sentence.getAttributeIndex().getIndex("gender");
			int agr1 = sentence.getAttributeIndex().getIndex("agr1");
			
			for (int i=0; i<sentence.getTokenNumber(); i++){
				
				String agr1_value = "NULL";
				
				if ( i>0 ){
					agr1_value = NerdFeatureGenerator.agree3attributes(cas, nmb, gnd, 
							sentence.getTokens().get(i), sentence.getTokens().get(i-1));						
				}
				
				sentence.getTokens().get(i).setAttributeValue(agr1, agr1_value);
			}
		}
		
		NerdFeatureGenerator.time += System.nanoTime() - timeGenerateStart;
		// przy pierwszym zdaniu
		if (NerdFeatureGenerator.initTime == 0)
			NerdFeatureGenerator.initTime = NerdFeatureGenerator.time;
	}
	
	/**
	 * 
	 * @param tokens
	 * @param index_from
	 * @param index_to
	 * @throws IOException
	 */
	private static void generateAttributesForRange(ArrayList<Token> tokens, int index_from, int index_to) throws IOException{
		String featureLoad = NerdFeatureGenerator.docstart_config_features;
		featureLoad = featureLoad.replace("-DOCSTART CONFIG FEATURES ", "");
		featureLoad = featureLoad.replace(" ", ",");

		NerdFeatureGenerator.writeline("@FEATURES");
		NerdFeatureGenerator.writeline(NerdFeatureGenerator.configuration  + " -l " + featureLoad);
		NerdFeatureGenerator.writeline(NerdFeatureGenerator.docstart_config_features);
		
		for (int i=index_from; i<index_to; i++) {
			Token token = tokens.get(i);
			String line = String.format("%s %s %s O", token.getAttributeValue(0),
				token.getAttributeValue(1), token.getAttributeValue(2));
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j<token.getNumAttributes(); j++)
				sb.append(token.getAttributeValue(j) + " ");
			sb.append("O");
			NerdFeatureGenerator.writeline(sb.toString());
		}

		NerdFeatureGenerator.writeline("@EOC");	
		NerdFeatureGenerator.output.flush();
		NerdFeatureGenerator.input.readLine();
		
//		String error = null;
//		while ( (error = FeatureGenerator.error.readLine()) != null)
//			System.out.println("!!: " + error);
		
		for (int i=index_from; i<index_to; i++) {
			Token token = tokens.get(i);
			String line = NerdFeatureGenerator.input.readLine().trim();
			line = line.substring(0, line.length() - 2);
			String[] featureValues = line.split(" ");
			for (int j = 0; j < featureValues.length; j++)
				token.setAttributeValue(j, featureValues[j]);					
		}

//		String error = null;
//		while ( (error = FeatureGenerator.error.readLine()) != null)
//			System.out.println("!!: " + error);
				
		/* XXX */
		NerdFeatureGenerator.input.readLine();
		NerdFeatureGenerator.input.readLine();
		NerdFeatureGenerator.input.readLine();
		
	}
	
	/**
	 * 
	 * @param a1
	 * @param a2
	 * @param a3
	 * @param t1
	 * @param t2
	 * @return
	 */
	private static String agree3attributes(int a1, int a2, int a3, Token t1, Token t2){

		if ( t1.getAttributeValue(a1).equals("null") || t2.getAttributeValue(a1).equals("null")
				|| t1.getAttributeValue(a2).equals("null") || t2.getAttributeValue(a2).equals("null")
				|| t1.getAttributeValue(a3).equals("null") || t2.getAttributeValue(a3).equals("null"))
			return "NULL";
		else if ( t1.getAttributeValue(a1).equals(t2.getAttributeValue(a1))
				&& t1.getAttributeValue(a2).equals(t2.getAttributeValue(a2)) 
				&& t1.getAttributeValue(a3).equals(t2.getAttributeValue(a3)) )
			return "1";
		else
			return "0";
	}
	
	public static void close() throws IOException{
		if (NerdFeatureGenerator.initialized) {
			NerdFeatureGenerator.output.write("@EOF\n");
			NerdFeatureGenerator.output.flush();
			NerdFeatureGenerator.initialized = false;
		}
	}
	
	public static long getTime() {
		return NerdFeatureGenerator.time;
	}
	
		
	private static void writeline(String line) throws IOException{
		NerdFeatureGenerator.output.write(line + "\n");
		//System.out.println("NERD: " + line);
	}

}