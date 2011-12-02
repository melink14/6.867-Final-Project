package project6867;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.berkeley.compbio.jlibsvm.ImmutableSvmParameterGrid;
import edu.berkeley.compbio.jlibsvm.MutableSvmProblem;
import edu.berkeley.compbio.jlibsvm.SVM;
import edu.berkeley.compbio.jlibsvm.SolutionModel;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.binary.C_SVC;
import edu.berkeley.compbio.jlibsvm.kernel.GaussianRBFKernel;
import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.berkeley.compbio.jlibsvm.kernel.LinearKernel;
import edu.berkeley.compbio.jlibsvm.labelinverter.StringLabelInverter;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassModel.AllVsAllMode;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassModel.OneVsAllMode;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassificationSVM;
import edu.berkeley.compbio.jlibsvm.multi.MutableMultiClassProblemImpl;
import edu.berkeley.compbio.jlibsvm.scaler.NoopScalingModel;
import edu.berkeley.compbio.jlibsvm.scaler.NoopScalingModelLearner;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import edu.berkeley.compbio.ml.CrossValidationResults;



public class SVMTrain {
	
	private ImmutableSvmParameterGrid<String, SparseVector> param;
	SVM svm;
	private MutableSvmProblem problem;		// set by readProblem
	private SolutionModel<String, SparseVector> model;
	
	private static String inputFileName = "";
	private  static String modelFileName = "";
	
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		inputFileName = "basic.data";
		modelFileName = "basic.model";
		
		new SVMTrain().run();
		
		
		

	}

	private void run() throws IOException {
		buildParam();
		readProblem();
		
		long startTime = System.currentTimeMillis();
		
		if (svm instanceof BinaryClassificationSVM && problem.getLabels().size() > 2) {
			svm = new MultiClassificationSVM((BinaryClassificationSVM) svm);
		}
		
		model = svm.train(problem, param);
		
		CrossValidationResults cv = model.getCrossValidationResults();
		if (cv == null)
		{
			// but if not, force it
			cv = svm.performCrossValidation(problem, param); //, execService);
		}
		if (cv != null)
		{
			System.out.println(cv.toString());
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.print("Finished in " + (endTime-startTime) + " ms");
		
		model.save(modelFileName);
		
	}
	

	private void readProblem() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		
		Vector<String> labels = new Vector<String>();
		Vector<SparseVector> data = new Vector<SparseVector>();
		int max_index = 4694;
		
		String line;
		while((line = br.readLine()) != null) {
			ArrayList<String> parts = parseLine(line);
			if(parts.size() != 2)
				continue;
			
			labels.add(parts.get(0));
			
			StringTokenizer st = new StringTokenizer(parts.get(1), "{ ,}");
			int m = st.countTokens();
			SparseVector dp = new SparseVector(m);
			
			for(int i = 0; i < m; ++i) {
				dp.indexes[i] = Integer.parseInt(st.nextToken());
				dp.values[i] = 1.0f;
			}
			
			data.addElement(dp);
		
		}
		
		problem = 
				new MutableMultiClassProblemImpl<String, SparseVector> (String.class, 
																		new StringLabelInverter(), 
																		labels.size(), 
																		new NoopScalingModel<SparseVector>());
		
		for(int i = 0; i < labels.size(); ++i) {
			problem.addExample(data.elementAt(i), labels.elementAt(i));
		}
		

		br.close();
		
	}

	private ArrayList<String> parseLine(String line) {
		// Parses format with boolean array followed by integer indices in pairs
		Pattern pattern = Pattern.compile("(\\[.*\\])\\s(\\{.*\\})");
		
		Matcher matcher = pattern.matcher(line);
		
		ArrayList<String> ret = new ArrayList<String>(2);
		if(matcher.matches()) { 
			ret.add(0, matcher.group(1));
			ret.add(1, matcher.group(2));
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void buildParam() {
		//SvmParameter
				ImmutableSvmParameterGrid.Builder<String, SparseVector> builder = ImmutableSvmParameterGrid.builder();
				
				builder.shrinking = true;
				builder.redistributeUnbalancedC = false;
				builder.cache_size = 1000;
				builder.eps = 1e-3f;
				builder.p = 0.1f;
				builder.probability = false;
				
				Collection<Float> gammaSet = new HashSet<Float>();
				builder.Cset = new HashSet<Float>();
				
				// Create gamma range for grid search
//				for(int i = -15; i <= 3; i += 2) {
//					gammaSet.add((float)Math.pow(2.0, i));
//				}
				gammaSet.add(.01f);
				gammaSet.add(.1f);
				gammaSet.add(.5f);
				
				// Create C range for grid search
				
				for(int i = -5; i <= 15; i+= 2) {
					builder.Cset.add((float)Math.pow(2, i));
				}
				
//				builder.Cset.add(4.0f);
//				builder.Cset.add(200f);
//				builder.Cset.add(.5f);
						
				
				builder.scalingModelLearner = new NoopScalingModelLearner<SparseVector>();
				
				builder.kernelSet = new HashSet<KernelFunction<SparseVector>>();
				
				
//				for(Float gamma : gammaSet)
//					builder.kernelSet.add(new GaussianRBFKernel(gamma));
				
				builder.kernelSet.add(new LinearKernel());
						
				this.param = (ImmutableSvmParameterGrid<String, SparseVector>)builder.build();
				
				svm = new C_SVC<Float, SparseVector>();
	}
	
	

}
