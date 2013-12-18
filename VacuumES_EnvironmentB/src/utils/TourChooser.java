package utils;

import java.awt.Point;
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
	private Map<DefaultWeightedEdge, List<Point>> edgePath;

	public TourChooser(MapInterface map, double remainingEnergy, List<Point> consideredDirty) {
		this.map = map;
		this.agentPosition = this.map.getCurrentPositionPoint();
		this.remainingEnergy = remainingEnergy;
		this.consideredDirty = consideredDirty;
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
		//graph.addVertex(map.getBase().getPoint());
		for (Point p : consideredDirty) {
			a.astar(agentPosition, p);
			if (a.getPath().size()*2 > remainingEnergy) 
				break;
			graph.addVertex(p);
		}
		for (Point p1 : graph.vertexSet()) {
			for (Point p2 : graph.vertexSet()) {
				if (p1.equals(p2))
					break;
				DefaultWeightedEdge e = graph.addEdge(p1, p2);
				int weight = a.astar(p1, p2).getPath().size();
				this.edgePath.put(e, a.getPointPath());
				graph.setEdgeWeight(e, weight);
				//System.out.println(e + " weight -> " + weight);
			}
		}
	}
	
	public List<Point> getPathFromEdge(Point from, Point to) {
		if (from.equals(to))
			return null;
		
		DefaultWeightedEdge e = this.graph.getEdge(from, to);
		List<Point> path = this.edgePath.get(e);
		return path;
	}

	public void getBestHamiltonianTour() {
		HamiltonianCycle ham = new HamiltonianCycle();
		this.hamiltonianCycle = ham.getApproximateOptimalForCompleteGraph(this.graph);
		System.out.println(this.hamiltonianCycle);
	}

	public List<Point> getPathHamiltonian() {
		this.getBestHamiltonianTour();
		List<Point> hamiltonianPath = new LinkedList<Point>();
		this.hamiltonianCycle.remove(hamiltonianCycle.size()-1);
		Astar as = new Astar(this.map);
		Point current = this.hamiltonianCycle.get(0);
		for (Iterator<Point> iterator = this.hamiltonianCycle.iterator(); iterator.hasNext();) {
			Point point = (Point) iterator.next();
			as.astar(current, point);
			List<Point> tempPath = as.getPointPath();
			hamiltonianPath.addAll(tempPath);
			current = point;
		}
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
