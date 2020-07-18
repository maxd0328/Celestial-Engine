package celestial.util;

/**
 * A functional interface similar to a factory,
 * but does not create a default instantiation
 * of the object.
 * <p>
 * Instead, the cloner reads in an object and
 * creates and exact clone of that object.
 * <p>
 * This interface is vague enough that a deep or
 * shallow clone should depend solely on specific
 * implementation.
 * <p>
 * This interface is intended to be a flexible
 * replacement for the <a href="#{@link}">{@link
 * Cloneable}</a> interface, allowing clones to be
 * created externally, rather than within the object
 * itself.
 * 
 * @param <T>	The type of data to clone
 * 
 * @author Max D
 */
public interface Cloner<T> {
	
	/**
	 * Constructs a clone of the source object
	 * and returns it.
	 * <p>
	 * Whether it is a deep or shallow clone
	 * should depend on the underlying implementation
	 * upon which this cloner is being used.
	 * 
	 * @param src	The object to clone
	 * @return		The clone of the source object
	 */
	public T clone(T src);
	
}
