package celestial.ctrl;

import celestial.beans.property.Property;

public final class IntervalBounds<E extends Comparable<E>> implements PropertyBounds<E> {
	
	private static final long serialVersionUID = -1207805000713079755L;
	
	private final Class<E> type;
	private final E min, max;
	private final IntervalType minType, maxType;
	
	private Property<E>[] properties;
	private E lastValue;
	
	public IntervalBounds(Class<E> type, E min, IntervalType minType, E max, IntervalType maxType, E defaultValue) {
		this.type = type;
		this.min = min;
		this.minType = minType;
		this.max = max;
		this.maxType = maxType;
		this.lastValue = defaultValue;
	}
	
	@Override
	public void loadProperties(@SuppressWarnings("unchecked") Property<E>... properties) {
		this.properties = properties;
	}
	
	@Override
	public void validateBounds() {
		for(Property<E> property : properties) {
			
			int minState = property.get().compareTo(min);
			if(minState == -1) property.set(minType == IntervalType.EXCLUSIVE ? lastValue : min);
			else if(minState == 0 && minType == IntervalType.EXCLUSIVE) property.set(lastValue);
			
			int maxState = property.get().compareTo(max);
			if(maxState == +1) property.set(maxType == IntervalType.EXCLUSIVE ? lastValue : max);
			else if(maxState == 0 && maxType == IntervalType.EXCLUSIVE) property.set(lastValue);
			
			lastValue = property.get();
			
		}
	}
	
	@Override
	public Class<E> getType() {
		return type;
	}
	
	@Override
	public IntervalBounds<E> duplicate() {
		return new IntervalBounds<E>(type, min, minType, max, maxType, lastValue);
	}
	
	public E getMin() {
		return min;
	}
	
	public IntervalType getMinType() {
		return minType;
	}
	
	public E getMax() {
		return max;
	}
	
	public IntervalType getMaxType() {
		return maxType;
	}
	
	@Override
	public Property<E>[] getProperties() {
		return properties;
	}
	
	public static enum IntervalType {
		
		INCLUSIVE,
		
		EXCLUSIVE;
		
	}
	
}
