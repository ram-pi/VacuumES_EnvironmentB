package utils;

import java.awt.Point;

import aima.core.agent.Action;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public interface VacuumMapsUtils {

	public enum Movement {
		left, down, right, up;
	}

	public boolean isVisited (Point p);
	public boolean isWall (Point p);
	public Tile getBase();
	public void setInitialTile(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep);
	public void updateMap(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep, Movement lastAction);
}
