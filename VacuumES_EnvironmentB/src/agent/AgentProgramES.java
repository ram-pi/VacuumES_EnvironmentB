package agent;


import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import map.MapImpl;
import map.MapInterface;
import map.MapInterface.Movement;

import utils.Astar;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;
import explorer.ExplorerDFS;
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
	private List<Point> dirtyKnownPoints;
	private Action suck, left, down, right, up;

	//states
	private State state;
	
	private enum State { fullExploration, 
						conservativeExploration, 
						baseKnownExploration, 
						cleaningFarAway,  
						comingBackHome,
						NO_OP};
						
						
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
		this.explorer = new ExplorerDFS(this);
		lastMovement = null;
		state = State.fullExploration;
		dirtyKnownPoints = new LinkedList<Point>();
		
	}

	public MapInterface getMap() {
		return this.map;
	}
	
	private void initAtFirstStep(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		map.setInitialTile(vep);
		init(vep.getActionEnergyCosts().keySet());	
		explorer.init(map.getCurrentPositionPoint());
	}
	
	private void switchToBKExploration() {
		explorer = new ExplorerMushroomHunter(this);
		explorer.init(map.getBase().getPoint());
		state = State.baseKnownExploration;
	}
	
	private void switchToCExploration() {
		explorer = new ExplorerDFS(this);
		explorer.init(map.getCurrentPositionPoint());
		state = State.conservativeExploration;
	}
	
	private void switchToCBHome() {
		explorer = new ExplorerFollowPath(this);
		explorer.init(map.getBase().getPoint());
		state = State.comingBackHome;
	}
	
	private void switchToCFAway() {
		// TODO Auto-generated method stub
		Point d = dirtyKnownPoints.get(0);
		explorer = new ExplorerFollowPath(this);
		explorer.init(d);
		state = State.cleaningFarAway;
	}
	
	private boolean checkMinimalEnergy(double currentEnergy) {
		// TODO check if map.getBase is null */
		Astar astar = new Astar(map);
		astar.astar(map.getCurrentPositionPoint(), map.getBase().getPoint());

		List<Point> path = astar.getPointPath();
		//System.out.println("energy to return to base now: " + path.size());
		/* have we enough energy to move, suck, remove and come back? */
		/* TODO get energy action cost from vep */
		if (path.size() > currentEnergy - 3)
			return false;

		return true;

	}
	
	private boolean wantToCleanFarTiles() {
		// TODO Auto-generated method stub
		return dirtyKnownPoints.size()>0;
	}

	private boolean checkConservativeExploring() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private Action chooseAction(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {

		while (state != State.NO_OP) {
			
			if (vep.getCurrentEnergy() == 0) {
				System.out.println("NO MORE ENERGY");
				state = State.NO_OP;
				continue;
			}
			
			switch(state){
				case fullExploration:
					if (vep.isOnBase()) {
						switchToBKExploration();
						break;	
					}
					if (checkConservativeExploring()){
						switchToCExploration();
						break;
					}
					/* fall-off state */
					if (map.isCompletelyExplored()) {
						switchToCBHome();
						break;
						
					}
					
					/* TODO expand for ENV C */
					if (vep.getState().getLocState() == LocationState.Dirty) {
						lastMovement = null;
						return suck;
					}
					
					lastMovement = explorer.nextAction();
					if (lastMovement == null && map.isCompletelyExplored()) {
						switchToCBHome();
						break;
					}
					
					return actionFromMovement(lastMovement);
					
				case conservativeExploration:
					if (vep.isOnBase()) {
						switchToBKExploration();
						break;
					}
					
					lastMovement = explorer.nextAction();
					if (lastMovement == null && map.isCompletelyExplored()) {
						System.out.println("ERROR: map completely epxlored but no base is found!");
						return NoOpAction.NO_OP;
					}
					
					return actionFromMovement(lastMovement);
					
				case baseKnownExploration:
					if (!checkMinimalEnergy(vep.getCurrentEnergy())) {
						switchToCBHome();
						break;
					}
					if (wantToCleanFarTiles()) {
						switchToCFAway();
						break;
					}
					if (map.isCompletelyExplored()) {
						switchToCBHome();
						break;
					}
				
					/* TODO expand for ENV C */
					if (vep.getState().getLocState() == LocationState.Dirty) {
						lastMovement = null;
						return suck;
					}
					
					lastMovement = explorer.nextAction();
					if (lastMovement == null && map.isCompletelyExplored()) {
						switchToCBHome();
						break;
					}
					
					return actionFromMovement(lastMovement);
					

					
				case cleaningFarAway:
					if (!checkMinimalEnergy(vep.getCurrentEnergy())) {
						switchToCBHome();
						break;
					}
					
					/* TODO expand for ENV C */
					if (vep.getState().getLocState() == LocationState.Dirty) {
						lastMovement = null;
						return suck;
					}
					
					lastMovement = explorer.nextAction();
					if (lastMovement == null) {
						switchToBKExploration();
						break;
					}
					return actionFromMovement(lastMovement);
					
				case comingBackHome:
					
					lastMovement = explorer.nextAction();
					/* I'm at home :) */
					if (lastMovement == null) {
						System.out.println("I've still "+vep.getCurrentEnergy()+ " E");
						return NoOpAction.NO_OP;
					}
					
					return actionFromMovement(lastMovement);
				
				case NO_OP:
					return NoOpAction.NO_OP;
						
			}
		}
		//System.out.println("CurrEnergy: " + vep.getCurrentEnergy());
		return null;
	}



	@Override
	public Action execute(final Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;


		if (this.step == 0) {
			initAtFirstStep(vep);
		}

		
		map.updateMap(vep, this.lastMovement);
		// made a suck last step 
		if (lastMovement == null) 
			if (vep.getState().getLocState() == LocationState.Clean)
				dirtyKnownPoints.remove(map.getCurrentPositionPoint());
		
		if (vep.getState().getLocState() == LocationState.Dirty && !dirtyKnownPoints.contains(map.getCurrentPositionPoint()))
			dirtyKnownPoints.add(map.getCurrentPositionPoint());
			
		this.step++;
		return chooseAction(vep);

	}
	
}
