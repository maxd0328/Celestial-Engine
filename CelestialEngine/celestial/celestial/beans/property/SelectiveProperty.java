package celestial.beans.property;

import java.util.Collection;
import celestial.beans.driver.DriverSystem;
import celestial.beans.observable.ChangeListener;
import celestial.beans.observable.EventListener;
import celestial.collections.ObservableArrayList;

/**
 * Represents a property wrapper for any other property,
 * but is constrained to a list of possible values. This
 * class will filter all attempts to set the wrapped
 * property by ensuring that its value is one of a set
 * list of possible values.
 * 
 * @param <T>	The type of data that the property will hold.
 * 
 * @author Max D
 */
public final class SelectiveProperty<T> implements Property<T> {
	
	private static final long serialVersionUID = 3605255765420505461L;
	
	/**
	 * The wrapped property
	 */
	private final Property<T> src;
	
	/**
	 * The list of possible values of this property
	 */
	private final ObservableArrayList<PropertySelection<T>> selections;
	
	/**
	 * Creates a new selective property with a wrapped property
	 * and a list of possible values.
	 * 
	 * @param src			The source property to wrap.
	 * @param selections	The list of possible values for this property.
	 */
	public SelectiveProperty(Property<T> src, Collection<PropertySelection<T>> selections) {
		this.src = src;
		this.selections = new ObservableArrayList<PropertySelection<T>>(selections);
	}
	
	/**
	 * Creates a new selective property with a wrapped property
	 * and a vararg array of possible values.
	 * 
	 * @param src			The source property to wrap.
	 * @param selections	The array of possible values for this property.
	 */
	@SafeVarargs
	public SelectiveProperty(Property<T> src, PropertySelection<T>... selections) {
		this.src = src;
		this.selections = new ObservableArrayList<PropertySelection<T>>(selections);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(ChangeListener<T> listener) {
		src.addListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(ChangeListener<T> listener) {
		src.removeListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ChangeListener<T>> getListeners() {
		return src.getListeners();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEventListener(EventListener listener) {
		src.addEventListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEventListener(EventListener listener) {
		src.removeEventListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<EventListener> getEventListeners() {
		return src.getEventListeners();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		return src.get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T value) {
		validateConstraints(value);
		src.set(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getBase() {
		return src.getBase();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBase(T value) {
		validateConstraints(value);
		src.setBase(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DriverSystem<T> getDriver() {
		return src.getDriver();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(Property<T> property, BindingOrder order) {
		src.bind(property, order);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(Property<T> property) {
		src.unbind(property);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Property<Float> subProperty(int index) {
		return src.subProperty(index);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(boolean advanceDrivers) {
		src.update(advanceDrivers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getPropertyType() {
		return src.getPropertyType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Property<T> clone() {
		return new SelectiveProperty<T>(src.clone(), selections);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.io.Serializable getUserPointer() {
		return src.getUserPointer();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserPointer(java.io.Serializable userPtr) {
		src.setUserPointer(userPtr);
	}
	
	/**
	 * Returns the list of possible selections for this property.
	 * 
	 * @return	This property's list of possible selections.
	 */
	public Collection<PropertySelection<T>> getSelections() {
		return new ObservableArrayList<PropertySelection<T>>(selections);
	}
	
	/**
	 * Ensures that the given value is specified in this property's
	 * list of possible values. If not, an exception is thrown.
	 * 
	 * @throws IllegalArgumentException if the given value is not given
	 * 									in this property's list of selections.
	 * 
	 * @param value	The value to ensure for selective validity.
	 */
	private void validateConstraints(T value) {
		Constraint:
		do {
			for(PropertySelection<T> selection : selections)
				if((selection.getValue() == null && value == null) || (selection.getValue() != null && selection.getValue().equals(value)))
					break Constraint;
			throw new IllegalArgumentException("Given value is not supported by this property's selection list.");
		}
		while(false);
	}
	
	/**
	 * Represents a single selection out of a list of possible
	 * selections.
	 * <p>
	 * This class is used by selective properties to define its
	 * list of valid states.
	 * 
	 * @param <T>	The type of data that this property selection will hold.
	 * 
	 * @author Max D
	 */
	public static final class PropertySelection<T> implements java.io.Serializable {
		
		private static final long serialVersionUID = 4060178748771924839L;
		
		/**
		 * This selection's name
		 */
		private final String identifier;
		
		/**
		 * This selection's value
		 */
		private final T value;
		
		/**
		 * Creates a new property selection given its identifier
		 * and value.
		 * 
		 * @param identifier	This selection's identifier.
		 * @param value			This selection's value.
		 */
		public PropertySelection(String identifier, T value) {
			this.identifier = identifier;
			this.value = value;
		}
		
		/**
		 * Returns the identifier attached to this property
		 * selection.
		 * 
		 * @return	This selection's identifier.
		 */
		public String getIdentifier() {
			return identifier;
		}
		
		/**
		 * Returns the value attached to this property selection.
		 * 
		 * @return	This selection's value.
		 */
		public T getValue() {
			return value;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return getIdentifier();
		}
		
	}
	
}
