package liner2.features;

import java.util.ArrayList;

import liner2.LinerOptions;
import liner2.features.tokens.DictFeature;
import liner2.features.tokens.Feature;
import liner2.features.tokens.TokenFeature;
import liner2.features.tokens.TokenFeatureFactory;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class TokenFeatureGenerator {

	private ArrayList<TokenFeature> tokenGenerators = new ArrayList<TokenFeature>();
	private ArrayList<DictFeature> sentenceGenerators = new ArrayList<DictFeature>();
	private TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
	private String[] sourceFeatures = new String[]{"orth", "base", "ctag"};
	
	/**
	 * 
	 * @param features — array with feature definitions
	 */
	public TokenFeatureGenerator(ArrayList<String> features){
		for( String sf: sourceFeatures)
			this.attributeIndex.addAttribute(sf);

		for ( String feature : features ){
			Feature f = TokenFeatureFactory.create(feature);
			if  (f != null){
				if (DictFeature.class.isInstance(f))
					this.sentenceGenerators.add((DictFeature) f);
				else
					this.tokenGenerators.add((TokenFeature) f);
				System.out.println(f.getName());
				this.attributeIndex.addAttribute(f.getName());
			}
		}
	}
	
	/**
	 * Return index of token attributes (mapping from feature name to their corresponding
	 * position in the array of attributes).
	 * @return
	 */
	public TokenAttributeIndex getAttributeIndex(){
		return this.attributeIndex;
	}
	
	/**
	 * Generates feature for every token in the paragraph set. The features are added to the 
	 * token list of attributes.
	 * @param ps
	 * @throws Exception
	 */
	public void generateFeatures(ParagraphSet ps) throws Exception {
		ps.getAttributeIndex().update(this.attributeIndex.allAtributes());
		for (Paragraph p : ps.getParagraphs()){
			generateFeatures(p);
		}
		ps.getAttributeIndex().update(LinerOptions.get().featureNames);
	}

	public void generateFeatures(Paragraph p) throws Exception {
		for (Sentence s : p.getSentences())
			generateFeatures(s);
	}

	public void generateFeatures(Sentence s) throws Exception {
		for (DictFeature f : this.sentenceGenerators){
			f.generate(s, this.attributeIndex.getIndex(f.getName()));}
		for (Token t : s.getTokens()){
			ArrayList<Integer> toDel = new ArrayList<Integer>();
			generateFeatures(t);
			for(String sourceFeat: sourceFeatures)
				if(!LinerOptions.get().featureNames.contains(sourceFeat))
					toDel.add(this.attributeIndex.getIndex(sourceFeat)-toDel.size());
			for(int idx: toDel){
				t.removeAttribute(idx);
			}

		}
	}

	public void generateFeatures(Token t) throws Exception {
		for (TokenFeature f : this.tokenGenerators){
			t.setAttributeValue(this.attributeIndex.getIndex(f.getName()), f.generate(t));
		}
	}
}