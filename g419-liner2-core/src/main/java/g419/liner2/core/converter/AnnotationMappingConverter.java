package g419.liner2.core.converter;


import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Created by michal on 6/3/14.
 */
public class AnnotationMappingConverter extends Converter {

  private class PatternStringEntry implements Entry<Pattern, String> {

    private Pattern pattern = null;
    private String string = null;

    public PatternStringEntry(Pattern pattern, String string) {
      this.pattern = pattern;
      this.string = string;
    }

    @Override
    public Pattern getKey() {
      return this.pattern;
    }

    @Override
    public String getValue() {
      return this.string;
    }

    @Override
    public String setValue(String value) {
      this.string = value;
      return value;
    }

  }

  ArrayList<PatternStringEntry> channelsMapping;

  public AnnotationMappingConverter(String mappingFile) {
    try {
      parseMapping(mappingFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void finish(Document doc) {

  }

  @Override
  public void start(Document doc) {

  }

  @Override
  public void apply(Sentence sentence) {
    LinkedHashSet<Annotation> result = new LinkedHashSet<Annotation>();
    for (Annotation ann : sentence.getChunks()) {
      for (Entry<Pattern, String> entry : channelsMapping) {
        if (entry.getKey().matcher(ann.getType()).find()) {
          ann.setType(entry.getValue());
        }
      }
      result.add(ann);
    }
    sentence.setAnnotations(new AnnotationSet(sentence, result));
  }

  private void parseMapping(String mappingFile) throws IOException {
    channelsMapping = new ArrayList<PatternStringEntry>();
    BufferedReader br = new BufferedReader(new FileReader(mappingFile));

    try {
      String line = br.readLine();
      while (line != null) {
        if (!line.isEmpty() && !line.startsWith("#")) {
          if (line.contains("#")) {
            String[] parts = line.split("#");
            if (parts.length > 0) {
              line = parts[0].trim();
            }
          }
          String[] pattern_val = line.split("->");
          channelsMapping.add(new PatternStringEntry(Pattern.compile("^" + pattern_val[0].trim() + "$"), pattern_val[1].trim()));
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
  }
}
