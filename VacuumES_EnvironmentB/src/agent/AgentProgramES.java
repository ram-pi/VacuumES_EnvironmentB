package agent;


import java.awt.Point;
import java.util.List;
import java.util.Set;

import map.MapImpl;
import map.MapInterface;
import map.MapInterface.Movement;

import utils.Astar;
import utils.Logger;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;
import explorer.ExplorerDFS;
import explorer.ExplorerFindWalls;
import explorer.ExplorerFollowPath;
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
	private boolean comeBackHome;
	private boolean mapExplored;
	private boolean inConservativeExploring;
	
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
		this.map = new MapImpl();
		//this.explorer = new ExplorerFindWalls(this);
		//this.explorer = new ExplorerMushroomHunter(this);
		this.explorer = new ExplorerDFS(this );
		lastMovement = null;
		mapExplored = baseFound = comeBackHome = wallsDetected = inConservativeExploring = false;
		
	}

	public MapInterface getMap() {
		return this.map;
	}
	
	private void initAtFirstStep(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		map.setInitialTile(vep);
		init(vep.getActionEnergyCosts().keySet());
		
		explorer.init(map.getCurrentPositionPoint());
	}
	
	private Action chooseAction(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {

		//System.out.println("CurrEnergy: " + vep.getCurrentEnergy());
		if (vep.getCurrentEnergy() <= 0 || (map.isCompletelyExplored() && !baseFound))
			return NoOpAction.NO_OP;
		
		
		if (comeBackHome && vep.isOnBase())
			return NoOpAction.NO_OP;
		
		if (comeBackHome) {
			lastMovement = explorer.nextAction();
			return actionFromMovement(lastMovement);
		}
		
		if ( !baseFound && vep.isOnBase()) {
			baseFound = true;
			/* if explorer != mushroomhunter */
			//explorer = new ExplorerMushroomHunter(this); 
			//explorer.init(map.getCurrentPositionPoint());
		}
		
		/* If dirty suck */
//		if (vep.getState().getLocState() == LocationState.Dirty) {
//			lastMovement = null;
//			return suck;
//		} 
	
//		if (!wallsDetected) {
//			wallsDetected = map.areWallsDetected();
//			if (wallsDetected) {
//				System.out.println("Wall detected");
//				explorer = new ExplorerMushroomHunter(this);
//				explorer.init(map.getCurrentPositionPoint());
//			}
//				
//		}
		
		/* We're exploring searching for the base */
		if (!baseFound) { 
			lastMovement = explorer.nextAction();
			return actionFromMovement(lastMovement);
		}
		
		/* baseFound = true */
		if (!checkForEnergy(vep)) {
			
			if (vep.isOnBase())
				return NoOpAction.NO_OP;
			
			explorer = new ExplorerFollowPath(this);
			explorer.init(map.getBase().getPoint());
			lastMovement = explorer.nextAction();
			comeBackHome = true;
			return actionFromMovement(lastMovement); 		
		}
		

		
		lastMovement = explorer.nextAction();
		
		if (!mapExplored && lastMovement == null) {
			mapExplored = true;
			comeBackHome = true;
			
			if (vep.isOnBase())
				return NoOpAction.NO_OP;
			
			explorer = new ExplorerFollowPath(this);
			explorer.init(map.getBase().getPoint());
			lastMovement = explorer.nextAction();
			
			return actionFromMovement(lastMovement);
		}
		
		return actionFromMovement(lastMovement);
		
		
	}
	private boolean checkForEnergy(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		// TODO check if map.getBase is null */
		Astar astar = new Astar(map);
		astar.astar(map.getCurrentPositionPoint(), map.getBase().getPoint());
		
		List<Point> path = astar.getPointPath();
		//System.out.println("energy to return to base now: " + path.size());
		/* have we enough energy to move, suck, remove and come back? */
		/* TODO get energy action cost from vep */
		if (path.size() > vep.getCurrentEnergy() - 3)
			return false;
		
		return true;
	}

	@Override
	public Action execute(final Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;


		if (this.step == 0) {
			initAtFirstStep(vep);
		}
		
		
		map.updateMap(vep, this.lastMovement);
		this.step++;
		return chooseAction(vep);

	}
	
}
