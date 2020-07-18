package celestial.glutil;

import java.io.Serializable;

public final class GLViewport implements Serializable {
	
	private static final long serialVersionUID = 2807190700853360277L;
	
	private final float minX, minY;
	private final float maxX, maxY;
	
	public GLViewport(float minX, float minY, float maxX, float maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}
	
	public float getMinX() {
		return minX;
	}
	
	public float getMinY() {
		return minY;
	}
	
	public float getMaxX() {
		return maxX;
	}
	
	public float getMaxY() {
		return maxY;
	}
	
	public int fitMinX(int bound) {
		return (int) (minX * bound);
	}
	
	public int fitMinY(int bound) {
		return (int) (minY * bound);
	}
	
	public int fitMaxX(int bound) {
		return (int) (maxX * bound) - (int) (minX * bound);
	}
	
	public int fitMaxY(int bound) {
		return (int) (maxY * bound) - (int) (minY * bound);
	}
	
	@Override
	public String toString() {
		return String.format("GLViewport:[(%f, %f) (%f, %f)]", minX, minY, maxX, maxY);
	}
	
	public static GLViewport changeMinX(GLViewport viewport, float minX) {
		return new GLViewport(minX, viewport.minY, viewport.maxX, viewport.maxY);
	}
	
	public static GLViewport changeMinY(GLViewport viewport, float minY) {
		return new GLViewport(viewport.minX, minY, viewport.maxX, viewport.maxY);
	}
	
	public static GLViewport changeMaxX(GLViewport viewport, float maxX) {
		return new GLViewport(viewport.minX, viewport.minY, maxX, viewport.maxY);
	}
	
	public static GLViewport changeMaxY(GLViewport viewport, float maxY) {
		return new GLViewport(viewport.minX, viewport.minY, viewport.maxX, maxY);
	}
	
}
