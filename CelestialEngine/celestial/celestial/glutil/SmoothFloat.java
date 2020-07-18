package celestial.glutil;

public final class SmoothFloat implements java.io.Serializable {
	
	private static final long serialVersionUID = -3207081404867672464L;
	
	private final float agility;
	
	private float target;
	private float actual;
	
	public SmoothFloat(float initialValue, float agility){
		this.target = initialValue;
		this.actual = initialValue;
		this.agility = agility;
	}
	
	public void update(float delta){
		float offset = target - actual;
		float change = offset * delta * agility;
		actual += change;
	}
	
	public void increaseTarget(float dT){
		this.target += dT;
	}
	
	public void setTarget(float target){
		this.target = target;
	}
	
	public void instantIncrease(float increase){
		this.actual += increase;
	}
	
	public void instantSet(float value) {
		this.actual = value;
		this.target = value;
	}
	
	public float get(){
		return actual;
	}
	
	public void force(){}
	
	public float getTarget(){
		return target;
	}
	
}
