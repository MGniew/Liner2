package liner2.filter;

import liner2.structure.Chunk;

public class FilterUppercase extends Filter{

	static String lc = "(?:[a-z]|ą|ż|ś|ź|ę|ć|ń|ó|ł)";
	static String uc = "(?:[A-Z]|Ą|Ż|Ś|Ź|Ę|Ć|Ń|Ó|Ł)";
	static String ucWord = String.format("%s%s*", FilterUppercase.uc, FilterUppercase.lc);
	static String ucWordSeq = String.format("%s( %s)*", FilterUppercase.ucWord, FilterUppercase.ucWord);
	
	public FilterUppercase() {
		this.appliesTo.add("ROAD_NAM");
	}
	
	@Override
	public String getDescription() {
		return "Uppercase: " + FilterUppercase.ucWordSeq;
	}
	
	@Override
	public Chunk pass(Chunk chunk, CharSequence charSeq) {
		if (charSeq.toString().matches(FilterUppercase.ucWordSeq))
			return chunk;
		else
			return null;
	}

}