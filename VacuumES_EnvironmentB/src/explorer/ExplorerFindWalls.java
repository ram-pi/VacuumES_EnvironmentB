package explorer;

import java.awt.Point;
import java.util.Random;

import agent.AgentProgramES;

import map.MapInterface;
import map.MapUtils;
import map.MapInterface.Movement;

public class ExplorerFindWalls implements ExplorerInterface{

	private AgentProgramES agent;
	private MapInterface map;
	private Random random;
	private Movement currentDirection;
	
	
	public ExplorerFindWalls(AgentProgramES a) {
		agent = a;
		map = a.getMap();
		/* TODO add seed */
		random = new Random(); 
		currentDirection = Movement.values()[random.nextInt(Movement.values().length)];
	}
	
	@Override
	public void init(Point p) {
		return;	
	}

	@Override
	public Movement nextAction() {
		Point p = MapUtils.neighbourFromDirection(map.getCurrentPositionPoint(), currentDirection);

		while (map.isObstacle(p)) {
			currentDirection = MapUtils.roundRobinMovement(currentDirection);
			p = MapUtils.neighbourFromDirection(map.getCurrentPositionPoint(), currentDirection);
		}

		return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), p);

	}

}
