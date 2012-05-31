package mosquito.g5;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
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
	private Set<Integer> assignedSectors; // Stores sectors currently occupied by a light
	private Set<Light> lights;
	private Set<Collector> collectors;
	private Set<Line2D> walls;
	private Map<Integer,Tuple<Integer,Integer>> objective;
	private Light[] lightArr;
	private int[] sectorArr;
	
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

	
	public int getMosquitoesInSector(int s, int[][] board) {
		int mosquitoCount = 0;
		for ( Tuple<Integer,Integer> t : sectors.get(s) ) {
			mosquitoCount += board[t.x][t.y];
		}
		return mosquitoCount;
	}

	/*
	 * aStar
	 * Taken from http://en.wikipedia.org/wiki/A*_search_algorithm
	 */

/*	
	public Tuple<Integer,Integer> aStar ( Tuple<Integer,Integer> start, Tuple<Integer,Integer> goal) {
		// Set closedSet = empty set
		HashSet<Tuple<Integer,Integer>> closedSet = new HashSet<Tuple<Integer,Integer>>();

		// The set of tentative nodes to be evaluated
		HashSet<Tuple<Integer,Integer>> openSet = new HashSet<Tuple<Integer,Integer>>();
		// initially containing the start node
		openSet.add(start);

		// The map of navigated nodes.
		// Initially set to empty map
		HashMap<Integer,Tuple<Integer,Integer>> cameFrom = new HashMap<Integer,Tuple<Integer,Integer>>();

 
     
		g_score[start] := 0    // Cost from start along best known path.
     // Estimated total cost from start to goal through y.
     f_score[start] := g_score[start] + heuristic_cost_estimate(start, goal)
 
     while ( !openSet.isEmpty() ) {
         current := the node in openset having the lowest f_score[] value
         if current = goal
             return reconstruct_path(came_from, goal)
 
		 //remove current from openset
		 openSet.remove(current);
         
         //add current to closedset
     	 closedSet.add(current);

     	 // for each neighbor in neighbor_nodes(current)
     	 for ( Tuple<Integer,Integer> neighbor : neighbor_nodes(current) ) {
            // if neighbor in closedset
     		if ( closedSet.contains(neighbor)) 
                 continue;
            tentative_g_score := g_score[current] + dist_between(current,neighbor)
 
            if ( !openSet.contains(neighbor) || tentative_g_score < g_score[neighbor] ) { 
                 // add neighbor to openset
            	 openSet.add(neighbor);
                 came_from[neighbor] := current
                 g_score[neighbor] := tentative_g_score
                 f_score[neighbor] := g_score[neighbor] + heuristic_cost_estimate(neighbor, goal)
            }
     }
 
     // Failure
     return new Tuple<Integer,Integer>(-1,-1);
}
*/	
	
	/*
	 * This is used to find the sector containing the most mosquitoes
	 * excepting sectors that have already been assigned to other lights
	 */
	public int getSectorWithMostMosquitoes( int[][] board ) {

		int mostMosquitoes = 0;
		int sectorWithMostMosquitoes = 0; // Defaults to 0
		Tuple<Integer,Integer> startingPoint = new Tuple<Integer,Integer>(1,1);
		sectors = getSectors();
		log.trace("Number of sectors is: " + sectors.size());
		for ( int j = 0; j < sectors.size(); j++ ) {
			if ( !assignedSectors.contains(j) ) {
				int mosquitoCount = 0;
				int tupleCount = 0;
				Tuple lastTupleChecked = new Tuple<Integer,Integer>(1,1);
				mosquitoCount = getMosquitoesInSector(j, board);
				log.trace("Spaces in sector " + j + ":" + tupleCount);
				log.trace("Mosquitoes in sector " + j + ":" + mosquitoCount);
				
				if ( mosquitoCount > mostMosquitoes ) {
					mostMosquitoes = mosquitoCount;
					sectorWithMostMosquitoes = j;
					// arbitrarily place the light in the last spot we checked
					startingPoint = lastTupleChecked;
				}
			}
		}
		return sectorWithMostMosquitoes;
	}

	/*
	 * Sure, it says random, but really it just returns the first tuple
	 */
	public Tuple<Integer,Integer> getRandomSpaceInSector ( HashSet<Tuple<Integer,Integer>> s) {
		Tuple<Integer,Integer> space = null;
		for ( Tuple<Integer,Integer> t : s ) {
			space = t;
			break;
		}
		return space;
	}
	

	/*
	 * This is used to determine the initial placement of the lights.
	 * It is called after startNewGame.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> getLights(int[][] board) {
		// Initially place the lights randomly, and put the collector next to the last light

		assignedSectors = new HashSet<Integer>();
		lights = new HashSet<Light>();
		objective = new HashMap<Integer,Tuple<Integer,Integer>>();
		lightArr = new Light[numLights];
		sectorArr = new int [numLights];
		Random r = new Random();
		Set<Integer> sectorsTaken = new HashSet<Integer>();

		for(int i=0; i<numLights;i++)
		{

			int mostMosquitoes = 0;
			int sectorWithMostMosquitoes = getSectorWithMostMosquitoes(board);
			assignedSectors.add(sectorWithMostMosquitoes);

			Tuple<Integer,Integer> startingPoint = null;
			HashSet<Tuple<Integer,Integer>> sector = getSectors().get(sectorWithMostMosquitoes);
			for ( Tuple<Integer,Integer> t : sector ) {
				startingPoint = t;
				break; // Arbitrarily just start in the first tuple of the sector
			}


			//int x = startingPoint.x;
			lastLight = new Point2D.Double(startingPoint.x, startingPoint.y);
			MoveableLight l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
			log.trace("Positioned a light at (" + lastLight.getX() + ", " + lastLight.getY() + ")");
			lights.add(l);
			lightArr[i]=l;
			sectorArr[i]=sectorWithMostMosquitoes;
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
			double slope = -1 * ( (p2.getY() - p1.getY()) / (p2.getX() - p1.getX()) );
			if(slope > 1 || slope < -1)
			{
				//hori 
				if(p1.getY() > p2.getY())
				{
					Point2D.Double t = (Double) p1;
					p1 = p2;
					p2 = t;
				}

				HashSet<Tuple<Integer,Integer>> leftSet = new HashSet<Tuple<Integer,Integer>>();
				HashSet<Tuple<Integer,Integer>> rightSet = new HashSet<Tuple<Integer,Integer>>();
				for(int y = (int)(p1.getY()); y < p2.getY(); y++)
				{
					for(int x  = (int)(((y - p1.getY())/slope + p1.getX()) +.5 ); 
							x < 100 && !intersectsLines(((y - p1.getY())/slope + p1.getX()) +.5,y,x,y); x++)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						rightSet.add(t);
					}
					for(int x  = (int)(((y - p1.getY())/slope + p1.getX()) -.5 ); 
							x > 0 && !intersectsLines(((y - p1.getY())/slope + p1.getX()) -.5,y,x,y); x--)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						leftSet.add(t);
					}
				}
				if(leftSet.size()>0)
					m.put(m.size(), leftSet);
				if(rightSet.size()>0)
					m.put(m.size(), rightSet);

			}
			else
			{
				//vert
				HashSet<Tuple<Integer,Integer>> upSet = new HashSet<Tuple<Integer,Integer>>();
				HashSet<Tuple<Integer,Integer>> downSet = new HashSet<Tuple<Integer,Integer>>();
				for(int x = (int)(p1.getX()); x < p2.getX(); x++)
				{
					for(int y  = (int)(x * slope + p1.getY() +.5); 
							y < 100 && !intersectsLines(x,(x * slope + p1.getY() +.5),x,y); y++)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						downSet.add(t);
					}
					for(int y  = (int)(x * slope + p1.getY() -.5); 
							y > 0 && !intersectsLines(x,(x * slope + p1.getY() -.5),x,y); y--)
					{
						//add (x,y) to the sector
						Tuple<Integer,Integer> t = new Tuple<Integer, Integer>(x,y);
						upSet.add(t);
					}
				}
				if(downSet.size() > 0)
					m.put(m.size(), downSet);
				if(downSet.size() > 0)
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
			s.remove(q.peek());
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
					int sector = getSectorWithMostMosquitoes(board);
					assignedSectors.remove(sectorArr[i]);
					if ( getMosquitoesInSector(sector, board) > 0 ) {
						assignedSectors.add(sector);
						sectorArr[i]=sector;
						objective.put(i, getRandomSpaceInSector(sectors.get(sector)));
					} else {
						// Go to collector
						objective.put(i, new Tuple<Integer,Integer>(50,51));
					}
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

		public String toString(){
			return "<" + x + ", " + y + ">";
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof Tuple<?,?>) {

				if(x.equals(((Tuple<X,Y>)obj).x) && y.equals(((Tuple<X,Y>)obj).y))
				{
					return true;
				}
			}
			return false;
		}
		@Override
		public int hashCode()
		{
			String s = toString();
			s = s.substring(1,s.length()-1);
			int i = Integer.parseInt(s.substring(0,s.indexOf(",")));
			int j = Integer.parseInt(s.substring(s.indexOf(",")+2, s.length()));

			return (i+1)*100+j;
		}
	}

}