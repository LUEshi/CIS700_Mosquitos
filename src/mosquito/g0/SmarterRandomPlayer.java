package mosquito.g0;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.MoveableLight;



public class SmarterRandomPlayer extends mosquito.sim.Player {

	private int numLights;
	private int numCollectors;
	private Point2D.Double lastLight;
	private Logger log = Logger.getLogger(this.getClass()); // for logging
	
	@Override
	public String getName() {
		return "Smarter Random Moving Player";
	}
	
	private Set<Light> lights;
	private Set<Collector> collectors;
	private Set<Line2D> walls;
	
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


	/*
	 * This is used to determine the initial placement of the lights.
	 * It is called after startNewGame.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> getLights(int[][] board) {
		// Initially place the lights randomly, and put the collector next to the last light

		lights = new HashSet<Light>();
		Random r = new Random();
		for(int i = 0; i<numLights;i++)
		{
			// this player just picks random points for the Light
			lastLight = new Point2D.Double(r.nextInt(100), r.nextInt(100));
			
			/*
			 * The arguments to the Light constructor are: 
			 * - X coordinate
			 * - Y coordinate
			 * - whether or not the light is on
			 */
			MoveableLight l = new MoveableLight(lastLight.getX(),lastLight.getY(), true);

			log.trace("Positioned a light at (" + lastLight.getX() + ", " + lastLight.getY() + ")");
			lights.add(l);
		}
		
		return lights;
	}
	
	/*
	 * This is called at the beginning of each step (before the mosquitoes have moved)
	 * If your Set contains additional lights, an error will occur. 
	 * Also, if a light moves more than one space in any direction, an error will occur.
	 * The board tells you where the mosquitoes are: board[x][y] tells you the
	 * number of mosquitoes at coordinate (x, y)
	 */
	public Set<Light> updateLights(int[][] board) {
		
		Random r = new Random();

		int count = 0;
		
		for (Light l : lights) {
			MoveableLight ml = (MoveableLight)l;
			
			if (l.getX() == lastLight.x && l.getY() == lastLight.y) {
				if (r.nextInt(2) == 0) ml.turnOff(); else ml.turnOn();
				continue;
			}
			

			// randomly move it in one direction
			// these methods return true if the move is allowed, false otherwise
			// a move is not allowed if it would go beyond the world boundaries
			// you can get the light's position with getX() and getY()
			switch (r.nextInt(4)) {
			case 0: ml.moveUp(); break;
			case 1: ml.moveDown(); break;
			case 2: ml.moveLeft(); break;
			case 3: ml.moveRight(); break;
			}

			// always leave the light on, unless you're near the collector
			double dx = l.getX() - collector.getX();
			double dy = l.getY() - collector.getY();
			double distance = Math.sqrt(dx*dx+dy*dy);
			if (distance < 20) ml.turnOff();
			else ml.turnOn();
		}
		
		return lights;
	}
	
	private Collector collector;

	/*
	 * Currently this is only called once (after getLights), so you cannot
	 * move the Collectors.
	 */
	@Override
	public Set<Collector> getCollectors() {
		// this one just places a collector next to the last light that was added
		collectors = new HashSet<Collector>();
		collector = new Collector(lastLight.getX()+1,lastLight.getY() +1);
		log.debug("Positioned a Collector at (" + (lastLight.getX()+1) + ", " + (lastLight.getY()+1) + ")");
		collectors.add(collector);
		return collectors;
	}

}
