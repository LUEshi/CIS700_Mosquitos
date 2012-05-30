package mosquito.sim;

public class Light extends GameObject {
	private double d;
	private double t;
	private double s;
	boolean forced_on = false;
	/**
	 * Constructs a new light object
	 * @param x X co-ordinate
	 * @param y Y co-ordinate
	 * @param d Interval between lighting cycles
	 * @param t How long a light stays on within the cycle
	 * @param s When the cycle starts
	 */
	public Light(double x, double y, double d, double t, double s) {
		this.x = x;
		this.y = y;
		this.d = d;
		this.t= t;
		this.s = s;
		if(t > d)
			throw new IllegalArgumentException("t can not be > d");
	}
	public boolean isOn(int time)
	{
		if(forced_on)
			return true;
		if(time >= s)
		{
			time -=s;
			//Find out how far we are in the cycle
			time = (int) (time % d);
			if(time < t)
				return true;
		}
		return false;
	}
}
