package project6867;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import libsvm.LibSVM;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

public class MarioML {

	private static final int FEATURE_SIZE = 4695;
	private static Dataset data;	


	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		MarioML marioml = new MarioML();
		Collection<String> filenames = new ArrayList<String>(5);
		
		// add the data I want to load
		filenames.add("basic.data");
		filenames.add("basicenemies.data");
		filenames.add("basicgaps.data");
		filenames.add("eneimesblocksgaps.data");
		filenames.add("eneimesblocks.data");
		
		int datasize = 100000;
		
		float gamma = .5f;
		float C = 10f;
		String kernel = "rbf";
		
		String output = "svm" + kernel + "g" + gamma + "c" + C + "ds" + datasize +".out";
		
		
		Classifier clf = marioml.runSVM(filenames, datasize, C, gamma, kernel);
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
		
		oos.writeObject(clf);

	}
	
	
	private Classifier runSVM(Collection<String> filenames, int datasize, float c,
			float gamma, String kernel) {
		
		for(String str : filenames) {
			System.out.println("load " + str);
			MarioML.loadFile(str);
		}
		
//		//Sampling s = Sampling.StratifiedSubsampling;
//		Pair<Dataset, Dataset> datas = s.sample(data, datasize);
//		System.out.println("Done Sampling");
		
		long start = System.currentTimeMillis();
		LibSVM svm = new LibSVM();
		svm.getParameters().C = c;
		svm.getParameters().kernel_type = libsvm.svm_parameter.RBF;
		svm.getParameters().gamma = gamma;
		svm.getParameters().cache_size = 1000;
		
		svm.buildClassifier(data);
		
		System.out.println(System.currentTimeMillis() - start);
		
		start = System.currentTimeMillis();
		Map<Object, PerformanceMeasure> p = new CrossValidation(svm).crossValidation(data, 5);
		System.out.println(System.currentTimeMillis() - start);
		
		for(Object o:p.keySet()) {
			System.out.println(o+": "+p.get(o).getAccuracy());
			System.out.println(o+": "+p.get(o).getPrecision());
			System.out.println(o+": "+p.get(o).getRecall());
			System.out.println(o+": "+p.get(o).getFMeasure());
		
		}
		
		return svm;
		
	}


	/*
	 * Loads a file into the class' data field.
	 * @param filename
	 */
	private static void loadFile(String filename) {
		try {
			data = (data == null)?new DefaultDataset():data;
			BufferedReader r = new BufferedReader(new FileReader(filename));
			Pattern p = Pattern.compile("(.*)\\s\\{(.*)\\}");
			Matcher m;
			String s;
			Instance feature_vector;
			int count = 0;
			while ((s = r.readLine()) != null) {
				m = p.matcher(s);
				if (m.matches()) {
					feature_vector = new SparseInstance(FEATURE_SIZE);
					feature_vector.setClassValue(m.group(1));
					for(String index : m.group(2).split(", ")){
						if(index.equals(""))
							break;
						feature_vector.put(Integer.parseInt(index), 1.0);
					}
					data.add(feature_vector);
					if(++count >= 25000)
						break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
