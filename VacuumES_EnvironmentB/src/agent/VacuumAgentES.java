package agent;

import java.util.Set;

import aima.core.agent.Action;
import aima.core.agent.impl.AbstractAgent;

public class VacuumAgentES extends AbstractAgent {
	//just test
	public Action suck, left, down, right, up;

	public VacuumAgentES() {
		super(new AgentProgramES());	
	}

	public void init (Set<Action> actionKeySet) {
		suck = (Action) actionKeySet.toArray()[0];
		left = (Action) actionKeySet.toArray()[1];
		down = (Action) actionKeySet.toArray()[2];
		right = (Action) actionKeySet.toArray()[3];
		up = (Action) actionKeySet.toArray()[4];
	}

}
