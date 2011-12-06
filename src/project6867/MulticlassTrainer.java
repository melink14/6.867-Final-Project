package project6867;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import be.abeel.util.Pair;

import project6867.ClassifierTrainer.ClassifierType;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.parser.JFlex.Out;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;

public class MulticlassTrainer implements Classifier {
	private static final long serialVersionUID = -7248992109506404838L;
	DataHandler dh;
	Map<Pair<Object, Object>, Classifier> classifiers;
	Map<Pair<Object, Object>, Double> weights;
	ClassifierType ct;
	Object[] args;
	
	public MulticlassTrainer(ClassifierType type, Object[] args){
		this.ct = type;
		this.args = args;
		dh = new DataHandler();
		classifiers = new HashMap<Pair<Object, Object>, Classifier>();
		weights = new HashMap<Pair<Object, Object>, Double>();
	}
	
	@Override
	public void buildClassifier(Dataset data) {
		Set<Object> classes = data.classes();
		System.out.println(classes);
		Classifier cl;
		for(Object a : classes){
			for(Object b : classes){
				if(!a.equals(b) && !classifiers.containsKey(new Pair<Object, Object>(a,b))
						&& !classifiers.containsKey(new Pair<Object, Object>(b,a))){
					Dataset temp = new DefaultDataset();
					for(int x = 0; x < data.size(); x++){
						Instance i = data.get(x);
						if(i.classValue().equals(a) || i.classValue().equals(b)){
							temp.add(i);
						}
					}
					cl = getClassifier();
					cl.buildClassifier(temp);
					classifiers.put(new Pair<Object, Object>(a, b), cl);
					//weights.put(new Pair<Object, Object>(a, b), Math.log(temp.size()));
					weights.put(new Pair<Object, Object>(a, b), 1.0);
				}
			}
		}
	}

	private Classifier getClassifier(){
		Classifier cl;
		switch(ct){
			case KNN:	cl = new KNearestNeighbors((Integer) args[0]); break;
			case NB:	cl = new NaiveBayesClassifier((Boolean)args[0], (Boolean)args[1], (Boolean)args[2]); break;
			default:	cl = new NaiveBayesClassifier((Boolean)args[0], (Boolean)args[1], (Boolean)args[2]);
		}
		return cl;
	}
	@Override
	public Map<Object, Double> classDistribution(Instance i) {
		Map<Object, Double> out = new HashMap<Object, Double>();
		Map<Object, Double> dist;
		for(Entry<Pair<Object,Object>, Classifier> e : classifiers.entrySet()){
			dist = e.getValue().classDistribution(i);
			for(Entry<Object, Double> d : dist.entrySet()){
				out.put(d.getKey(), out.get(d.getKey())+d.getValue()/classifiers.size());
			}
		}
		return out;
	}

	@Override
	public Object classify(Instance i) {
		double max = 0;
		Object maxClass = null;
		Map<Object, Double> votes = new HashMap<Object, Double>();
		for(Entry<Pair<Object,Object>, Classifier> e : classifiers.entrySet()){
			Object vote = e.getValue().classify(i);
			if(votes.containsKey(vote)){
				votes.put(vote, votes.get(vote)+weights.get(e.getKey()));
			}else{
				votes.put(vote, weights.get(e.getKey()));
			}
		}
		for(Entry<Object, Double> e : votes.entrySet()){
			if(e.getValue() > max){
				max = e.getValue();
				maxClass = e.getKey();
			}
		}
		return maxClass;
	}

}
