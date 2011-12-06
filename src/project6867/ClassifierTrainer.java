/**
 * 
 */
package project6867;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.weka.WekaClassifier;
import weka.classifiers.trees.REPTree;

public class ClassifierTrainer {
	private static int DATA_SIZE = 1000;
	public static enum DataType{FULL, ONE, FIVE, TEN};
	public static enum ClassifierType{KNN, NB, ID};
	
	public static Classifier getClassifier(ClassifierType classType, DataType dataType, Object[] args){
		Dataset data = new DefaultDataset();
		switch(dataType){
			case FULL:	data = DataHandler.getCompositeDataset(DATA_SIZE); break;
			case ONE:	data = DataHandler.getMaskedCompositeDataset("forward@0.01_5000mixed.data", DATA_SIZE); break;
			case FIVE:	data = DataHandler.getMaskedCompositeDataset("forward@0.05_5000mixed.data", DATA_SIZE); break;
			case TEN:	data = DataHandler.getMaskedCompositeDataset("forward@0.1_5000mixed.data", DATA_SIZE); break;
			default:	data = DataHandler.getMaskedCompositeDataset("forward@0.01_5000mixed.data", DATA_SIZE); break;
		}
		Classifier cl;
		switch(classType){
			case KNN:	cl = new KNearestNeighbors(3); break;
			case NB:	cl = new NaiveBayesClassifier(false, true, true); break;
			case ID:    
				REPTree rep = new REPTree();
				rep.setNoPruning(false);
				cl = new WekaClassifier(rep); break;
			default:	cl = new NaiveBayesClassifier(false, true, true);
		}
//		CrossValidation cv = new CrossValidation(cl);
//		cv.crossValidation(data,10);
		cl.buildClassifier(data);
		return cl;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataHandler dh = new DataHandler();
		Classifier cl1 = new NaiveBayesClassifier(false, true, true);
		Classifier cl2 = new NaiveBayesClassifier(false, true, true);
		Classifier cl3 = new NaiveBayesClassifier(false, true, true);
		Runnable[] runners = new Runnable[3];
		Thread[] threads = new Thread[3];
		runners[0] = new ClassifierThread(cl1, DataHandler.getMaskedCompositeDataset("forward@0.01_5000mixed.data", DATA_SIZE), "NB"+(5*DATA_SIZE/1000)+"k_.01.out");
		runners[1] = new ClassifierThread(cl2, DataHandler.getMaskedCompositeDataset("forward@0.05_5000mixed.data", DATA_SIZE), "NB"+(5*DATA_SIZE/1000)+"k_.05.out");
		runners[2] = new ClassifierThread(cl3, DataHandler.getMaskedCompositeDataset("forward@0.1_5000mixed.data", DATA_SIZE), "NB"+(5*DATA_SIZE/1000)+"k_.1.out");
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
					out.write(e.getKey().toString() + " : " + e.getValue().getPrecision() +
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
	
}