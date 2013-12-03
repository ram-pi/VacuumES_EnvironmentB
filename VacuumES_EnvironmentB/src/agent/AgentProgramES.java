package agent;

import java.util.Random;
import java.util.Set;

import utils.Explorer;
import utils.Logger;
import utils.MapKB;
import utils.VacuumMapsUtils;
import utils.VacuumMapsUtils.Movement;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.NoOpAction;

public class AgentProgramES implements AgentProgram {

	private int step;
	public Movement lastMovement;
	private VacuumMapsUtils map;
	private Explorer explorer;
	private Logger log;
	private boolean baseFound;
	
	
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

		if (vep.getState().getLocState() == LocationState.Dirty && !baseFound) {
//			lastMovement = null;
//			return suck;
		} else if (vep.getState().getLocState() == LocationState.Dirty && baseFound) {
			// Count if the energy is enough to reach the base
		}
		
		if (this.step == 0) {
			this.map.setInitialTile(vep);
			explorer.init(map.getCurrentPositionPoint());
			init(vep.getActionEnergyCosts().keySet());	
		}
		else {
			this.map.updateMap(vep, this.lastMovement);
		}
		
		if (vep.isOnBase() && baseFound) {
			//todo
		}
		
		this.step++;
		
		

		this.lastMovement = explorer.nextAction();
		
		if (lastMovement == null) {
			System.out.println("null movement..");
			
		}
		return actionFromMovement(lastMovement);


	}
	
}
