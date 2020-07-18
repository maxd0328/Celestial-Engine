package celestial.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import celestial.error.CelestialGenericException;

public abstract class StandardDeserializer implements Deserializer {
	
	protected abstract byte[] implRead(int len);
	
	@Override
	public abstract void close();
	
	@Override
	public boolean readBoolean() {
		return implRead(1)[0] != 0 ? true : false;
	}
	
	@Override
	public byte readByte() {
		return implRead(1)[0];
	}
	
	@Override
	public short readShort() {
		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(implRead(2)).getShort(0);
	}
	
	@Override
	public int readInt() {
		return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(implRead(4)).getInt(0);
	}
	
	@Override
	public long readLong() {
		return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(implRead(8)).getLong(0);
	}
	
	@Override
	public float readFloat() {
		return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(implRead(4)).getFloat(0);
	}
	
	@Override
	public double readDouble() {
		return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(implRead(8)).getDouble(0);
	}
	
	@Override
	public String readString() {
		int id = readByte();
		if(id == 0) return null;
		int length = readInt();
		return new String(implRead(length));
	}
	
	@Override
	public Object readNative() {
		ByteArrayInputStream b_in = new ByteArrayInputStream(readBytes());
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(new GZIPInputStream(b_in));
			return in.readObject();
		}
		catch(IOException | ClassNotFoundException ex) {
			throw new CelestialGenericException(ex.getMessage(), ex);
		}
		finally {
			try {
				if(in != null) in.close();
			}
			catch(IOException ex) {}
		}
	}
	
	@Override
	public <T> T read(Persistable<T> v) {
		int id = readByte();
		if(id == 0) return null;
		else return v.read(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T readUnknown(T v) {
		if(v instanceof Persistable) return (T) read((Persistable<?>) v);
		else if(v instanceof Serializable) return (T) readNative();
		else if(v instanceof Boolean) return (T) (Boolean) readBoolean();
		else if(v instanceof Byte) return (T) (Byte) readByte();
		else if(v instanceof Short) return (T) (Short) readShort();
		else if(v instanceof Integer) return (T) (Integer) readInt();
		else if(v instanceof Long) return (T) (Long) readLong();
		else if(v instanceof Float) return (T) (Float) readFloat();
		else if(v instanceof Double) return (T) (Double) readDouble();
		else throw new UnserializableException();
	}
	
	@Override
	public boolean[] readBooleans() {
		int len = readInt();
		boolean[] arr = new boolean[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readBoolean();
		return arr;
	}
	
	@Override
	public byte[] readBytes() {
		int len = readInt();
		return implRead(len);
	}
	
	@Override
	public short[] readShorts() {
		int len = readInt();
		short[] arr = new short[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readShort();
		return arr;
	}
	
	@Override
	public int[] readInts() {
		int len = readInt();
		int[] arr = new int[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readInt();
		return arr;
	}
	
	@Override
	public long[] readLongs() {
		int len = readInt();
		long[] arr = new long[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readLong();
		return arr;
	}
	
	@Override
	public float[] readFloats() {
		int len = readInt();
		float[] arr = new float[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readFloat();
		return arr;
	}
	
	@Override
	public double[] readDoubles() {
		int len = readInt();
		double[] arr = new double[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readDouble();
		return arr;
	}
	
	@Override
	public float[][] readFloatMap() {
		int len = readInt();
		float[][] map = new float[len][];
		for(int i = 0 ; i < len ; ++i) map[i] = readFloats();
		return map;
	}
	
	@Override
	public Object[] readNativeArray() {
		int len = readInt();
		Object[] arr = new Object[len];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = readNative();
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] readArray(Persistable<T> v) {
		int len = readInt();
		T[] arr = (T[]) Array.newInstance(v.getClass(), len);
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = read(v);
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] readUnknownArray(T v) {
		if(v instanceof Persistable) return (T[]) readArray((Persistable<?>) v);
		else if(v instanceof Serializable) return (T[]) readNativeArray();
		else if(v instanceof Boolean) return (T[]) ArrayAdapter.fromPrimitive(readBooleans());
		else if(v instanceof Byte) return (T[]) ArrayAdapter.fromPrimitive(readBytes());
		else if(v instanceof Short) return (T[]) ArrayAdapter.fromPrimitive(readShorts());
		else if(v instanceof Integer) return (T[]) ArrayAdapter.fromPrimitive(readInts());
		else if(v instanceof Long) return (T[]) ArrayAdapter.fromPrimitive(readLongs());
		else if(v instanceof Float) return (T[]) ArrayAdapter.fromPrimitive(readFloats());
		else if(v instanceof Double) return (T[]) ArrayAdapter.fromPrimitive(readDoubles());
		else throw new UnserializableException();
	}
	
	@Override
	public Collection<Boolean> readBooleanCollection() {
		Collection<Boolean> col = new ArrayList<Boolean>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readBoolean());
		return col;
	}
	
	@Override
	public Collection<Byte> readByteCollection() {
		Collection<Byte> col = new ArrayList<Byte>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readByte());
		return col;
	}
	
	@Override
	public Collection<Short> readShortCollection() {
		Collection<Short> col = new ArrayList<Short>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readShort());
		return col;
	}
	
	@Override
	public Collection<Integer> readIntCollection() {
		Collection<Integer> col = new ArrayList<Integer>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readInt());
		return col;
	}
	
	@Override
	public Collection<Long> readLongCollection() {
		Collection<Long> col = new ArrayList<Long>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readLong());
		return col;
	}
	
	@Override
	public Collection<Float> readFloatCollection() {
		Collection<Float> col = new ArrayList<Float>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readFloat());
		return col;
	}
	
	@Override
	public Collection<Double> readDoubleCollection() {
		Collection<Double> col = new ArrayList<Double>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readDouble());
		return col;
	}
	
	@Override
	public Collection<Object> readNativeCollection() {
		Collection<Object> col = new ArrayList<Object>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(readNative());
		return col;
	}
	
	@Override
	public <T> Collection<T> readCollection(Persistable<T> v) {
		Collection<T> col = new ArrayList<T>();
		int len = readInt();
		for(int i = 0 ; i < len ; ++i) col.add(read(v));
		return col;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> readUnknownCollection(T v) {
		if(v instanceof Persistable) return (Collection<T>) readCollection((Persistable<?>) v);
		else if(v instanceof Serializable) return (Collection<T>) readNativeCollection();
		else if(v instanceof Boolean) return (Collection<T>) readBooleanCollection();
		else if(v instanceof Byte) return (Collection<T>) readByteCollection();
		else if(v instanceof Short) return (Collection<T>) readShortCollection();
		else if(v instanceof Integer) return (Collection<T>) readIntCollection();
		else if(v instanceof Long) return (Collection<T>) readLongCollection();
		else if(v instanceof Float) return (Collection<T>) readFloatCollection();
		else if(v instanceof Double) return (Collection<T>) readDoubleCollection();
		else throw new UnserializableException();
	}
	
}
