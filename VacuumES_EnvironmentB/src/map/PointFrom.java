package map;

import java.awt.Point;

public class PointFrom extends Point {
	private Point from;
	public Point getFrom() {
		return from;
	}
	
	public void setFrom(Point from) {
		this.from = from;
	}
	
	public PointFrom(Point p) {
		super(p);
	}
	
	public PointFrom(Point p, Point from) {
		super(p);
		this.from = from;
	}
	
	@Override
	public boolean equals(Object p) {
		if (p instanceof Point) {
			return p.equals((Point)this);
		}
		PointFrom pf;
		if (p instanceof PointFrom) {
			pf = (PointFrom)p;
			return super.equals((Point)pf) && pf.from.equals((Point)this.from);
		}
		return super.equals(p);
	}
}
