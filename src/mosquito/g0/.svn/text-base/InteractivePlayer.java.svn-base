package mosquito.g0;

import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mosquito.sim.Collector;
import mosquito.sim.Light;
import mosquito.sim.Player;

public class InteractivePlayer extends Player {
	private int numLights;
	
	@Override
	public String getName() {
		return "Interactive Player";
	}

	@Override
	public void startNewGame(Set<Line2D> walls, int NumLights) {
		this.numLights = NumLights;
	}

	private int lastx;
	private int lasty;
	@Override
	public Set<Light> getLights() {
		HashSet<Light> ret = new HashSet<Light>();
		for(int i = 0; i<numLights;i++)
		{
			Light l = new Light(i+50, i+50, 1,1,3);
			lastx = i+50;
			lasty = i+50;
			ret.add(l);
		}
		return ret;
	}

	@Override
	public Collector getCollector() {
		Collector c = new Collector(lastx,lasty);
		return c;
	}


}
