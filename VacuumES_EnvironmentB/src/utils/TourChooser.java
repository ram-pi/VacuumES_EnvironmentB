package utils;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import aima.core.search.nondeterministic.Path;
import map.MapInterface;

public class TourChooser {
	private MapInterface map;
	private SimpleWeightedGraph<Point, DefaultWeightedEdge> graph;
	private List<Point> hamiltonianCycle;
	private double remainingEnergy;
	private Point agentPosition;
	private List<Point> consideredDirty;
	private Map<Edge, List<Point>> edgePath;
	private boolean weHaveEnd;
	private Point end;

	public TourChooser(MapInterface map, double remainingEnergy, List<Point> consideredDirty, Point end) {
		this.map = map;
		this.agentPosition = this.map.getCurrentPositionPoint();
		this.remainingEnergy = remainingEnergy;
		this.consideredDirty = consideredDirty;
		this.edgePath = new HashMap<Edge, List<Point>>();
		this.end = end;
		if (!(this.agentPosition == this.end))
			weHaveEnd = true;
		else
			weHaveEnd = false;
		this.init();
	}

	public SimpleWeightedGraph<Point, DefaultWeightedEdge> getGraph() {
		return this.graph;
	}

	public List<Point> getConsideredDirty() {
		return this.consideredDirty;
	}
	
	public Point getEnd() {
		return this.end;
	}

	public void init() {
		graph = new SimpleWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Astar a = new Astar(this.getMap());
		graph.addVertex(this.agentPosition);
		/* Check if the end position is the base position */
		if (weHaveEnd) 
			graph.addVertex(end);
		for (Point p : consideredDirty) {
			a.astar(agentPosition, p);
			graph.addVertex(p);
		}
		for (Point p1 : graph.vertexSet()) {
			for (Point p2 : graph.vertexSet()) {
				if (p1.equals(p2))
					break;
				DefaultWeightedEdge e = graph.addEdge(p1, p2);
				a.astar(p1, p2);
				List<Point> path = a.getPointPath();
				int weight = a.getPointPath().size();
				Edge ed1 = new Edge(p1, p2);
				//System.out.println(ed1.toString() + "the path is " + path);
				this.edgePath.put(ed1, path);
				Edge ed2 = new Edge(p2, p1);
				List<Point> pathReverse = new LinkedList<Point>();
				pathReverse.addAll(path);
				Collections.reverse(pathReverse);
				pathReverse.remove(p2);
				pathReverse.add(p1);
				this.edgePath.put(ed2, pathReverse);
				//System.out.println(ed2.toString() + "the path is " + edgePath.get(ed2));
				graph.setEdgeWeight(e, weight);
				//System.out.println(e + " weight -> " + graph.getEdgeWeight(e));
			}
		}
		/* Check if is necessary to add dummy node */
		if (weHaveEnd)
			this.addDummy();
	}
	
	public List<Point> getPathFromEdge(Edge e) {
		List<Point> pathFromContainer = (List<Point>) this.edgePath.get(e);
		//System.out.println(e.toString());
		//System.out.println("Path -> " + pathFromContainer);
		return pathFromContainer;
	}

	public void getBestHamiltonianTour() {
		HamiltonianCycle ham = new HamiltonianCycle();
		this.hamiltonianCycle = ham.getApproximateOptimalForCompleteGraph(this.graph);
		System.out.println("The Hamiltonian cycle before fixing is -> " + this.hamiltonianCycle);
		fixCycle();
		//System.out.println("The Hamiltonian cycle is -> " + this.hamiltonianCycle);
	}

	public List<Point> getPathHamiltonian() {
		if (this.hamiltonianCycle == null) {
			this.getBestHamiltonianTour();
		} else {
			this.getBestHamiltonianTour();
		}
		List<Point> hamiltonianPath = new LinkedList<Point>();
		Astar as = new Astar(this.map);
		Point current = this.hamiltonianCycle.remove(0);
		for (Iterator<Point> iterator = this.hamiltonianCycle.iterator(); iterator.hasNext();) {
			Point point = (Point) iterator.next();
			List<Point> tempPath = this.getPathFromEdge(new Edge(current, point));
			//System.out.println("Getting path from " + current + " to " + point + "\n" + tempPath);
			//List<Point> tempPath = as.astar(current, point).getPointPath();
			if (tempPath == null)
				continue;
			hamiltonianPath.addAll(tempPath);
			current = point;
		}
		//System.out.println("The path to perform the cycle is -> " + hamiltonianPath);
		printStats();
		return hamiltonianPath; 
	}
	
	public void addDummy() {
		double maxDistance = (double) map.getCols()*map.getRows()+1;
		Point dummy = new Point(this.map.getRows()+1, this.map.getCols()+1);
		this.graph.addVertex(dummy);
		DefaultWeightedEdge e = this.graph.addEdge(end, dummy);
		this.graph.setEdgeWeight(e, maxDistance-1);
		//System.out.println(e + " -> " + graph.getEdgeWeight(e));
		e = this.graph.addEdge(agentPosition, dummy);
		this.graph.setEdgeWeight(e, maxDistance-1);
		//System.out.println(e + " -> " + graph.getEdgeWeight(e));
		for (Point p : consideredDirty) {
			e = this.graph.addEdge(p, dummy);
			this.graph.setEdgeWeight(e, maxDistance);
			//System.out.println(e + " -> " + graph.getEdgeWeight(e));
		}
	}
	
	public void fixCycle() {
		hamiltonianCycle.add(0, agentPosition);
		int hsize = hamiltonianCycle.size();
		if (weHaveEnd) {
			hamiltonianCycle.remove(hsize-1);
			hsize  = hamiltonianCycle.size();
			hamiltonianCycle.remove(hsize-2);
		}
	}
	
	public void printStats() {
		System.out.println("End node is ->" + this.end);
		System.out.println("Current position is ->" + this.agentPosition);
		printGraph();
	}
	
	public void printGraph() {
		for (Point p1 : graph.vertexSet()) {
			for (Point p2 : graph.vertexSet()) {
				if (p1.equals(p2))
					break;
				DefaultWeightedEdge e = graph.getEdge(p1, p2);
				double w = graph.getEdgeWeight(e);
				System.out.println(p1 + " " + p2 + " " + w);
			}
		}
	}

	public List<Point> getHamiltonianCycle() {
		return this.hamiltonianCycle;
	}

	public MapInterface getMap() {
		return map;
	}

	public void setMap(MapInterface map) {
		this.map = map;
	}
}
