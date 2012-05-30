package mosquito.sim;

import java.awt.geom.Point2D;

public abstract class GameObject {
	protected double x;
	protected double y;
	public Point2D getLocation()
	{
		return new Point2D.Double(x,y);
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
}
