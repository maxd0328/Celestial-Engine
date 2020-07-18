package celestial.util;

@FunctionalInterface
public interface Predicate<E> {
	
	public boolean validate(E e);
	
}
