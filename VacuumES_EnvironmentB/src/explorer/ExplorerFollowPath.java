package explorer;

import java.awt.Point;
import java.util.List;

import utils.Astar;
import agent.AgentProgramES;
import map.MapInterface;
import map.MapInterface.Movement;
import map.MapUtils;

public class ExplorerFollowPath implements ExplorerInterface {

	private List<Point> path;
	private Point dest;
	private MapInterface map;
	
	public ExplorerFollowPath(AgentProgramES agent) {
		map = agent.getMap();
		path = null;
		dest = null;
	}
	
	@Override
	public void init(Point p) {
		dest = p;

	}

	@Override
	public Movement nextAction() {
		if (path != null && path.size() > 0) 
			return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), path.remove(0));
		
		Astar astar = new Astar(map);
		astar.astar(map.getCurrentPositionPoint(), dest);
		
		/* TODO errore checking (can we arrive at destination???) */
		path = astar.getPointPath();
		if (path.size() == 0) {
			/* TODO we are at dest?? */
			return null;
		}
		return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), path.remove(0));
	}

}
