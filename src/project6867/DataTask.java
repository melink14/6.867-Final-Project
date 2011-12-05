

package project6867;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.MarioAIOptions;
import erekspeed.ActionWrapper;
import erekspeed.CuckooSubAgent;



public class DataTask extends LearningTask
{

private Map<BitSet, Map<ActionWrapper, List<DataFitness> > > dataMap = new HashMap<BitSet, Map<ActionWrapper, List<DataFitness> > >();
private final int maxEvals = 30000;
private final int refineEvals = 1000;
private int startRefine = 0;
private boolean hasWon = false;
private int curEvals = 0;

public DataTask(MarioAIOptions marioAIOptions)
{
   super(marioAIOptions);
}

/**
 * @param repetitionsOfSingleEpisode
 * @return boolean flag whether controller is disqualified or not
 */
public boolean runSingleEpisode(final int repetitionsOfSingleEpisode)
{
    long c = System.currentTimeMillis();
    for (int r = 0; r < repetitionsOfSingleEpisode; ++r)
    {
        this.reset();
        CuckooSubAgent myAgent = (CuckooSubAgent)agent;
        float prevFit = environment.getIntermediateEval().computeWeightedFitness();
        ArrayList<DataFitness> newPoints = new ArrayList<DataFitness>();
        Map<BitSet, Map<ActionWrapper, List<DataFitness> > > localMap = new HashMap<BitSet, Map<ActionWrapper, List<DataFitness> > >();
        
        while (!environment.isLevelFinished())
        {
            environment.tick();
            if (!GlobalOptions.isGameplayStopped)
            {
                c = System.currentTimeMillis();
                
                agent.integrateObservation(environment);
                BitSet data = myAgent.mergedObservationBit;
                boolean[] action = agent.getAction();
                ActionWrapper wrap = new ActionWrapper(action);
               // System.out.println(ActionWrapper.intFromBooleanArray(wrap.acts));
                agent.giveIntermediateReward(environment.getIntermediateReward());
                
                float intFit = environment.getIntermediateEval().computeWeightedFitness();
                
                DataFitness fit = new DataFitness(false, Math.signum(intFit-prevFit)*(float)Math.pow(intFit - prevFit, 2));
                newPoints.add(fit);
                prevFit = intFit;
                recordData(data, wrap, fit, localMap);
                
                
                
                
                if (System.currentTimeMillis() - c > COMPUTATION_TIME_BOUND)
                    return false;
//                System.out.println("action = " + Arrays.toString(action));
//            environment.setRecording(GlobalOptions.isRecording);
                environment.performAction(action);
            }
        }
        environment.closeRecorder(); //recorder initialized in environment.reset
        environment.getEvaluationInfo().setTaskName(name);
        this.evaluationInfo = environment.getEvaluationInfo().clone();
        if(this.evaluationInfo.marioStatus == 1) {
        	if(!hasWon) startRefine = curEvals;
        	hasWon = true;
        	dataMap.putAll(localMap);
        	updateData(evaluationInfo.computeWeightedFitness(), newPoints);
        }
     
        
    }

    return true;
}



public int evaluate(Agent agent)
{
    if ((hasWon && curEvals > startRefine+refineEvals) || curEvals > maxEvals)
        return 0;
    curEvals++;
    options.setAgent(agent);
    environment.reset(options);
    this.runSingleEpisode(1);
    return this.getEvaluationInfo().computeWeightedFitness();
}

public void reset(MarioAIOptions opts) {
	curEvals = 0;
	hasWon = false;
	startRefine = 0;
	super.reset(opts);
}

/**
 * @return the dataMap
 */
public Map<BitSet, Map<ActionWrapper, List<DataFitness>>> getDataMap() {
	return dataMap;
}

/**
 * @param dataMap the dataMap to set
 */
public void setDataMap(
		Map<BitSet, Map<ActionWrapper, List<DataFitness>>> dataMap) {
	this.dataMap = dataMap;
}

private void recordData(BitSet data, ActionWrapper wrap, DataFitness fit,
		Map<BitSet, Map<ActionWrapper, List<DataFitness>>> map) {
	
	Map<ActionWrapper, List<DataFitness> > actions;
	if(map.containsKey(data)) {
		actions = map.get(data);
		//System.out.println("dup1" + Arrays.deepToString(actions.keySet().toArray()));
	}
	else {
		Map<ActionWrapper, List<DataFitness> > tMap = new HashMap<ActionWrapper, List<DataFitness> >();
		map.put(data, tMap);
		actions = tMap;
		
	}
	if(actions.containsKey(wrap)) {
		actions.get(wrap).add(fit);
		//System.out.println("dup2 " + Arrays.deepToString(actions.get(wrap).toArray()));
	}
	else {
		List<DataFitness> l = new ArrayList<DataFitness>();
		l.add(fit);
		actions.put(wrap, l);
	}
	
}

private void recordData(BitSet data, ActionWrapper wrap, DataFitness fit) {
	
	recordData(data, wrap, fit, dataMap);
	
}

void updateData(float fitness, List<DataFitness> d) {
	
	for(DataFitness df : d) {
		df.add(fitness);
		df.hasVisited();
	}
}

}