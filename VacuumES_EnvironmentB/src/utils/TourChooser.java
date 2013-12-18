package utils;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import map.MapInterface;
import map.Tile;

public class TourChooser {
	private MapInterface map;
	private SimpleWeightedGraph<Point, DefaultWeightedEdge> graph;
	private Map<Point, Integer> dirtyPointsDistance;
	private Point farestDirtyPoint;
	private List<Point> hamiltonianCycle;

	public TourChooser(MapInterface map) {
		this.map = map;
		this.dirtyPointsDistance = new HashMap<Point, Integer>();
		this.init();
	}

	public SimpleWeightedGraph<Point, DefaultWeightedEdge> getGraph() {
		return this.graph;
	}

	public void init() {
		graph = new SimpleWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		HashMap<Point, Tile> mapInfo = (HashMap<Point, Tile>) this.getMap().getMap();
		Astar a = new Astar(this.getMap());
		Point base = map.getBase().getPoint();
		Set<Point> keys =  mapInfo.keySet();
		graph.addVertex(map.getBase().getPoint());
		int maxDistance = Integer.MIN_VALUE;
		for (Point p : keys) {
			Tile t = mapInfo.get(p);
			if (t.isDirty()) {
				graph.addVertex(p);
				a.astar(p, base);
				int distanceFromBase = a.getPath().size();
				if (distanceFromBase > maxDistance) {
					maxDistance = distanceFromBase;
					this.farestDirtyPoint = p;
				}
				dirtyPointsDistance.put(p, distanceFromBase);
				
			}
		}
		for (Point p1 : graph.vertexSet()) {
			for (Point p2 : graph.vertexSet()) {
				if (p1.equals(p2))
					break;
				DefaultWeightedEdge e = graph.addEdge(p1, p2);
				int weight = a.astar(p1, p2).getPath().size();
				graph.setEdgeWeight(e, weight);
				//System.out.println(e + " weight -> " + weight);
			}
		}
	}
	
	public void getBestHamiltonianTour() {
		HamiltonianCycle ham = new HamiltonianCycle();
		this.hamiltonianCycle = ham.getApproximateOptimalForCompleteGraph(this.graph);
		System.out.println(this.hamiltonianCycle);
	}
	
	public Point getFarestDirtyPoint () {
		return this.farestDirtyPoint;
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
