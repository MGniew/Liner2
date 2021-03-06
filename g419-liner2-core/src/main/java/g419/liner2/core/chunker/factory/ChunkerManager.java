package g419.liner2.core.chunker.factory;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.CrfppChunker;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.ini4j.Ini;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 9/3/13
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkerManager {

  private HashMap<String, Chunker> chunkers = new HashMap<String, Chunker>();
  public LinerOptions opts;
  public ArrayList<Document> trainingData;
  private HashMap<String, CrfTemplate> chunkerTemplates = new HashMap<String, CrfTemplate>();

  public ChunkerManager(LinerOptions config) {
    opts = config;
  }

  /**
   * Creates a map of chunkers according to their descriptions.
   *
   * @throws Exception
   */
  public void loadChunkers() throws Exception {
    for (Ini.Section chunkerDesc : opts.getChunkerDescriptions()) {
      Chunker chunker = ChunkerFactory.createChunker(chunkerDesc, this);
      addChunker(chunkerDesc.getName().substring(8), chunker);
    }
  }

  /**
   * Creates a map of chunkers according to their parametrized ({NAME} holders) descriptions.
   *
   * @param foldNumber Number of the fold.
   * @throws Exception
   */
  public void loadChunkers(Integer foldNumber) throws Exception {
    for (Ini.Section chunkerDesc : opts.getParametrizedChunkerDescriptions(foldNumber)) {
      Chunker chunker = ChunkerFactory.createChunker(chunkerDesc, this);
      addChunker(chunkerDesc.getName().substring(8), chunker);
    }
  }

  public void resetChunkers() {
    chunkers = new HashMap<String, Chunker>();
  }


  public void setChunkerTemplate(String chunkerName, CrfTemplate template) {
    chunkerTemplates.put(chunkerName, template);
  }

  public CrfTemplate getChunkerTemplate(String chunkerName) {
    return chunkerTemplates.containsKey(chunkerName) ? chunkerTemplates.get(chunkerName) : null;
  }

  public CrfTemplate getChunkerTemplate2(String chunkerName) {
    return chunkers.containsKey(chunkerName) ? ((CrfppChunker) (chunkers.get(chunkerName))).getTemplate() : null;
  }


  public void addChunker(String name, Chunker chunker) {
    if (chunkers.containsKey(name)) {
      throw new Error(String.format("Chunker name '%s' duplicated", name));
    }
    chunkers.put(name, chunker);
  }

  public void addChunker(String name, Ini.Section description) {
    try {
      addChunker(name, ChunkerFactory.createChunker(description, this));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Chunker getChunkerByName(String name) {
    return chunkers.get(name);
  }


  public void loadTrainData(AbstractDocumentReader reader, TokenFeatureGenerator gen) throws Exception {
    trainingData = new ArrayList<Document>();
    Document document = reader.nextDocument();
    while (document != null) {
      if (gen != null) {
        gen.generateFeatures(document);
      }
      trainingData.add(document);
      document = reader.nextDocument();
    }
  }
}
