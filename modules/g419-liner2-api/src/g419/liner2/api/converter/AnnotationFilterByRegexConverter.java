package g419.liner2.api.converter;

import g419.corpus.structure.Annotation;

import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * Created by michal on 8/20/14.
 */
public class AnnotationFilterByRegexConverter extends Converter{

    private Pattern pattern = null;

    public AnnotationFilterByRegexConverter(String pattern){
        this.pattern = Pattern.compile("^"+pattern+"$");
    }

    @Override
    public void apply(LinkedHashSet<Annotation> sentenceAnnotations) {
        LinkedHashSet<Annotation> toRemove = new LinkedHashSet<Annotation>();
        for(Annotation ann: sentenceAnnotations){
            if(!pattern.matcher(ann.getType()).find()){
                toRemove.add(ann);
            }
        }
        for(Annotation ann: toRemove){
            sentenceAnnotations.remove(ann);
        }
    }
}
