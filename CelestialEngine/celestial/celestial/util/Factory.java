package celestial.util;

/**
 * A functional interface that describes a factory
 * for any generic type.
 * <p>
 * This interface is used primarily to create object
 * templates which can be used for reproducing objects
 * of a given type.
 * 
 * @param <T>	The type of object to be created.
 * 
 * @author Max D
 */
public interface Factory<T> extends java.io.Serializable {
	
	/**
	 * Creates and returns the object produced by this factory.
	 * 
	 * @return	The object produced by this factory.
	 */
	public T build();
	
	/**
	 * Creates and returns a null-returning factory of any
	 * arbitrary type.
	 * 
	 * @param <T>	The type of factory to create
	 * @return		The null-returning factory
	 */
	public static <T> Factory<T> nullFactory() {
		return () -> null;
	}
	
}
