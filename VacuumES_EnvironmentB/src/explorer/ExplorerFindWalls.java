package explorer;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import utils.Astar;

import agent.AgentProgramES;

import map.MapInterface;
import map.MapUtils;
import map.MapInterface.Movement;
import map.PointFrom;

public class ExplorerFindWalls implements ExplorerInterface{

	private AgentProgramES agent;
	private MapInterface map;
	private Random random;
	private Movement currentDirection;
	private List<Point> path;
	private int triesInSameDirection;
	
	public ExplorerFindWalls(AgentProgramES a) {
		agent = a;
		map = a.getMap();
		/* TODO add seed */
		random = new Random(); 
		currentDirection = Movement.values()[random.nextInt(Movement.values().length)];
		path = new LinkedList<Point>();
		triesInSameDirection = 0;
	}
	
	@Override
	public void init(Point p) {
		return;	
	}

	@Override
	public Movement nextAction() {
		/* we're following a path */
		if (path.size() != 0) 
			return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), path.remove(0));
		
		Point p = MapUtils.neighbourFromDirection(map.getCurrentPositionPoint(), currentDirection);

		/* check if I can move to the direction */
		
		/* OR if (!map.isObstacle(p)) ???*/
		if (!map.isVisited(p))	
			return currentDirection;
		
		/* we face an obstacle (or wall?) */
		
		List<Point> trasversal = MapUtils.getTrasversalPoint(map.getCurrentPositionPoint(), currentDirection);
		
		Collections.shuffle(trasversal, random);
		
		for (Point point : trasversal) {
			if (!map.isVisited(point)) {
				triesInSameDirection++;
				if (triesInSameDirection > 3) {
					currentDirection = MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), point);
					triesInSameDirection = 0;
				}
				return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), point);
			}
		}
		
		for (Point point : map.getAdjWalkablePoints(map.getCurrentPositionPoint())) 
			if (!map.isVisited(point))
				return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), point);
		
		
		/* are we in a dead end?? */
		List<PointFrom> unexplored = map.getUnexploredPoints();
		currentDirection = Movement.values()[random.nextInt(Movement.values().length)];
		
		int min = Integer.MAX_VALUE;
		PointFrom go = null;
		for (PointFrom pointFrom : unexplored) {
			if (map.manatthanDistance(map.getCurrentPositionPoint(), pointFrom) < min) {
				/* Should we use real distance calculated by A* */
				min = map.manatthanDistance(map.getCurrentPositionPoint(), pointFrom);
				go = new PointFrom(pointFrom);
			}
		}
		
		Astar astar = new Astar(map);
		path = astar.astar(map.getCurrentPositionPoint(), go.getFrom()).getPointPath();
		path.add(go);
		return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), path.remove(0));

	}
	
	

}
