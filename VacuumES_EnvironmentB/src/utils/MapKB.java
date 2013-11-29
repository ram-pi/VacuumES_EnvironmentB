package utils;

import java.awt.Point;

import aima.core.agent.Action;

import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import java.util.HashMap;
import java.util.Map;



public class MapKB implements VacuumMapsUtils {

    private Map<Point, Tile> map;
    
    
    public MapKB() {
    	this.map = new HashMap<Point, Tile>();
    }


    public boolean isVisited(Point p) {
    	return false;
    }

    public boolean isWall(Point p) {
    	return false;
    }
    
    public void updateMap (LocalVacuumEnvironmentPerceptTaskEnvironmentB vep, Action lastAction) {
	
    }

    public Point getBase() {
    	return null;
    }
    
}
