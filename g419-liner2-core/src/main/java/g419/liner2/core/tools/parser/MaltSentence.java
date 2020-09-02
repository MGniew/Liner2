package g419.liner2.core.tools.parser;

import com.google.common.collect.Lists;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.maltparser.core.exception.MaltChainedException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
public class MaltSentence {
  List<MaltSentenceLink> links = Lists.newArrayList();
  private final Map<String, String> posMapping;
  private String[] maltData;
  private final Set<Annotation> annotations;
  private final Sentence sentence;

  public MaltSentence(final Sentence sent, final Map<String, String> posMapping) {
    this.posMapping = posMapping;
    maltData = convertToCoNLL(sent).stream()
        .map(r -> String.join("\t", Arrays.asList(r)))
        .toArray(String[]::new);
    annotations = sent.getChunks();
    sentence = sent;
  }

  public void setMaltDataAndLinks(final String[] output) {
    maltData = output;
    links = IntStream.range(0, output.length)
        .mapToObj(i -> new ImmutablePair<>(i, output[i].split("\t")))
        .map(p -> new MaltSentenceLink(p.getKey(), Integer.valueOf(p.getRight()[8]) - 1, p.getRight()[9]))
        .collect(Collectors.toList());
  }

  public String[] getMaltData() {
    return maltData;
  }

  public Sentence getSentence() {
    return this.sentence;
  }

  public MaltSentenceLink getLink(final int index) {
    return index >= this.links.size() ? null : this.links.get(index);
  }

  /**
   * Zwraca listę linków wskazujących na token o wskazanym indeksie.
   *
   * @param index
   * @return
   */
  public List<MaltSentenceLink> getLinksByTargetIndex(final int index) {
    return links.stream()
        .filter(link -> link.getTargetIndex() == index)
        .collect(Collectors.toList());
  }

  public Set<Annotation> getAnnotations() {
    return annotations;
  }

  private List<String[]> convertToCoNLL(final Sentence sent) {
    final List<String[]> tokens = sent.getTokens().stream()
        .map(this::tokenToConll)
        .collect(Collectors.toList());
    IntStream.range(0, tokens.size()).forEach(n -> tokens.get(n)[0] = String.valueOf(n + 1));
    return tokens;
  }

  private String[] tokenToConll(final Token token) {
    final TokenAttributeIndex attributes = token.getAttributeIndex();
    final String ctag = token.getAttributeValue(attributes.getIndex("ctag"));
    final List<String> ctagElements = Arrays.asList(ctag.split(":"));
    final String[] tokData = Stream.generate(() -> "_").limit(8).toArray(String[]::new);
    tokData[0] = String.valueOf(0);
    tokData[1] = token.getAttributeValue(attributes.getIndex("orth"));
    tokData[2] = token.getAttributeValue(attributes.getIndex("base"));
    tokData[3] = posMapping.get(ctagElements.get(0));
    tokData[4] = ctagElements.get(0);
    tokData[5] = getDefaultIfEmpty(String.join("|", ctagElements.subList(1, ctagElements.size())));
    return tokData;
  }

  private static String getDefaultIfEmpty(final String str) {
    return str == null || str.isEmpty() ? "_" : str;
  }

  private class TreeNode {

    final String name;
    final List<TreeNode> children = Lists.newArrayList();
    String relationWithParent = "";

    public TreeNode(final String name) {
      this.name = name;
    }

    public void addChild(final TreeNode node) {
      children.add(node);
    }

    public void setRelationWithParent(final String relation) {
      relationWithParent = relation;
    }

    public List<TreeNode> getChildren() {
      return children;
    }

    public void print() {
      System.out.println("ROOT");
      print("", true);
      System.out.println();
    }

    private void print(final String prefix, final boolean isTail) {
      System.out.println(String.format("%s%s──(%s)── %s", prefix, isTail ? "└" : "├", relationWithParent, name));
      for (int i = 0; i < children.size() - 1; i++) {
        children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
      }
      if (children.size() > 0) {
        children.get(children.size() - 1)
            .print(prefix + (isTail ? "    " : "│   "), true);
      }
    }
  }

  public void printAsTree() {
    final List<TreeNode> nodes = sentence.getTokens().stream()
        .map(t -> String.format("%s                    [%s]", t.getOrth(), t.getDisambTag().toString()))
        .map(TreeNode::new)
        .collect(Collectors.toList());
    links.stream()
        .filter(l -> l.sourceIndex > -1 && l.targetIndex > -1)
        .forEach(l -> {
          nodes.get(l.targetIndex).addChild(nodes.get(l.sourceIndex));
          nodes.get(l.sourceIndex).setRelationWithParent(l.relationType);
        });
    IntStream.range(0, nodes.size())
        .filter(n -> links.get(n).targetIndex == -1)
        .mapToObj(nodes::get)
        .forEach(TreeNode::print);
  }

  public String getRelPathAsString(Relation rel)  {
    int index1 = rel.getAnnotationFrom().getHead();
    int index2 = rel.getAnnotationTo().getHead();

    Pair<List<MaltSentenceLink>, List<MaltSentenceLink>> path = this.getPathBetween(index1, index2);

    return this.getPathBetweenAsString(rel,path.getLeft(),path.getRight());
  }

  public Pair<List<MaltSentenceLink>,List<MaltSentenceLink>>
  getPathBetween(int index1, int index2) {
    List<MaltSentenceLink> parents1 = getParentsAscending(index1);
    List<MaltSentenceLink> parents2 = getParentsAscending(index2);
    Pair<Integer,Integer> indexes = findIndexesToLowestCommonLink(parents1,parents2);

    return Pair.of(
                parents1.subList(0,indexes.getLeft()+1),
        parents2.subList(0,indexes.getRight()+1)
    );
  }

  public List<MaltSentenceLink> getParentsAscending(final int index) {
    return getParentsAscending(index,new LinkedList<>());
  }

  private List<MaltSentenceLink> getParentsAscending(final int index, List<MaltSentenceLink> accumulated) {
    Optional<MaltSentenceLink> optOutLink =  links.stream()
        .filter(link -> link.getSourceIndex() == index)
        .findFirst();

    if(!optOutLink.isPresent()) {
      return accumulated;
    }
    MaltSentenceLink outLink = optOutLink.get();
    accumulated.add(outLink);
    return getParentsAscending(outLink.getTargetIndex(),accumulated);
  }

  public Pair<Integer,Integer> findIndexesToLowestCommonLink(List<MaltSentenceLink> list1,
                                                             List<MaltSentenceLink> list2) {
    for(int i=0;i<list1.size();i++)
      for(int j=0;j<list2.size();j++)
      {
        if(list1.get(i).isTheSameAs(list2.get(j)))
          return Pair.of(i,j);
      }

    // it should never come here !
    return null;
  }

  public String getPathBetweenAsString(Relation rel,
                                       List<MaltSentenceLink> path1,
                                       List<MaltSentenceLink> path2) {
    List<Token> tokens = rel.getAnnotationFrom().getSentence().getTokens();
    StringBuilder s = new StringBuilder();
    s.append(rel.getType()).append(": ");

    s.append(rel.getAnnotationFrom().getType()).append(" -> ");
    s.append (path1.stream()
        .map(msl -> tokens.get(msl.getSourceIndex()).getDisambTag().getBase())
        .collect(Collectors.joining(" -> ")) );

    Collections.reverse(path2);
    s.append(" <- ");
    s.append (path2.stream()
        .skip(1)
        .map(msl -> tokens.get(msl.getSourceIndex()).getDisambTag().getBase())
        .collect(Collectors.joining(" <- ")) );
    s.append(" <- ").append(rel.getAnnotationTo().getType());

    return s.toString();
  }




}
