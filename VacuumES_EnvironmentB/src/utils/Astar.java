/* This is a path finder that implements the A* Algorithm for searching the minimum distance to a cell
 * and next find the shortest path with the nearest neighbor algorithm
 *  */

package utils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import map.MapInterface;

public class Astar {

	private class A_Point {

		private double g, h;
		private Point position;
		private Point father;

		public A_Point() {
		}

		public A_Point (double g, double h, Point position, Point father) {
			this.g = g;
			this.h = h;
			this.position = position;
			this.father = father;
		}

		public double getG() {
			return g;
		}

		public void setG(int g) {
			this.g = g;
		}

		public double getH() {
			return h;
		}

		public void setH(double d) {
			this.h = d;
		}

		public Point getPosition() {
			return position;
		}

		public void setPosition(Point position) {
			this.position = position;
		}

		public double getF () {
			return g+h;
		}

		public Point getFather() {
			return father;
		}

		public void setG(double g) {
			this.g = g;
		}

		public void setFather(Point father) {
			this.father = father;
		}

		@Override
		public boolean equals(Object obj) {
			A_Point a = (A_Point) obj;
			if (this.position.getX() == a.getPosition().getX() && this.position.getY() == a.getPosition().getY())
				return true;
			return false;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("["+position.x+","+position.y+"] g:"+g+" f:"+getF()+" fath:["+father.x+","+father.y+"]");
			return sb.toString();
		}

	}


	private List<A_Point> closedList;
	private List<A_Point> openList;
	private A_Point current;
	private List<A_Point> path;
	private MapInterface map;

	public Astar(MapInterface map) {
		this.closedList = new LinkedList<A_Point>()
				{
					public String toString() {
						StringBuilder sb = new StringBuilder(); 
						for (A_Point ap : this) {
							sb.append(ap);
							sb.append("\n");
						}
						return sb.toString();
					}
				};
		this.openList = new LinkedList<A_Point>()				{
			public String toString() {
				StringBuilder sb = new StringBuilder(); 
				for (A_Point ap : this) {
					sb.append(ap);
					sb.append("\n");
				}
				return sb.toString();
			}
		};
		this.current = new A_Point();
		this.path = new ArrayList<A_Point>(); 	
		this.map = map;
	}

	public void clearLists () {
		this.getOpenList().clear();
		this.getClosedList().clear();
		this.getPath().clear();
		this.current = new A_Point();
	}

	public Astar astar (Point start, Point finish) {
		System.out.println("path from "+start.x+" "+start.y+"  to "+finish.x + " " + finish.y);
		boolean finishPointReached = false;
		List<A_Point> walkableFromCurrent = new LinkedList<A_Point>();

		// Starting to set the starting node as the current node
		clearLists();
		current.setG(0);
		current.setH(manhattanDistance(start, finish));
		current.setPosition(start);
		current.setFather(start);

		closedList.add(current);
		walkableFromCurrent = getAdjForOpenList(getAdjacent(current.getPosition(), finish), current, finish);
		openList.addAll(walkableFromCurrent);

		int step = 0;
		boolean debug = false;
		while (!finishPointReached) {

			step++;
			if (debug){
				
			System.out.println(closedList);
			System.out.println(openList);
			}
			// Verify if I reached the destination
			if (current.getPosition().equals(finish)) {
				finishPointReached = true;
				continue;
			}

			// Get the square S on the openlist which has the lowest F
			current = findPointLowestF();
			if (current == null) {
				path.clear();
				return this;
			}

			// Remove S from openList and add it to closedList
			openList.remove(current);
			closedList.add(current);

			//if (current == null)
			//system.out.println("current is null");


			// Find the adjacent tiles to current position
			walkableFromCurrent = getAdjForOpenList(getAdjacent(current.getPosition(), finish), current, finish);

			// Foreach Tiles T adjacent to current if T is in the closed list ignore it and if it is not in the openlis add it
			for (A_Point ap : walkableFromCurrent) {
				List<Point> closedListPoint = this.getListOfPoint(closedList);
				List<Point> openListPoint = this.getListOfPoint(openList);
				if (!closedListPoint.contains(ap.getPosition()) && !openListPoint.contains(ap.getPosition())) {
					openList.add(ap);
				}
				// If T is in the openlist check if F score is lower and update his parent with his path
				else if (openListPoint.contains(ap.getPosition())) {
					checkPath(ap);
				}
			}

		}

		this.buildPath();
		return this;
	}

	public void buildPath () {
		A_Point curr = this.getClosedList().get(this.getClosedList().size()-1);

		while (!curr.getPosition().equals(curr.getFather())) {
			this.getPath().add(curr);
			curr = searchInClosedByPosition(curr.getFather());
		}

		//		this.getPath().add(curr);
		Collections.reverse(this.getPath());
	}

	public A_Point searchInClosedByPosition (Point p) {
		for (A_Point ap : this.getClosedList()) {
			if (ap.getPosition().equals(p))
				return ap;
		}
		return null;
	}

	public List<Point> getListOfPoint (List<A_Point> l) {
		List<Point> res = new LinkedList<Point>();
		for (A_Point ap : l) {
			res.add(ap.getPosition());
		}
		return res;
	}

	public void checkPath (A_Point adjP) {

		A_Point fromOpen = null;
		// Get the A_Point already in the openList and check if it is lower then the adjP
		for (A_Point p : this.getOpenList()) {
			if (p.getPosition().equals(adjP.getPosition())){
				fromOpen = p;
				break;
			}
		}

		double fromOpenF = fromOpen.getF();
		if (fromOpenF >= adjP.getF()) {
			this.getOpenList().remove(fromOpen);
			this.getOpenList().add(adjP);
			return;
		}
	}

	public int getIndexFromList (List<A_Point> l, A_Point a) {
		int index = -1;
		for (int i = 0; i < l.size(); i++) {
			if (l.get(i).equals(a))	
				return i;
		}
		return index;
	}

	public A_Point findPointLowestF () {
		double lowestF;
		A_Point result;
		List<A_Point> sel = this.getOpenList();

		if (sel.isEmpty())
			return null;

		lowestF = sel.get(0).getF();
		result = sel.get(0);
		for (A_Point ap : sel) {
			if (ap.getF() < lowestF) {
				lowestF = ap.getF();
				result = ap;
			}
		}
		return result;
	}

	public void printApointList (List<A_Point> l) {

		return;
	}

	public List<A_Point> getAdjForOpenList (List<Point> adj, A_Point father, Point destination) {
		List<A_Point> adjOpen = new LinkedList<A_Point>();
		double g = father.getG()+1;
		double h;
		Point f = father.getPosition();
		A_Point tmp;

		for (Point point : adj) {
			h = manhattanDistance(point, destination);
			tmp = new A_Point(g, h, point, f);
			adjOpen.add(tmp);
		}

		return adjOpen;
	}

	public List<Point> getAdjacent (Point p, Point dest) {
		List<Point> adj = map.getAdjWalkablePoints(p);
		List<Point> result = new LinkedList<Point>();
	
		for (Point point : adj) {

			if (map.isVisited(point) || point.equals(dest))
				result.add(point);
		}
		
		return result;
	}

	public void printPath (List<Point> l) {
		System.out.println("The path is :");
		for (Point point : l) {
			System.out.println(point);
		}
	}

	public double manhattanDistance (Point a, Point b) {

		double x = Math.abs(a.getX() - b.getX());
		double y = Math.abs(a.getY() - b.getY());

		return x+y;
	}

	public List<Point> getPointPath () {
		List<Point> l = new LinkedList<Point>();
		for (A_Point ap : this.getPath()) {
			l.add(ap.getPosition());
		}
		return l;
	}


	public List<A_Point> getClosedList() {
		return closedList;
	}

	public List<A_Point> getOpenList() {
		return openList;
	}

	public A_Point getCurrent() {
		return current;
	}

	public List<A_Point> getPath() {
		return path;
	}

	public void setClosedList(List<A_Point> closedList) {
		this.closedList = closedList;
	}

	public void setOpenList(List<A_Point> openList) {
		this.openList = openList;
	}

	public void setCurrent(A_Point current) {
		this.current = current;
	}

	public void setPath(List<A_Point> path) {
		this.path = path;
	}

}
