package erekspeed;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.ai.agents.controllers;
 */

public class ForwardJumpingAgent extends BasicAIAgent implements Agent, Serializable {
	static final long serialVersionUID = 7452990632549411103L;

	public ForwardJumpingAgent() {
		super("ForwardJumpingAgent");
		reset();
	}

	public boolean[] getAction() {
		action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
		return action;
	}

	public void reset() {
		action = new boolean[Environment.numberOfKeys];
		action[Mario.KEY_RIGHT] = true;
		action[Mario.KEY_SPEED] = true;
	}
}