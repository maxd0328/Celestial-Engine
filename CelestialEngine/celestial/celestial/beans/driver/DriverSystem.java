package celestial.beans.driver;

import java.util.HashMap;
import celestial.beans.property.Property;
import celestial.util.Factory;

public abstract class DriverSystem<T> implements java.io.Serializable {
	
	private static final long serialVersionUID = -6368876489895429766L;
	
	private static final HashMap<Class<?>, Factory<DriverSystem<?>>> DRIVER_COMPATIBILITY_TYPES = new HashMap<Class<?>, Factory<DriverSystem<?>>>();
	
	static {
		
		DRIVER_COMPATIBILITY_TYPES.put(Float.class, () -> new FloatDriverSystem());
		DRIVER_COMPATIBILITY_TYPES.put(Integer.class, () -> new IntegerDriverSystem());
		
	}
	
	public static void addDriverCompatibilityType(Class<?> type, Factory<DriverSystem<?>> factory) {
		DRIVER_COMPATIBILITY_TYPES.put(type, factory);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> DriverSystem<T> createDriverSystem(Property<T> property) {
		for(Class<?> type : DRIVER_COMPATIBILITY_TYPES.keySet()) {
			if(type.isAssignableFrom(property.getPropertyType()))
				return (DriverSystem<T>) DRIVER_COMPATIBILITY_TYPES.get(type).build();
		}
		return null;
	}
	
	protected Driver driver = null;
	
	protected DriverSystem() {
	}
	
	public void update(T baseValue) {
		if(driver != null) {
			driver.setBaseValue(convert(baseValue));
			driver.update();
		}
	}
	
	public void reset() {
		if(driver != null)
			driver.reset();
	}
	
	public Driver get() {
		return driver;
	}
	
	public void set(Driver driver) {
		this.driver = driver == null ? null : driver.clone();
	}
	
	protected void validateBounds(int index, int min, int max) {
		if(index < min || index > max)
			throw new IndexOutOfBoundsException(index);
	}
	
	protected abstract float convert(T value);
	
	public abstract T apply(T baseValue);
	
	public abstract DriverSystem<T> clone();
	
	private static final class FloatDriverSystem extends DriverSystem<Float> {
		
		private static final long serialVersionUID = 8591203813516674422L;
		
		@Override
		protected float convert(Float value) {
			return (float) value;
		}
		
		@Override
		public Float apply(Float baseValue) {
			return baseValue + (driver == null ? 0f : driver.getValue());
		}
		
		@Override
		public DriverSystem<Float> clone() {
			FloatDriverSystem system = new FloatDriverSystem();
			if(driver != null) system.set(driver.clone());
			return system;
		}
		
	}
	
	private static final class IntegerDriverSystem extends DriverSystem<Integer> {
		
		private static final long serialVersionUID = -45196621122531219L;
		
		@Override
		protected float convert(Integer value) {
			return (float) (int) value;
		}
		
		@Override
		public Integer apply(Integer baseValue) {
			return baseValue + (driver == null ? 0 : (int) driver.getValue());
		}
		
		@Override
		public DriverSystem<Integer> clone() {
			IntegerDriverSystem system = new IntegerDriverSystem();
			if(driver != null) system.set(driver.clone());
			return system;
		}
		
		
	}
	
}
