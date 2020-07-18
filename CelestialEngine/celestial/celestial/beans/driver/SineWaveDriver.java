package celestial.beans.driver;

import celestial.core.EngineRuntime;
import celestial.util.Factory;

public class SineWaveDriver extends Driver {
	
	private static final long serialVersionUID = 5291910927344300452L;
	
	public static final Factory<SineWaveDriver> FACTORY = () -> new SineWaveDriver(0f, 0f, 1);
	
	private float min;
	private float max;
	private int time;
	
	private float counter = 0;
	
	public SineWaveDriver(float min, float max, int time) {
		this.min = min;
		this.max = max;
		this.time = time;
	}
	
	public float getMin() {
		return min;
	}
	
	public void setMin(float min) {
		this.min = min;
	}
	
	public float getMax() {
		return max;
	}
	
	public void setMax(float max) {
		this.max = max;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	@Override
	protected void update() {
		super.value = min + ((float) Math.sin(((counter += EngineRuntime.frameTimeRelative()) / time) * (Math.PI * 2)) / 2f + 0.5f) * (max - min);
	}
	
	@Override
	protected SineWaveDriver clone() {
		return new SineWaveDriver(min, max, time);
	}
	
}
