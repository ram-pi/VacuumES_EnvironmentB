package explorer;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import utils.Astar;

import agent.AgentProgramES;

import map.MapInterface;
import map.MapInterface.Movement;
import map.MapUtils;

public class ExplorerDFS implements ExplorerInterface {

	private MapInterface map;
	private AgentProgramES agent;
	private List<Point> stack;
	private List<Point> path;
	
	
	public ExplorerDFS(AgentProgramES agent) {
		map = agent.getMap();
		this.agent = agent;
		path = new LinkedList<Point>();
		stack = new LinkedList<Point>();
	
	}
	
	@Override
	public void init(Point p) {
		stack.clear();
		path.clear();
		if (!map.getCurrentPositionPoint().equals(p))
			stack.add(p);
	}

	@Override
	public Movement nextAction() {
		if (map.isCompletelyExplored())
			return null;
		
		Point current = map.getCurrentPositionPoint();
		if (path != null && path.size() > 0)
			return MapUtils.movementFromTwoPoints(current, path.remove(0));

	
		List<Point> adj = map.getAdjWalkablePoints(current);
		for (Point point : map.getAdjWalkablePoints(current)) {
			if (map.isVisited(point))
				adj.remove(point);
		}

		//TODO choose random
		if (adj.size() > 0) {
			Point go = adj.remove(0);
			stack.remove(go);
				
			Movement ret = MapUtils.movementFromTwoPoints(current, go);
			for (Point point : adj) 
				if (!stack.contains(point))
					stack.add(point);
			
			return ret;
		}	
		

		removeWallsFromStack();
		//Backtracking
		if (stack.size() > 0)
			return goTo(stack.remove(stack.size()-1));
		
		/* TODO take the nearest one */
		return goTo(map.getUnexploredPoints().get(0));
			
		
	}
	private void removeWallsFromStack() {
		List<Point> sc = new LinkedList<Point>();
		sc.addAll(stack);
		for (Point p : sc) 
			if (map.isObstacle(p))
				stack.remove(p);
		
	}

	private Movement goTo(Point to) {
		Point current = this.map.getCurrentPositionPoint();
		/* if adj */
		if (map.manatthanDistance(map.getCurrentPositionPoint(), to) == 1)  
			return MapUtils.movementFromTwoPoints(current, to);
		
		Astar a = new Astar(map);
		path = a.astar(current, to).getPointPath();
		
		if (path == null || path.size() == 0)
			System.out.println("ERROR, find path to an unreacheable point!");
		
		return MapUtils.movementFromTwoPoints(current, path.remove(0));
		
	}

}
