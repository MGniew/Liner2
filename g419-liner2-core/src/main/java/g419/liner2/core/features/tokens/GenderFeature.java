package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.util.ArrayList;
import java.util.Arrays;


public class GenderFeature extends TokenFeature {

  private String name;
  private ArrayList<String> possible_genders = new ArrayList<String>(Arrays.asList("m1", "m2", "m3", "f", "n", "n1", "n2", "p1", "p2", "p3"));

  public GenderFeature(String name) {
    super(name);
    this.name = name;
  }

  public String generate(Token token, TokenAttributeIndex index) {
    String ctag = token.getAttributeValue(index.getIndex("ctag"));
    if (ctag != null) {
      for (String val : ctag.split(":")) {
        if (this.possible_genders.contains(val)) {
          return val;
        }
      }
    }
    return null;
  }


}
