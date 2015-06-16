package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.annotations.AnnotationFeatureGender;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.features.enumvalues.Gender;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingGenderAgreement extends AbstractClusterMentionFeature<Boolean> {

	private final boolean softMasculinum;
	
	public ClusterMentionClosestPreceedingGenderAgreement(boolean softMasculinum) {
		super();
		this.softMasculinum = softMasculinum;
	}
	
	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = false;
			return;
		}
		
		AnnotationFeatureGender genderMention = new AnnotationFeatureGender();
		AnnotationFeatureGender genderPreceding = new AnnotationFeatureGender();
		
		genderMention.generateFeature(mention);
		genderPreceding.generateFeature(closestPreceeding);
		
		if(Gender.UNDEFINED.equals(genderMention.getValue())){
			this.value = false;
			return;
		}
		
		if(!softMasculinum)
			this.value = genderMention.getValue().equals(genderPreceding.getValue());
		else
			this.value = genderMention.getValue().equalsSoftMasculinum(genderPreceding.getValue());
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_gender_agreement" + (softMasculinum?"_soft_masculinum":"");
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}
	
}
