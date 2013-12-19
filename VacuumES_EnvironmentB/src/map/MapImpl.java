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

	private List<Point> unexploredPoints;
	private Map<Point, Tile> map;


	private Tile base;

	private Tile currentPosition;

	/* for wall detection */
	int minX, maxX, minY, maxY;
	private boolean rowsWallsDetected;
	private boolean colsWallsDetected;

	public MapImpl() {
		this.map = new HashMap<Point, Tile>();
		this.base = null;
		minX = minY = 0;
		maxX = maxY = 0;
		unexploredPoints = new LinkedList<Point>();
		rowsWallsDetected = colsWallsDetected = false;
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

	public void setTile(Tile t) {
		updateMinMax(t);


		/* TODO check, will hashCode of points works? */
		this.map.put(t.getPoint(), t);
	}

	/* Can we known where are the walls ? */
	private void checkWalls() {

		if (maxX - minX == cols-1 && !rowsWallsDetected) 
			rowsWallsDetected = true;


		if (maxY - minY == rows-1 && !colsWallsDetected) 
			colsWallsDetected = true;

	}

	public void setInitialTile(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		boolean dirty = (vep.getState().getLocState() == LocationState.Clean) ? false : true;
		Tile t = new Tile (new Point(0,0), false, false, dirty, vep.isOnBase());

		rows = vep.getN();
		cols = vep.getM();


		this.setTile(t);
		currentPosition = t;

		if (vep.isOnBase())
			this.setBase(t);

		for (Point p: getAdjWalkablePoints(getCurrentPositionPoint())) 
			unexploredPoints.add(p);



	}

	private void setBase(Tile t) {
		if (!t.isBase()) {
			//TODO error
		}
		this.base = t;
	}


	public boolean isVisited(Point p) {
		if (isWall(p))
			return true;

		return this.map.containsKey(p);
	}


	public boolean isVisited(Tile t) {
		return this.map.containsKey(t.getPoint());
	}

	//TODO check if is visited e
	public boolean isWall(Point p) {
		if (p.x < -cols + 1 + maxX)
			return true;
		if (p.x > cols + minX -1)
			return true;

		if (p.y > rows + minY -1)
			return true;
		if (p.y < -rows + 1 + maxY)
			return true;

		if (map.containsKey(p))
			return this.map.get(p).isWall();

		return false;
	}

	public boolean isWall(Tile t) {
		if (this.isVisited(t))
			return this.map.get(t.getPoint()).isWall();

		//TODO some err
		return false;
	}

	public boolean isDirty(Point p) {
		Tile t = getTile(p);
		if (t == null)
			return false;
		return t.isDirty();

	}

	public boolean isObstacle(Point p) {
		if (isWall(p))
			return true;

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
			updateUnexploredPointListNoMove();
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


		//System.out.println(getCurrentPosition().getPoint());

		if (!colsWallsDetected || !rowsWallsDetected)
			checkWalls();
	}



	private void updateUnexploredPointListNoMove () {
		/* we remove explored points from unexploredPoints list in setTile method */
		LinkedList<Point> uec = new LinkedList<Point>();
		uec.addAll(unexploredPoints);

		for (Point p : uec) 
			if (isVisited(p))
				unexploredPoints.remove(p);
	}

	private void updateUnexploredPointList() {
		/* we remove explored points from unexploredPoints list in setTile method */
		LinkedList<Point> uec = new LinkedList<Point>();
		uec.addAll(unexploredPoints);

		for (Point p : uec) 
			if (isVisited(p))
				unexploredPoints.remove(p);

		/* no new points to add in unexplored points list */
		if (isObstacle(getCurrentPositionPoint()))
			return;

		for (Point p : getAdjWalkablePoints(getCurrentPositionPoint())) {
			if (!unexploredPoints.contains(p) && !isVisited(p)) 
				unexploredPoints.add(p);
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
	public List<Point> getUnexploredPoints() {
		return unexploredPoints;
	}

	@Override
	public double percentExplored() {
		return (double)(map.values().size())/cols*rows;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public Map<Point, Tile> getMap() {
		return this.map;
	}

	private int cols,rows;

	@Override
	public Point getNearestUnexplored(Point p) {
		Point nearest = null;
		List<Point> unexplored = this.getUnexploredPoints();
		int minDistance = Integer.MAX_VALUE;
		for (Point tmp : unexplored) {
			int tmpDistance = this.manatthanDistance(p, tmp);
			if (tmpDistance < minDistance) {
				nearest = tmp;
				minDistance = tmpDistance;
			}
		}
		return nearest;
	}

}
