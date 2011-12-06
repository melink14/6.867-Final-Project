/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.javaml.classification.Classifier;
import project6867.ClassifierTrainer.ClassifierType;
import project6867.DataHandler.DataType;
import ch.idsia.agents.Agent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;
/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */



public final class MarioTest
{

	private static MarioAIOptions options;
	private static BufferedWriter output;

	private static Map<String, String> ops;
	
	public static void evaluate(Agent agent) {
		options.setAgent(agent);
		final BasicTask basicTask = new BasicTask(options);
		//options.setVisualization(true);
		basicTask.doEpisodes(1, false, 1);
		EvaluationInfo info = basicTask.getEnvironment().getEvaluationInfo();
		try {
			output.write("{" + ops.get(options.asString().trim()) + "," + options.getLevelDifficulty() + "} "
					+ info.computeWeightedFitness() + ", "
					+ info.computeBasicFitness() + ", "
					+ info.distancePassedCells + ", "
					+ info.marioStatus + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args)
	{
		try{output = new BufferedWriter(new FileWriter("MCNB10k_.1results.txt"));}catch(IOException e){e.printStackTrace();}
		Object[] a = {false, true, true};
		MulticlassTrainer mt = new MulticlassTrainer(ClassifierType.NB, a);
		mt.buildClassifier(new DataHandler().getDataset(1000, DataType.ONE));
		Agent agent = new MLAgent(mt);
		//Agent agent = new MLAgent(ClassifierTrainer.getClassifier(ClassifierType.NB, DataType.TEN, null));
		//Agent agent = new MLAgent("1KNN5k_.05.classifier");
		
		ops = new HashMap<String,String>();
		ops.put("-vis off -ll 256 -lb off -lco off -lca off -ltb off -lg off -le off -ls 98886", "0,0,0");
		ops.put("-vis off -ll 256 -lb off -lco off -lca off -ltb off -lg off -ls 31646", "1,0,0");
		ops.put("-vis off -ll 256 -lb off -lco off -lca off -ltb off -lg on -le off -ls 16007", "0,0,1");
		ops.put("-vis off -ll 256 -lb on -lco off -lca off -ltb off -lg off -ls 19682", "1,1,0");
		ops.put("-vis off -ll 256 -lb on -lco off -lca off -ltb off -lg on -ls 79612", "1,1,1");
		for(int diff = 1; diff < 10; diff++){
	    	for(Entry<String,String> e : ops.entrySet()){
	    		System.out.println(e.getKey());
	    		options = new MarioAIOptions(args);
	    		options.setArgs(e.getKey());
	    		options.setLevelDifficulty(diff);
	    		evaluate(agent);
	    	}
	    }		
		try{output.close();}catch(IOException e){e.printStackTrace();}
		System.exit(0);
	}
}
