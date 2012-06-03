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



public class BFSPlayer extends mosquito.sim.Player {

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
	private int[] failedMoves;
	private boolean[][] validBoard;
	private int defaultEndpointBufferSize = 1;
	private int currentEndpointBufferSize = 1;
	private double defaultWallOffset = .1;
	private double currentWallOffset = .1;
	
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

	
	public Tuple<Integer,Integer> pointToTuple ( Point2D point ) {
		return new Tuple<Integer,Integer>( (int) point.getX(), (int) point.getY() );
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
	
	public boolean[][] getValidBoard ( int[][] board ) {
		return getValidBoard(board,this.currentWallOffset);
	}
	
	public boolean[][] getValidBoard ( int[][] board, double offset ) {
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
						for ( double buffer = offset*(-1); buffer <= offset; buffer++ ) {
							if ( currentY-buffer >= 0 && currentY-buffer < 100  )
								v[(int) currentX][(int) (currentY-buffer)] = false;
							if ( currentY+buffer >= 0 && currentY+buffer < 100 )
								v[(int) currentX][(int) (currentY+buffer)] = false;
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
						for ( double buffer = offset*(-1); buffer <= offset; buffer++ ) {
							if ( currentX-buffer >= 0 && currentX-buffer < 100 )
								v[(int) (currentX-buffer)][(int) currentY] = false;
							if ( currentX+buffer >= 0 && currentX+buffer < 100 )
								v[(int) (currentX+buffer)][(int) currentY] = false;
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
	

	public int getMosquitoesCaptured( Light light, int[][] board ) {
		int count = 0;
		// I'm just using a 5x5 square for now because it is simple
		// Also, if they are more than 5 out, they may technically "belong" to another light
		for ( int i = (int) light.getX()-10; i<(int) light.getX()+10; i++ ) {
			for ( int j = (int) light.getX()-10; j<(int) light.getX()+10; j++ ) {
				if ( i>=0 && j>=0 && i<100 && j<100 ) {
					count = count + board[i][j];
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
		return nearEndpoints(node, this.currentEndpointBufferSize );
	}
		
	public boolean nearEndpoints( Tuple<Integer,Integer> node, double endpointBufferSize ) {
		double room = endpointBufferSize;
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
				if ( x==0 || y==0 ) {
					Tuple<Integer,Integer> neighbor = new Tuple<Integer,Integer>(node.x+x,node.y+y);
					if ( isValidSpace(neighbor) && !(x==0 && y==0) 
							&& gameBoard[neighbor.x][neighbor.y] 
							&& ( !nearEndpoints(neighbor) ) ) {
						neighbors.add(neighbor);
					}
				}
			}
		}
		return neighbors;
	}

	public HashSet<Tuple<Integer,Integer>> getNeighborNodes( Tuple<Integer,Integer> node ) {
		return getNeighborNodes( node, this.validBoard );
	}

	

	public List<Tuple<Integer,Integer>> reconstructPath ( HashMap<Tuple<Integer,Integer>,Tuple<Integer,Integer>> cameFrom, Tuple<Integer,Integer> currentNode ) {
		int delayNearEndpoints = 10;
		int roomNearEndpoints = 5;
		
		//log.trace("reconstructPath: cameFrom size:" + cameFrom.size() );
		List<Tuple<Integer,Integer>> path = new ArrayList<Tuple<Integer,Integer>>();
		if ( cameFrom.containsKey(currentNode) ) {
			path = reconstructPath( cameFrom, cameFrom.get(currentNode));
			path.add(currentNode);
			// experimenting -- stay still near endpoints
			
			if ( nearEndpoints(currentNode, roomNearEndpoints) ) {
				for ( int i = 0; i < delayNearEndpoints; i++ ) {
					path.add(currentNode);
				}
			}
			
			return path;
		} else {
			path.add(currentNode);
			// experimenting -- stay still near endpoints
			
			if ( nearEndpoints(currentNode,roomNearEndpoints) ) {
				for ( int i = 0; i < delayNearEndpoints; i++ ) {
					path.add(currentNode);
				}
			}
			
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
				/*
				for ( Tuple<Integer,Integer> point : path ) {
					//log.debug(point + " is valid: " + this.validBoard[point.x][point.y]);
				}
				*/
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
		this.failedMoves = new int[numLights];
		Random r = new Random();

/* four corners config */
		lastLight = new Point2D.Double(5.0, 0.0);
		MoveableLight l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
		lights.add(l);
		lightArr[0]=l;
		
		lastLight = new Point2D.Double(94.0, 0.0);
		l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
		lights.add(l);
		lightArr[1]=l;
		
		lastLight = new Point2D.Double(0.0, 94.0);
		l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
		lights.add(l);
		lightArr[2]=l;
		
		lastLight = new Point2D.Double(94.0, 94.0);
		l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
		lights.add(l);
		lightArr[3]=l;
		
		lastLight = new Point2D.Double(51.0, 51.0);
		l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);
		lights.add(l);
		lightArr[4]=l;

		
		/*
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
			this.failedMoves[i]=0;

			objective.put(i, null);
		}
*/

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
		currentMove++;
		
		// If all mosquitoes captured, make sure they are all heading to a collector
		if ( allMosquitoesCaptured(board) ) {
			for(int i = 0; i < numLights; i++) {
				// Retrieve the objective (destination) for the current light
				List<Tuple<Integer,Integer>> o = objective.get(i);
				// Find the destination (last list item)
				Tuple<Integer,Integer> destination;
				if ( o!=null && !o.isEmpty()  ) {
					destination = o.get(o.size()-1);
				} else {
					// Set an impossible destination
					destination = new Tuple<Integer,Integer>(-1,-1);
				}
				// Here we could loop over the collectors
				// Although in this case, we know there is just one
				Tuple<Integer,Integer> collectorPosition = new Tuple<Integer,Integer>(50,51);
				if ( !destination.equals(collectorPosition) ) {
					// Find path to collector using A*
					Tuple<Integer,Integer> currentLightPosition = new Tuple<Integer,Integer>( (int) lightArr[i].getX() ,(int) lightArr[i].getY() );
					o = aStar(currentLightPosition,collectorPosition);
					objective.put(i, o);
				}
			}
		}

		for(int i = 0; i < numLights; i++)
		{
			// Retrieve the objective (destination) for the current light
			List<Tuple<Integer,Integer>> o = objective.get(i);

			if ( o!=null ) {
				log.debug(i+":"+"o: "+ o + " " + o.size());
			}
			
			// If the objective is null, calculate a new objective
			if ( o==null || o.isEmpty() ) {

				
				Tuple<Integer,Integer> currentLightPosition = new Tuple<Integer,Integer>( (int) lightArr[i].getX(), (int) lightArr[i].getY());
				// Place these in ints so value does not change
				int currentX = currentLightPosition.x;
				int currentY = currentLightPosition.y;
				Tuple<Integer,Integer> t = getClosestInSight(board,currentLightPosition);

				if ( t == null ) {
					log.trace("Failed to find anything in sight from " + currentLightPosition);
					//t = getClosest(board,currentLightPosition);
					// Idea:
					// Iterate over endpoints
					List<Tuple<Integer,Integer>> endpointList = new ArrayList<Tuple<Integer,Integer>>();
					for ( Line2D wall : walls ) {
						endpointList.add( pointToTuple(wall.getP1()) );
						endpointList.add( pointToTuple(wall.getP2()) );
					}
					double shortestDistance = Integer.MAX_VALUE;
					// Find distance to endpoints
					for ( Tuple<Integer,Integer> endpoint : endpointList ) {
						// This distance is not good enough--we need line-of-sight distance
						double distance = getDistance( currentLightPosition, endpoint );
						
						/*
						int[][] fakeMosquitoBoardWithEndpoints = new int[100][100];
						// Create a new board using the endpoint as a pseudo-mosquito
						for ( int h = 0; h < 100; h++ ) {
							for ( int v = 0; v < 100; v++ ) {
								fakeMosquitoBoardWithEndpoints[v][h]=0;
							}
						}
						fakeMosquitoBoardWithEndpoints[(int) endpoint.x][(int) endpoint.y] = 1;
						distance = getDistance (currentLightPosition, getClosestInSight( fakeMosquitoBoardWithEndpoints, currentLightPosition ));
						*/
						
						// Find closest mosquito to endpoint
						Tuple<Integer,Integer> closestMosquito = getClosestInSight(board,endpoint);
						// It is possible that there are no mosquitoes in view of an endpoint
						if ( !(closestMosquito == null) ) {
							double mosquitoDistance = getDistance( endpoint, closestMosquito );
							// Total distance from light to endpoint to mosquito
							double totalDistance = distance + mosquitoDistance;
							if ( totalDistance < shortestDistance ) {
								// This is our new best destination
								// What if the endpoint is in a corner? No way around it? Possible issue
								shortestDistance = totalDistance;
								t = closestMosquito;
							}
						}
					}
				}
				if ( t == null ) {
					// go to a collector
					Iterator<Collector> iter = collectors.iterator();
					while ( iter.hasNext() ) {
						Collector aCollector = (Collector) iter.next();
						t = new Tuple<Integer,Integer>( (int) aCollector.getX(), (int) aCollector.getY() );
						break;
					}
				}

				// shrink the wall buffer and endpoint buffers -- there must be a way from point A to point B on a valid board
//				for ( double wallBuffer = this.currentWallOffset-.5; wallBuffer > 0; wallBuffer-- ) {
					double wallBuffer = 0;
					getValidBoard(board,wallBuffer);

					// shrink endpoint buffer
					for ( int a = 0; a <= this.defaultEndpointBufferSize; a++ ) {
						this.currentEndpointBufferSize = this.defaultEndpointBufferSize-a;
						o = aStar(currentLightPosition,t);
						if ( o != null ) {
							break;
						}
					}					
//				}
				
				// reset valid board
				getValidBoard(board,this.currentWallOffset);
				
				if ( o == null ) {
					log.debug("A* could not find a path. Not even with endpoint buffer zero. This board may be impossible.");
				}
				//log.debug("Steps to objective from " + currentLightPosition);
				for ( Tuple<Integer,Integer> step : o ) {
					//log.debug(step + " " + this.validBoard[step.x][step.y]);
				}
				// remove first item? which is the same as the start item?
				o.remove(0);
			}
			


			// Set the new light position based on the objective
			//o=objective.get(i);

			Tuple<Integer,Integer> nextStop;


			// Try to avoid stuck lights with random moves
			log.debug("Failed moves: " + this.failedMoves[i] + "!!!");
			if ( this.failedMoves[i]>=20 ) {
				// set a random objective?
				Random r = new Random();
				Tuple<Integer,Integer> randomSpace = new Tuple<Integer,Integer>(r.nextInt(100),r.nextInt(100));
				o = aStar(new Tuple<Integer,Integer>((int)lightArr[i].getX(),(int)lightArr[i].getY()), randomSpace);
				this.failedMoves[i]=0;
			}
			
			
			// Go to collector (find path)
			Tuple<Integer,Integer> coll = new Tuple<Integer,Integer>(50,51);
			if ( 
					//!( !o.isEmpty() && coll.equals(new Tuple<Integer,Integer>(o.get(o.size()-1).x,o.get(o.size()-1).y)) )
					this.failedMoves[i]>=20 // this needs to be > the endpoint/corner delay
					// If a light has over n (250?) mosquitoes, go to the collector!
					|| ( getMosquitoesCaptured(lightArr[i],board) > 250 && !coll.equals(new Tuple<Integer,Integer>(o.get(o.size()-1).x,o.get(o.size()-1).y)))
					|| o.isEmpty() 
					|| (allMosquitoesCaptured(board) && !o.get(o.size()-1).equals(coll)) ) {
				o = aStar(new Tuple<Integer,Integer>((int)lightArr[i].getX(),(int)lightArr[i].getY()), coll);
				// The light should wait around by the collector for a while to drop off mosquitoes
				if ( o != null ) {
					for ( int wait = 0; wait<=20; wait++ ) {
						o.add(coll);
					}
				}
			}
			nextStop = o.get(0);

			
			log.debug( "Light " + i + " - Objective X:" + nextStop.x + ", Current X:" + lightArr[i].getX() + ", Objective Y:" + nextStop.y + ", Current Y:" + lightArr[i].getY() );

			if ( nextStop.x == 50 && lightArr[i].getX() == 50 && nextStop.y == 51 && lightArr[i].getX() == 52 ) {
				log.debug("Almost there!");
			}
			
			if(nextStop.y<lightArr[i].getY()) {
				log.debug("try to move up");
				//log.debug("Move up:" + ((MoveableLight)lightArr[i]).moveUp());
				if ( !((MoveableLight)lightArr[i]).moveUp() ) {
					log.debug("Failed to move from " + lightArr[i].getX() + "," + lightArr[i].getY() + " to " + lightArr[i].getX() + "," + nextStop.y);
					log.debug(validBoard[(int) lightArr[i].getX()][nextStop.y]);
				}
			}
			if(nextStop.y>lightArr[i].getY()) {
				log.debug("try to move down");
				//log.debug("Move down:" + ((MoveableLight)lightArr[i]).moveDown());
				if ( !((MoveableLight)lightArr[i]).moveDown() ) {
					log.debug("Failed to move from " + lightArr[i].getX() + "," + lightArr[i].getY() + " to " + lightArr[i].getX() + "," + nextStop.y);
					log.debug(validBoard[(int) lightArr[i].getX()][nextStop.y]);
				}
			}
			if(nextStop.x>lightArr[i].getX()) {
				log.debug("try to move right");
				//log.debug("Move right:" + ((MoveableLight)lightArr[i]).moveRight());
				if ( !((MoveableLight)lightArr[i]).moveRight() ) {
					log.debug("Failed to move from " + lightArr[i].getX() + "," + lightArr[i].getY() + " to " + nextStop.x + "," + lightArr[i].getY());
					log.debug(validBoard[nextStop.x][(int) lightArr[i].getY()]);
				}
			}
			if(nextStop.x<lightArr[i].getX()) {
				log.debug("try to move left");
				//log.debug("Move left:" + ((MoveableLight)lightArr[i]).moveLeft());
				if ( !((MoveableLight)lightArr[i]).moveLeft() ) {
					log.debug("Failed to move from " + lightArr[i].getX() + "," + lightArr[i].getY() + " to " + nextStop.x + "," + lightArr[i].getY());
					log.debug(validBoard[nextStop.x][(int) lightArr[i].getY()]);
				}
			}
			
			log.debug( "New position (light " + i + "):  (" + lightArr[i].getX() + "," + lightArr[i].getY() + ")" );

			// Remove the old objective
			log.debug( o.get(0) + " =? " + lightArr[i].getX() + " " + lightArr[i].getY());
			if ( !o.isEmpty() && o.get(0).equals(new Tuple<Integer,Integer>( (int) lightArr[i].getX(), (int) lightArr[i].getY()) ) ) {
				o.remove(0);
				this.objective.put(i, o);
				this.failedMoves[i]=0;
			} else {
				this.failedMoves[i]++;
			}
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

			//return (i+1)*100+j;
			return 1;
		}
	}

}