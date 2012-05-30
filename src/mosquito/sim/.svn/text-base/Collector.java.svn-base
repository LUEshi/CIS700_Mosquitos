package mosquito.sim;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Collector extends GameObject {
	public Collector(double x, double y) {
		this.x = x;
		this.y = y;
		boundingBox = new Ellipse2D.Double(x - RADIUS / 2, y - RADIUS / 2,
				RADIUS, RADIUS);
	}

	private Ellipse2D boundingBox;

	public boolean contains(Mosquito m) {
		return boundingBox.contains(m.location);
	}

	public boolean intersects(Line2D line) {
		return line.intersects(boundingBox.getBounds());
	}
	public boolean contains(Point2D p)
	{
		return boundingBox.contains(p);
	}
	public static final double RADIUS = 1;
}
