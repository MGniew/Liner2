package g419.liner2.core.chunker.factory;


import g419.lib.cli.ParameterException;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.HeuristicChunker;
import org.ini4j.Ini;




/*
 * @author Maciej Janicki
 */

public class ChunkerFactoryItemHeuristic extends ChunkerFactoryItem {

  public ChunkerFactoryItemHeuristic() {
    super("heuristic");
  }

  @Override
  public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
    HeuristicChunker chunker = null;
    if (description.containsKey("heuristics")) {
      try {
        chunker = new HeuristicChunker(description.get("heuristics").split(","));
      } catch (ParameterException ex) {
        ex.printStackTrace();
      }
    } else {
      chunker = new HeuristicChunker();
    }
    return chunker;
  }

}
