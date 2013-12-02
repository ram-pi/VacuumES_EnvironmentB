package utils;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import utils.VacuumMapsUtils.Movement;


import agent.AgentProgramES;



public class Explorer implements VacuumExplorationUtils {

	private class PointExp extends Point {


		private double d;
		private PointExp from;

		public PointExp() {
			super();
			this.d = 0;
			
		}

		public PointExp(Point p) {
			super(p.x, p.y);
			this.d = 0;
			
		}


	}


	private List<PointExp> closedList;
	private List<PointExp> openList;
	private AgentProgramES agent;
	private VacuumMapsUtils map;
	private PointExp start;
	private List<Point> path;
	



	public Explorer (AgentProgramES agent) {
		this.agent = agent;
		this.closedList = new LinkedList<PointExp>();
		this.openList = new LinkedList<PointExp>();
		this.map = agent.getMap();
		path = new LinkedList<Point>();
	}


	@Override
	public void init(Point p) {
		this.start = new PointExp(p);
		this.openList.add(start);
	}
	
	public PointExp findPointInClosedList(Point p) {
		for (PointExp pe: closedList) {
			if (p.x == pe.x && p.y == pe.y) {
				return pe;
			}
		}
		return null;
	}
	
	public PointExp findPointInOpenList(Point p) {
		for (PointExp pe: openList) {
			if (p.x == pe.x && p.y == pe.y) {
				return pe;
			}
		}
		return null;
	}

	@Override
	public Movement nextAction() {
		Point current = map.getCurrentPositionPoint();
		List<PointExp> adj = new LinkedList<PointExp>();


		for (Point p : this.map.getAdjWalkablePoints(current)) {
			if (!this.map.isVisited(p)) {
				PointExp pe = new PointExp(p);
				pe.from = findPointInOpenList(current);
				pe.d = this.map.eucladianDistance(start, pe);
				if (!closedList.contains(pe))
					closedList.add(pe);
				adj.add(pe);
			}
		}

		PointExp go = new PointExp();
		/* we have to come back, no more tail to explore here */
		if (adj.size() == 0) {
			int min = Integer.MAX_VALUE;
			for (PointExp p: closedList) {
				int d = map.manatthanDistance(current, p);
				if (d < min) {
					min = d;
					go = p;
				}
			}	
		}
		else {

			if (closedList.size() == 0) {
				//TODO we explore all the map?
			}

			double min = Integer.MAX_VALUE;
			for (PointExp p: adj) {
				double d = map.eucladianDistance(start, p);
				if (d < min) {
					min = d;
					go = p;
				}
			}

		}

		
		Movement m = this.chooseRouteNextMovement(go); //TODO change name
		closedList.remove(go);
		openList.add(go);
		
		return m;
	}

	/* TODO this is only a stub */
	public Movement chooseRouteNextMovement(PointExp dest) {
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
			PointExp go = findPointInOpenList(map.getCurrentPositionPoint());
			
			if (path.size() == 0) {
				findPathToPoint(dest);
			}
			return chooseRouteNextMovement(new PointExp(path.remove(0)));
		}

	}
	
	private void findPathToPoint(Point dest) {
		Astar a = new Astar(map);
		path = a.astar(map.getCurrentPositionPoint(), dest).getPointPath();
	}

}
