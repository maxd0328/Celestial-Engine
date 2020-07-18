package celestial.beans.property;

import celestial.util.IGetter;
import celestial.util.ISetter;

/**
 * A classification of property in which the property's value is
 * held externally and only referenced from inside the property
 * object.
 * <p>
 * This type of property is best used for pre-existing variables
 * or third-party libraries in which the variables are directly
 * accessible or are not already contained in a property object.
 * It allows for essentially any form of data to be adapted into
 * a property object no matter its container or origin.
 * <p>
 * This property implementation contains complete features for
 * observable features as well as property binding.
 * <p>
 * All that is required to implement an external property is a
 * get/set implementation for the external variable.
 * 
 * @param <T>	The type of value that this property will hold.
 * 
 * @author Max D
 */
public abstract class ExternalProperty<T> extends AbstractProperty<T> {
	
	private static final long serialVersionUID = 4954794291260070337L;
	
	/**
	 * The getter and setter implementations for the referenced variable.
	 */
	private final IGetter<T> getter;
	private final ISetter<T> setter;
	
	/**
	 * Creates a new external property given the getter and setter
	 * implementations. This will connect the external variable to
	 * the property and will allow it to function as any other internal
	 * property.
	 * 
	 * @param getter				The getter implementation for this property.
	 * @param setter				The setter implementation for this property.
	 * @param defaultPropertyType	The type class object for this property.
	 */
	protected ExternalProperty(IGetter<T> getter, ISetter<T> setter, Class<T> defaultPropertyType) {
		super(getter.get(), defaultPropertyType);
		this.getter = getter;
		this.setter = setter;
	}
	
	/**
	 * Creates a new external property given the getter and setter
	 * implementations. This will connect the external variable to
	 * the property and will allow it to function as any other internal
	 * property.
	 * <p>
	 * Since no property type is provided, this property's type
	 * must be specified by the implementable method <a href=
	 * "#{@link}">{@link Property#getPropertyType}</a>.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 */
	protected ExternalProperty(IGetter<T> getter, ISetter<T> setter) {
		super(getter.get());
		this.getter = getter;
		this.setter = setter;
	}
	
	/**
	 * Creates a new external property as a clone of an already
	 * existing external property. All corresponding components
	 * are deep-cloned.
	 * 
	 * @param src	The external property to create a clone of.
	 */
	protected ExternalProperty(ExternalProperty<T> src) {
		super(src);
		this.getter = src.getter;
		this.setter = src.setter;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getBase() {
		return getter.get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBase(T value) {
		if(setter != null)
			setter.set(value);
	}
	
	/**
	 * Returns the getter implementation for this external property.
	 * 
	 * @return	This property's getter implementation.
	 */
	public IGetter<T> getter() {
		return getter;
	}
	
	/**
	 * Returns the setter implementation for this external property.
	 * 
	 * @return	This property's setter implementation.
	 */
	public ISetter<T> setter() {
		return setter;
	}
	
}
