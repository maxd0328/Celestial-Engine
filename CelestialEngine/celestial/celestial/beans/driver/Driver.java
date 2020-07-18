package celestial.beans.driver;

public abstract class Driver implements java.io.Serializable {
	
	private static final long serialVersionUID = 4239031356649973064L;
	
	protected float value = 0f;
	private float baseValue = 0f;
	
	protected Driver() {
	}
	
	protected abstract void update();
	
	protected abstract Driver clone();
	
	public void reset() {
		this.value = 0f;
	}
	
	public float getValue() {
		return value;
	}
	
	public float getBaseValue() {
		return baseValue;
	}
	
	protected void setBaseValue(float baseValue) {
		this.baseValue = baseValue;
	}
	
}
