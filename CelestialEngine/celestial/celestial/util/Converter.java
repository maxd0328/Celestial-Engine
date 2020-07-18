package celestial.util;

@FunctionalInterface
public interface Converter<A, B> {
	
	public B convert(A a);
	
}
