package utils;

import java.awt.Point;

import aima.core.agent.Action;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public interface VacuumMapsUtils {
	
	public boolean isVisited (Point p);
	public boolean isWall (Point p);
	public Point getBase();
	public void updateMap(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep, Action action);
}
