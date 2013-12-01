package utils;

import java.awt.Point;

import utils.VacuumMapsUtils.Movement;

import aima.core.agent.Action;

public interface VacuumExplorationUtils {
	public void init(Point p);
	public Movement nextAction();
}