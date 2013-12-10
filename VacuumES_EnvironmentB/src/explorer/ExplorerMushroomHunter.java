package explorer;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import map.MapInterface;
import map.MapUtils;
import map.MapInterface.Movement;
import map.PointFrom;

import utils.Astar;


import agent.AgentProgramES;



public class ExplorerMushroomHunter implements ExplorerInterface {


	private AgentProgramES agent;
	private MapInterface map;
	private Point start;
	private List<Point> path;
	



	public ExplorerMushroomHunter (AgentProgramES agent) {
		this.agent = agent;
		this.map = agent.getMap();
		path = new LinkedList<Point>();
	}


	@Override
	public void init(Point p) {
		this.start = new Point(p);
	}
	

	@Override
	public Movement nextAction() {
		Point current = map.getCurrentPositionPoint();
		List<Point> adj = new LinkedList<Point>();
		List<PointFrom> unexploredPoints = map.getUnexploredPoints();
		
		if (path != null && path.size() > 0) 
			return MapUtils.movementFromTwoPoints(current, path.remove(0));

		for (Point p : this.map.getAdjWalkablePoints(current)) {
			if (!this.map.isVisited(p)) {
				adj.add(p);
			} 
		}

		PointFrom go = null;
		/* we have to come back, no more tail to explore here */
		if (adj.size() == 0) {
			System.out.println("No ADJ!");
			
			int min = Integer.MAX_VALUE;

			if (unexploredPoints.size() == 0) {
				System.out.println("We explored all maps??");
				return null;
			}		
				
			for (PointFrom p: unexploredPoints) {
				
				int d = map.manatthanDistance(current, p);
				if (d < min) {
					min = d;
					go = p;
				}
			}	
		}
		else {


			double min = Integer.MAX_VALUE;
			for (Point p: adj) {
				double d = map.eucladianDistance(start, p);
				if (d < min) {
					min = d;
					go = new PointFrom(p);
				}
			}

		}

		
		Movement m = this.chooseRouteNextMovement(go); //TODO change name
						
		return m;
	}

	/* TODO this is only a stub */	
	private Movement chooseRouteNextMovement(PointFrom dest) {
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
			return chooseRouteNextMovement(new PointFrom(path.remove(0)));
		}

	}
	
	private void findPathToPoint(Point dest) {
		Astar a = new Astar(map);
		path = a.astar(map.getCurrentPositionPoint(), dest).getPointPath();
	}
	

}
