package celestial.serialization;

import java.io.Serializable;
import java.util.Collection;

public interface Serializer {
	
	public void write(boolean v);
	
	public void write(byte v);
	
	public void write(short v);
	
	public void write(int v);
	
	public void write(long v);
	
	public void write(float v);
	
	public void write(double v);
	
	public void write(String v);
	
	public void write(Serializable v);
	
	public <T> void write(Persistable<T> v);
	
	public <T> void writeUnknown(T v);
	
	public void write(boolean... arr);
	
	public void write(byte... arr);
	
	public void write(short... arr);
	
	public void write(int... arr);
	
	public void write(long... arr);
	
	public void write(float... arr);
	
	public void write(double... arr);
	
	public void write(float[]... arr);
	
	public void write(Serializable... arr);
	
	@SuppressWarnings("unchecked")
	public <T> void write(Persistable<T>... arr);
	
	@SuppressWarnings("unchecked")
	public <T> void writeUnknown(T... arr);
	
	public void writeBooleans(Collection<Boolean> col);
	
	public void writeBytes(Collection<Byte> col);
	
	public void writeShorts(Collection<Short> col);
	
	public void writeInts(Collection<Integer> col);
	
	public void writeLongs(Collection<Long> col);
	
	public void writeFloats(Collection<Float> col);
	
	public void writeDoubles(Collection<Double> col);
	
	public void writeNativeObjects(Collection<Serializable> col);
	
	public <T> void writeObjects(Collection<Persistable<T>> col);
	
	public <T> void writeUnknownObjects(Collection<T> col);
	
	public void flush();
	
	public void close();
	
}
