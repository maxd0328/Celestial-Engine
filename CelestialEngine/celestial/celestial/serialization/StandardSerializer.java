package celestial.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import celestial.error.CelestialGenericException;

public abstract class StandardSerializer implements Serializer {
	
	protected abstract void implWrite(byte... arr);
	
	@Override
	public abstract void flush();
	
	@Override
	public abstract void close();
	
	@Override
	public void write(boolean v) {
		implWrite(v ? (byte) 1 : (byte) 0);
	}
	
	@Override
	public void write(byte v) {
		implWrite(v);
	}
	
	@Override
	public void write(short v) {
		implWrite(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(v).array());
	}
	
	@Override
	public void write(int v) {
		implWrite(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array());
	}
	
	@Override
	public void write(long v) {
		implWrite(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(v).array());
	}
	
	@Override
	public void write(float v) {
		byte[] arr = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(v).array();
		System.out.println(v + " " + arr[0] + " " + arr[1] + " " + arr[2] + " " + arr[3]);
		for(StackTraceElement e : Thread.currentThread().getStackTrace()) System.out.println(e);
		implWrite(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(v).array());
	}
	
	@Override
	public void write(double v) {
		implWrite(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(v).array());
	}
	
	@Override
	public void write(String v) {
		if(v == null) {
			implWrite(new byte[] { 0x0 });
			return;
		}
		
		implWrite(new byte[] { 0x1 });
		byte[] bytes = v.getBytes();
		write(bytes.length);
		implWrite(bytes);
	}
	
	@Override
	public void write(Serializable v) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream s_out = new ObjectOutputStream(new GZIPOutputStream(out));
			s_out.writeObject(v);
			s_out.flush();
			s_out.close();
			
			write(out.toByteArray());
		}
		catch(IOException ex) {
			throw new CelestialGenericException(ex.getMessage(), ex);
		}
		finally {
			try {
				out.close();
			}
			catch(IOException ex) {}
		}
	}
	
	@Override
	public <T> void write(Persistable<T> v) {
		if(v == null) {
			implWrite(new byte[] { 0x0 });
			return;
		}
		
		implWrite(new byte[] { 0x1 });
		v.write(this);
	}
	
	@Override
	public <T> void writeUnknown(T v) {
		if(v instanceof Persistable) write((Persistable<?>) v);
		if(v instanceof Serializable) write((Serializable) v);
		else if(v instanceof Boolean) write((boolean) (Boolean) v);
		else if(v instanceof Byte) write((byte) (Byte) v);
		else if(v instanceof Short) write((short) (Short) v);
		else if(v instanceof Integer) write((int) (Integer) v);
		else if(v instanceof Long) write((long) (Long) v);
		else if(v instanceof Float) write((float) (Float) v);
		else if(v instanceof Double) write((double) (Double) v);
		else throw new UnserializableException();
	}
	
	@Override
	public void write(boolean... arr) {
		write(arr.length);
		for(boolean v : arr) write(v);
	}
	
	@Override
	public void write(byte... arr) {
		write(arr.length);
		for(byte v : arr) write(v);
	}
	
	@Override
	public void write(short... arr) {
		write(arr.length);
		for(short v : arr) write(v);
	}
	
	@Override
	public void write(int... arr) {
		write(arr.length);
		for(int v : arr) write(v);
	}
	
	@Override
	public void write(long... arr) {
		write(arr.length);
		for(long v : arr) write(v);
	}
	
	@Override
	public void write(float... arr) {
		write(arr.length);
		for(float v : arr) write(v);
	}
	
	@Override
	public void write(double... arr) {
		write(arr.length);
		for(double v : arr) write(v);
	}
	
	@Override
	public void write(float[]... arr) {
		write(arr.length);
		for(float[] v : arr) write(v);
	}
	
	@Override
	public void write(Serializable... arr) {
		write(arr.length);
		for(Serializable v : arr) write(v);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void write(Persistable<T>... arr) {
		write(arr.length);
		for(Persistable<T> v : arr) write(v);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void writeUnknown(T... arr) {
		write(arr.length);
		for(T v : arr) {
			if(v instanceof Persistable) write((Persistable<?>) v);
			else if(v instanceof Serializable) write((Serializable) v);
			else if(v instanceof Boolean) write((Boolean) v);
			else if(v instanceof Byte) write((Byte) v);
			else if(v instanceof Short) write((Short) v);
			else if(v instanceof Integer) write((Integer) v);
			else if(v instanceof Long) write((Long) v);
			else if(v instanceof Float) write((Float) v);
			else if(v instanceof Double) write((Double) v);
			else throw new UnserializableException();
		}
	}
	
	@Override
	public void writeBooleans(Collection<Boolean> col) {
		write(col.size());
		for(boolean v : col) write(v);
	}
	
	@Override
	public void writeBytes(Collection<Byte> col) {
		write(col.size());
		for(byte v : col) write(v);
	}
	
	@Override
	public void writeShorts(Collection<Short> col) {
		write(col.size());
		for(short v : col) write(v);
	}
	
	@Override
	public void writeInts(Collection<Integer> col) {
		write(col.size());
		for(int v : col) write(v);
	}
	
	@Override
	public void writeLongs(Collection<Long> col) {
		write(col.size());
		for(long v : col) write(v);
	}
	
	@Override
	public void writeFloats(Collection<Float> col) {
		write(col.size());
		for(float v : col) write(v);
	}
	
	@Override
	public void writeDoubles(Collection<Double> col) {
		write(col.size());
		for(double v : col) write(v);
	}
	
	@Override
	public void writeNativeObjects(Collection<Serializable> col) {
		write(col.size());
		for(Serializable v : col) write(v);
	}
	
	@Override
	public <T> void writeObjects(Collection<Persistable<T>> col) {
		write(col.size());
		for(Persistable<T> v : col) write(v);
	}
	
	@Override
	public <T> void writeUnknownObjects(Collection<T> col) {
		write(col.size());
		for(T v : col) {
			if(v instanceof Persistable) write((Persistable<?>) v);
			else if(v instanceof Serializable) write((Serializable) v);
			else if(v instanceof Boolean) write((Boolean) v);
			else if(v instanceof Byte) write((Byte) v);
			else if(v instanceof Short) write((Short) v);
			else if(v instanceof Integer) write((Integer) v);
			else if(v instanceof Long) write((Long) v);
			else if(v instanceof Float) write((Float) v);
			else if(v instanceof Double) write((Double) v);
			else throw new UnserializableException();
		}
	}
	
}
