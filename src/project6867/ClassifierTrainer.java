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

import project6867.DataHandler.DataType;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.featureselection.scoring.SymmetricalUncertainty;
import net.sf.javaml.tools.weka.WekaClassifier;
import weka.classifiers.trees.REPTree;

public class ClassifierTrainer {
	private static int DATA_SIZE = 500;
	public static enum ClassifierType{KNN, NB, ID};
	
	public static Classifier getClassifier(ClassifierType classType, DataType dataType, int dataSize){
		DATA_SIZE = dataSize;
		return getClassifier(classType, dataType, null);
	}
	
	public static Classifier getClassifier(ClassifierType classType, DataType dataType, Object[] args){
		Dataset data = DataHandler.getDataset(DATA_SIZE, dataType);
		Classifier cl;
		switch(classType){
			case KNN:	cl = new KNearestNeighbors(3, new JaccardIndex()); break;
			case NB:	cl = new NaiveBayesClassifier(false, true, true); break;
			case ID:    
				REPTree rep = new REPTree();
				rep.setNoPruning(false);
				cl = new WekaClassifier(rep); break;
			default:	cl = new NaiveBayesClassifier(false, true, true);
		}
		cl.buildClassifier(data);
		return cl;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Classifier[] cl = new Classifier[3];
		for(int i=0; i < 3; i++){
			REPTree rep = new REPTree();
			rep.setNoPruning(false);
			cl[i] = new WekaClassifier(rep);
		}
		/*
		cl[0] = new KNearestNeighbors(3, new JaccardIndex());
		cl[1] = new KNearestNeighbors(3, new EuclideanDistance());
		cl[2] = new KNearestNeighbors(3, new JaccardIndex());
		*/
		Runnable[] runners = new Runnable[3];
		Thread[] threads = new Thread[3];
		runners[0] = new ClassifierThread(cl[0], DataHandler.getDataset(500, DataType.ONE), "ID"+(5*500/1000.0)+"k_.01.out");
		runners[1] = new ClassifierThread(cl[1], DataHandler.getDataset(500, DataType.FIVE), "ID"+(5*500/1000.0)+"k_.05.out");
		runners[2] = new ClassifierThread(cl[2], DataHandler.getDataset(500, DataType.TEN), "ID"+(5*500/1000.0)+"k_.10out");
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
			Map<Object, PerformanceMeasure> pm = cv.crossValidation(this.d, 5);
			try{
				System.out.println(this.outputFile);
				BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFile));
				
				for(Entry<Object, PerformanceMeasure> e : pm.entrySet()){
					/*
					out.write(e.getKey().toString() + ", " + e.getValue().getPrecision() + ", " +
							e.getValue().getAccuracy() + ", " + e.getValue().getFMeasure() + ", " +
							e.getValue().getRecall() + ", " + e.getValue().toString() + "\n");
							*/
					out.write(e.getKey().toString() + ", " + e.getValue().toString() + "\n");
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