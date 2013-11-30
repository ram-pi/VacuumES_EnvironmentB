package utils;

import instanceXMLParser.CellLogicalState;

import java.awt.Point;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.impl.AbstractAgent;

import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;

import java.util.HashMap;
import java.util.Map;



public class MapKB implements VacuumMapsUtils {


	private Map<Point, Tile> map;
	private int m,n;

	private Tile base;
	private AgentProgram agent;


	private Point currentPosition;

	public MapKB(AgentProgram a) {
		this.agent = a;
		this.map = new HashMap<Point, Tile>();
		this.base = null;
		this.currentPosition = new Point (0,0);
	}

	private void setTile(Tile t) {
		this.map.put(t.getPoint(), t);
	}

	public void setInitialTile(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		boolean dirty = (vep.getState().getLocState() == LocationState.Clean) ? false : true;
		Tile t = new Tile (new Point(0,0), false, false, dirty, vep.isOnBase());

		this.setTile(t);
		if (vep.isOnBase())
			this.setBase(t);

	}

	private void setBase(Tile t) {
		if (!t.isBase()) {
			//TODO error
		}
		this.base = t;
	}

	public boolean isVisited(Point p) {
		return false;
	}

	public boolean isWall(Point p) {
		return false;
	}

	public void updateMap (LocalVacuumEnvironmentPerceptTaskEnvironmentB vep, Movement lastAction) {

		int lastX = this.currentPosition.x;
		int lastY = this.currentPosition.y;

		Point p = null;

		switch(lastAction) {
			case left:
				p = new Point(lastX-1, lastY);
				break;
			case down:
				p = new Point(lastX, lastY-1);
				break;
			case right:
				p = new Point(lastX+1, lastY);
				break;
			case up:
				p = new Point(lastX, lastY+1);
				break;
			default:
				//TODO some err
		}

		/* we hit an obstacle */
		if (!vep.isMovedLastTime()) {

			Tile t = new Tile(
					p, 
					false, /*  we need it?? TODO */
					true, /* is an obstacles */
					false, /* wall never dirty */
					false); /* wall never base */
			//,

			this.setTile(t);

		}
		else {
			// We moved
			Tile t = new Tile(
					p, 
					false, /*  we need it?? TODO */
					false, /* is no an obstacles */
					(vep.getState().getLocState() == LocationState.Dirty), 
					vep.isOnBase()); 
			this.setTile(t);

			this.currentPosition = p;
		}
	}

	/* return null if base not found yet */
	public Tile getBase() {
		return this.base;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Tile t : this.map.values()) {
			sb.append("x: " + t.getPoint().x);
			sb.append("y: " + t.getPoint().y);
			sb.append(" obs: " + t.isObstacle());
			sb.append(" dirty: " + t.isDirty());
			sb.append(" base: " + t.isBase());
			sb.append("\n");
		}
		return sb.toString();
	}

}
