package mosquito.g0;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import mosquito.sim.Collector;
import mosquito.sim.GameListener;
import mosquito.sim.Light;
import mosquito.sim.Player;
import mosquito.sim.GameListener.GameUpdateType;

public class DumbPlayer1 extends Player {
	private int numLights;
	private Logger log = Logger.getLogger(this.getClass());
	@Override
	public String getName() {
		return "Dumb Player 1";
	}
	@Override
	public void startSimulatedGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
	}
	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
		this.runSimulation(400,new GameListener() {
			
			@Override
			public void gameUpdated(GameUpdateType type) {
				if(type.equals(GameUpdateType.MOVEPROCESSED))
				{
					log.debug("We had a move happen, " + getSimulationRounds() +", caught: " + getSimulationNumCaught());
				}
				else if(type.equals(GameUpdateType.GAMEOVER))
				{
					log.debug("Game ended at ticks: " + getSimulationRounds());
				}
			}
		});
	}

	Point2D lastLight = null;
	@Override
	public Set<Light> getLights() {
		HashSet<Light> ret = new HashSet<Light>();
		Random r = new Random();
		for(int i = 0; i<numLights;i++)
		{
			lastLight = new Point2D.Double(r.nextInt(100), r.nextInt(100));
			Light l = new Light(lastLight.getX(),lastLight.getY(), 10,r.nextInt(10),1);
			ret.add(l);
		}
		return ret;
	}

	@Override
	public Collector getCollector() {
		Random r = new Random();
		Collector c = new Collector(lastLight.getX()+1,lastLight.getY() +1);
		return c;
	}


}
