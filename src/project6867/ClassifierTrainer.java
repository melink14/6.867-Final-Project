/**
 * 
 */
package project6867;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import project6867.DataHandler.DataType;

public class ClassifierTrainer {
	private static int DATA_SIZE = 1000;
	private static DataHandler dh;
	public static enum ClassifierType{KNN, NB};
		
	public static Classifier getClassifier(ClassifierType classType, DataType dataType, Object[] args){
		dh = new DataHandler();
		Dataset data = dh.getDataset(DATA_SIZE, dataType);
		Classifier cl;
		switch(classType){
			case KNN:	cl = new KNearestNeighbors(3); break;
			case NB:	cl = new NaiveBayesClassifier(false, true, true); break;
			default:	cl = new NaiveBayesClassifier(false, true, true);
		}
		cl.buildClassifier(data);
		return cl;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		dh = new DataHandler();
		Object[] a = {false, true, true};
		Classifier cl1 = new MulticlassTrainer(ClassifierType.NB, a);
		//Classifier cl2 = new MulticlassTrainer(ClassifierType.NB, a);
		//Classifier cl3 = new MulticlassTrainer(ClassifierType.NB, a);
		Runnable[] runners = new Runnable[3];
		Thread[] threads = new Thread[3];
		runners[0] = new ClassifierThread(cl1, dh.getDataset(DATA_SIZE, DataType.ONE), "1KNN"+(5*DATA_SIZE/1000)+"k_.01.out");
		//runners[1] = new ClassifierThread(cl2, dh.getDataset(DATA_SIZE, DataType.FIVE), "1KNN"+(5*DATA_SIZE/1000)+"k_.05.out");
		//runners[2] = new ClassifierThread(cl3, dh.getDataset(DATA_SIZE, DataType.TEN), "1KNN"+(5*DATA_SIZE/1000)+"k_.1.out");
		for(int i = 0; i < 1; i++){
			threads[i] = new Thread(runners[i]);
			threads[i].start();
		}
		try{
			for(int i = 0; i < 1; i++){
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
					out.write(e.getKey().toString() + " : " + e.getValue().getPrecision() +
							"(" + (e.getValue().tp+e.getValue().fn) + "," + (e.getValue().fp + e.getValue().tn) + ")" +
							"-->" + e.getValue().toString() + "\n");
				}
				out.flush();
				out.close();
				if(cl instanceof Serializable){
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.outputFile.replace(".out", ".classifier")));
					oos.writeObject(cl);
					oos.close();
				}
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