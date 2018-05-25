package g419.corpus.io.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;

import g419.corpus.io.UnknownFormatException;

/**
 * 
 * Factory for creating a writer from its' description.
 * 
 * @author Michał Marcińczuk
 *
 */
public class WriterFactory {

	private static final WriterFactory factory = new WriterFactory();
	
	private WriterFactory(){}
	
	public static WriterFactory get(){
		return WriterFactory.factory; 
	}

	/**
	 * Tworzy piśnik dla określonego formatu outputFormat i lokalizacji outputFile.
	 * Jeżeli outputFile jest nullem, to strumieniem wyjściowym jest System.out.
	 * 
	 * @param outputFile
	 * @param outputFormat
	 * @return
	 * @throws Exception
	 */
	public AbstractDocumentWriter getStreamWriter(String outputFile, String outputFormat) throws Exception {
		if (outputFormat.startsWith("batch:")){
            String format = outputFormat.substring(6);
            return new BatchWriter(outputFile, format);
        }
		else{
			boolean gzOutput = false;
			String outputFormatNoGz = outputFormat; 
			if ( outputFormat.endsWith(":gz") ){
				gzOutput = true;
				outputFormatNoGz = outputFormat.substring(0, outputFormat.length()-3);
			}
			
			switch (outputFormatNoGz){
				case "tei":
					return this.getTEIWriter(outputFile, gzOutput);
				case "cclrel":
					return this.getCclRelWriter(outputFile, gzOutput, false);
				case "cclrel-disamb":
					return this.getCclRelWriter(outputFile, gzOutput, true);
				default:
					return this.getStreamWriter(this.getOutputStreamFileOrOut(outputFile), outputFormat);
	        }			
		}		
	}

    /**
     * Zwraca piśnik dla formatu outputFormat. Dane zapisywane są do strumieia out.
     * @param out
     * @param outputFormat
     * @return
     * @throws Exception
     */
	public AbstractDocumentWriter getStreamWriter(OutputStream out, String outputFormat) throws Exception {
		OutputStream outWrapped = out;
		if ( outputFormat.endsWith(":gz") ){
			outWrapped = new GZIPOutputStream(out);
			outputFormat = outputFormat.substring(0, outputFormat.length() - 3);
		}
		switch (outputFormat){
			case "ccl":
				return new CclStreamWriter(outWrapped);
			case "ccl-disamb":
				return new CclStreamWriter(outWrapped, true);
			case "iob":
				return new IobStreamWriter(outWrapped);
			case "conll":
				return new ConllStreamWriter(outWrapped);
			case "zero_verb":
				return new ZeroVerbWriter(outWrapped);
			case "iob-tab":
				return new IobTabStreamWriter(outWrapped);
			case "token-hash":
				return new TokenHashWriter(outWrapped);
			case "token-lemma":
				return new TokenLemmaWriter(outWrapped);
			case "tuples":
				return new AnnotationTupleStreamWriter(outWrapped);
			case "json":
				return new JsonStreamWriter(outWrapped);
			case "json-annotations":
				return new JsonAnnotationsStreamWriter(outWrapped);
			case "json-frames":
				return new JsonFramesStreamWriter(outWrapped);
			case "tokens":
				return new TokensStreamWriter(outWrapped);
			case "arff":
				return new ArffTokenStreamWriter(outWrapped);
			case "verb_eval":
				return new MinosVerbEvalWriter(outWrapped);
			case "simple_rel":
				return new SimpleRelationClusterSetWriter(outWrapped);
			case "tuples-relations":
				return new RelationTuplesWriter(outWrapped);
			case "csv-relations":
				return new CsvRelationsWriter(outWrapped);
			case "bsnlp":
				return new BsnlpStreamWriter(outWrapped);
			default:
				throw new UnknownFormatException("Output format " + outputFormat + " not recognized.");
		}
	}

//	/**
//	 * Zwraca piśnik do zapisu danych w formacie Arff.
//	 * @param outputFile
//	 * @param features
//	 * @return
//	 * @throws Exception
//	 */
//    public ArffGenericWriter getArffAnnotationWriter(String outputFile, String relationName, List<String> features) throws Exception {
//        return new ArffGenericWriter(getOutputStreamGz(outputFile, false), relationName, features);
//    }
	
    /**
     * Zwraca piśnik do formatu ccl. Dane zapisywane są do dwóch plików: .xml i .rel.xml.
     * @param outputFile
     * @param gz
     * @return
     * @throws Exception
     */
	public AbstractDocumentWriter getCclRelWriter(String outputFile, boolean gz, boolean disambOnly) throws Exception{
		OutputStream out = this.getOutputStreamGz(outputFile, gz);		
		String relFilename = FilenameUtils.getBaseName(outputFile) + ".rel." + FilenameUtils.getExtension(outputFile);
		String relPath = new File(new File(outputFile).getParentFile(), relFilename).getPath();
		OutputStream outRel = this.getOutputStreamGz(relPath, gz);
		return new CclStreamWriter(out, outRel, disambOnly);
	}

    public AbstractDocumentWriter getTEIWriter(String outputFolder, boolean gz) throws Exception{
        if(outputFolder == null){
            throw new FileNotFoundException("TEI format requires existing folder as a target (-t) parameter value)");
        }
        File folder = new File(outputFolder);
        if ( !folder.exists() ){
        	folder.mkdirs();
        }
		return new TeiWriter(
				getOutputStreamGz(new File(folder,"text.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"metadata.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_segmentation.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_morphosyntax.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_props.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_named.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_mentions.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_chunks.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_annotations.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_coreference.xml").getPath(), gz),
				getOutputStreamGz(new File(folder,"ann_relations.xml").getPath(), gz),
				new File(outputFolder).getName());
    }

    /**
     * Tworzy strumień do zapisu danych. Jeżeli gz jest true, to strumień zostaje opakowany w obiekt GZIPOutputStream.
     * @param outputFile
     * @param gz
     * @return
     * @throws Exception
     */
	private OutputStream getOutputStreamGz(String outputFile, boolean gz) throws Exception {
		OutputStream output = new FileOutputStream(outputFile + (gz ? ".gz" : "")); 
		if ( gz ){
			output = new GZIPOutputStream(output);
		}
		return output;
	}
	
	/**
	 * Zwraca strumień do zapisu danych w zależności od wartości outputFile.
	 * @param outputFile nazwa pliku lub null
	 * @return System.out jeżeli outputFile jest nullem, wpp obiekt FileOutputStream dla outputFile.
	 * @throws FileNotFoundException
	 */
	private OutputStream getOutputStreamFileOrOut(String outputFile) throws FileNotFoundException{
		if ((outputFile == null) || (outputFile.isEmpty())){
			return System.out;
		} else {
			return new FileOutputStream(outputFile);
		}
	}
}
