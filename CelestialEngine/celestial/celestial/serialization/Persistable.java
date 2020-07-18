package celestial.serialization;

public interface Persistable<E> {
	
	public void write(Serializer serializer);
	
	public E read(Deserializer deserializer);
	
}
