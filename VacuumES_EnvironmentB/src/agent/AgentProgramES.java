package agent;


import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import map.MapImpl;
import map.MapInterface;
import map.MapInterface.Movement;
import utils.Astar;
import utils.Edge;
import utils.TourChooser;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;
import explorer.ExplorerDFS;
import explorer.ExplorerFollowPath;
import explorer.ExplorerToDestination;
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
	private double currentEnergy;
	
	
	private TourChooser tc;
	private List<Point> hamiltonianCycle;
	private HashMap<Point, HashMap<Point, List<Point>>> bestPaths; 
	
	/* statistics */
	private int energyUsed;

	//states
	private State state;
	private boolean cleanedFarAway;
	
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
	
	private void putOnBestPath (Point from, Point to, List<Point> path) {
		if (!bestPaths.containsKey(from))
			bestPaths.put(from, new HashMap<Point, List<Point>>());
		bestPaths.get(from).put(to, path);
	}
	
	private List<Point> getFromBestPath (Point from, Point to) {
		if (!bestPaths.containsKey(from))
			return null;
		
		return bestPaths.get(from).get(to);
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

		
		bestPaths = new HashMap<Point, HashMap<Point,List<Point>>>();

		lastMovement = null;
		state = State.fullExploration;
		dirtyKnownPoints = new LinkedList<Point>();
		energyUsed = 0;
		
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
		/* Standard Behavior */
		if (dirtyKnownPoints.size() == 0)
			cleanedFarAway = true;
		
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
		explorer = new ExplorerToDestination(this);
		explorer.init(map.getBase().getPoint());
		state = State.comingBackHome;
		printStats();
	}
	
	private void switchToCFAway() {
		
		/* Find minimum Hamiltonian Cycle and give a path to walk it */
		explorer = new ExplorerFollowPath(this, hamiltonianCycle);
		state = State.cleaningFarAway;
	}
	
	
	private boolean checkMinimalEnergy(double currentEnergy) {
		
		Astar astar = new Astar(map);
		astar.astar(map.getCurrentPositionPoint(), map.getBase().getPoint());

		List<Point> path = astar.getPointPath();
		
		/* have we enough energy to move (maybe far away from base)  and come back? */
		if (currentEnergy >= path.size() + 2)
			return true;

		return false;

	}
	
	private boolean wantToCleanFarTiles() {
		if (cleanedFarAway)
			return false;
		
		List<Point> dirtyPointConsidered = new LinkedList<Point>();
		if (dirtyKnownPoints.size() == 0)
			return false;

		for (Point p : dirtyKnownPoints) {
			if (map.manatthanDistance(map.getCurrentPositionPoint(), p)  > currentEnergy)
				continue;
			
			dirtyPointConsidered.add(p);
		}
		
		if (dirtyPointConsidered.size() == 0)
			return false;
		
		
		if (dirtyPointConsidered.size() < 20) {
			Point end = map.getNearestUnexplored(map.getCurrentPositionPoint());
			tc = new TourChooser(map, currentEnergy, dirtyPointConsidered, end);
			List<Point> hamiltonianPath = tc.getPathHamiltonian();
			if (hamiltonianPath.size() < currentEnergy) {
				hamiltonianCycle  = hamiltonianPath;
				cleanedFarAway = true;
				return true;
			}
		}
		
		
		/* NN */
		boolean pathFound = false;
		Point curr = map.getCurrentPositionPoint();
		Point next = null;
		int currEnergy = 0;
		double min = Integer.MAX_VALUE;
		List<Point> cellToClean = new LinkedList<Point>();
		Astar astar = new Astar(map);
		while (!pathFound) {
			double distance;
			min = Integer.MAX_VALUE;
			for (Point point : dirtyPointConsidered) {
				if (tc != null) {
					distance = tc.getGraph().getEdgeWeight(tc.getGraph().getEdge(curr, point));
				}
				else {
					List<Point> path = new LinkedList<Point>();
					path = astar.astar(curr, point).getPointPath();
					putOnBestPath(curr, point, path);
					distance = path.size();
				}
				if (distance < min) {
					min = distance;
					next = point;
				}
			}
		
			
			if (dirtyPointConsidered.size() == 0 || !checkPathCost(next, (int)(currEnergy+min))) {
				pathFound = true;
				if (cellToClean.size() == 0) {
					cleanedFarAway = true;
					return false;
				}
				
				hamiltonianCycle = makePathFromPoints(cellToClean);
				cleanedFarAway = true;
				return true;
			}
			
			cellToClean.add(next);
			dirtyPointConsidered.remove(next);
			curr = next;
			currEnergy += min + 1;
		}
		
		cleanedFarAway = true;
		return true;
	}

	private boolean checkPathCost(Point last, int currEnergy) {
	
		List<Point> lastToNextUnexplored;
		List<Point> toHome;
		
		Astar astar = new Astar(map);
		
		Point nu = map.getNearestUnexplored(map.getBase().getPoint());
		
		if (nu != null) {
			lastToNextUnexplored = getFromBestPath(last, nu);

			if (lastToNextUnexplored == null) {
					lastToNextUnexplored = astar.astar(last, nu).getPointPath();
					putOnBestPath(last, nu, lastToNextUnexplored);
			}
			
			toHome = getFromBestPath(nu, map.getBase().getPoint()); 
			
			if (toHome == null) {
				toHome = astar.astar(nu, map.getBase().getPoint()).getPointPath();
				putOnBestPath(nu, map.getBase().getPoint(), toHome);
			}
			
			if (currEnergy + lastToNextUnexplored.size() + toHome.size() + 1 < currentEnergy) { 
				return true;
			}
		}
		
		//else
		toHome = getFromBestPath(last, map.getBase().getPoint()); 
				
		if (toHome == null) {
			toHome = astar.astar(last, map.getBase().getPoint()).getPointPath();
			putOnBestPath(last, map.getBase().getPoint(), toHome);
		}
				
		if (currEnergy + toHome.size() < currentEnergy)
					return true;
		
		
		return false;
	}
	
	private boolean suck() {
		
		List<Point> toHome;
		Astar astar = new Astar(map);
		if (lastMovement == null && getFromBestPath(map.getCurrentPositionPoint(), map.getBase().getPoint()) != null)
		{
			toHome = getFromBestPath(map.getCurrentPositionPoint(), map.getBase().getPoint());
		}
		else {
		toHome = astar.astar(map.getCurrentPositionPoint(), map.getBase().getPoint()).getPointPath();
		putOnBestPath(map.getCurrentPositionPoint(), map.getBase().getPoint(), toHome);
		}

		
		if (currentEnergy >= toHome.size() + 1 ) 
			return true;
		
		return false;
			
	}
	
	private List<Point> makePathFromPoints(List<Point> cellToClean) {
		Iterator<Point> it = cellToClean.iterator();
		List<Point> ret = new LinkedList<Point>();
		Point from = map.getCurrentPositionPoint();
	
		while (it.hasNext()) {
			Point to = it.next();
			if (tc != null) 
				ret.addAll(tc.getPathFromEdge(new Edge(from, to)));
			else
				ret.addAll(getFromBestPath(from, to));
			from = to;
		}
		
		/* add to unexplored or to base */
		Point last = ret.get(ret.size()-1);
		Point nu = map.getNearestUnexplored(map.getBase().getPoint());
		
		List<Point> lastToNu;
		List<Point> toHome;
		
		if (nu != null) {
			lastToNu = getFromBestPath(last, nu);
			toHome = getFromBestPath(nu, map.getBase().getPoint());
			
			if (ret.size() + lastToNu.size() + toHome.size() < currentEnergy) {
				ret.addAll(lastToNu);		
				return ret;
			}
		}

		return ret;

	}

	private boolean checkConservativeExploring() {
		
		double estimatedUnobservedCells = (this.map.getRows()*this.map.getCols()) - map.getMap().keySet().size();
		if (this.currentEnergy < estimatedUnobservedCells*2) {
			System.out.println("GOING IN CONSERVATIVE MODE");
			return true;
		}
		return false;
	}
	
	private Action chooseAction(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		
		this.currentEnergy = vep.getCurrentEnergy();

		while (state != State.NO_OP) {
			
			if (vep.getCurrentEnergy() == 0) {
				System.out.println("NO MORE ENERGY");
				state = State.NO_OP;
				continue;
			}
			
			switch(state){
				case fullExploration:
					if (vep.isOnBase()) {
						/* Testing the minimal path search */
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
					
					
					if (vep.getState().getLocState() == LocationState.Dirty) {
						lastMovement = null;
						return suck;
					}
					
					lastMovement = explorer.nextAction();
					if (lastMovement == null && map.isCompletelyExplored()) {
						System.out.println("ERROR: map completely epxlored but no base is found!");
						return NoOpAction.NO_OP;
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
					
					if (!checkMinimalEnergy(vep.getState().getLocState() == LocationState.Dirty?currentEnergy-1:currentEnergy)) {
						switchToCBHome();
						break;
					}
					if (wantToCleanFarTiles()) {
						switchToCFAway();
						break;
					}
					if (map.isCompletelyExplored() && this.dirtyKnownPoints.isEmpty()) {
						switchToCBHome();
						break;
					}
				
					
					if (vep.getState().getLocState() == LocationState.Dirty) {
						lastMovement = null;
						return suck;
					}
					
					lastMovement = explorer.nextAction();
					if (lastMovement == null && map.isCompletelyExplored()) {
						switchToCBHome();
						break;
					}
					if (lastMovement == null && !map.isCompletelyExplored()) {
						switchToBKExploration();
						break;
					}
					
					return actionFromMovement(lastMovement);
					

					
				case cleaningFarAway:
					if (!checkMinimalEnergy(vep.getState().getLocState() == LocationState.Dirty?currentEnergy-1:currentEnergy)) {
						switchToCBHome();
						break;
					}
					
					
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
					
					if (vep.getState().getLocState() == LocationState.Dirty && suck()) {
						lastMovement = null;
						return suck;
					}
					
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
		return NoOpAction.NO_OP;
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
		
		
		
		
		Action act = chooseAction(vep);
		if (act == NoOpAction.NO_OP)
			printStats();
		
		if (act != suck) 
			energyUsed++;
		
		return act;

	}

	private void printStats() {
		System.out.println("Map size: " + (map.getCols()*map.getRows()));
		System.out.println("Energy Used: " + (energyUsed));
		System.out.println("Percent: " + (double)(energyUsed)/(map.getCols()*map.getRows()));
	}
	
}
