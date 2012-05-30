package mosquito.sim;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

public class Mosquito {
	public Point2D location;
	public boolean caught;
	public Mosquito(Point2D p)
	{
		this.location = p;
		this.caught=  false;
	}
	public void moveInDirection(double d, HashSet<Line2D> walls) {
		d = d -30 + GameConfig.random.nextInt(60);
		if(location.getY() - Math.sin(d*Math.PI/180) < 0 || location.getY() - Math.sin(d*Math.PI/180) > 100 || location.getX() + Math.cos(d*Math.PI/180) > 100 || location.getX() + Math.cos(d*Math.PI/180) < 0)
			return;
		Line2D.Double pathLine = new Line2D.Double(location.getX(),location.getY(),location.getX() + Math.cos(d*Math.PI/180), location.getY() - Math.sin(d*Math.PI/180));
		for(Line2D l : walls)
		{
			if(l.intersectsLine(pathLine))
				return;
		}
		location.setLocation(location.getX() + Math.cos(d*Math.PI/180), location.getY() - Math.sin(d*Math.PI/180));
	}
}
