package g419.crete.api.annotation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PatternAnnotationSelector extends AbstractAnnotationSelector{

	private List<Pattern> types;
	
	public PatternAnnotationSelector(String[] patterns){
		types = new ArrayList<Pattern>();
		for(String pattern : patterns) types.add(Pattern.compile(pattern));
	}
	
	@Override
	public List<Annotation> selectAnnotations(Document document) {
		return document.getAnnotations(types);
	}

}
