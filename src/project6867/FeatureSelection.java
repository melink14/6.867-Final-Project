package project6867;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.featureselection.ranking.RecursiveFeatureEliminationSVM;
import net.sf.javaml.featureselection.scoring.GainRatio;

public class FeatureSelection {

	private static HashMap<String, Integer> classes;
	private static int DATA_SIZE = 2000;
	private static int FEATURE_SIZE = 4695;
	private static Dataset data;

	/*
	 * Loads a file into the class' data field.
	 * @param filename
	 */
	private static void loadFile(String filename) {
		try {
			data = new DefaultDataset();
			BufferedReader r = new BufferedReader(new FileReader(filename));
			Pattern p = Pattern.compile("(.*)\\s\\{(.*)\\}");
			Matcher m;
			String s;
			Instance feature_vector;
			while ((s = r.readLine()) != null) {
				m = p.matcher(s);
				if (m.matches()) {
					feature_vector = new SparseInstance(FEATURE_SIZE);
					feature_vector.setClassValue(m.group(1));
					for(String index : m.group(2).split(", ")){
						feature_vector.put(Integer.parseInt(index), 1.0);
					}
					data.add(feature_vector);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		loadFile("basic.data");
		System.out.println(data.size() + " datapoints");
		System.out.println("data loaded...");
		//TODO: use svm feature eliminator or various other methods.
				
		System.out.println("Done");
		System.exit(0);
	}

}
