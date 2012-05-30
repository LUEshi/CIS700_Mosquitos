
package mosquito.sim;

import java.awt.geom.Line2D;
import java.util.Set;

import javax.swing.JSlider;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosquito.sim.GameListener.GameUpdateType;
import mosquito.sim.ui.GUI;

/**
 *
 * @author Jon Bell
 */
public abstract class Player {
	private GameConfig myConfig;
	private int simulationRounds = 0;
	private int numCaught = 0;
	private GUI myGUI;
	public void setGUI(GUI g)
	{
		this.myGUI = g;
	}
	protected int getSimulationRounds()
	{
		return simulationRounds;
	}
	protected int getSimulationNumCaught()
	{
		return numCaught;
	}
	public void setMyConfig(GameConfig myConfig) {
		this.myConfig = myConfig;
	}
	public void startSimulatedGame(Set<Line2D> walls, int NumLights)
	{
		//no-op
	}
	private GameEngine e;
	protected void runSimulation(int maxRounds, GameListener listener)
	{
		GameConfig dup = (GameConfig) myConfig.clone();
		dup.max_rounds = maxRounds;
		e = new GameEngine(dup);
//		if(myGUI != null)
//		{
//			myGUI.setEngine(e);
//			e.addGameListener(myGUI);
//			myGUI.is_recursive = true;
//		}
		e.addGameListener(listener);
		e.setUpGame();
		numCaught = 0;
		simulationRounds = 0;
	
			// TODO Auto-generated method stub
				while (e.step())
				{
					numCaught = e.getNumCaught();
					simulationRounds++;
				}
				if(myGUI != null)
				{
//					myGUI.restoreEngine();
//					myGUI.is_recursive = false;
//					myGUI.repaint();
					e = null;
				}		
	}
    /**
     * Returns the name for this player
     */
    public abstract String getName();
    
    /**
     * Called on the player when it is instantiated
     */
	public void Register()
	{
		//Do nothing is OK!
	}
	
	/**
	 * Called on the player when a new game starts
	 */
	public abstract void startNewGame(Set<Line2D> walls,int NumLights);

	/**
	 * Returns the set of lights that you would like to place. You must place
	 * exactly as many lights as numLights
	 * @return Set of lights
	 */
	public abstract Set<Light> getLights();
	
	/**
	 * Returns the collector that you would like to place
	 * @return
	 */
	public abstract Collector getCollector();

}

