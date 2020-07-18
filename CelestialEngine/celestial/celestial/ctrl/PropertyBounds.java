package celestial.ctrl;

import java.io.Serializable;

import celestial.beans.property.Property;

public interface PropertyBounds<E> extends Serializable {
	
	@SuppressWarnings("unchecked")
	public void loadProperties(Property<E>... properties);
	
	public Property<E>[] getProperties();
	
	public void validateBounds();
	
	public PropertyBounds<E> duplicate();
	
	public Class<E> getType();
	
}
