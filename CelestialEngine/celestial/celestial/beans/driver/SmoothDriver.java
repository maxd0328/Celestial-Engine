package celestial.beans.driver;

import celestial.core.EngineRuntime;
import celestial.util.Factory;

public class SmoothDriver extends Driver {
	
	private static final long serialVersionUID = -2522304524153752747L;
	
	public static final Factory<SmoothDriver> FACTORY = () -> new SmoothDriver(1f);
	
	private float agility;
	
	public SmoothDriver(float agility) {
		this.agility = agility;
	}
	
	public float getAgility() {
		return agility;
	}
	
	public void setAgility(float agility) {
		this.agility = agility;
	}
	
	private boolean started = false;
	private float actual = super.getBaseValue();
	
	private boolean reset = false;
	
	@Override
	protected void update() {
		if(!started) {
			actual = super.getBaseValue();
			started = true;
			return;
		}
		
		float target = super.getBaseValue();
		if(reset) {
			actual = target;
			reset = false;
			return;
		}
		
		float offset = target - actual;
		float change = offset * 0.01f * agility * EngineRuntime.frameTimeRelative();
		
		actual += change;
		
		super.value = actual - super.getBaseValue();
	}
	
	@Override
	protected SmoothDriver clone() {
		return new SmoothDriver(agility);
	}
	
	@Override
	public void reset() {
		super.reset();
		reset = true;
	}
	
}
