package celestial.ctrl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import celestial.beans.property.Property;
import celestial.error.CelestialGenericException;

public final class PropertyController implements java.io.Serializable {
	
	private static final long serialVersionUID = 6304961181455225788L;
	
	private final LinkedHashMap<String, Property<?>> properties;
	private final ArrayList<PropertyBounds<?>> bounds;
	private final ArrayList<BoundsInfo> boundsInfo;
	
	public PropertyController() {
		this.properties = new LinkedHashMap<String, Property<?>>();
		this.bounds = new ArrayList<PropertyBounds<?>>();
		this.boundsInfo = new ArrayList<BoundsInfo>();
	}
	
	public PropertyController withProperty(String identifier, Property<?> property) {
		this.properties.put(identifier, property);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <E> PropertyController withPropertyBounds(PropertyBounds<E> bounds, int propertyCount) {
		ArrayList<Property<E>> list = new ArrayList<Property<E>>();
		for(int i = properties.size() - 1 ; i >= properties.size() - propertyCount ; --i) {
			Property<?> prop = new ArrayList<>(properties.values()).get(i);
			for(PropertyBounds<?> testBounds : this.bounds)
				for(Property<?> testProp : testBounds.getProperties())
					if(testProp == prop)
						throw new CelestialGenericException("Overlapping bounds");
			if(!bounds.getType().isAssignableFrom(prop.getPropertyType())) throw new CelestialGenericException("Invalid property type");
			list.add((Property<E>) prop);
		}
		Collections.reverse(list);
		Property<E>[] arr = (Property<E>[]) Array.newInstance(Property.class, list.size());
		list.toArray(arr);
		bounds.loadProperties(arr);
		this.bounds.add(bounds);
		this.boundsInfo.add(new BoundsInfo(properties.size(), propertyCount, bounds));
		return this;
	}
	
	public Property<?> getProperty(String identifier) {
		return properties.get(identifier);
	}
	
	public PropertyBounds<?> getBounds(Property<?> property) {
		for(PropertyBounds<?> bounds : this.bounds)
			for(Property<?> prop : bounds.getProperties())
				if(prop == property)
					return bounds;
		return null;
	}
	
	public LinkedHashMap<String, Property<?>> getProperties() {
		return new LinkedHashMap<String, Property<?>>(properties);
	}
	
	public ArrayList<PropertyBounds<?>> getBounds() {
		return new ArrayList<PropertyBounds<?>>(bounds);
	}
	
	public ArrayList<BoundsInfo> getBoundsInfo() {
		return new ArrayList<BoundsInfo>(boundsInfo);
	}
	
	public void validateBounds() {
		for(PropertyBounds<?> bound : bounds) {
			bound.validateBounds();
		}
	}
	
	public static final class BoundsInfo implements java.io.Serializable {
		
		private static final long serialVersionUID = -4607519667336702726L;
		
		private final int location, size;
		private final PropertyBounds<?> bounds;
		
		public BoundsInfo(int location, int size, PropertyBounds<?> bounds) {
			this.location = location;
			this.size = size;
			this.bounds = bounds;
		}
		
		public int getLocation() {
			return location;
		}
		
		public int getSize() {
			return size;
		}
		
		public PropertyBounds<?> getBounds() {
			return bounds;
		}
		
	}
	
}
