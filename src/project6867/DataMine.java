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

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;
import erekspeed.ActionWrapper;
import erekspeed.ErekSpeedCuckooAgent;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Mar 17, 2010 Time: 8:34:17 AM
 * Package: ch.idsia.scenarios
 */

/**
 * Class used for agent evaluation in Learning track
 * http://www.marioai.org/learning-track
 */

public final class DataMine
{
final static boolean scoring = false;
final static Random ran = new Random();


private static int evaluateSubmission(MarioAIOptions marioAIOptions, LearningAgent learningAgent, int fromD, int toD, int numSeeds, String prefix)
{
    DataTask learningTask = new DataTask(marioAIOptions); // records data as it evaluates
    learningAgent.setLearningTask(learningTask);  // gives LearningAgent access to evaluator via method LearningTask.evaluate(Agent)
    
    for(int i = fromD; i <= toD; i++) {
    	marioAIOptions.setLevelDifficulty(i);
    	for(int j = 0; j < numSeeds; j++) {
    		marioAIOptions.setLevelRandSeed(ran.nextInt());
    		
    		learningTask.reset(marioAIOptions);
    		learningAgent.init();
    	    learningAgent.learn();
    	}
    }
    
    Map<BitSet, Map<ActionWrapper, List<DataFitness>>> rawData = learningTask.getDataMap();
    
    Map<BitSet, ActionWrapper> data = processData(rawData);
    
    
    

    Agent agent = learningAgent.getBestAgent(); // this agent will be evaluated

    // perform the gameplay task on the same level
    marioAIOptions.setVisualization(true);
    System.out.println("LearningTrack best agent = " + agent);
    marioAIOptions.setAgent(agent);
    BasicTask basicTask = new BasicTask(marioAIOptions);
    basicTask.setOptionsAndReset(marioAIOptions);
    System.out.println("basicTask = " + basicTask);
    System.out.println("agent = " + agent);

    boolean verbose = true;

    if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
    {
        System.out.println("MarioAI: out of computational time per action! Agent disqualified!");
    }
    EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
    System.out.println(evaluationInfo.toString());

    int f = evaluationInfo.computeWeightedFitness();
    if (verbose)
    {
        System.out.println("Intermediate SCORE = " + f + ";\n Details: " + evaluationInfo.toString());
    }

    return f;

   // return 0;
}

private static Map<BitSet, ActionWrapper> processData(Map<BitSet, Map<ActionWrapper, List<DataFitness>>> rawData) {
	Map<BitSet, ActionWrapper> ret = new HashMap<BitSet, ActionWrapper>();
	
	for(Entry<BitSet, Map<ActionWrapper, List<DataFitness>>> d : rawData.entrySet()) {
		float bestFitness = Float.MIN_VALUE;
		ActionWrapper bestAction = null;
		for(Entry<ActionWrapper, List<DataFitness>> fits : d.getValue().entrySet()) {
			float curFit = averageFitness(fits.getValue());
			
			if(curFit > bestFitness) {
				bestFitness = curFit;
				bestAction = fits.getKey();
			}
		}
		ret.put(d.getKey(), bestAction);
	}
	return ret;
}

private static float averageFitness(List<DataFitness> vals) {
	float acc = 0.0f;
	int n = 0;
	
	for(DataFitness df : vals) {
		acc += df.getFitness();
		n++;
	}
	
	return acc/n;
}


public static void main(String[] args)
{
    // set up parameters
    MarioAIOptions marioAIOptions = new MarioAIOptions(args);
    LearningAgent learningAgent = new ErekSpeedCuckooAgent(marioAIOptions);

//  no enemies or gaps or blocks
    marioAIOptions.setArgs("-vis off -ll 128 -le off -lb on -lco off -lca off -ltb off -lg off");
    DataMine.evaluateSubmission(marioAIOptions, learningAgent, 0, 5, 3, "basic");

//        Level 1
//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setAgent(learningAgent);
//    marioAIOptions.setArgs("-vis off -lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");
//    DataMine.evaluateSubmission(marioAIOptions, learningAgent, 0, 5, 5, "gaps");

//        Level 2
//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setArgs("-lde on -i on -ld 30 -ls 133434");
//    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);
//
////        Level 3
//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setArgs("-lde on -i on -ld 30 -ls 133434 -lhb on");
//    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);
//
////        Level 4
//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setArgs("-lla on -le off -lhs on -lde on -ld 5 -ls 1332656");
//    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);
//
//    // Level 5 (bonus level)
//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setArgs("-le off -lhs on -lde on -ld 5 -ls 1332656");
//    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);

    System.exit(0);
}
}
