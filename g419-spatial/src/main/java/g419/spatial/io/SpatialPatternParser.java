package g419.spatial.io;

import g419.spatial.structure.SpatialRelationPattern;
import g419.spatial.structure.SpatialRelationPatternMatcher;
import g419.toolbox.sumo.Sumo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class SpatialPatternParser {

	private BufferedReader reader = null;
	private Sumo sumo = null;
	private int lineNo = 0;
	private String currentLine = null;
	private Logger logger = Logger.getLogger(SpatialPatternParser.class); 
	
	public SpatialPatternParser(Reader reader, Sumo sumo){
		this.reader = new BufferedReader(reader);
		this.sumo = sumo;
	}
	
	private String nextLine() throws IOException{
		this.lineNo++;
		this.currentLine = this.reader.readLine(); 
		return this.currentLine;
	}
	
	public SpatialRelationPatternMatcher parse() throws IOException{
		this.nextLine();
		List<SpatialRelationPattern> patterns = new LinkedList<SpatialRelationPattern>();
		
		while ( this.currentLine != null ){
			if ( this.currentLine.startsWith("si:") ){
				String si = this.currentLine.substring(3).trim();
				this.nextLine();
				if ( this.currentLine == null || !this.currentLine.startsWith("tr:")){
					Logger.getLogger(SpatialPatternParser.class).warn(
							String.format("Linia nr %d: oczekiwano 'tr:', ale napotkano '%s'", this.lineNo, this.currentLine));
					continue;
				}
				Set<String> trajectorConcepts = this.parseConcepts(this.currentLine.substring(3).trim());
				
				this.nextLine();
				if ( this.currentLine == null || !this.currentLine.startsWith("lm:")){
					logger.warn(
							String.format("Linia nr %d: oczekiwano 'lm:', ale napotkano '%s'", this.lineNo, this.currentLine));
					continue;
				}
				Set<String> landmarkConcepts = this.parseConcepts(this.currentLine.substring(3).trim());
				
				if ( trajectorConcepts.size() > 0 && landmarkConcepts.size() > 0 ){
					patterns.add(new SpatialRelationPattern(si, trajectorConcepts, landmarkConcepts));
				}
				else if ( trajectorConcepts.size() == 0 ){
					this.logWarning("Pusty zbiór trajectorConcept");
				}
				else if ( landmarkConcepts.size() == 0 ){
					this.logWarning("Pusty zbiór landmarkConcept");
				}
				
				this.nextLine();
			}
			else if ( this.currentLine.length() > 0 ){
				logger.warn(
						String.format("Linia nr %d: oczekiwano 'si:', ale napotkano '%s'", this.lineNo, this.currentLine));				
			}
			this.nextLine();
		}
		
		logger.info(String.format("Liczba wczytanych wzorców: %d", patterns.size()));
		return new SpatialRelationPatternMatcher(patterns, this.sumo);
	}	
	
	private Set<String> parseConcepts(String line){
		Set<String> concepts = new HashSet<String>();
		for (String part : line.split(",( )*") ){
			part = part.trim().substring(1);
			if ( this.sumo.containsClass(part) ){
				concepts.add(part);
			}
			else{
				this.logWarning(String.format("Concept '%s' not found in SUMO", part));
			}
		}
		return concepts;
	}
	
	private void logWarning(String message){
		this.logger.warn(String.format("Linia nr %d: %s", this.lineNo, message));
	}
}