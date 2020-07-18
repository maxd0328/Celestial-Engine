package celestial.beans.property;

import java.util.LinkedHashMap;
import celestial.util.Cloner;
import celestial.vecmath.GenericVector;

/**
 * A classification of property in which the property's value
 * is held inside of the property object.
 * <p>
 * This is best used for properties in which the value does not
 * exist elsewhere and is easily created within the property object.
 * <p>
 * This property implementation contains complete features for
 * observable features as well as property binding.
 * 
 * @param <T>	The type of value that this property will hold.
 * 
 * @see Property
 * @author Max D
 */
public abstract class InternalProperty<T> extends AbstractProperty<T> {
	
	private static final long serialVersionUID = 7919615663581843930L;
	
	/**
	 * A map that keeps track of all available cloners. These cloners
	 * are used during the cloning of internal properties, in which the
	 * value of that property is cloned if its type is contained within
	 * this map.
	 */
	private static final LinkedHashMap<Class<?>, Cloner<?>> INTERNAL_CLONERS = new LinkedHashMap<Class<?>, Cloner<?>>();
	
	static {
		INTERNAL_CLONERS.put(GenericVector.class, o -> ((GenericVector) o).clone());
	}
	
	/**
	 * Adds a new cloner to the list of applicable internal cloners.
	 * <p>
	 * These cloners are used when internal properties are being cloned.
	 * If the value of the original internal property has its type contained
	 * in the list of internal cloners, it too will be cloned during the
	 * creation of the new internal property.;
	 * 
	 * @param <T>		The generic type of property data that can be cloned
	 * 
	 * @param type		The class object of the property data that can be cloned
	 * @param cloner	The cloner implementation for this type
	 */
	public static <T> void addInternalCloner(Class<T> type, Cloner<T> cloner) {
		INTERNAL_CLONERS.put(type, cloner);
	}
	
	/**
	 * The property's value
	 */
	private T value;
	
	/**
	 * Creates a new internal property with a default value of
	 * {@code null}.
	 * 
	 * @param defaultPropertyType	The default type class of this property.
	 */
	public InternalProperty(Class<T> defaultPropertyType) {
		this((T) null, defaultPropertyType);
	}
	
	/**
	 * Creates a new internal property with a value specified
	 * by the value argument.
	 * 
	 * @param value					The initial value of this property.
	 * @param defaultPropertyType	The default type class of this property.
	 */
	protected InternalProperty(T value, Class<T> defaultPropertyType) {
		super(value, defaultPropertyType);
		this.value = value;
	}
	
	/**
	 * Creates a new internal property with a default value of
	 * {@code null}.
	 * <p>
	 * Since no property type is provided, this property's type
	 * must be specified by the implementable method <a href=
	 * "#{@link}">{@link Property#getPropertyType}</a>.
	 */
	protected InternalProperty() {
		this((T) null);
	}
	
	/**
	 * Creates a new internal property with a value specified
	 * by the value argument.
	 * <p>
	 * Since no property type is provided, this property's type
	 * will be specified by the implementable method <a href=
	 * "#{@link}">{@link Property#getPropertyType}</a>.
	 * 
	 * @param value	The initial value of this property.
	 */
	protected InternalProperty(T value) {
		super(value);
		this.value = value;
	}
	
	/**
	 * Creates a new internal property as a clone of an already
	 * existing internal property. All corresponding components
	 * are deep-cloned.
	 * <p>
	 * The value of the original property is also cloned if its
	 * type is contained in this class' list of internal cloners.
	 * 
	 * @see InternalProperty#addInternalCloner(Class, Cloner)
	 * @param src	The internal property to create a clone of.
	 */
	@SuppressWarnings("unchecked")
	protected InternalProperty(InternalProperty<T> src) {
		super(src);
		
		if(src.value != null) {
			for(Class<?> type : INTERNAL_CLONERS.keySet()) {
				if(type.isInstance(src.value)) {
					this.value = ((Cloner<T>) INTERNAL_CLONERS.get(type)).clone(src.value);
					return;
				}
			}
		}
		this.value = src.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getBase() {
		return value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBase(T value) {
		this.value = value;
	}
	
}
