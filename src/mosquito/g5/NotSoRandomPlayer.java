package mosquito.g5;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.MoveableLight;



public class NotSoRandomPlayer extends mosquito.sim.Player {

	private final double epsilon = .00001;

	private int numLights;
	private int numCollectors;
	private Point2D.Double lastLight;
	private Logger log = Logger.getLogger(this.getClass()); // for logging
	Map<Integer, HashSet<Tuple<Integer,Integer>>> sectors;
	
	private Set<Light> lights;
	private Set<Collector> collectors;
	private Set<Line2D> walls;
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
		this.walls = walls;
	}
	
	@Override
	public String getName() {
		return "Not So Random Player";
	}



	/*
	 * This is used to determine the initial placement of the lights.
	 * It is called after startNewGame.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> getLights(int[][] board) {
		// Initially place the lights randomly, and put the collector next to the last light

		lights = new HashSet<Light>();
		objective = new HashMap<Integer,Tuple<Integer,Integer>>();
		lightArr = new Light[numLights];
		Random r = new Random();
		Set<Integer> sectorsTaken = new HashSet<Integer>();

		for(int i=0; i<numLights;i++)
		{

			int mostMosquitoes = 0;
			int sectorWithMostMosquitoes = 1; // Defaults to 1
			Tuple<Integer,Integer> startingPoint = new Tuple<Integer,Integer>(1,1);
			sectors = getSectors();
			log.trace("Number of sectors is: " + sectors.size());
			for ( int j = 0; j < sectors.size(); j++ ) {
				if ( !sectorsTaken.contains(j) ) {
					int mosquitoCount = 0;
					int tupleCount = 0;
					Tuple lastTupleChecked = new Tuple<Integer,Integer>(1,1);
					for ( Tuple<Integer,Integer> t : sectors.get(j) ) {
						tupleCount++;
						mosquitoCount += board[t.x][t.y];
					}
					log.trace("Spaces in sector " + j + ":" + tupleCount);
					log.trace("Mosquitoes in sector " + j + ":" + mosquitoCount);
					
					if ( mosquitoCount > mostMosquitoes ) {
						mostMosquitoes = mosquitoCount;
						sectorWithMostMosquitoes = j;
						// arbitrarily place the light in the last spot we checked
						startingPoint = lastTupleChecked;
						sectorsTaken.add(j);
					}
				}
			}

			
			//int x = startingPoint.x;
			lastLight = new Point2D.Double(startingPoint.x, startingPoint.y);
			MoveableLight l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
			log.trace("Positioned a light at (" + lastLight.getX() + ", " + lastLight.getY() + ")");
			lights.add(l);
			lightArr[i]=l;
			objective.put(i, null);
		}
		return lights;
	}

	private static double getSlope(Line2D l)
	{
		Point2D p1 = l.getP1();
		Point2D p2 = l.getP2();
		if(p1.getX() > p2.getX())
		{
			p1 = p2;
			p2 = l.getP1();
		}
		return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX()) ;
	}
	
	public boolean intersectsLines(double x1, double y1, double x2, double y2)
	{
		return intersectsLines(new Line2D.Double(x1,y1,x2,y2));
	}
	
	public boolean intersectsLines(Line2D l)
	{
		for(Line2D x : this.walls)
		{
			if (x.intersectsLine(l))
				return true;
		}
		return false;
	}
	
	public HashMap<Integer, HashSet<Tuple<Integer,Integer>>> getSectors()
	{
		HashMap<Integer, HashSet<Tuple<Integer,Integer>>> m =
				new HashMap<Integer, HashSet<Tuple<Integer,Integer>>>();
		//FOR EACH LINE, check angle, make sector vert or horiz based on angle.
		for(Line2D l : this.walls)
		{
			Point2D p1 = l.getP1();
			Point2D p2 = l.getP2();
			if(p1.getX() > p2.getX())
			{
				p1 = p2;
				p2 = l.getP1();
			}
			double slope = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
			if(slope > 1 || slope < -1)
			{
				//hori 
				HashSet<Tuple<Integer,Integer>> leftSet = new HashSet<Tuple<Integer,Integer>>();
				HashSet<Tuple<Integer,Integer>> rightSet = new HashSet<Tuple<Integer,Integer>>();
				for(int y = (int)(p1.getY()); y < p2.getY(); y++)
				{
					for(int x  = (int)((y - p1.getY())/slope ); 
							x < 100 && !intersectsLines((y - p1.getY())/slope,y,x,y); x++)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						rightSet.add(t);
					}
					for(int x  = (int)((y - p1.getY())/slope ); 
							x > 0 && !intersectsLines((y - p1.getY())/slope,y,x,y); x--)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						leftSet.add(t);
					}
				}
				m.put(m.size(), leftSet);
				m.put(m.size(), rightSet);

			}
			else
			{
				//vert
				HashSet<Tuple<Integer,Integer>> upSet = new HashSet<Tuple<Integer,Integer>>();
				HashSet<Tuple<Integer,Integer>> downSet = new HashSet<Tuple<Integer,Integer>>();
				for(int x = (int)(p1.getX()); x < p2.getX(); x++)
				{
					for(int y  = (int)(x * slope + p1.getY()); 
							y < 100 && !intersectsLines(x,(x * slope + p1.getY()),x,y); y++)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						downSet.add(t);
					}
					for(int y  = (int)(x * slope + p1.getY()); 
							y > 0 && !intersectsLines(x,(x * slope + p1.getY()),x,y); y--)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						upSet.add(t);
					}
				}
				m.put(m.size(), downSet);
				m.put(m.size(), upSet);
			}
		}

		HashSet<Tuple<Integer,Integer>> s = new HashSet<Tuple<Integer,Integer>>();
		for(int j = 0; j<100; j++)
		{
			for(int k = 0; k<100; k++)
			{
				Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(j,k);
				s.add(t);
			}
		}
		for( Integer in : m.keySet())
		{
			HashSet<Tuple<Integer,Integer>> v = m.get(in);
			s.removeAll(v);
		}
		while(!s.isEmpty()){
			Queue<Tuple<Integer, Integer>> q = new LinkedList<Tuple<Integer,Integer>>();
			q.add(s.iterator().next());
			HashSet<Tuple<Integer,Integer>> x = new HashSet<Tuple<Integer,Integer>>();
			while(!q.isEmpty()){
				Tuple<Integer,Integer> t =q.poll();
				x.add(t);
				s.remove(t);
				Tuple<Integer,Integer> t2 = new Tuple<Integer,Integer>(t.x+1,t.y);
				if(s.contains(t2))
				{
					x.add(t2);
					s.remove(t2);
				}
				t2 = new Tuple<Integer,Integer>(t.x-1,t.y);
				if(s.contains(t2))
				{
					x.add(t2);
					s.remove(t2);
				}
				t2 = new Tuple<Integer,Integer>(t.x,t.y+1);
				if(s.contains(t2))
				{
					x.add(t2);
					s.remove(t2);
				}
				t2 = new Tuple<Integer,Integer>(t.x+1,t.y-1);
				if(s.contains(t2))
				{
					x.add(t2);
					s.remove(t2);
				}
			}
			m.put(m.size(),x);
		}
		
		m.put(m.size(), s);

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


			// If the objective is null, calculate a new objective
			if( objective.get(i)==null ||
					(Math.abs(o.x - lightArr[i].getX()) < epsilon && Math.abs(lightArr[i].getY() - o.y) < epsilon)){

				double shortestDistance = 9999;
				Tuple<Integer,Integer> boardSpace = null;
				for(Tuple<Integer,Integer> t : sectors.get(i))
				{
					double x = Math.pow(t.x-lightArr[i].getX(),2);
					double y = Math.pow(t.y-lightArr[i].getY(),2);
					double distance = Math.pow(x+y,.5);
					if(distance > 20 && distance < shortestDistance && board[t.x][t.y] > 0)
					{

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
							shortestDistance=distance;
							boardSpace = t;
						}					
					}
				}
				// If we found a valid boardSpace for the closest mosquito, set that as the new objective
				// Otherwise, move to the collector
				if ( boardSpace!=null) {
					objective.put(i, boardSpace);
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
		// this one just places a collector next to the last light that was added
		collectors = new HashSet<Collector>();
		//Collector c = new Collector(lastLight.getX()+1,lastLight.getY() +1);
		//log.debug("Positioned a Collector at (" + (lastLight.getX()+1) + ", " + (lastLight.getY()+1) + ")");
		Collector c = new Collector(50,51);
		//Collector c = new Collector(2,2);
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
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) 
		{
			if(x.equals(((Tuple<X,Y>)obj).x) && y.equals(((Tuple<X,Y>)obj).y))
			{
				return true;
			}
			return false;
		}
	}

}