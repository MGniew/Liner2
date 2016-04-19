package g419.liner2.api.chunker.factory;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.RuleRoadChunker;
import g419.liner2.api.tools.TrieDictNode;

/**
 * @author Michał Marcińczuk
 */
public class ChunkerFactoryItemRuleRoad extends ChunkerFactoryItem {

	public ChunkerFactoryItemRuleRoad() {
		super("rule-road");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        TrieDictNode dict = null;
        
        String dictionaryPath = description.get("dictionary");
        if ( dictionaryPath == null ){
        	dict = new TrieDictNode(false);
        	Logger.getLogger(this.getClass()).error("Brak parametru 'dictionary' w opisie chunkera rule-road");
        }
        else{
        	dict = TrieDictNode.loadPlain(dictionaryPath);
        }
        
        return new RuleRoadChunker(dict);
	}

}
