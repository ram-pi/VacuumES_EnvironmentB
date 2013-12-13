package explorer;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import map.MapInterface;
import map.MapUtils;
import map.MapInterface.Movement;


import utils.Astar;


import agent.AgentProgramES;



public class ExplorerMushroomHunter implements ExplorerInterface {


	private AgentProgramES agent;
	private MapInterface map;
	private Point start;
	private List<Point> path;
	private List<Point> currentFront;
	private int currentFrontDistance;
	private List<Point> unreacheables;



	public ExplorerMushroomHunter (AgentProgramES agent) {
		this.agent = agent;
		this.map = agent.getMap();
		path = new LinkedList<Point>();
		currentFront = new LinkedList<Point>();
		unreacheables = new LinkedList<Point>();
	}

	@Override
	public void init(Point p) {
		this.start = new Point(p);
		currentFront.clear();
		currentFrontDistance = 0;
		currentFront.add(p);
	}

	@Override
	public Movement nextAction() {
		
		if (map.isCompletelyExplored())
			return null;
		
		Point current = map.getCurrentPositionPoint();
		List<Point> adj = new LinkedList<Point>();
		List<Point> unexploredPoints = map.getUnexploredPoints();
		
		if (path != null && path.size() > 0) 
			return MapUtils.movementFromTwoPoints(current, path.remove(0));

		
		updateFront();
		
		checkReachability();
		
		if (path != null)
			return MapUtils.movementFromTwoPoints(current, path.remove(0));
		
		/* Expand front */
		if (currentFront.size() == 0) 
			expandFront();
		
		
		for (Point p : map.getAdjWalkablePoints(map.getCurrentPositionPoint())) 
			if (currentFront.contains(p))
				return chooseRouteNextMovement(p);
		
		
		
		Point p;
		/* unreacheable points (have we detect a wall?) TODO */
		do  {
			p = findNearestPoint();
			unreacheables.addAll(currentFront);
			currentFront.clear();
			expandFront();
		} while(p == null);
		/* TODO some err */
			
		
		
		Movement m = this.chooseRouteNextMovement(path.remove(0)); //TODO change name
						
		return m;
	}

	private void checkReachability() {
		LinkedList<Point> urc = new LinkedList<Point>();
		urc.addAll(unreacheables);
		Astar astar = new Astar(map);
		List<Point> pathToN = null;
		List<Point> ret =  null;
		int min = Integer.MAX_VALUE;
		
		for (Point p : urc) {
			if (map.isVisited(p)) {
				unreacheables.remove(p);
				continue;
			}
			
			astar.astar(map.getCurrentPositionPoint(), p);
			pathToN = astar.getPointPath();
			
			if (pathToN.size() < min && pathToN.size() > 0) { 
				min = pathToN.size();
				ret = new LinkedList<Point>();
				ret.addAll(pathToN);
			}
		}
		path = ret;
		return;
	}


	private Point findNearestPoint() {
		Astar astar = new Astar(map);
		Point ret = null;
		int min = Integer.MAX_VALUE;
		List<Point> pathToN = null;
		List<Point> retPath = new LinkedList<Point>();
		LinkedList<Point> cfc = new LinkedList<Point>();
		cfc.addAll(currentFront);
		for (Point p : cfc) {
			astar.astar(map.getCurrentPositionPoint(), p);
			pathToN = astar.getPointPath();
			
			if (pathToN.size() == 0)
				continue;
			
			if (pathToN.size() < min) { 
				ret = p;
				min = pathToN.size();
				retPath.clear();
				retPath.addAll(pathToN);
			}
		}
		
		path = retPath;
		return ret;
	}


	private void updateFront() {
		LinkedList<Point> cfc = new LinkedList<Point>();
		cfc.addAll(currentFront);
		
		for (Point p : cfc) 
			if(map.isVisited(p))
				currentFront.remove(p);

	}


	/* TODO this is only a stub */	
	private Movement chooseRouteNextMovement(Point dest) {
		if (dest == null) {
			//TODO error 
			return null;
		}
		Point curr = this.map.getCurrentPositionPoint();
		/* if adj */
		if (map.manatthanDistance(map.getCurrentPositionPoint(), dest) == 1) { 
			if (dest.x < curr.x)
				return Movement.left;
			if (dest.y < curr.y)
				return Movement.down;
			if (dest.x > curr.x)
				return Movement.right;
			if (dest.y > curr.y)
				return Movement.up;

			return Movement.left;
		}
		else {
			if (path.size() == 0) {
				findPathToPoint(dest);
			}
			return chooseRouteNextMovement(path.remove(0));
		}

	}
	
	private void expandFront() {
		int d = ++currentFrontDistance;
		
		Point p;
		for (int i = start.x - d; i <= start.x + d; i++) {
			p = new Point(i, start.y+d);
			if (!map.isVisited(p) && !currentFront.contains(p))
				currentFront.add(p);
			p = new Point(i, start.y-d);
			if (!map.isVisited(p) && !currentFront.contains(p))
				currentFront.add(p);
		}
		
		for (int i = start.y - d; i <= start.y + d; i++) {
			p = new Point(start.x+d, i);
			if (!map.isVisited(p) && !currentFront.contains(p))
				currentFront.add(p);
			p = new Point(start.x-d, i);
			if (!map.isVisited(p) && !currentFront.contains(p))
				currentFront.add(p);
		}
		
	}
	private void findPathToPoint(Point dest) {
		Astar a = new Astar(map);
		path = a.astar(map.getCurrentPositionPoint(), dest).getPointPath();
	}
	

}
