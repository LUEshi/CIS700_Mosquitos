package mosquito.sim;

import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Set;

public class MoveableLight extends Light {

	public MoveableLight(double x, double y, double d, double t, double s) {
		super(x, y, d, t, s);
	}
	
	public MoveableLight(double x, double y, boolean on) {
		super(x, y, 0, 0, 0);
		isLightOn = on;
	}
	
	private boolean isLightOn = true;

	@Override
	public boolean isOn(int time) {
		return isLightOn;
	}
	
	public boolean isOn() {
		return isLightOn;
	}
	
	public void turnOn() {
		isLightOn = true;
	}
	
	public void turnOff() {
		isLightOn = false;
	}
	
	public boolean moveUp() {
		if (this.y > 0) {
			if (isLegalMove(this.x, this.y-1)) {
				this.y--;
				return true;
			}
			else return false;
		}
		return false;
	}

	public boolean moveDown() {
		if (this.y < 100) {
			if (isLegalMove(this.x, this.y+1)) {
				this.y++;
				return true;
			}
			else return false;
		}
		return false;
	}

	public boolean moveLeft() {
		if (this.x > 0) {
			if (isLegalMove(this.x-1, this.y)) {
				this.x--;
				return true;
			}
			else return false;
		}
		return false;
	}
	
	public boolean moveRight() {
		if (this.x < 100) {
			if (isLegalMove(this.x+1, this.y)) {
				this.x++;
				return true;
			}
			else return false;
		}
		return false;
	}
	
	protected boolean isLegalMove(double newX, double newY) {
		Line2D.Double pathLine = new Line2D.Double(this.x,this.y,newX,newY);
		for(Line2D l : Board.walls)
		{
			if(l.intersectsLine(pathLine))
				return false;
		}
		return true;
	}
}
