package g419.crete.api.classifier;

import g419.crete.api.classifier.serialization.WekaModelSerializer;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.J48graft;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;


public class WekaDecisionTreesClassifier extends WekaClassifier<Classifier, Integer>{
		
	public WekaDecisionTreesClassifier(List<String> features) {
		super(features);
	}

	@Override
	public List<Integer> classify(List<Instance> instances) {
		
		Instances clasInst = new Instances(this.instances); 
		for(Instance instance : instances) clasInst.add(instance);
		
		clasInst.setClass(attributes.get(attributes.size() - 1));
		
		ArrayList<Integer> labels = new ArrayList<Integer>();
		
		Classifier cls = this.model.getModel();

		
		for(int i = 0; i < clasInst.numInstances(); i++){
			Instance instance = clasInst.instance(i);
			try {
//				System.out.println(instance);
				System.out.println(cls.classifyInstance(instance));
				labels.add((int)Math.round(cls.classifyInstance(instance)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return labels;
	}

	private void exportInstances(Instances instances, String path) throws IOException{
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File(path));
//		saver.setDestination(new File("./data/test.arff"));   // **not** necessary in 3.5.4 and later
		saver.writeBatch();
	}
	
	@Override
	public void train() {
//		try {
//			crossValidate(10);
//			return;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Instances tInstances = instances;
		tInstances.setClass(attributes.get(attributes.size() - 1));
		
		for(int i = 0; i < this.trainingInstances.size(); i++){
			Instance instance = this.trainingInstances.get(i);
			instance.setDataset(tInstances);
			Integer oldLabel = this.trainingInstanceLabels.get(i);
			String newLabel = oldLabel > 0 ? "COREF" : "NON_COREF";
			instance.setClassValue(newLabel);
			tInstances.add(instance);
		}
		
		try {
//			exportInstances(tInstances, "/home/adam/crete-training-instances/rawinstances_verb_merge.arff");
			NumericToNominal numToNom =  new NumericToNominal();
			numToNom.setInputFormat(tInstances);
			numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
			numToNom.setInputFormat(tInstances);
			
			tInstances = Filter.useFilter(tInstances, numToNom);
			
			SMOTE smote = new SMOTE();
			smote.setInputFormat(tInstances);
			smote.setClassValue("0");
			
			Resample resample = new Resample();
			resample.setBiasToUniformClass(1.0);
			resample.setSampleSizePercent(100);
			resample.setInputFormat(tInstances);
			
			tInstances = Filter.useFilter(tInstances, smote);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
//		J48graft j48 = new J48graft();
		J48 j48 = new J48();
		RandomForest rf = new RandomForest();
		rf.setNumTrees(10);
		rf.setNumFeatures(10);
				
		Classifier cModel = (Classifier) rf;
		try {
			cModel.buildClassifier(tInstances);
			displayDebugInfo(cModel, tInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.model = new WekaModelSerializer(cModel);
	}
	
	public void crossValidate(int folds) throws Exception{
		Random rand = new Random();   // create seeded number generator
//		Instances randData = new Instances(this.instances);   // create copy of original data
		Instances randData = instances;
		randData.setClass(attributes.get(attributes.size() - 1));
		
		for(int i = 0; i < this.trainingInstances.size(); i++){
			Instance instance = this.trainingInstances.get(i);
			instance.setDataset(randData);
			Integer oldLabel = this.trainingInstanceLabels.get(i);
			String newLabel = oldLabel > 0 ? "COREF" : "NON_COREF";
			instance.setClassValue(newLabel);
			randData.add(instance);
		}
		
		try {
			NumericToNominal numToNom =  new NumericToNominal();
			numToNom.setInputFormat(randData);
			numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
			numToNom.setInputFormat(randData);
			randData = Filter.useFilter(randData, numToNom);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
//		SMOTE smote = new SMOTE();
//		smote.setInputFormat(randData);
//		smote.setClassValue("0");
//		randData = Filter.useFilter(randData, smote);
		
//		Resample resample = new Resample();
//		resample.setBiasToUniformClass(1.0);
//		resample.setSampleSizePercent(100);
//		resample.setInputFormat(randData);
//		randData = Filter.useFilter(randData, resample);
		
//		Resample resample = new Resample();
//		resample.setInputFormat(randData);
//		resample.setBiasToUniformClass(1.0);
//		
//		randData  = Filter.useFilter(randData, resample);
//		
		
		Resample resample = new Resample();
		resample.setBiasToUniformClass(1.0);
		resample.setSampleSizePercent(100);
		resample.setInputFormat(randData);
		
		randData = Filter.useFilter(randData, resample);
		
		randData.randomize(rand);
		randData.stratify(folds);
		
		
		Evaluation eval = new Evaluation(randData);
		   
		for (int n = 0; n < folds; n++) {
		   Instances train = randData.trainCV(folds, n);
		   Instances test = randData.testCV(folds, n);
		   Evaluation evalLocal = new Evaluation(test);
		   
		   J48graft j48 = new J48graft();
		   Classifier cls = (Classifier) j48;
		   
		   	Classifier clsCopy = Classifier.makeCopy(cls);
	        clsCopy.buildClassifier(train);
	        eval.evaluateModel(clsCopy, test);
		    evalLocal.evaluateModel(clsCopy, test);
		    System.out.println(evalLocal.toSummaryString());
		    System.out.println(evalLocal.toMatrixString());
//		    System.out.println(clsCopy);
		    
		    this.model = new  WekaModelSerializer(clsCopy);
		 }
		
		System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
		System.out.println(eval.toMatrixString());
		
	}
	
	private void displayDebugInfo(Classifier cModel, Instances tInstances) throws Exception{
		Evaluation eTest = new Evaluation(tInstances);
		eTest.evaluateModel(cModel, tInstances);
		String strSummary = eTest.toSummaryString();
		System.out.println(strSummary);
		System.out.println(cModel.toString());
		// Get the confusion matrix
		 System.out.println(eTest.toMatrixString());
	}
	
	private void display(J48graft tree) throws Exception{
		 final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
	     jf.setSize(1920,1080);
	     jf.getContentPane().setLayout(new BorderLayout());
	     TreeVisualizer tv = new TreeVisualizer(null, tree.graph(), new PlaceNode2());
	     jf.getContentPane().add(tv, BorderLayout.CENTER);
	     jf.addWindowListener(new java.awt.event.WindowAdapter() {
	       public void windowClosing(java.awt.event.WindowEvent e) {
	         jf.dispose();
	       }
	     });

	     jf.setVisible(true);
	     tv.fitToScreen();
	}

	@Override public Class<Integer> getLabelClass() {return Integer.class;}
	
}
