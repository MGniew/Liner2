package g419.liner2.core.chunker;


import g419.corpus.ConsolePrinter;
import g419.corpus.structure.*;
import g419.liner2.core.chunker.interfaces.DeserializableChunkerInterface;
import g419.liner2.core.chunker.interfaces.SerializableChunkerInterface;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * @author Maciej Janicki
 * @author Michał Marcińczuk
 */

public class FullDictionaryChunker extends Chunker
    implements DeserializableChunkerInterface, SerializableChunkerInterface {

  private HashMap<String, HashSet<String>> dictionary = null;

  public FullDictionaryChunker() {
  }

  private AnnotationSet chunkSentence(Sentence sentence) {
    AnnotationSet chunking = new AnnotationSet(sentence);
    List<Token> tokens = sentence.getTokens();
    int sentenceLength = sentence.getTokenNumber();

    // [długość] -> początek => n-gram
    ArrayList<HashMap<Integer, String>> nGrams =
        new ArrayList<HashMap<Integer, String>>();

    // wygeneruj unigramy
    nGrams.add(new HashMap<Integer, String>());
    for (int i = 0; i < sentenceLength; i++) {
      nGrams.get(0).put(new Integer(i), tokens.get(i).getOrth());
    }
    // wygeneruj n-gramy
    for (int n = 1; n < sentenceLength; n++) {
      nGrams.add(new HashMap<Integer, String>());
      for (int j = 0; j < sentenceLength - n; j++) {
        nGrams.get(n).put(new Integer(j),
            nGrams.get(n - 1).get(j) + " " + tokens.get(j + n).getOrth());
      }
    }

    // chunkuj (poczynając od najdłuższych n-gramów) - DO SPRAWDZENIA
    for (int n = sentenceLength - 1; n >= 0; n--) {
      for (int i = 0; i < sentenceLength - n; i++) {
        int idx = new Integer(i);

        // jeśli danego n-gramu nie ma w tablicy, to kontynuuj
        if (nGrams.get(n).get(idx) == null) {
          continue;
        }

        // jeśli znaleziono w słowniku
        if (this.dictionary.containsKey(nGrams.get(n).get(idx))) {

          // dodaj wszystkie chunki
          HashSet<String> types = this.dictionary.get(nGrams.get(n).get(idx));
          for (String type : types) {
            chunking.addChunk(new Annotation(i, i + n, type, sentence));
          }

          // usuń z tablicy wszystkie krótsze n-gramy, które zahaczają o to miejsce
          // j - dł. odrzucanego n-gramu - 1, k - pozycja startowa
          for (int j = n; j >= 0; j--) {
            for (int k = i - j; k <= i + n; k++) {
              nGrams.get(j).remove(new Integer(k));
            }
          }
        }
      }
    }

    return chunking;
  }

  public void loadDictionary(String dictFile) {
    this.dictionary = new HashMap<String, HashSet<String>>();
    try {
      BufferedReader dictReader = new BufferedReader(new FileReader(dictFile));
      String line = dictReader.readLine();
      int added = 0;
      while (line != null) {
        String[] content = line.split("\t");
        if (content.length >= 2) {
          if (!this.dictionary.containsKey(content[1])) {
            this.dictionary.put(content[1], new HashSet<String>());
          }
          if (!this.dictionary.get(content[1]).contains(content[0].toUpperCase())) {
            this.dictionary.get(content[1]).add(content[0].toUpperCase());
            added += 1;
          }
        }
        line = dictReader.readLine();
      }
      ConsolePrinter.log("Full dictionary chunker compiled.", true);
      ConsolePrinter.log("Added: " + added, true);
      dictReader.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Wczytuje chunker z modelu binarnego.
   *
   * @param filename
   */
  @Override
  @SuppressWarnings("unchecked")
  public void deserialize(String filename) {
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
      this.dictionary = (HashMap<String, HashSet<String>>) in.readObject();
      in.close();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void serialize(String filename) {
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
      out.writeObject(this.dictionary);
      out.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void close() {

  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
    HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
    for (Paragraph paragraph : ps.getParagraphs()) {
      for (Sentence sentence : paragraph.getSentences()) {
        chunkings.put(sentence, this.chunkSentence(sentence));
      }
    }
    return chunkings;
  }

}	
