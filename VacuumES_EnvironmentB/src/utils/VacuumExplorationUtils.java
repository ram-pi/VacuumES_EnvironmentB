package utils;

import java.awt.Point;

import aima.core.agent.Action;

public interface VacuumExplorationUtils {
	public void init(Point p);
	public Action nextAction();
}