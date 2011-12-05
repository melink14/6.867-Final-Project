package project6867;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.tools.DatasetTools;

public class FeatureSelection {
	private static int DATA_SIZE = 1000;
	private static int FEATURE_SIZE = 4695;

	/*
	 * Loads a file into the data field.
	 * TODO: Make it a random sampling instead of first N records.
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	/*	
	private static void doGreedy(Dataset data, double percentToKeep){
		try{
			GreedyBackwardElimination back = new GreedyBackwardElimination((int)(FEATURE_SIZE*percentToKeep), new EuclideanDistance());
			back.build(data);
			Set<Integer> backSelect = back.selectedAttributes();
			BufferedWriter out = new BufferedWriter(new FileWriter("backward"+(int)(percentToKeep*100)+"percent"+data.size()+"mixed.data")); 
			for(Integer i : backSelect){
				out.write(i.toString() + ", ");
			}
			out.flush();
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		try{
			GreedyForwardSelection forward = new GreedyForwardSelection((int)(FEATURE_SIZE*percentToKeep), new PearsonCorrelationCoefficient());
			forward.build(data);
			Set<Integer> selected = forward.selectedAttributes();
			BufferedWriter out = new BufferedWriter(new FileWriter("forward"+(int)(percentToKeep*100)+"percent"+data.size()+"mixed.data")); 
			for(Integer i : selected){
				out.write(i.toString() + ", ");
			}
			out.flush();
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dataset data = new DefaultDataset();
		DatasetTools.merge(data,
				loadFile("basic.data", DATA_SIZE),
				loadFile("basicenemies.data", DATA_SIZE),
				loadFile("basicgaps.data", DATA_SIZE),
				loadFile("enemiesblocks.data", DATA_SIZE),
				loadFile("enemiesblocksgaps.data", DATA_SIZE));
		System.out.println(data.size() + " datapoints loaded...");
		
		
		Runnable[] runners = new Runnable[3];
		Thread[] threads = new Thread[3];
		runners[0] = new FeatureThread(data, FEATURE_SIZE, .05, FeatureThread.Direction.FORWARD);
		runners[1] = new FeatureThread(data, FEATURE_SIZE, .10, FeatureThread.Direction.FORWARD);
		runners[2] = new FeatureThread(data, FEATURE_SIZE, .01, FeatureThread.Direction.FORWARD);
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

}
