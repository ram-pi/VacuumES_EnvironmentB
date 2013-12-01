package agent;

import java.util.Random;
import java.util.Set;

import utils.Explorer;
import utils.MapKB;
import utils.VacuumMapsUtils;
import utils.VacuumMapsUtils.Movement;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;

public class AgentProgramES implements AgentProgram {

	private int step;
	public Movement lastMovement;
	private VacuumMapsUtils map;
	private Explorer explorer;
	
	
	private Action suck, left, down, right, up;

	private void init (Set<Action> actionKeySet) {
		suck = (Action) actionKeySet.toArray()[0];
		left = (Action) actionKeySet.toArray()[1];
		down = (Action) actionKeySet.toArray()[2];
		right = (Action) actionKeySet.toArray()[3];
		up = (Action) actionKeySet.toArray()[4];
	}
	
	private Action actionFromMovement(Movement m) {
		switch(m) {
			case left:
				return left;
			case down:
				return down;
			case right:
				return right;
			case up:
				return up;
		}
		return null;
	}
	
	public AgentProgramES ()
	{
		this.step = 0;
		this.map = new MapKB(this);
		this.explorer = new Explorer(this);
		
	
	}

	public VacuumMapsUtils getMap() {
		return this.map;
	}
	
	@Override
	public Action execute(final Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;

		if (this.step == 0) {
			this.map.setInitialTile(vep);
			explorer.init(map.getCurrentPositionPoint());
			init(vep.getActionEnergyCosts().keySet());	
		}
		else {
			this.map.updateMap(vep, this.lastMovement);
		}

		this.step++;
		this.lastMovement = explorer.nextAction();
		
		
		return actionFromMovement(lastMovement);


	}
	
}
