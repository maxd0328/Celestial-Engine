package celestial.vecmath;

import java.nio.FloatBuffer;

public abstract class Matrix<T> extends GenericMatrix {
	
	private static final long serialVersionUID = -1673844327965908355L;
	
	protected Matrix() {
		super();
	}
	
	public void store(FloatBuffer buffer) {
		float[][] inBuf = readRaw();
		for(int i = 0 ; i < inBuf.length ; ++i) {
			for(int j = 0 ; j < inBuf[0].length ; ++j) {
				buffer.put(inBuf[i][j]);
			}
		}
	}
	
	public abstract T setIdentity();
	
	public abstract T setZero();
	
	public abstract T set(GenericMatrix src);
	
	public abstract T negate();
	
	public abstract T transpose();
	
	public abstract T invert();
	
	public abstract float determinant();
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(":: " + super.getClass().getName() + " ::\n");
		float[][] buffer = readRaw();
		for(int i = 0 ; i < buffer.length ; ++i) {
			str.append("[ ");
			for(int j = 0 ; j < buffer[0].length ; ++j) str.append(buffer[i][j]).append(" ");
			str.append("]" + (i == buffer.length - 1 ? "" : "\n"));
		}
		return str.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GenericMatrix)) return false;
		GenericMatrix mat = (GenericMatrix) obj;
		float[][] bufferA = readRaw(), bufferB = mat.readRaw();
		if(bufferA.length != bufferB.length || bufferA[0].length != bufferB[0].length) return false;
		for(int i = 0 ; i < bufferA.length ; ++i) {
			for(int j = 0 ; j < bufferA[0].length ; ++j) {
				if(bufferA[i][j] != bufferB[i][j]) return false;
			}
		}
		return true;
	}
	
}
