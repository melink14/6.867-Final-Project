package project6867;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.BitSet;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;
import erekspeed.ActionWrapper;

public class MLAgent implements Agent {

	Classifier clf;
	String filename;
	BitSet obs;
	private String name;
	
	public MLAgent(String fn) {
		try {
			long start, stop, elapsed;
			start = System.currentTimeMillis();
			FileInputStream is = new FileInputStream(fn);
			ObjectInputStream in = new ObjectInputStream(is);
			Object obj = in.readObject();
			
			if(obj instanceof Classifier)
				clf = (Classifier)obj;
			
			stop = System.currentTimeMillis();
			elapsed = stop - start;

			System.out.println("Time taken to load:" + elapsed);
		}
		catch (Exception ex) {
			System.err.println(ex);
		}
		setName("ML Agent");
	}
	
	public MLAgent(Classifier c) {
		clf = c;
		setName("ML Agent");
	}

	@Override
	public boolean[] getAction() {
		String cls = clf.classify(getInstance()).toString();
		System.out.print(cls + " ");
		int cl = Integer.parseInt(cls);
		
		ActionWrapper a = ActionWrapper.parseActionWrapper(cl);
		
		return a.acts;
	}

	private Instance getInstance() {
		Instance ret = new SparseInstance(obs.size());
		int index = 0;
		
		while((index = obs.nextSetBit(index)) != -1) {
			ret.put(index, 1.0);
			index += 1;
		}
		
		return ret;
	}

	@Override
	public void integrateObservation(Environment environment) {
		obs = environment.getMergedObservationZZBit(3, 3);
		
		if(environment.getMarioMode() != 0)
			obs.set(environment.getReceptiveFieldHeight()*environment.getReceptiveFieldWidth()*13 + environment.getMarioMode()-1);
		if(environment.isMarioAbleToJump())
			obs.set(environment.getReceptiveFieldHeight()*environment.getReceptiveFieldWidth()*13 + 2);
		if(!environment.isMarioOnGround())
			obs.set(environment.getReceptiveFieldHeight()*environment.getReceptiveFieldWidth()*13 + 3);
		
//		obs = new BitSet();
//		if(environment.isMarioAbleToJump())
//			obs.set(0);
//		
//		if(!environment.isMarioOnGround())
//			obs.set(1);
		
//		System.out.println(obs.toString());
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
			int egoCol) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

}
