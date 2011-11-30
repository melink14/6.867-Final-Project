/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of the Mario AI nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package project6867;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.MarioAIOptions;
import erekspeed.ActionWrapper;
import erekspeed.CuckooSubAgent;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy,
 * sergey@idsia.ch
 * Date: Mar 14, 2010 Time: 4:47:33 PM
 */

public class DataTask extends LearningTask
{

private Map<BitSet, Map<ActionWrapper, List<DataFitness> > > dataMap = new HashMap<BitSet, Map<ActionWrapper, List<DataFitness> > >();

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
        float prevFit = 0;
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
                
                agent.giveIntermediateReward(environment.getIntermediateReward());
                
                float intFit = environment.getIntermediateEval().computeWeightedFitness();
                
                DataFitness fit = new DataFitness(false, Math.signum(intFit-prevFit)*(float)Math.pow(intFit - prevFit, 2));
                prevFit = intFit;
                recordData(data, wrap, fit); 
                
                
                
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
        updateData(evaluationInfo.computeWeightedFitness());
    }

    return true;
}

void recordData(BitSet data, ActionWrapper wrap, DataFitness fit) {
	Map<ActionWrapper, List<DataFitness> > actions;
	if(dataMap.containsKey(data)) {
		actions = dataMap.get(data);
		System.out.println("dup1" + Arrays.deepToString(actions.keySet().toArray()));
	}
	else {
		Map<ActionWrapper, List<DataFitness> > tMap = new HashMap<ActionWrapper, List<DataFitness> >();
		dataMap.put(data, tMap);
		actions = tMap;
		
	}
	if(actions.containsKey(wrap)) {
		actions.get(wrap).add(fit);
		System.out.println("dup2 " + Arrays.deepToString(actions.get(wrap).toArray()));
	}
	else {
		List<DataFitness> l = new ArrayList<DataFitness>();
		l.add(fit);
		actions.put(wrap, l);
	}
	
}

void updateData(float fitness) {
	for( BitSet d : dataMap.keySet()) {
		for( ActionWrapper act : dataMap.get(d).keySet() ) {
			for(DataFitness df : dataMap.get(d).get(act)){
				if(!df.hasVisited()) {
					df.add(fitness);
				}
			}
		}
	}
}

}