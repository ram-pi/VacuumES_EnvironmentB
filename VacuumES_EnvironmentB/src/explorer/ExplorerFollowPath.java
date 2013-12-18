package explorer;

import java.awt.Point;
import java.util.List;

import agent.AgentProgramES;

import map.MapInterface;
import map.MapInterface.Movement;
import map.MapUtils;

public class ExplorerFollowPath implements ExplorerInterface {
	
	private List<Point> path;
	private MapInterface map;
	
	public ExplorerFollowPath(AgentProgramES agent, List<Point> path) {
		this.path = path;
		map = agent.getMap();	
	}
	
	@Override
	public void init(Point p) {
		// TODO Auto-generated method stub

	}

	@Override
	public Movement nextAction() {
		if (path != null && path.size() > 0 && path.get(0).equals(map.getCurrentPositionPoint()))
				path.remove(0);
		
		if (path != null && path.size() > 0)
			return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), path.remove(0));
		
		return null;
	}

}
