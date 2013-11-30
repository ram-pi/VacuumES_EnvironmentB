package utils;

import java.awt.Point;

import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;


public class Tile {
	private Point point;

	private boolean wall;
	private boolean obstacle;
	private boolean dirty;
	private boolean base;

	private int dirtyAmount;

	public Tile (Point p, boolean wall, boolean obstacle, boolean dirty, boolean base) {
		this.wall = wall;
		this.obstacle = obstacle;
		this.dirty = dirty;
		this.base = base;
		this.dirtyAmount = 0;
		this.point = p;
	}


	public boolean isBase () {
		return this.base;
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
		return this.point;
	}


}
