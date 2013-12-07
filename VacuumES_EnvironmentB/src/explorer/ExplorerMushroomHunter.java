package explorer;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import map.MapInterface;
import map.MapInterface.Movement;

import utils.Astar;


import agent.AgentProgramES;



public class ExplorerMushroomHunter implements ExplorerInterface {

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
	private MapInterface map;
	private PointExp start;
	private List<Point> path;
	



	public ExplorerMushroomHunter (AgentProgramES agent) {
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
	
	private PointExp findPointInClosedList(Point p) {
		for (PointExp pe: closedList) {
			if (p.x == pe.x && p.y == pe.y) {
				return pe;
			}
		}
		return null;
	}
	
	private PointExp findPointInOpenList(Point p) {
		for (PointExp pe: openList) {
			if (p.x == pe.x && p.y == pe.y) {
				return pe;
			}
		}
		return null;
	}
	
	private void printClosedList() {
		StringBuilder sb = new StringBuilder();
		sb.append("Closed list:\n");
		for (Point p : closedList) {
			sb.append("[" + p.x + "," + p.y + "]\n");
		}
		System.out.println(sb.toString());
	}

	@Override
	public Movement nextAction() {
		Point current = map.getCurrentPositionPoint();
		List<PointExp> adj = new LinkedList<PointExp>();

		if (containedInClosedList(current))
			closedList.remove(current);
		
		for (PointExp p: new LinkedList<PointExp>(closedList))
			if (map.isObstacle(p)) 
				closedList.remove(p);

		for (Point p : this.map.getAdjWalkablePoints(current)) {
			if (!this.map.isVisited(p)) {
				PointExp pe = new PointExp(p);
				pe.from = findPointInOpenList(current);
				pe.d = this.map.eucladianDistance(start, pe);
				if (!containedInClosedList(p))
					closedList.add(pe);
				adj.add(pe);
			} 
		}

		PointExp go = new PointExp();
		/* we have to come back, no more tail to explore here */
		if (adj.size() == 0) {
			System.out.println("No ADJ!");
			printClosedList();
			

			int min = Integer.MAX_VALUE;



			if (closedList.size() == 0) {
				System.out.println("We explored all maps??");
				return null;
			}		
				

			for (PointExp p: closedList) {
				
				if (map.isObstacle(p)) {
					closedList.remove(p);
					continue;
				}
				
				int d = map.manatthanDistance(current, p);
				if (d < min) {
					min = d;
					go = p;
				}
			}	
		}
		else {


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
				findPathToPoint(findPointInClosedList(dest).from);
			}
			return chooseRouteNextMovement(new PointExp(path.remove(0)));
		}

	}
	
	private void findPathToPoint(Point dest) {
		Astar a = new Astar(map);
		path = a.astar(map.getCurrentPositionPoint(), dest).getPointPath();
	}
	
	private boolean containedInClosedList (Point p) {
		
		for (PointExp pe : closedList) {
			if (pe.x == p.x && pe.y == p.y) {
				return true;
			}
		}
		return false;
	}

}
