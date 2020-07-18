package celestial.beans.driver;

import celestial.core.EngineRuntime;
import celestial.util.Factory;

public class LinearDriver extends Driver {
	
	private static final long serialVersionUID = 2956854771513464417L;
	
	public static final Factory<LinearDriver> FACTORY = () -> new LinearDriver(0f);
	
	private float slope;
	
	public LinearDriver(float slope) {
		this.slope = slope;
	}
	
	public float getSlope() {
		return slope;
	}
	
	public void setSlope(float slope) {
		this.slope = slope;
	}
	
	@Override
	protected void update() {
		super.value += slope * EngineRuntime.frameTimeRelative();
	}
	
	@Override
	protected LinearDriver clone() {
		return new LinearDriver(slope);
	}
	
}
