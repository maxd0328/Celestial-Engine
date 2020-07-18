package celestial.vecmath;

import java.io.Serializable;

public abstract class GenericVector implements Serializable {
	
	private static final long serialVersionUID = -2488256857989567428L;
	
	protected GenericVector() {
		super();
	}
	
	public abstract GenericVector set(GenericVector src);
	
	protected abstract float[] readRaw();
	
	protected float[] read(int size) {
		float[] buffer = readRaw();
		if(buffer.length != size) {
			float[] newBuffer = new float[size];
			for(int i = 0 ; i < size ; ++i) newBuffer[i] = (i >= buffer.length ? 0 : buffer[i]);
			return newBuffer;
		}
		else return buffer;
	}
	
	public int size() {
		return readRaw().length;
	}
	
	public float getAtIndex(int index) {
		return readRaw()[index];
	}
	
	public void setAtIndex(final int index, final float value) {
		float[] src = readRaw();
		GenericVector v = new GenericVector() {
			
			private static final long serialVersionUID = 0L;
			
			public GenericVector set(GenericVector src) { return this; }
			
			public float[] readRaw() {
				float[] arr = new float[src.length];
				for(int i = 0 ; i < arr.length ; ++i) arr[i] = src[i];
				arr[index] = value;
				return arr;
			}

			public GenericVector clone() { return null; }
			
		};
		set(v);
	}
	
	public abstract GenericVector clone();
	
}
