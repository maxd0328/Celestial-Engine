package celestial.beans.property;

import java.util.Arrays;
import java.util.Collection;
import celestial.beans.driver.DriverSystem;
import celestial.beans.observable.ChangeListener;
import celestial.beans.observable.EventListener;
import celestial.collections.ObservableArrayList;
import celestial.vecmath.GenericVector;

/**
 * Represents a compound manifestation of many
 * properties represented as a single property.
 * <p>
 * This class is used to blend properties together
 * so that they can be modified in unison.
 * <p>
 * All setting operations under this class will have
 * their corresponding effects done to all children
 * properties that are attached to this compound
 * property.
 * <p>
 * All getting operations under this class will
 * return their corresponding data from the first
 * child property that is attached to this compound
 * property.
 * 
 * @param <T>	The type of data that the child
 * 				properties will contain.
 * 
 * @author Max D
 */
public final class CompoundProperty<T> implements Property<T> {
	
	private static final long serialVersionUID = 6262449646142763102L;
	
	/**
	 * The list of children properties
	 */
	private final ObservableArrayList<Property<T>> properties;
	
	/**
	 * Creates a new compound property given the list of
	 * children properties.
	 * 
	 * @param properties	The list of children properties.
	 */
	public CompoundProperty(Collection<Property<T>> properties) {
		this.properties = new ObservableArrayList<Property<T>>(properties);
	}
	
	/**
	 * Creates a new compound property given a vararg array
	 * of child properties.
	 * 
	 * @param properties	The array of children properties.
	 */
	@SafeVarargs
	public CompoundProperty(Property<T>... properties) {
		this(Arrays.asList(properties));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(ChangeListener<T> listener) {
		for(Property<T> property : properties)
			property.addListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(ChangeListener<T> listener) {
		for(Property<T> property : properties)
			property.removeListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ChangeListener<T>> getListeners() {
		return properties.size() == 0 ? null : properties.get(0).getListeners();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEventListener(EventListener listener) {
		for(Property<T> property : properties)
			property.addEventListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEventListener(EventListener listener) {
		for(Property<T> property : properties)
			property.removeEventListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<EventListener> getEventListeners() {
		return properties.size() == 0 ? null : properties.get(0).getEventListeners();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		return properties.size() == 0 ? null : properties.get(0).get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T value) {
		for(Property<T> property : properties)
			property.set(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getBase() {
		return properties.size() == 0 ? null : properties.get(0).getBase();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBase(T value) {
		for(Property<T> property : properties)
			property.setBase(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DriverSystem<T> getDriver() {
		return properties.size() == 0 ? null : properties.get(0).getDriver();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(Property<T> property, BindingOrder order) {
		for(Property<T> _property : properties)
			_property.bind(property, order);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(Property<T> property) {
		for(Property<T> _property : properties)
			_property.unbind(property);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For the compound implementation, a compound sub-property
	 * is returned for all properties.
	 */
	@Override
	public Property<Float> subProperty(int index) {
		if(!GenericVector.class.isAssignableFrom(getPropertyType()))
			throw new UnsupportedOperationException("subProperty");
		
		CompoundProperty<Float> out = new CompoundProperty<>();
		for(Property<T> prop : properties)
			out.addProperty(prop.subProperty(index));
		return out;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(boolean advanceDrivers) {
		for(Property<T> property : properties)
			property.update(advanceDrivers);
		properties.update();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getPropertyType() {
		return properties.size() == 0 ? null : properties.get(0).getPropertyType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Property<T> clone() {
		Collection<Property<T>> newProperties = new ObservableArrayList<Property<T>>();
		for(Property<T> property : properties)
			newProperties.add(property.clone());
		return new CompoundProperty<T>(newProperties);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.io.Serializable getUserPointer() {
		return properties.size() == 0 ? null : properties.get(0).getUserPointer();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserPointer(java.io.Serializable userPtr) {
		for(Property<T> property : properties)
			property.setUserPointer(userPtr);
	}
	
	/**
	 * Returns all children properties attached to this property.
	 * 
	 * @return This property's children
	 */
	public ObservableArrayList<Property<T>> getProperties() {
		return properties;
	}
	
	/**
	 * Adds a property to this compound property's list of children
	 * properties.
	 * 
	 * @param property	The property to add
	 */
	@SuppressWarnings("unchecked")
	public void addProperty(Property<?> property) {
		this.properties.add((Property<T>) property);
	}
	
}
