package project6867;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.clusterers.EM;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.distance.PearsonCorrelationCoefficient;
import net.sf.javaml.featureselection.subset.GreedyBackwardElimination;
import net.sf.javaml.featureselection.subset.GreedyForwardSelection;
import net.sf.javaml.tools.DatasetTools;
import net.sf.javaml.tools.data.FileHandler;
import net.sf.javaml.tools.weka.WekaClassifier;
import net.sf.javaml.tools.weka.WekaClusterer;

public class FeatureSelection {

	private static Random rand;
	private static int DATA_SIZE = 500;
	private static int FEATURE_SIZE = 4697;
	private static int featureCount = 0;
	private static DateFormat dateFormat;

	/*
	 * Loads a file into the data field.
	 * 
	 * @param filename
	 */
	private static Dataset loadFile(String filename, int numRecords) {
		System.out.println("Loading " + filename);
		Dataset data = new DefaultDataset();
		try {			
			BufferedReader r = new BufferedReader(new FileReader(filename));
			Pattern p = Pattern.compile("(.*)\\s\\{(.*)\\}");
			Matcher m;
			String s;
			Instance feature_vector;
			for(int i = 0; i < numRecords; i++){s = r.readLine(); //while ((s = r.readLine()) != null) {
				m = p.matcher(s);
				if (m.matches()) {
					feature_vector = new SparseInstance(FEATURE_SIZE);
					feature_vector.setClassValue(m.group(1));
					for(String index : m.group(2).replaceAll(" ","").split(",")){
						if(index.length() > 0){
							feature_vector.put(Integer.parseInt(index), 1.0);
						}
					}
					data.add(feature_vector);
				}
			}
			FileHandler.exportDataset(data, new File(filename+".tsf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data.folds(data.size()/numRecords, rand)[0]; 
	}
	
	private static Dataset loadMaskedFile(String maskFile, String filename, int numRecords) {
		System.out.println("Loading " + filename + " and keeping features as specified by " + maskFile);
		Dataset data = new DefaultDataset();
		boolean[] mask = null;
		try {
			BufferedReader r;
			r = new BufferedReader(new FileReader(maskFile));
			String[] maskString = r.readLine().replace(" ","").split(",");
			mask = new boolean[FEATURE_SIZE];
			for( String s : maskString){
				featureCount++;
				mask[Integer.parseInt(s)] = true;
			}
			
			r = new BufferedReader(new FileReader(filename));
			Pattern p = Pattern.compile("(.*)\\s\\{(.*)\\}");
			Matcher m;
			String s;
			Instance feature_vector;
			for(int i = 0; i < numRecords; i++){s = r.readLine(); //while ((s = r.readLine()) != null) {
				m = p.matcher(s);
				if (m.matches()) {
					feature_vector = new SparseInstance(featureCount);
					feature_vector.setClassValue(m.group(1));
					for(String idx : m.group(2).replaceAll(" ","").split(",")){
						if(idx.length() > 0){
							int index = Integer.parseInt(idx);
							if(mask[index])
								feature_vector.put(index, 1.0);
						}
					}
					data.add(feature_vector);
				}
			}
			FileHandler.exportDataset(data, new File(filename+".tsf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data.folds(data.size()/numRecords, rand)[0]; 
	}
	
	private static Dataset getCompositeDataset(int numRecords){
		Dataset data = new DefaultDataset();
		DatasetTools.merge(data,
				loadFile("basic.data", DATA_SIZE),
				loadFile("basicenemies.data", DATA_SIZE),
				loadFile("basicgaps.data", DATA_SIZE),
				loadFile("enemiesblocks.data", DATA_SIZE),
				loadFile("enemiesblocksgaps.data", DATA_SIZE));

		return data;
	}
	
	private static Dataset getMaskedCompositeDataset(String maskFile, int numRecords){
		Dataset data = new DefaultDataset();
		DatasetTools.merge(data,
				loadMaskedFile(maskFile, "basic.data", DATA_SIZE),
				loadMaskedFile(maskFile, "basicenemies.data", DATA_SIZE),
				loadMaskedFile(maskFile, "basicgaps.data", DATA_SIZE),
				loadMaskedFile(maskFile, "enemiesblocks.data", DATA_SIZE),
				loadMaskedFile(maskFile, "enemiesblocksgaps.data", DATA_SIZE));
		return data;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		rand = new Random();
		dateFormat = new SimpleDateFormat("HH:mm:ss");
		//Dataset data = getMaskedCompositeDataset("forward@0.01_20000mixednew.data", DATA_SIZE);

		//System.out.println(data.size() + " datapoints loaded...");

		
		
		
		Classifier cl1 = new KNearestNeighbors(3);
		Classifier cl2 = new KNearestNeighbors(3);
		Classifier cl3 = new KNearestNeighbors(3);
		Runnable[] runners = new Runnable[3];
		Thread[] threads = new Thread[3];
		//runners[0] = new FeatureThread(data, FEATURE_SIZE, .05, FeatureThread.Direction.FORWARD);
		//runners[1] = new FeatureThread(data, FEATURE_SIZE, .10, FeatureThread.Direction.FORWARD);
		//runners[2] = new FeatureThread(data, FEATURE_SIZE, .01, FeatureThread.Direction.FORWARD);
		runners[0] = new ClassifierThread(cl1, getMaskedCompositeDataset("forward@0.01_5000mixednew.data", DATA_SIZE), "3KNN"+(5*DATA_SIZE/1000)+"k_.01.out");
		runners[1] = new ClassifierThread(cl2, getMaskedCompositeDataset("forward@0.05_5000mixednew.data", DATA_SIZE), "3KNN"+(5*DATA_SIZE/1000)+"k_.05.out");
		runners[2] = new ClassifierThread(cl3, getMaskedCompositeDataset("forward@0.1_5000mixednew.data", DATA_SIZE), "3KNN"+(5*DATA_SIZE/1000)+"k_.1.out");
		for(int i = 0; i < 3; i++){
			threads[i] = new Thread(runners[i]);
			threads[i].start();
		}
		try{
			for(int i = 0; i < 3; i++){
				threads[i].join();
			}
		}catch(InterruptedException e){
			e.printStackTrace();
		}

		try{
			/*
			EM em = new EM();
			em.setMaxIterations(1000);
			em.setNumClusters(16);
			Clusterer jmlem = new WekaClusterer(em);
			Dataset[] clusters = jmlem.cluster(data);
			System.out.println("End at " + dateFormat.format(new Date()));
			System.out.println(em.toString()+"\n");
			for(int i = 0; i < clusters.length; i++){
				System.out.println("Cluster "+ i + ":" + clusters[i].size() + " elements.");
				System.out.println(clusters[i].classes());
			}
			*/
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	private static class ClassifierThread implements Runnable{
		private Dataset d;
		private Classifier cl;
		private DateFormat dateFormat;
		private String outputFile;
		
		public ClassifierThread(Classifier cl, Dataset d){
			dateFormat = new SimpleDateFormat("HH:mm:ss");
			this.d = d.copy();
			this.cl = cl;
			outputFile = this.toString() + dateFormat.format(new Date()) + ".out";
		}
		
		public ClassifierThread(Classifier cl, Dataset d, String outputFile){
			this.outputFile = outputFile;
			this.cl = cl;
			this.d = d.copy();
			dateFormat = new SimpleDateFormat("HH:mm:ss");
		}
		
		@Override
		public void run() {
			System.out.println(this.toString() + ":Start at " + dateFormat.format(new Date()));
			CrossValidation cv = new CrossValidation(this.cl);
			Map<Object, PerformanceMeasure> pm = cv.crossValidation(this.d, 10);
			try{
				System.out.println(this.outputFile);
				BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFile));
				
				for(Entry<Object, PerformanceMeasure> e : pm.entrySet()){
					out.write(e.getKey().toString() + " : " + e.getValue().getAccuracy() +
							"(" + (e.getValue().tp+e.getValue().fn) + "," + (e.getValue().fp + e.getValue().tn) + ")" +
							"-->" + e.getValue().toString() + "\n");
				}
				out.flush();
				out.close();
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.outputFile.replace(".out", ".classifier")));
				oos.writeObject(cl);
				oos.close();
				System.out.println(this.toString() + ":Finished successfully at " + dateFormat.format(new Date()));
			}catch(IOException e){
				e.printStackTrace();
				System.out.println(this.toString() + ":End at " + dateFormat.format(new Date()));				
			}
		}
		
		public String toString(){
			String c = cl.getClass().toString();
			c = c.substring(c.lastIndexOf(".")+1);
			return "ClassifierThread(" + c + ")"; 
		}
	}
	
	private static class FeatureThread implements Runnable {

		private Dataset data;
		private double n;
		private int featureSize;
		private Direction direction;
		private DateFormat dateFormat;
		static enum Direction{FORWARD, BACKWARD};
		
		public FeatureThread(Dataset d, int featureSize, double retention, Direction dir){
			this.data = d.copy();
			this.n = retention;
			this.featureSize = featureSize;
			this.direction = dir;
			dateFormat = new SimpleDateFormat("HH:mm:ss");
		}
		
		@Override
		public void run() {
			System.out.println(this.toString() + ":Started at " + dateFormat.format(new Date()));
			Set<Integer> selected;
			if(this.direction == Direction.FORWARD){
				GreedyForwardSelection selector = new GreedyForwardSelection((int)(this.featureSize*this.n), new PearsonCorrelationCoefficient());
				selector.build(data);
				selected = selector.selectedAttributes();
			}else{
				GreedyBackwardElimination selector = new GreedyBackwardElimination((int)(this.featureSize*this.n), new PearsonCorrelationCoefficient());
				selector.build(data);
				selected = selector.selectedAttributes();
			}
			System.out.println(this.toString() + ":Writing output at " + dateFormat.format(new Date()));
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(this.toString() + "_" + data.size() + "mixed.data"));
				for(Integer i : selected){
					out.write(i.toString() + ", ");
				}
				out.flush();
				out.close();
				System.out.println(this.toString() + ":Finished successfully at " + dateFormat.format(new Date()));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		public String toString(){
			return (this.direction == Direction.FORWARD ? "forward" : "backward") + "@" + this.n;
		}
	}

}


