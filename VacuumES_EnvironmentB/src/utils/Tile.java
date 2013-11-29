package utils;

import java.awt.Point;


public class Tile {
    private Point p;

    private boolean wall;
    private boolean obstacle;
    private boolean dirty;

    public Tile (Point p, boolean wall, boolean obstacle, boolean dirty) {
	this.wall = wall;
	this.obstacle = obstacle;
	this.dirty = dirty;
    }

    public boolean isWall () {
	return this.wall;
    }

    public boolean isObstacle () {
	return this.obstacle;
    }

    public boolean isDirty () {
	return this.dirty;
    }

    public void setWall(boolean w) {
	this.wall = w;
    }

    public void setObstacel(boolean o) {
	this.obstacle = o;
    }

    public void setDirty(boolean d) {
	this.dirty = d;
    }

    public Point getPoint () {
	return this.p;
    }
}
