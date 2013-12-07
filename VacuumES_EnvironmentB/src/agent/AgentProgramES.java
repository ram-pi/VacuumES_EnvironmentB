package agent;


import java.util.Set;

import map.MapImpl;
import map.MapInterface;
import map.MapInterface.Movement;

import utils.Logger;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;
import explorer.ExplorerFindWalls;
import explorer.ExplorerInterface;
import explorer.ExplorerMushroomHunter;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.NoOpAction;

public class AgentProgramES implements AgentProgram {

	private int step;
	public Movement lastMovement;
	private MapInterface map;
	private ExplorerInterface explorer;
	private Logger log;
	private boolean baseFound;
	private boolean wallsDetected;
	
	
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
		this.map = new MapImpl(this);
		this.explorer = new ExplorerFindWalls(this);
	}

	public MapInterface getMap() {
		return this.map;
	}
	
	@Override
	public Action execute(final Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;


		if (this.step == 0) {
			this.map.setInitialTile(vep);
			init(vep.getActionEnergyCosts().keySet());	
		}
		else {
			this.map.updateMap(vep, this.lastMovement);
		}
		if (vep.getState().getLocState() == LocationState.Dirty && !baseFound) {
			lastMovement = null;
			return suck;
		} else if (vep.getState().getLocState() == LocationState.Dirty && baseFound) {
			// Count if the energy is enough to reach the base
		}
		
		if (!wallsDetected) {
			wallsDetected = map.areWallsDetected();
			if (wallsDetected) {
				explorer = new ExplorerMushroomHunter(this);
				explorer.init(map.getCurrentPositionPoint());
			}
				
		}
		if (vep.isOnBase() && baseFound) {
			//todo
		}
		
		this.step++;
		
		
		Movement moveTo =  explorer.nextAction();
		if (moveTo == null) {
			return NoOpAction.NO_OP;
		}
		this.lastMovement = moveTo;
		
		if (lastMovement == null) {
			System.out.println("null movement..");
			
		}
		return actionFromMovement(lastMovement);


	}
	
}
