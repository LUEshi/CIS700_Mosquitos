package mosquito.g5;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.MoveableLight;



public class RadialPlayer extends mosquito.sim.Player {

private final double epsilon = .00001;

private int numLights;
private int numCollectors;
private Point2D.Double lastLight;
private Logger log = Logger.getLogger(this.getClass()); // for logging
Map<Integer, HashSet<Tuple<Integer,Integer>>> sectors;

@Override
public String getName() {
	return "Radial Player";
}

private Set<Light> lights;
private Set<Collector> collectors;
private Map<Integer,Tuple<Integer,Integer>> objective;
private Light[] lightArr;

/*
* This is called when a new game starts. It is passed the set
* of lines that comprise the different walls, as well as the
* maximum number of lights and collectors you are allowed to use.
*/
@Override
public void startNewGame(Set<Line2D> walls, int numLights, int numCollectors) {
	this.numLights = numLights;
	this.numCollectors = numCollectors;
}


/*
* This is used to determine the initial placement of the lights.
* It is called after startNewGame.
* The board tells you where the mosquitoes are: board[x][y] tells you the
* number of mosquitoes at coordinate (x, y)
*/
public Set<Light> getLights(int[][] board) {
	lights = new HashSet<Light>();
	objective = new HashMap<Integer,Tuple<Integer,Integer>>();
	lightArr = new Light[numLights];

	sectors = getSectors();
	for( int i=0; i<numLights; i++)
	{
		int x = 0;
		int y = 0;
		double theta = i*(360/numLights) + (360/numLights)/2;
		log.trace("i" + i + " - theta:" + theta);
		double slope = Math.tan(Math.toRadians(theta));
		int xf = 1;
		int yf = 1;
		if ( theta > 90 && theta < 270 ) {
			xf = -1;
		}
		// Placement should be more precise...this is very lazy
		// Special case: vertical lines
		if ( theta == 90 || theta == 270 ) {
			while ( Math.abs(y) < 50 ) {
				y++;
			}
		} else {
			while ( Math.abs(x)<30 && Math.abs(y)<30 ) {
				x=x+xf;
				y=(int) Math.floor(slope*x);
				y = (int) (slope*x);
				log.trace(i+":(x,y):"+x+","+y+"slope"+slope);
			}
		}
		lastLight = new Point2D.Double(x+50, y+50);
		MoveableLight l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
		log.trace("Positioned a light at (" + lastLight.getX() + ", " + lastLight.getY() + ")");
		lights.add(l);
		lightArr[i]=l;
		objective.put(i, null);
	}
	return lights;
}

public HashMap<Integer, HashSet<Tuple<Integer,Integer>>> getSectors()
{
	HashMap<Integer, HashSet<Tuple<Integer,Integer>>> m =
	new HashMap<Integer, HashSet<Tuple<Integer,Integer>>>();
	int degreesPerSector = 360/this.numLights;
	for(int i=0; i<this.numLights; i++)
	{
		HashSet<Tuple<Integer,Integer>> s = new HashSet<Tuple<Integer,Integer>>();
		// Loop over every radial line out from the center
		// There are more efficient ways to do this, but this works & is simple
		for(int j = i*degreesPerSector; j<degreesPerSector*(i+1); j++)
		{
			int x = 0;
			int y = 0;
			int xf = 1;
			int yf = 1;
			int theta = j;
			if ( theta > 90 && theta < 270 ) {
				xf = -1;
			}
			if ( theta > 180 && theta < 360 ) {
				yf = -1;
			}

			// Special case: vertical lines
			if ( theta == 90 || theta == 270 ) {
				while ( Math.abs(y) < 50 ) {
					Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x+50,y+50);
					y++;
				}
			} else {
				// y = slope*x
				double slope = Math.tan(Math.toRadians(theta));
	
				while ( Math.abs(x) < 50 && Math.abs(y) < 50 ) {
					Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x+50,y+50);
					s.add(t);
					x=x+xf;
					y=(int) Math.floor(slope*x);
				}
			}
		}
		m.put(i, s);
	}
	return m;
}


/*
* This is called at the beginning of each step (before the mosquitoes have moved)
* If your Set contains additional lights, an error will occur.
* Also, if a light moves more than one space in any direction, an error will occur.
* The board tells you where the mosquitoes are: board[x][y] tells you the
* number of mosquitoes at coordinate (x, y)
*/
public Set<Light> updateLights(int[][] board) {
	
	for(int i = 0; i < numLights; i++)
	{
		// Retrieve the objective (destination) for the current light
		Tuple<Integer,Integer> o = objective.get(i);

		// Find the closest mosquito within the light's sector
		Tuple<Integer,Integer> closestMosquito = null;
		double closestMosquitoDistance = Double.MAX_VALUE;
		for ( Tuple<Integer,Integer> t: sectors.get(i) ) {
			double x = Math.pow(t.x-lightArr[i].getX(),2);
			double y = Math.pow(t.y-lightArr[i].getY(),2);
			double distance = Math.pow(x+y,.5);
			if ( board[t.x][t.y] > 0 && distance > 20 && distance < closestMosquitoDistance ) {
				// Make sure the mosquito hasn't already been captured by another light
				boolean captured = false;
				// Check to make sure item isn't within the radius of another light (already captured)
				// This check doesn't seem that helpful, time-wise
				for (int j = 0; j < numLights; j++) {
					if ( i != j ) {
						x = Math.pow(t.x-lightArr[j].getX(),2);
						y = Math.pow(t.y-lightArr[j].getY(),2);
						distance = Math.pow(x+y,.5);
						if ( distance < 20 ) {
							captured = true;
						}
					}
				}

				if ( !captured ) {
					closestMosquitoDistance = distance;
					closestMosquito = t;
				}
			}
		}
		log.debug("closestMosquito to light " + i + ":" + (closestMosquito==null) );

		// If the objective is null, calculate a new objective
		if( objective.get(i)==null ||
		(Math.abs(o.x - lightArr[i].getX()) < epsilon && Math.abs(lightArr[i].getY() - o.y) < epsilon)){
			if ( closestMosquito != null ) {
				objective.put(i, closestMosquito);
			} else {
				objective.put(i, new Tuple<Integer,Integer>(50,51));
			}
				log.debug("New objective for light " + i +": " + objective.get(i).x + "," + objective.get(i).y);
		}

		// Set the new light position based on the objective
		o=objective.get(i);
		log.debug( "Light " + i + " - Objective X:" + o.x + ", Current X:" + lightArr[i].getX() + ", Objective Y:" + o.y + ", Current Y:" + lightArr[i].getY() );
		
		if(o.y<lightArr[i].getY())
			((MoveableLight)lightArr[i]).moveUp();
		if(o.y>lightArr[i].getY())
			((MoveableLight)lightArr[i]).moveDown();
		if(o.x>lightArr[i].getX())
			((MoveableLight)lightArr[i]).moveRight();
		if(o.x<lightArr[i].getX())
			((MoveableLight)lightArr[i]).moveLeft();
	}
	
	return lights;
}

/*
* Currently this is only called once (after getLights), so you cannot
* move the Collectors.
*/
@Override
public Set<Collector> getCollectors() {
	// Position collector at center
	collectors = new HashSet<Collector>();
	Collector c = new Collector(50,51);
	log.debug("Positioned a Collector at (" + 50 + ", " + 51 + ")");
	collectors.add(c);
	return collectors;
}


private class Tuple<X,Y>
{
	public final X x;
	public final Y y;
	public Tuple(X x, Y y)
	{
		this.x=x;
		this.y=y;
	}
}

}