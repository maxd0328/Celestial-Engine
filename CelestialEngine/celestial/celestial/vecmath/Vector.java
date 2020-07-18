package celestial.vecmath;

import java.text.DecimalFormat;

public abstract class Vector<T> extends GenericVector {
	
	private static final long serialVersionUID = -8799079254182353671L;
	
	protected Vector() {
		super();
	}
	
	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}
	
	public abstract float lengthSquared();
	
	public abstract T negate();
	
	public abstract T scale(float amount);
	
	public T normalize() {
		float len = this.length();
		if(len == 0f) throw new VecmathException("Cannot normalize a zero-length vector");
		return this.scale(1f / len);
	}
	
	public T gammaCorrect() {
		float[] arr = readRaw();
		float maxValue = -Float.MAX_VALUE;
		for(int i = 0 ; i < arr.length ; ++i)
			if(arr[i] > maxValue)
				maxValue = arr[i];
		return this.scale(1f / maxValue);
	}
	
	public String toNotation(String format) {
		DecimalFormat f = new DecimalFormat(format);
		float[] arr = readRaw();
		String out = "(" + (arr.length == 0 ? "" : f.format(arr[0]));
		for(int i = 1 ; i < arr.length ; ++i) out += "," + f.format(arr[i]);
		return out + ")";
	}
	
	@Override
	public String toString() {
		float[] arr = readRaw();
		String vecStr = arr.length == 0 ? "" : Float.toString(arr[0]);
		for(int i = 1 ; i < arr.length ; ++i) vecStr += ", " + Float.toString(arr[i]);
		return super.getClass().getName() + "(" + vecStr + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GenericVector)) return false;
		GenericVector vec = (GenericVector) obj;
		float[] bufferA = readRaw(), bufferB = vec.readRaw();
		if(bufferA.length != bufferB.length) return false;
		for(int i = 0 ; i < bufferA.length ; ++i) if(bufferA[i] != bufferB[i]) return false;
		return true;
	}
	
}
