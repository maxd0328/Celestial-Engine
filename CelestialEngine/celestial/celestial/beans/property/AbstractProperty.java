package celestial.beans.property;

import java.io.IOException;
import java.io.ObjectInputStream;

import celestial.beans.driver.DriverSystem;
import celestial.beans.observable.ObservableImpl;
import celestial.collections.ObservableArrayList;
import celestial.serialization.SerializerImpl;
import celestial.vecmath.GenericVector;

/**
 * An abstract implementation of the property interface.
 * <p>
 * This class provides a baseline implementation for bindings,
 * listeners, but does not include the implementatinos for
 * getting and setting the value of the property. This is
 * handled entirely by the main sub-classes. Which are the
 * <a href="#{@link}">{@link ExternalProperty}</a> and
 * <a href="#{@link}">{@link InternalProperty}</a> classes.
 * 
 * @param <T>	The type of value that this property will hold.
 * 
 * @author Max D
 */
public abstract class AbstractProperty<T> extends ObservableImpl<T> implements Property<T> {
	
	private static final long serialVersionUID = -4569256417236941487L;
	
	/**
	 * The list of this property's bindings
	 */
	private final ObservableArrayList<Binding<T>> bindings;
	
	/**
	 * The driver system associated with this property
	 */
	private final DriverSystem<T> driverSystem;
	
	/**
	 * A default class object for this property's type
	 */
	private final Class<T> defaultPropertyType;
	
	/**
	 * The cached list of sub-properties extracted from this
	 * property. This used used for updating bindings attached
	 * to those properties.
	 */
	private final ObservableArrayList<VectorSubProperty> subPropertyCache;
	
	/**
	 * The user pointer currently attached to this property
	 */
	private java.io.Serializable userPtr = null;
	
	/**
	 * Object locks
	 */
	private transient Object bindingsLock = new Object();
	private transient Object driverLock = new Object();
	private transient Object cacheLock = new Object();
	private transient Object userPtrLock = new Object();
	
	/**
	 * Creates a new abstract property given the initial value. All
	 * bindings and listeners will be initialized with their default
	 * values.
	 * 
	 * @param initialValue			The initial value of this property
	 * @param defaultPropertyType	The default property type for this instance
	 */
	protected AbstractProperty(T initialValue, Class<T> defaultPropertyType) {
		super(new ObservableArrayList<>(), new ObservableArrayList<>(), initialValue);
		this.bindings = new ObservableArrayList<Binding<T>>();
		this.defaultPropertyType = defaultPropertyType;
		this.driverSystem = DriverSystem.createDriverSystem(this);
		this.subPropertyCache = new ObservableArrayList<>();
	}
	
	/**
	 * Creates a new abstract property given the initial value. All
	 * bindings and listeners will be initialized with their default
	 * values.
	 * <p>
	 * No default property type for this property will be provided.
	 * 
	 * @param initialValue			The initial value of this property
	 */
	protected AbstractProperty(T initialValue) {
		this(initialValue, null);
	}
	
	/**
	 * Creates a new abstract property given a previous property. This
	 * will clone the property and create a new property given the previous.
	 * 
	 * @param src	The property to create a clone of.
	 */
	protected AbstractProperty(AbstractProperty<T> src) {
		super(new ObservableArrayList<>(src.getListeners()), new ObservableArrayList<>(src.getEventListeners()), src.get());
		this.bindings = new ObservableArrayList<>();
		for(Binding<T> binding : src.bindings)
			this.bindings.add(new Binding<T>(binding, this));
		
		this.defaultPropertyType = src.defaultPropertyType;
		this.driverSystem = src.driverSystem == null ? null : src.driverSystem.clone();
		this.subPropertyCache = new ObservableArrayList<>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		if(GenericVector.class.isAssignableFrom(getPropertyType())) {
			GenericVector v = driverSystem == null ? ((GenericVector) getBase()).clone() : ((GenericVector) driverSystem.apply(getBase())).clone();
			synchronized(cacheLock) {
				for(VectorSubProperty prop : subPropertyCache)
					v.setAtIndex(prop.index, prop.get());
			}
			return (T) v;
		}
		else return driverSystem == null ? getBase() : driverSystem.apply(getBase());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T value) {
		setBase(value);
		synchronized(driverLock) {
			if(driverSystem != null)
				driverSystem.reset();
		}
		synchronized(cacheLock) {
			for(VectorSubProperty prop : subPropertyCache)
				if(prop.getDriver() != null)
					prop.getDriver().reset();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract T getBase();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void setBase(T value);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected T getValue() {
		return get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DriverSystem<T> getDriver() {
		synchronized(driverLock) {
			return driverSystem;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(Property<T> property, BindingOrder order) {
		synchronized(bindingsLock) {
			this.bindings.add(new Binding<T>(this, property, order));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind(Property<T> property) {
		synchronized(bindingsLock) {
			this.bindings.removeIf(binding -> binding.getGuest() == property);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Property<Float> subProperty(int index) {
		if(!GenericVector.class.isAssignableFrom(getPropertyType()))
			throw new UnsupportedOperationException("subProperty");
		
		VectorSubProperty prop;
		synchronized(cacheLock) {
			for(VectorSubProperty _prop : subPropertyCache)
				if(_prop.index == index)
					return _prop;
			
			prop = new VectorSubProperty((Property<GenericVector>) this, index);
			this.subPropertyCache.add(prop);
		}
		return prop;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(boolean advanceDrivers) {
		super.update();
		synchronized(bindingsLock) {
			for(Binding<T> binding : bindings)
				binding.update();
		}
		synchronized(cacheLock) {
			for(VectorSubProperty prop : subPropertyCache)
				prop.update(advanceDrivers);
		}
		synchronized(driverLock) {
			if(advanceDrivers && driverSystem != null)
				driverSystem.update(getBase());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update() {
		update(true);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If no default property type is provided,
	 * this method will return null.
	 * <p>
	 * It is recommended that this method is
	 * overriden in the event that no property
	 * type is explicitly provided in this class'
	 * constructor.
	 */
	@Override
	public Class<T> getPropertyType() {
		return defaultPropertyType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract Property<T> clone();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.io.Serializable getUserPointer() {
		synchronized(userPtrLock) {
			return userPtr;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserPointer(java.io.Serializable userPtr) {
		synchronized(userPtrLock) {
			this.userPtr = userPtr;
		}
	}
	
	/**
	 * @see java.io.Serializable
	 */
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.bindingsLock = new Object();
		this.driverLock = new Object();
		this.cacheLock = new Object();
		this.userPtrLock = new Object();
	}
	
	/**
	 * An external implementation meant to represent
	 * an individual component within a vector property.
	 * <p>
	 * Specifically, this class can turn an {@code x/y/z/w}
	 * component into stand alone property.
	 * 
	 * @author Max D
	 */
	private static final class VectorSubProperty extends ExternalProperty<Float> {
		
		private static final long serialVersionUID = 6931672133363780608L;
		
		/**
		 * The vector component to draw the property from
		 */
		private final Property<GenericVector> root;
		
		/**
		 * The index from within the vector property
		 */
		private final int index;
		
		/**
		 * Creates a new vector sub property with the vector
		 * property and the index.
		 * 
		 * @param root	The vector component to draw from
		 * @param index	The index to use for this property
		 */
		public VectorSubProperty(Property<GenericVector> root, int index) {
			super(() -> root.getBase().getAtIndex(index), s -> root.getBase().setAtIndex(index, s));
			this.root = root;
			this.index = index;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Float> getPropertyType() {
			return Float.class;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Property<Float> clone() {
			return new VectorSubProperty(root, index);
		}
		
	}
	
}
