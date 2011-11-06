package erekspeed;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:30:41 AM
 * Package: ch.idsia.controllers.agents.controllers;
 */
public class BasicAIAgent implements Agent, Serializable {
	static final long serialVersionUID = 548334862486559097L;

	protected boolean action[] = new boolean[Environment.numberOfKeys];
	protected String name = "Instance_of_BasicAIAgent._Change_this_name";

	/*final*/
	protected byte[][] levelScene;
	/*final */
	protected byte[][] enemies;
	protected byte[][] mergedObservation;

	protected float[] marioFloatPos = null;
	protected float[] enemiesFloatPos = null;

	protected int[] marioState = null;

	protected int marioStatus;
	protected int marioMode;
	protected boolean isMarioOnGround;
	protected boolean isMarioAbleToJump;
	protected boolean isMarioAbleToShoot;
	protected boolean isMarioCarrying;
	protected int getKillsTotal;
	protected int getKillsByFire;
	protected int getKillsByStomp;
	protected int getKillsByShell;

	// values of these variables could be changed during the Agent-Environment interaction.
	// Use them to get more detailed or less detailed description of the level.
	// for information see documentation for the benchmark <link: marioai.org/marioaibenchmark/zLevels
	int zLevelScene = 1;
	int zLevelEnemies = 0;


	public BasicAIAgent(String s) {
		setName(s);
	}

	public void giveIntermediateReward(float intermediateReward) {

	}

	public void integrateObservation(Environment environment) {
		levelScene = environment.getLevelSceneObservationZ(zLevelScene);
		enemies = environment.getEnemiesObservationZ(zLevelEnemies);
		mergedObservation = environment.getMergedObservationZZ(1, 0);

		this.marioFloatPos = environment.getMarioFloatPos();
		this.enemiesFloatPos = environment.getEnemiesFloatPos();
		this.marioState = environment.getMarioState();

		// It also possible to use direct methods from Environment interface.
		//
		marioStatus = marioState[0];
		marioMode = marioState[1];
		isMarioOnGround = marioState[2] == 1;
		isMarioAbleToJump = marioState[3] == 1;
		isMarioAbleToShoot = marioState[4] == 1;
		isMarioCarrying = marioState[5] == 1;
		getKillsTotal = marioState[6];
		getKillsByFire = marioState[7];
		getKillsByStomp = marioState[8];
		getKillsByShell = marioState[9];
	}

	public void reset() {
		action = new boolean[Environment.numberOfKeys];// Empty action
	}

	public boolean[] getAction() {
		return new boolean[Environment.numberOfKeys]; // Empty action
	}

	public String getName() {
		return name;
	}

	public void setName(String Name) {
		this.name = Name;
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
			int egoCol) {
		// TODO Auto-generated method stub
		
	}
}