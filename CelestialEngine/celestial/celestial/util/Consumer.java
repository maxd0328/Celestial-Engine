package celestial.util;

@FunctionalInterface
public interface Consumer<T> {
	
	public void accept(T t);
	
}
