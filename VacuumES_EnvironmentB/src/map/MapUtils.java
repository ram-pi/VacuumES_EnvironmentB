package map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import map.MapInterface.Movement;

public class MapUtils {
	public static Point neighbourFromDirection(Point from, Movement dir) {
		Point p = null;
		
		int lastX = from.x;
		int lastY = from.y;
		
		switch(dir) {
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
		return p;
	}
	
	public static Movement movementFromTwoPoints(Point from, Point to) {
		/* if adj */
		
		if (to.x < from.x)
			return Movement.left;
		if (to.y < from.y)
			return Movement.down;
		if (to.x > from.x)
			return Movement.right;
		if (to.y > from.y)
			return Movement.up;

		/* TODO some err */

		return null;
	}
	
	public static Movement roundRobinMovement (Movement m) {
		int n = (int) m.ordinal();
		n++;
		if (n >= Movement.values().length)
			n = 0;
		return Movement.values()[n];
	}
	
	public static List<Point> getTrasversalPoint (Point from, Movement dir) {
		ArrayList<Point> ret = new ArrayList<Point>();
		if (dir == Movement.left || dir == Movement.right) {
			ret.add(neighbourFromDirection(from, Movement.up));
			ret.add(neighbourFromDirection(from, Movement.down));
		}
		else if (dir == Movement.up || dir == Movement.down) {
			ret.add(neighbourFromDirection(from, Movement.left));
			ret.add(neighbourFromDirection(from, Movement.right));
		}
		return ret;
	}
}
