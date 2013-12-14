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
	private boolean clockwise;

	public ExplorerMushroomHunter (AgentProgramES agent) {
		this.agent = agent;
		this.map = agent.getMap();
		path = null;
		currentFront = new LinkedList<Point>();
		unreacheables = new LinkedList<Point>();
		this.clockwise = true;
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

		if (path != null && path.size() > 0)
			return MapUtils.movementFromTwoPoints(current, path.remove(0));

		/* Expand front */
		if (currentFront.size() == 0) 
			expandFront();
		
		/* Control of direction */
		Movement m = directionControl();
		if (m != null)
			return m;
		
		Point p;
		p = findNearestPoint();
		while(p == null){
			unreacheables.addAll(currentFront);
			currentFront.clear();
			expandFront();
			p = findNearestPoint();
		} 
		m = this.chooseRouteNextMovement(path.remove(0)); //TODO change name

		return m;
	}
	
	public Movement directionControl () {
		Point current = map.getCurrentPositionPoint();
		
		List<Point> walkable = map.getAdjWalkablePoints(map.getCurrentPositionPoint());
		for (Point p : map.getAdjWalkablePoints(current)) 
			if (!currentFront.contains(p))
				walkable.remove(p);

		/* Direction control */
		if (walkable.size() == 2){
			if (clockwise) {
				if (MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), walkable.get(0)) == Movement.right ||
						MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), walkable.get(0)) == Movement.up) {
					return MapUtils.movementFromTwoPoints(current, walkable.get(0));
				} else {
					return MapUtils.movementFromTwoPoints(current, walkable.get(1));
				}
			}else 
				if (MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), walkable.get(0)) == Movement.left ||
				MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), walkable.get(0)) == Movement.down) {
					return MapUtils.movementFromTwoPoints(current, walkable.get(0));
				} else {
					return MapUtils.movementFromTwoPoints(current, walkable.get(1));
				}
		} else if (walkable.size() == 1) {
			if (clockwise)
				if (MapUtils.movementFromTwoPoints(current, walkable.get(0)) == Movement.up ||
				MapUtils.movementFromTwoPoints(current, walkable.get(0)) == Movement.right)
					return MapUtils.movementFromTwoPoints(current, walkable.get(0));
				else {
					clockwise = false;
					return MapUtils.movementFromTwoPoints(current, walkable.get(0));
				}
			else
				if (MapUtils.movementFromTwoPoints(current, walkable.get(0)) == Movement.down ||
				MapUtils.movementFromTwoPoints(current, walkable.get(0)) == Movement.left)
					return MapUtils.movementFromTwoPoints(current, walkable.get(0));
				else {
					clockwise = true;
					return MapUtils.movementFromTwoPoints(current, walkable.get(0));
				}
		}
		
		return null;
	}

	private void checkReachability() {
		LinkedList<Point> urc = new LinkedList<Point>();
		urc.addAll(unreacheables);
		List<Point> unexplored = map.getUnexploredPoints();
		Astar astar = new Astar(map);

		for (Point point : urc) {
			/* Maybe these is not necessary TODO */
			if (map.isVisited(point)) {
				unreacheables.remove(point);
				continue;
			}

			if (unexplored.contains(point)) {
				/* Should be always ad adjacent point to out current position*/
				unreacheables.remove(point);
				astar.astar(map.getCurrentPositionPoint(), point);
				path = astar.getPointPath();
				return;
			}
		}

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
			if (!map.getUnexploredPoints().contains(p))
				continue;

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
