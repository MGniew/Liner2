package liner2.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.structure.Chunk;

public class FilterNoUnderline extends Filter {

	private static String hasSymbol = "([_])";
	private static Pattern pattern = null;
	
	public FilterNoUnderline(){
		this.appliesTo.add("PERSON_FIRST_NAM");
		this.appliesTo.add("PERSON_LAST_NAM");
		this.appliesTo.add("CITY_NAM"); 
		this.appliesTo.add("COUNTRY_NAM");
		this.appliesTo.add("ROAD_NAM");
		
		pattern = Pattern.compile(hasSymbol);
	}
	
	@Override
	public String getDescription() {
		return "Does not have any symbol: " + hasSymbol;
	}

	@Override
	public Chunk pass(Chunk chunk, CharSequence charSeq) {
		Matcher m = pattern.matcher(charSeq);
		if ( !m.find() )
			return chunk;
		else
			return null;
	}

}