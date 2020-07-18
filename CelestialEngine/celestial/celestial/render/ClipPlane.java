package celestial.render;

import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public final class ClipPlane {
	
	private boolean enabled;
	private Vector3f normal;
	private float distance;
	
	public ClipPlane(boolean enabled, Vector3f normal, float distance) {
		this.enabled = enabled;
		this.normal = normal;
		this.distance = distance;
	}
	
	public ClipPlane(Vector3f normal, float distance) {
		this(true, normal, distance);
	}
	
	public ClipPlane() {
		this(false, new Vector3f(0, 1, 0), 0);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public void disable() {
		this.enabled = false;
		this.normal = new Vector3f(0, -1, 0);
		this.distance = Float.MAX_VALUE;
	}
	
	public Vector3f getNormal() {
		return normal;
	}
	
	public void setNormal(Vector3f normal) {
		this.normal = normal;
	}
	
	public float getDistance() {
		return distance;
	}
	
	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	public Vector4f getEquationVector() {
		return new Vector4f(normal, distance);
	}
	
	public void set(Vector4f equation) {
		enable();
		this.normal = new Vector3f(equation);
		this.distance = equation.w;
	}
	
}
