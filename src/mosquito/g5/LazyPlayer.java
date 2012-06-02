package mosquito.g5;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.MoveableLight;



public class LazyPlayer extends mosquito.sim.Player {

	private final double epsilon = .00001;
	private int currentMove = 0;
	private int numLights;
	private int numCollectors;
	private Point2D.Double lastLight;
	private Logger log = Logger.getLogger(this.getClass()); // for logging
	private Set<Light> lights;
	private Set<Collector> collectors;
	private Set<Line2D> walls;
	private Map<Integer,List<Tuple<Integer,Integer>>> objective;
	private Light[] lightArr;
	private boolean[][] validBoard;
	private int defaultEndpointBufferSize = 0;
	private int currentEndpointBufferSize = 0;
	private int defaultWallOffset = 0;
	private int currentWallOffset = 0;
	
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
		return "BFS Player";
	}

	
	public List<Tuple<Integer,Integer>> allPointsInLine ( Line2D line ) {
		ArrayList<Tuple<Integer,Integer>> points = new ArrayList<Tuple<Integer,Integer>>();
		double x1 = line.getX1();
		
		
		return points;
	}
	
	public boolean isLineVertical( Line2D line ) {
		return ( line.getX1() == line.getX2() );
	}

	public boolean isLineHorizontal( Line2D line ) {
		return ( line.getY1() == line.getY2() );
	}
	
	
	public boolean[][] getValidBoard(int[][] board) {
		return getValidBoard (board, this.currentWallOffset);	
	}
	
	public boolean[][] getValidBoard ( int[][] board, int offset ) {
		log.trace("Creating validBoard based on walls present");
		boolean[][] v = new boolean[100][100];
		// initialize v
		for ( int i = 0; i < 100; i++ ) {
			for ( int j = 0; j < 100; j++ ) {
				v[i][j] = true;				
			}
		}
		// Loop over all walls and add remove the board spaces they pass through from v
		for ( Line2D wall : walls ) {
			
			// If buffer is too high, mosquitoes get caught in a buffer zone
			// Also, on some mazes, the walls may be too close together
			if ( !isLineVertical(wall) ) {

				double slope = getSlope(wall);
				log.trace("Slope is " + slope + " for line (" + wall.getX1() + "," + wall.getY1() + "),(" + wall.getX2() + "," + wall.getY2() + ")" );
				double startX;
				double startY;

				// First handle X
				if ( wall.getX1() < wall.getX2() ) {
					startX = wall.getX1();
					startY = wall.getY1();
				} else {
					startX = wall.getX2();
					startY = wall.getY2();
				}

				for ( int i = 0; i < Math.abs(wall.getX1()-wall.getX2()); i++ )  {
					double currentX = startX + i;
					double currentY = startY + slope*i;
					if ( currentX >= 0 && currentX < 100 && currentY >= 0 && currentY < 100 ) {
						v[(int) currentX][(int) currentY] = false;
						
						// todo: fix this hack
						for ( int buffer = offset*(-1); buffer <= offset; buffer++ ) {
							if ( currentY-buffer >= 0 && currentY-buffer < 100  )
								v[(int) currentX][((int) currentY)-buffer] = false;
							if ( currentY+buffer >= 0 && currentY+buffer < 100 )
								v[(int) currentX][((int) currentY)+buffer] = false;
						}
						
					} else {
						log.debug("Tried to mark position (" + currentX + "," + currentY + ") but it was not valid");
					}
				}

				// Now do Y
				if ( wall.getY1() < wall.getY2() ) {
					startX = wall.getX1();
					startY = wall.getY1();
				} else {
					startX = wall.getX2();
					startY = wall.getY2();
				}

				for ( int i = 0; i < Math.abs(wall.getY1()-wall.getY2()); i++ )  {
					double currentY = startY + i;
					double currentX = startX + i/slope;
					if ( currentX >= 0 && currentX < 100 && currentY >= 0 && currentY < 100 ) {
						v[(int) currentX][(int) currentY] = false;

						// todo: fix this hack
						for ( int buffer = offset*(-1); buffer <= offset; buffer++ ) {
							if ( currentX-buffer >= 0 && currentX-buffer < 100 )
								v[((int) currentX)-buffer][(int) currentY] = false;
							if ( currentX+buffer >= 0 && currentX+buffer < 100 )
								v[((int) currentX)+buffer][(int) currentY] = false;
						}

					
					} else {
						log.debug("Tried to mark position (" + currentX + "," + currentY + ") but it was not valid");
					}
				}

			
			
			}
		}

		// print v
		printBoard(v,null);
		
		return v;
	}
	
	public void printBoard( boolean[][] b, Tuple<Integer,Integer> currentLight ) {
		for ( int i = 0; i < 100; i++ ) {
			for ( int j = 0; j < 100; j++ ) {
				if (  currentLight != null && currentLight.y == i && currentLight.x == j ) {
					System.err.print( "*" );
				} else {
					if (b[j][i]) {
						System.err.print( "." );
					} else {
						//System.err.print( "("+i+","+j+")" );
						System.err.print( "#" );
					}					
				}
			}
			System.err.println( );
		}		
	}
	
	public double getDistance( Tuple<Integer,Integer> p1, Tuple<Integer,Integer> p2 ) {
		return getDistance( p1.x, p1.y, p2.x, p2.y );		
	}

	public double getDistance ( int x1, int y1, int x2, int y2 ) {
		int a = Math.abs(x1-x2);
		int b = Math.abs(y1-y2);
		int cSquared = (int) (Math.pow(a, 2) + Math.pow(b, 2));
		return Math.pow(cSquared, .5);
	}

	public int getManhattanDistance ( Tuple<Integer,Integer> p1, Tuple<Integer,Integer> p2 ) {
		return getManhattanDistance( p1.x, p1.y, p2.x, p2.y );
	}

	public int getManhattanDistance ( int x1, int y1, int x2, int y2 ) {
		int a = Math.abs(x1-x2);
		int b = Math.abs(y1-y2);
		return a+b;
	}
	
	public int wallCount( boolean[][] b ) {
		int count = 0;
		for ( int i = 0; i < 100; i++ ) {
			for ( int j = 0; j < 100; j++ ) {
				if ( !b[i][j] ) {
					count++;
				}
			}
		}
		return count;
	}
	

	public Tuple<Integer,Integer> getClosestInSight( int[][] mosquitoBoard, Tuple<Integer,Integer> currentLightPosition ) {
		
		// clone valid board
		boolean[][] bfsBoard = new boolean[100][100];
		for ( int i = 0; i < 100; i++ ) {
			for ( int j = 0; j < 100; j++ ) {
				bfsBoard[i][j] = this.validBoard[i][j];
			}
		}
		
		return getClosestInSight(mosquitoBoard,bfsBoard,currentLightPosition);	
	}
	
	
	public Tuple<Integer,Integer> getClosestInSight( int[][] mosquitoBoard, boolean[][] gameBoard, Tuple<Integer,Integer> currentLightPosition ) {
		
		LinkedList<Tuple<Integer,Integer>> q = new LinkedList<Tuple<Integer,Integer>>();
		
		q.add( currentLightPosition );
		gameBoard[ currentLightPosition.x ][ currentLightPosition.y ] = false;
		
		while ( !q.isEmpty() ) {
			Tuple<Integer,Integer> t = q.poll();
			//log.debug(t + ", mosquitoes:"+ mosquitoBoard[t.x][t.y] + ", distance:" + getDistance(t,currentLightPosition) + ", captured? " + isPositionCaptured(t));
			//log.debug(mosquitoBoard[t.x][t.y] > 0 && getDistance(t,currentLightPosition) > 20 && !isPositionCaptured(t));
			if ( mosquitoBoard[t.x][t.y] > 0 
					&& getDistance(t,currentLightPosition) > 20 
					&& !isPositionCaptured(t) ) {
				log.trace("Closest mosquito is at " + t);
				return t;
			} else {
				// Add adjacent moves to queue
				HashSet<Tuple<Integer,Integer>> neighbors = getNeighborNodes(t,gameBoard);
				//log.debug("Neighbors of " + t + ":" + neighbors.size());
				for ( Tuple<Integer,Integer> neighbor : neighbors ) {
					//log.debug(neighbor);
					gameBoard[neighbor.x][neighbor.y]=false;
					q.add( neighbor );
				}
			}
		}

		printBoard(gameBoard, currentLightPosition);
		return null;		
	}

	public Tuple<Integer,Integer> getClosest( int[][] mosquitoBoard, Tuple<Integer,Integer> currentLightPosition ) {
		//create empty board, ignoring walls
		boolean[][] emptyBoard = new boolean[100][100];
		for ( int i = 0; i<100; i++ ) {
			for ( int j = 0; j<100; j++ ) {
				emptyBoard[i][j] = true;
			}			
		}
		return getClosestInSight(mosquitoBoard,emptyBoard,currentLightPosition);
	}


	public int heuristicCostEstimate(Tuple<Integer,Integer> start, Tuple<Integer,Integer> goal) {
		// Manhattan distance
		return Math.abs(start.x-goal.x) + Math.abs(start.y-goal.y);
	}

	/*
	 * The idea is to give the endpoints of lines plenty of room
	 * so as not to lose mosquitoes
	 */
	public boolean nearEndpoints( Tuple<Integer,Integer> node ) {
		int room = this.currentEndpointBufferSize;
		for(Line2D wall : this.walls)
		{
			if ( (Math.abs(node.x - wall.getP1().getX()) + Math.abs(node.y - wall.getP1().getY()) < room) || (Math.abs(node.x - wall.getP2().getX()) + Math.abs(node.y - wall.getP2().getY()) < room ))  {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isValidSpace (Tuple<Integer,Integer> t) {
		return (t.x>=0 && t.x<100 && t.y>=0 && t.y<100);
	}
	
	
	public HashSet<Tuple<Integer,Integer>> getNeighborNodes( Tuple<Integer,Integer> node, boolean[][] gameBoard ) {
		HashSet<Tuple<Integer,Integer>> neighbors = new HashSet<Tuple<Integer,Integer>>();
		for ( int x = -1; x <= 1; x++ ) {
			for ( int y = -1; y <=1; y++ ) {
				// exclude diagonals
//				if ( x==0 || y==0 ) {
					Tuple<Integer,Integer> neighbor = new Tuple<Integer,Integer>(node.x+x,node.y+y);
					if ( isValidSpace(neighbor) && !(x==0 && y==0) 
							&& gameBoard[neighbor.x][neighbor.y] 
							&& ( !nearEndpoints(neighbor) ) ) {
						neighbors.add(neighbor);
					}
//				}
			}
		}
		return neighbors;
	}

	public HashSet<Tuple<Integer,Integer>> getNeighborNodes( Tuple<Integer,Integer> node ) {
		return getNeighborNodes( node, this.validBoard );
	}

	

	public List<Tuple<Integer,Integer>> reconstructPath ( HashMap<Tuple<Integer,Integer>,Tuple<Integer,Integer>> cameFrom, Tuple<Integer,Integer> currentNode ) {
		//log.trace("reconstructPath: cameFrom size:" + cameFrom.size() );
		List<Tuple<Integer,Integer>> path = new ArrayList<Tuple<Integer,Integer>>();
		if ( cameFrom.containsKey(currentNode) ) {
			path = reconstructPath( cameFrom, cameFrom.get(currentNode));
			path.add(currentNode);
			// experimenting -- stay still near endpoints
			/*
			if ( nearEndpoints(currentNode) ) {
				for ( int i = 0; i < 5; i++ ) {
					path.add(currentNode);
				}
			}
			*/
			return path;
		} else {
			path.add(currentNode);
			// experimenting -- stay still near endpoints
			/*
			if ( nearEndpoints(currentNode) ) {
				for ( int i = 0; i < 5; i++ ) {
					path.add(currentNode);
				}
			}
			*/
			return path;
		}
	}


	/*
	 * aStar
	 * Taken from http://en.wikipedia.org/wiki/A*_search_algorithm
	 */
	public List<Tuple<Integer,Integer>> aStar ( Tuple<Integer,Integer> start, Tuple<Integer,Integer> goal) {
		log.debug("A* is looking for a path from " + start + " to " + goal);
		// Set closedSet = empty set
		HashSet<Tuple<Integer,Integer>> closedSet = new HashSet<Tuple<Integer,Integer>>();

		// The set of tentative nodes to be evaluated
		HashSet<Tuple<Integer,Integer>> openSet = new HashSet<Tuple<Integer,Integer>>();
		// initially containing the start node
		openSet.add(start);

		// The map of navigated nodes.
		// Initially set to empty map
		HashMap<Tuple<Integer,Integer>,Tuple<Integer,Integer>> cameFrom = new HashMap<Tuple<Integer,Integer>,Tuple<Integer,Integer>>();


		HashMap<Tuple<Integer,Integer>,Integer> g_score = new HashMap<Tuple<Integer,Integer>,Integer>();
		g_score.put(start,0); // Cost from start along best known path.

		// Estimated total cost from start to goal through y.
		HashMap<Tuple<Integer,Integer>,Integer> f_score = new HashMap<Tuple<Integer,Integer>,Integer>();
		f_score.put(start, g_score.get(start) + heuristicCostEstimate(start, goal));

		while ( !openSet.isEmpty() ) {
			// current := the node in openset having the lowest f_score[] value
			int lowestF = Integer.MAX_VALUE;
			Tuple<Integer,Integer> current = null;
			for ( Tuple<Integer,Integer> node : openSet ) {
				if ( f_score.get(node) < lowestF ) {
					lowestF = f_score.get(node);
					current = node;
				}
			}
			if  ( current.equals(goal) ) {
				List<Tuple<Integer,Integer>> path = reconstructPath(cameFrom, goal);
				for ( Tuple<Integer,Integer> point : path ) {
					//log.debug(point + " is valid: " + this.validBoard[point.x][point.y]);
				}
				return reconstructPath(cameFrom, goal);
			}

			//remove current from openset
			openSet.remove(current);

			//add current to closedset
			closedSet.add(current);


			HashSet<Tuple<Integer,Integer>> neighbor_nodes = getNeighborNodes(current);
//			log.trace("Neighboring nodes: " + neighbor_nodes.size());
			// for each neighbor in neighbor_nodes(current)
			for ( Tuple<Integer,Integer> neighbor : neighbor_nodes ) {
				// if neighbor in closedset
				if ( closedSet.contains(neighbor) ) 
					continue;

				int tentative_g_score = g_score.get(current) + 1;

//				log.trace("Open set size:" + openSet.size());
				if ( !openSet.contains(neighbor) || tentative_g_score < g_score.get(neighbor) ) { 
					// add neighbor to openset
//					log.trace("adding a neighbor to openSet with tentative_g_score" + tentative_g_score);
					openSet.add(neighbor);
					cameFrom.put(neighbor, current);
					g_score.put(neighbor, tentative_g_score);
					f_score.put(neighbor, g_score.get(neighbor) + heuristicCostEstimate(neighbor, goal));
				}
			}
		}

		// Failure
		log.error("A* is returning null! This should never happen!");
		return null;
	}

	public List<Tuple<Integer,Integer>> leastEncumbered( int[][] mosqBoard ) {
		List<Tuple<Integer,Integer>> rank = new ArrayList();
		boolean[][] board = getValidBoard(mosqBoard, 0 );
		int[][] quadrantCount = new int[2][2];

		Tuple<Integer,Integer> corner1 = new Tuple<Integer,Integer>(0,0);
		
		for ( int i = 0; i<2; i++ ) {
			for ( int j = 0; j<2; j++ ) {
				int wallCount = 0;
				for ( int x = i*50; x< 50*(i+1); x++ ) {
					for ( int y = j*50; y< 50*(j+1); y++ ) {
						if ( !board[x][y] ) {
							wallCount++;
						}
					}
				}
				quadrantCount[i][j] = wallCount;
			}
		}

		int largest = Integer.MIN_VALUE;
		for ( int i = 0; i<2; i++ ) {
			for ( int j = 0; j<2; j++ ) {
				if ( quadrantCount[i][j] > largest ) {
					largest = i+j;
				}
			}
		}
		rank.add(e)
		
		
		return null;
	}


	/*
	 * This is used to determine the initial placement of the lights.
	 * It is called after startNewGame.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> getLights(int[][] board) {
		// Initially place the lights randomly, and put the collector next to the last light

		this.validBoard = getValidBoard(board);

		lights = new HashSet<Light>();
		objective = new HashMap<Integer,List<Tuple<Integer,Integer>>>();
		lightArr = new Light[numLights];
		Random r = new Random();


		for(int i=0; i<numLights;i++)
		{

			Tuple<Integer,Integer> startingPoint = null;

			// this player just picks random points for the Light
			lastLight = new Point2D.Double(r.nextInt(100), r.nextInt(100));
			startingPoint = new Tuple<Integer,Integer>((int) lastLight.x, (int) lastLight.y);
			
			// Make sure we're not starting out on a line or in a buffer zone
			while ( !this.validBoard[startingPoint.x][startingPoint.y] ) {
				lastLight = new Point2D.Double(r.nextInt(100), r.nextInt(100));
				startingPoint = new Tuple<Integer,Integer>((int) lastLight.x, (int) lastLight.y);
			}
			
			lastLight = new Point2D.Double(startingPoint.x, startingPoint.y);
			MoveableLight l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
			log.trace("Positioned a light at (" + lastLight.getX() + ", " + lastLight.getY() + ")");
			lights.add(l);
			lightArr[i]=l;

			objective.put(i, null);
		}

		return lights;
	}

	private static double getSlope(Line2D l) {
		// Adjustment for the fact that (0,0) is in the upper left
		//double y1 = 100-l.getY1();
		//double y2 = 100-l.getY2();
		double y1 = l.getY1();
		double y2 = l.getY2();
		return ((y1-y2)/(l.getX1()-l.getX2()));
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
	

	/*
	 * isPositionCaptured
	 * Takes a tuple and checks to see if the board position is already within the radius of a light
	 * 
	 */
	public boolean isPositionCaptured( Tuple<Integer,Integer> t ) {
		// Check to make sure item isn't within the radius of another light (already captured)
		// This check doesn't seem that helpful, time-wise
		for (int i = 0; i < numLights; i++) {
			if ( getDistance(t, new Tuple<Integer,Integer>( (int) lightArr[i].getX(), (int) lightArr[i].getY()) ) < 20 ) {
				return true;
			}
		}
		log.debug("Position is not captured!");
		return false;
	}
	

	public boolean allMosquitoesCaptured(int[][] board) {
		boolean captured = true;
		for ( int i = 0; i < 100; i++ ) {
			for ( int j = 0; j < 100; j++ ) { 
				if ( board[i][j] > 0 ) {
					boolean capturedByLight = false;
					for ( Light light : lights ) {
						double x = Math.pow(i-light.getX(),2);
						double y = Math.pow(j-light.getY(),2);
						double distance = Math.pow(x+y,.5);
						if( distance < 20 ) {
							capturedByLight = true;
						}
					}
					if ( !capturedByLight ) {
						captured = false;
					}
				}
			}
		}
		return captured;
	}
	

	/*
	 * This is called at the beginning of each step (before the mosquitoes have moved)
	 * If your Set contains additional lights, an error will occur.
	 * Also, if a light moves more than one space in any direction, an error will occur.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> updateLights(int[][] board) {		
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

			//return (i+1)*100+j;
			return 1;
		}
	}

}