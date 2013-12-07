package map;

import java.awt.Point;
import java.util.List;

import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public interface MapInterface {

	public enum Movement {
		left, down, right, up;
	}
	
	

	public List<PointFrom> getUnexploredPoints();
	public boolean areWallsDetected();
	public List<Point> getAdjWalkablePoints(Point from);
	public Tile getCurrentPosition();
	public Point getCurrentPositionPoint();
	public boolean isVisited (Point p);
	public boolean isWall (Point p);
	public Tile getBase();
	public void setInitialTile(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep);
	public void updateMap(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep, Movement lastAction);
	public boolean isObstacle(Point p);
	public int manatthanDistance(Point from, Point to);
	public double eucladianDistance(Point from, Point to);
}
