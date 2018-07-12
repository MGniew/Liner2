package g419.corpus.io.reader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.HasLogger;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.*;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IobStreamReader extends AbstractDocumentReader implements HasLogger {

    final public String IOB_HEADER_PREFIX = "-DOCSTART CONFIG FEATURES";
    final public String IOB_FILE_PREFIX = "-DOCSTART FILE";

    private final BufferedReader ir;
    static private final Pattern annotationPattern = Pattern.compile("([IB])-([^#]*)");
    private final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
    private int nextParagraphId = 1;
    private String nextFileId = null;

    public IobStreamReader(final InputStream is) throws DataFormatException, IOException {
        ir = new BufferedReader(new InputStreamReader(is));
        init();
    }

    /**
     * Read -DOCSTART CONFIG FEATURES line.
     */
    protected void init() throws DataFormatException, IOException {
        final String header = ir.readLine();
        if (header == null) {
            throw new DataFormatException("Header not found. Empty file?");
        }
        if (!header.startsWith(IOB_HEADER_PREFIX)) {
            throw new DataFormatException(
                    String.format("First line does not contain attributes definition, i.e. '%s a1 a2 a3'", IOB_HEADER_PREFIX));
        }
        parseFileHeader(header).stream().forEach(attributeIndex::addAttribute);
        nextFileId = goToNextFileBlock();
    }

    private List<String> parseFileHeader(final String header) {
        return Arrays.asList(header.substring(IOB_HEADER_PREFIX.length()).trim().split(" "));
    }

    @Override
    public void close() {
        try {
            ir.close();
        } catch (final IOException ex) {
            getLogger().error("Failed to close the input stream", ex);
        }
    }

    @Override
    protected TokenAttributeIndex getAttributeIndex() {
        return attributeIndex;
    }

    @Override
    public Document nextDocument() throws DataFormatException, IOException {
        return readNextDocument();
    }

    /**
     * @return File identifier
     */
    private String goToNextFileBlock() throws IOException {
        String line;
        do {
            line = ir.readLine();
        } while (line != null && !line.startsWith(IOB_FILE_PREFIX));
        return parseFileId(line);
    }

    private String parseFileId(final String line) {
        return Option.of(line).map(l -> l.substring(IOB_FILE_PREFIX.length()).trim()).getOrNull();
    }

    private Document readNextDocument() throws DataFormatException, IOException {
        if (nextFileId == null) {
            return null;
        }

        final TokenAttributeIndex index = attributeIndex.clone();
        final Document document = new Document("IOB todo", index);
        final Paragraph paragraph = new Paragraph("p" + (nextParagraphId++));
        document.addParagraph(paragraph);
        List<String> sentenceLabels = Lists.newArrayList();
        paragraph.setAttributeIndex(index);
        Sentence sentence = new Sentence();

        while (sentence != null) {
            final String line = ir.readLine();
            if (line == null || line.startsWith(IOB_FILE_PREFIX) || line.trim().length() == 0) {
                if (sentence.getTokenNumber() > 0) {
                    createAnnotations(sentence, sentenceLabels);
                    paragraph.addSentence(sentence);
                }
                sentence = null;
                if (line == null) {
                    nextFileId = null;
                } else if (line.startsWith(IOB_FILE_PREFIX)) {
                    nextFileId = parseFileId(line);
                } else {
                    sentence = new Sentence();
                    sentenceLabels = Lists.newArrayList();
                }
            } else {
                final Pair<Token, String> pair = parseToken(line, index);
                sentence.addToken(pair.getLeft());
                sentenceLabels.add(pair.getRight());
            }
        }

        return document;
    }

    private void createAnnotations(final Sentence sentence, final List<String> labels) throws DataFormatException {
        final List<Pair<String, Set<Integer>>> groups = labelsToAnnotations(labels);
        groups.stream()
                .map(p -> new Annotation(p.getRight(), p.getLeft(), sentence))
                .forEach(sentence::addChunk);
    }

    private List<Pair<String, Set<Integer>>> labelsToAnnotations(final List<String> labels) throws DataFormatException {
        final List<Pair<String, Set<Integer>>> groups = Lists.newArrayList();
        Map<String, Set<Integer>> annsByType = Maps.newHashMap();
        final List<String> labelsCopy = Lists.newArrayList(labels);
        labelsCopy.add("O");
        int tokenIndex = 0;
        for (final String label : labelsCopy) {
            if (label.equals("O")) {
                annsByType.entrySet().stream()
                        .map(p -> new ImmutablePair(p.getKey(), p.getValue()))
                        .forEach(groups::add);
                annsByType = Maps.newHashMap();
            } else {
                final Matcher m = annotationPattern.matcher(label);
                while (m.find()) {
                    final String annType = m.group(2);
                    switch (m.group(1)) {
                        case "B":
                            annsByType.put(annType, Sets.newHashSet(tokenIndex));
                            break;
                        case "I":
                            if (annsByType.containsKey(annType)) {
                                annsByType.get(annType).add(tokenIndex);
                            } else {
                                throw new DataFormatException("Invalid sequence of labels");
                            }
                            break;
                        default:
                            throw new DataFormatException("Unknown identifier in " + label);
                    }
                }
            }
            tokenIndex++;
        }
        return groups;
    }

    private Pair<Token, String> parseToken(final String line, final TokenAttributeIndex index) throws DataFormatException {
        final String[] cols = line.split("\t");
        final String[] attrs = Arrays.copyOfRange(cols, 0, cols.length - 1);
        final String labels = cols[cols.length - 1];
        return new ImmutablePair<>(createToken(attrs, index), labels);
    }


    private Token createToken(final String[] attrs, final TokenAttributeIndex index) throws DataFormatException {
        final Token token = new Token(index);
        if (attrs.length != index.getLength()) {
            throw new DataFormatException("Invalid number of attributes: " + StringUtils.join(attrs)
                    + ". Expecting " + index.getLength());
        }
        for (int i = 0; i < attrs.length; i++) {
            token.setAttributeValue(i, attrs[i]);
        }
        if (attributeIndex != null) {
            final String base = index.getAttributeValue(token, "base");
            final String ctag = index.getAttributeValue(token, "ctag");
            token.addTag(new Tag(base, ctag, false));
        }
        return token;
    }

    @Override
    public boolean hasNext() {
        return nextFileId != null;
    }
}
