package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.pair.AnnotationPairFeatureCosineSimilarity;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureCosineSimilarityItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Float> {

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Float>  createFeature() {
		return new AnnotationPairFeatureCosineSimilarity();
	}

}
