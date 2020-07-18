package celestial.vecmath;

import java.io.Serializable;

public abstract class GenericMatrix implements Serializable {
	
	private static final long serialVersionUID = -2630990503176070301L;
	
	protected GenericMatrix() {
		super();
	}
	
	protected abstract float[][] readRaw();
	
	protected float[][] read(int size) {
		float[][] buffer = readRaw();
		if(buffer.length != size || buffer[0].length != size) {
			float[][] newBuffer = new float[size][size];
			for(int i = 0 ; i < size ; ++i) {
				for(int j = 0 ; j < size ; ++j) {
					newBuffer[i][j] = (i >= buffer.length || j >= buffer[0].length ? (i == j ? 1 : 0) : buffer[i][j]);
				}
			}
			return newBuffer;
		}
		else return buffer;
	}
	
}
