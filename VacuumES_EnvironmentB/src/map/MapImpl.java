package map;



import java.awt.Point;


import aima.core.agent.AgentProgram;


import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;







public class MapImpl implements MapInterface {

	private List<PointFrom> unexploredPoints;
	private Map<Point, Tile> map;
	private int cols,rows;

	private Tile base;
	private AgentProgram agent;

	private Tile currentPosition;
	
	/* for wall detection */
	int minX, maxX, minY, maxY;
	private boolean rowsWallsDetected;
	private boolean colsWallsDetected;

	public MapImpl(AgentProgram a) {
		this.agent = a;
		this.map = new HashMap<Point, Tile>();
		this.base = null;
		minX = maxX = minY = maxY = 0;
		unexploredPoints = new LinkedList<PointFrom>();
	}

	private void updateMinMax(Tile t) {
		if (t.isObstacle())
			return;

		if (!rowsWallsDetected) {
			if (t.getPoint().x < minX)
				minX = t.getPoint().x;
			if (t.getPoint().x > maxX)
				maxX = t.getPoint().x;
		}
		if (!colsWallsDetected) {
			if (t.getPoint().y < minY)
				minY = t.getPoint().y;

			if (t.getPoint().y > maxY)
				maxY = t.getPoint().y;
		}
	}
	
	private void setTile(Tile t) {
		updateMinMax(t);
		
		if (unexploredPoints.contains(t.getPoint()))
			unexploredPoints.remove(t.getPoint());
		
		/* TODO check, will hashCode of points works? */
		this.map.put(t.getPoint(), t);
	}
	
	/* Can we known where are the walls ? */
	private void checkWalls() {
		Tile t;
		if (maxX - minX == cols-1 && !rowsWallsDetected) {
			rowsWallsDetected = true;
			for (int i = 0 - rows; i < rows; i++) {
				t = new Tile(new Point(minX-1, i), true, true, false, false);
				setTile(t);
				t = new Tile(new Point(maxX+1, i), true, true, false, false);
				setTile(t);
			}
		}
		
		if (maxY - minY == rows-1 && !colsWallsDetected) {
			colsWallsDetected = true;
			
			for (int i = 0 - cols; i < cols; i++) {
				t = new Tile(new Point(i, minY-1), true, true, false, false);
				setTile(t);
				t = new Tile(new Point(i, maxY+1), true, true, false, false);
				setTile(t);
			}
		}

	}

	public void setInitialTile(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		boolean dirty = (vep.getState().getLocState() == LocationState.Clean) ? false : true;
		Tile t = new Tile (new Point(0,0), false, false, dirty, vep.isOnBase());
		
		rows = vep.getN();
		cols = vep.getM();
		System.out.println(cols + " " + rows);

		this.setTile(t);
		currentPosition = t;
		
		if (vep.isOnBase())
			this.setBase(t);
		
		for (Point p: getAdjWalkablePoints(getCurrentPositionPoint())) {
			PointFrom pf = new PointFrom(p, t.getPoint());
			unexploredPoints.add(pf);
		}


	}

	private void setBase(Tile t) {
		if (!t.isBase()) {
			//TODO error
		}
		this.base = t;
	}

	
	public boolean isVisited(Point p) {
		return this.map.containsKey(p);
	}

	
	public boolean isVisited(Tile t) {
		return this.map.containsKey(t.getPoint());
	}

	//TODO check if is visited e
	public boolean isWall(Point p) {
		return this.map.get(p).isWall();
	}

	public boolean isWall(Tile t) {
		if (this.isVisited(t))
			return this.map.get(t.getPoint()).isWall();
		
		//TODO some err
		return false;
	}
	
	public boolean isObstacle(Point p) {
		if (this.getTile(p) != null)
			return this.map.get(p).isObstacle();
		
		//TODO some err
		return false;
	}
	
	/* return null if not visited yet */
	public Tile getTile(Point p) {
		return this.map.get(p); 
	}
	
	public boolean areWallsDetected() {
		return rowsWallsDetected && colsWallsDetected;
	}

	public void updateMap (LocalVacuumEnvironmentPerceptTaskEnvironmentB vep, Movement lastAction) {

		/* we don't move in the last step */
		if (lastAction == null)
			return;

		

		Point p = MapUtils.neighbourFromDirection(getCurrentPositionPoint(), lastAction);

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
			
			if (vep.isOnBase())
				setBase(t);

			this.currentPosition = t;
			updateUnexploredPointList();
		}
		
		
		System.out.println(getCurrentPosition().getPoint());
		
		if (!colsWallsDetected || !rowsWallsDetected)
			checkWalls();
	}

	private void updateUnexploredPointList() {
		/* we remove explored points from unexploredPoints list in setTile method */
		
		/* no new points to add in unexplored points list */
		if (isObstacle(getCurrentPositionPoint()))
			return;
		
		for (Point p : getAdjWalkablePoints(getCurrentPositionPoint())) {
			if (!unexploredPoints.contains(p) && !isVisited(p)) 
				unexploredPoints.add(new PointFrom(p,getCurrentPositionPoint()));
		}
		
	}

	/* return null if base not found yet */
	public Tile getBase() {
		return this.base;
	}
	
	@Override
	public List<Point> getAdjWalkablePoints(Point from) {
		List<Point> ret = new LinkedList<Point>();
				
		/* left */
		Point p = new Point(from.x, from.y);
		p.x -= 1;
		/* if (!isVisited(p) || isVisited(p) && !isObstacle(p)) */
		if (!isObstacle(p))
			ret.add(p);
			
		/* down */
		p = new Point(from.x, from.y);
		p.y -= 1;
		if (!isObstacle(p))
			ret.add(p);

		/* right */
		p = new Point(from.x, from.y);
		p.x += 1;
		if (!isObstacle(p)) 
			ret.add(p);

		/* up*/
		p = new Point(from.x, from.y);
		p.y += 1;
		if (!isObstacle(p)) 
			ret.add(p);

		return ret;
		
		
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

	@Override
	public Tile getCurrentPosition() {
		return this.currentPosition;
	}

	@Override
	public Point getCurrentPositionPoint() {
		return this.getCurrentPosition().getPoint();
	}

	@Override
	public int manatthanDistance(Point from, Point to) {
		return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
	}
	
	public double eucladianDistance(Point from, Point to) {
		
		return Math.sqrt(Math.pow(to.x-from.x,2)+Math.pow(to.y-from.y, 2));
	}
	
	public boolean isCompletelyExplored() {
		return unexploredPoints.size() == 0;
	}

	@Override
	public List<PointFrom> getUnexploredPoints() {
		return unexploredPoints;
	}

}
