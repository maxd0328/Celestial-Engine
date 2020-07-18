package celestial.beans.property;

import celestial.beans.driver.DriverSystem;
import celestial.beans.observable.Observable;

/**
 * As an extension of the <a href="#{@link}">{@link Observable}</a>
 * interface, the Property interface defines any value that is
 * categorized as a property.
 * <p>
 * Properties serve as standard wrappers for any pre-existing type,
 * but provide a variety of features.
 * <p>
 * Properties contain all features of observable values, meaning that
 * they can implement event listeners and change listeners. They can
 * also be bound to other properties, effectively linking their values
 * together so that they will always be equal.
 * <p>
 * There are two primary classifications of properties: external and
 * internal properties. External properties are ones that contain their
 * values outside of the property object, while internal properties
 * contain their values within the property object. All property features
 * can be implemented equally using both internal and external properties.
 * 
 * @param <T>	This type of value that this property will hold.
 * 
 * @see InternalProperty
 * @see ExternalProperty
 * @author Max D
 */
public interface Property<T> extends Observable<T> {
	
	/**
	 * Returns the current value of this property modified
	 * by it's driver system, if applicable.
	 * 
	 * @return	The modified value of this property.
	 */
	public T get();
	
	/**
	 * Sets the current value of this property. This
	 * is not always supported, as many properties are
	 * categorized as read-only.
	 * <p>
	 * If the value of this property was set, then its
	 * driver state will also be reset, if applicable.
	 * 
	 * @param value	The value to assign to this property.
	 */
	public void set(T value);
	
	/**
	 * Returns the base value of this property that is
	 * not modified by it's driver system.
	 * <p>
	 * If this property has no driver system, then this
	 * method is the same as <a href="#{@link}">{@link
	 * Property#get()}</a>
	 * 
	 * @return	The unmodified value of this property.
	 */
	public T getBase();
	
	/**
	 * Sets the current value of this property. This
	 * is not always supported, as many properties are
	 * categorized as read-only.
	 * <p>
	 * Unlike the <a href="#{@link}">{@link Property#
	 * set()}</a> method, the state of the driver system
	 * is not reset when using this method.
	 * 
	 * @param value	The value to assign to this property.
	 */
	public void setBase(T value);
	
	/**
	 * Returns the compounded <a href="#{@link}">{@link
	 * DriverSystem}</a> that is attached to this property.
	 * <p>
	 * If this property's associated type is not compatible
	 * with any known driver systems, then {@code null} is
	 * returned.
	 * 
	 * @return	This property's associated driver system.
	 */
	public DriverSystem<T> getDriver();
	
	/**
	 * Binds another property to this one, using this
	 * property object as the root property in the binding.
	 * <p>
	 * The binding order of this binding is specified with
	 * the order argument. Note that the terminology of the
	 * <a href="#{@link}">{@link BindingOrder}</a> argument
	 * applies to this property object, not the guest property
	 * object.
	 * 
	 * @param property	The guest property to bind to this property.
	 * @param order		The binding order to use for this binding.
	 */
	public void bind(Property<T> property, BindingOrder order);
	
	/**
	 * Removes a binding between this property and the given
	 * guest property from this object's list of bindings.
	 * <p>
	 * If no such binding exists, then no action is taken.
	 * 
	 * @param property	The property of which to remove the binding.
	 */
	public void unbind(Property<T> property);
	
	/**
	 * A method intended for vector-based properties.
	 * <p>
	 * This method will return a sub-property that is bound
	 * to the given index of the vector attached to this
	 * property. In the event that this is not a vector
	 * based property, an exception will be thrown.
	 * 
	 * @param index	The index of the vector to build the property
	 * 				from.
	 * 
	 * @throws 	UnsupportedOperationException when this property
	 * 		   	is not a vector-based property.
	 * @return	A float sub-property of this vector-based property.
	 */
	public Property<Float> subProperty(int index);
	
	/**
	 * Updates this property.
	 * <p>
	 * This method should first perform all updates of the observable
	 * interface. See <a href="#{@link}">{@link Observable#update()}</a>.
	 * <p>
	 * Then, this method should update all bindings of this property, as
	 * well as perform any update-requiring operation specific to this
	 * particular property.
	 * 
	 * @param advanceDrivers	If true, the drivers for this property will
	 * 							be advanced.
	 */
	public void update(boolean advanceDrivers);
	
	/**
	 * Returns the default class object for this property's type.
	 * Used for testing class compatibility in place of the
	 * {@code instanceof} operator, as per Java's object generic
	 * type erasure.
	 * 
	 * @return	The default class object for this property's type.
	 */
	public Class<T> getPropertyType();
	
	/**
	 * Creates a clone of this property, effectively deep-cloning the
	 * driver system, and all bindings currently attached to this
	 * property.
	 * 
	 * @return	A clone of this property.
	 */
	public Property<T> clone();
	
	/**
	 * Returns a previously set user pointer value.
	 * 
	 * @return	This property's current user pointer
	 */
	public java.io.Serializable getUserPointer();
	
	/**
	 * Sets a convenience value that can be retrieved from
	 * this pointer at a later time.
	 * <p>
	 * For serializability reasons, it must be ensured that
	 * this user pointer is serializable as well.
	 * 
	 * @param userPtr	The user-pointer value to set
	 */
	public void setUserPointer(java.io.Serializable userPtr);
	
	/**
	 * An alternate method to the <a href="#{@link}">{@link Property#bind
	 * (Property, BindingOrder)}</a> method. It simply calls that method
	 * using the given property with the default binding order of
	 * <a href="#{@link}">{@link BindingOrder#RECESSIVE}</a>. Note that
	 * with this default binding order, this property will then become
	 * subordinate to the bound property.
	 * 
	 * @param property
	 */
	public default void bind(Property<T> property) {
		bind(property, BindingOrder.RECESSIVE);
	}
	
	/**
	 * Updates this property.
	 * <p>
	 * This method should first perform all updates of the observable
	 * interface. See <a href="#{@link}">{@link Observable#update()}</a>.
	 * <p>
	 * Then, this method should update all bindings of this property, as
	 * well as perform any update-requiring operation specific to this
	 * particular property.
	 * <p>
	 * By default, this method advances all drivers as well.
	 */
	public default void update() {
		update(true);
	}
	
}
