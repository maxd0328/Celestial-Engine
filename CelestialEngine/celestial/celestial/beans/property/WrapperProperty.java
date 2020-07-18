package celestial.beans.property;

import java.util.Collection;
import celestial.beans.driver.DriverSystem;
import celestial.beans.observable.ChangeListener;
import celestial.beans.observable.EventListener;

/**
 * An abstract wrapper implementation for properties
 * allowing them to be wrapped in a different presentation.
 * <p>
 * This class provides implementable methods for processing
 * data in order to create a desired wrapped presentation.
 * <p>
 * This property will share the same memory as the original
 * property, and is not a clone in any way.
 * 
 * @param <T>	The type of property to wrap
 * 
 * @author Max D
 */
public abstract class WrapperProperty<T> implements Property<T> {
	
	private static final long serialVersionUID = -5841889151474649082L;
	
	/**
	 * The property upon which the wrapper property is based.
	 */
	private final Property<T> property;
	
	/**
	 * Creates a new wrapper property given the pre-existing
	 * property.
	 * 
	 * @param property	The property upon which this wrapper is based.
	 */
	public WrapperProperty(Property<T> property) {
		this.property = property;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(ChangeListener<T> listener) {
		property.addListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(ChangeListener<T> listener) {
		property.removeListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ChangeListener<T>> getListeners() {
		return property.getListeners();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEventListener(EventListener listener) {
		property.addEventListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEventListener(EventListener listener) {
		property.removeEventListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<EventListener> getEventListeners() {
		return property.getEventListeners();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * NOTE: For the wrapper implementation, this data will be
	 * processed by the wrapper specification.
	 */
	@Override
	public T get() {
		return process(property.get());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * NOTE: For the wrapper implementation, this data will be
	 * processed by the wrapper specification.
	 */
	@Override
	public void set(T value) {
		this.property.set(processInv(value));
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * NOTE: For the wrapper implementation, this data will be
	 * processed by the wrapper specification.
	 */
	@Override
	public T getBase() {
		return process(property.getBase());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * NOTE: For the wrapper implementation, this data will be
	 * processed by the wrapper specification.
	 */
	@Override
	public void setBase(T value) {
		this.property.setBase(processInv(value));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DriverSystem<T> getDriver() {
		return property.getDriver();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(Property<T> property, BindingOrder order) {
		this.property.bind(property, order);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(Property<T> property) {
		this.property.unbind(property);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Property<Float> subProperty(int index) {
		return property.subProperty(index);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(boolean advanceDrivers) {
		property.update(advanceDrivers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getPropertyType() {
		return property.getPropertyType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.io.Serializable getUserPointer() {
		return property.getUserPointer();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserPointer(java.io.Serializable userPtr) {
		property.setUserPointer(userPtr);
	}
	
	/**
	 * Returns the original property that this
	 * property wraps.
	 * 
	 * @return	The original property
	 */
	public Property<T> getProperty() {
		return property;
	}
	
	/**
	 * This function will process the data for
	 * the wrapper property in order to change
	 * its presentation in some way.
	 * <p>
	 * This function takes in a raw value from
	 * the wrapped property, and produces a value
	 * with the desired presentation.
	 * 
	 * @param value	The value from the wrapped property
	 * @return		The wrapper-processed output
	 */
	protected abstract T process(T value);
	
	
	/**
	 * This function should be the exact inverse of
	 * the <a href="#{@link}">{@link WrapperProperty
	 * #process(Object)}</a> function.
	 * <p>
	 * It should convert a wrapper-processed output
	 * that would be produced by the <a href="#{@link}">
	 * {@link WrapperProperty#process(Object)}</a>
	 * method, and convert it back into its corresponding
	 * input from the wrapped property.
	 * 
	 * @param value	The wrapper-processed value.
	 * @return		A value appropriate for the wrapped
	 * 				property
	 */
	protected abstract T processInv(T value);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract WrapperProperty<T> clone();
	
}