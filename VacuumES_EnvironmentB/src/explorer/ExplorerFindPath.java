package explorer;

import java.awt.Point;
import java.util.List;

import agent.AgentProgramES;

import map.MapInterface.Movement;
import map.MapInterface;
import map.MapUtils;

public class ExplorerFindPath implements ExplorerInterface {
	private List<Point> path;
	private MapInterface map;
	
	public ExplorerFindPath(AgentProgramES agent, List<Point> path) {
		this.path = path;
		this.map = agent.getMap();
	}
	@Override
	public void init(Point p) {
		// TODO Auto-generated method stub

	}

	@Override
	public Movement nextAction() {
		if (path != null && path.size() > 0)
			return MapUtils.movementFromTwoPoints(map.getCurrentPositionPoint(), path.remove(0));
		
		return null;
	}

}
