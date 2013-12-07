package explorer;

import java.awt.Point;

import map.MapInterface;
import map.MapInterface.Movement;


import aima.core.agent.Action;

public interface ExplorerInterface {
	public void init(Point p);
	public Movement nextAction();
}