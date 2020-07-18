package celestial.serialization;

import java.util.Collection;

public interface Deserializer {
	
	public boolean readBoolean();
	
	public byte readByte();
	
	public short readShort();
	
	public int readInt();
	
	public long readLong();
	
	public float readFloat();
	
	public double readDouble();
	
	public String readString();
	
	public Object readNative();
	
	public <T> T read(Persistable<T> v);
	
	public <T> T readUnknown(T v);
	
	public boolean[] readBooleans();
	
	public byte[] readBytes();
	
	public short[] readShorts();
	
	public int[] readInts();
	
	public long[] readLongs();
	
	public float[] readFloats();
	
	public double[] readDoubles();
	
	public float[][] readFloatMap();
	
	public Object[] readNativeArray();
	
	public <T> T[] readArray(Persistable<T> v);
	
	public <T> T[] readUnknownArray(T v);
	
	public Collection<Boolean> readBooleanCollection();
	
	public Collection<Byte> readByteCollection();
	
	public Collection<Short> readShortCollection();
	
	public Collection<Integer> readIntCollection();
	
	public Collection<Long> readLongCollection();
	
	public Collection<Float> readFloatCollection();
	
	public Collection<Double> readDoubleCollection();
	
	public Collection<Object> readNativeCollection();
	
	public <T> Collection<T> readCollection(Persistable<T> v);
	
	public <T> Collection<T> readUnknownCollection(T v);
	
	public void close();
	
}
