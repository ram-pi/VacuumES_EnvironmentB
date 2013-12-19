package utils;

import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import map.MapInterface;

public class TourChooser {
	private MapInterface map;
	private SimpleWeightedGraph<Point, DefaultWeightedEdge> graph;
	private List<Point> hamiltonianCycle;
	private double remainingEnergy;
	private Point agentPosition;
	private List<Point> consideredDirty;
	private Map<Edge, List<Point>> edgePath;

	public TourChooser(MapInterface map, double remainingEnergy, List<Point> consideredDirty) {
		this.map = map;
		this.agentPosition = this.map.getCurrentPositionPoint();
		this.remainingEnergy = remainingEnergy;
		this.consideredDirty = consideredDirty;
		this.edgePath = new HashMap<Edge, List<Point>>();
		this.init();
	}

	public SimpleWeightedGraph<Point, DefaultWeightedEdge> getGraph() {
		return this.graph;
	}

	public List<Point> getConsideredDirty() {
		return this.consideredDirty;
	}

	public void init() {
		graph = new SimpleWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Astar a = new Astar(this.getMap());
		graph.addVertex(this.agentPosition);
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
				int weight = a.getPointPath().size();
				Edge ed = new Edge(p1, p2);
				this.edgePath.put(ed, a.getPointPath());
				graph.setEdgeWeight(e, weight);
				System.out.println(e + " weight -> " + weight);
			}
		}
	}
	
	public List<Point> getPathFromEdge(Point from, Point to) {
		if (from.equals(to))
			return null;
		
		Edge e = new Edge(from, to);
		List<Point> path = this.edgePath.get(e);
		if (path == null) {
			e = new Edge(to, from);
			path = this.edgePath.get(e);
			Collections.reverse(path);
			return path;
		}
		return path;
	}

	public void getBestHamiltonianTour() {
		HamiltonianCycle ham = new HamiltonianCycle();
		this.hamiltonianCycle = ham.getApproximateOptimalForCompleteGraph(this.graph);
		Collections.reverse(this.hamiltonianCycle);
		System.out.println("The Hamiltonian cycle is -> " + this.hamiltonianCycle);
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
			//List<Point> tempPath = this.getPathFromEdge(current, point);
			List<Point> tempPath = as.astar(current, point).getPointPath();
			hamiltonianPath.addAll(tempPath);
			current = point;
		}
		System.out.println("The path to perform the cycle is -> " + hamiltonianPath);
		return hamiltonianPath;
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
