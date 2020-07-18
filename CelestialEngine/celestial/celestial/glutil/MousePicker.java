package celestial.glutil;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public final class MousePicker implements java.io.Serializable {
	
	private static final long serialVersionUID = 5860513576088166465L;
	
	private float scaleX, scaleY;
	private final Vector3f position;
	private final Matrix4f projectionMatrix;
	private final Matrix4f viewMatrix;
	
	private final Vector3f mouseRay;
	
	public MousePicker(float scaleX, float scaleY, Vector3f position, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.position = new Vector3f(position);
		this.projectionMatrix = new Matrix4f(projectionMatrix);
		this.viewMatrix = new Matrix4f(viewMatrix);
		this.mouseRay = new Vector3f(0, 0, 1);
	}
	
	public float getScaleX() {
		return scaleX;
	}
	
	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}
	
	public float getScaleY() {
		return scaleY;
	}
	
	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		this.projectionMatrix.set(projectionMatrix);
	}
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public void setViewMatrix(Matrix4f viewMatrix) {
		this.viewMatrix.set(viewMatrix);
	}
	
	public void update(Vector3f position, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
		if(position != null) this.position.set(position);
		if(projectionMatrix != null) this.projectionMatrix.set(projectionMatrix);
		if(viewMatrix != null) this.viewMatrix.set(viewMatrix);
		
		float mouseX = Mouse.getX(), mouseY = Mouse.getY();
		Vector2f ndc = new Vector2f((2.0f * mouseX) / Display.getWidth() - 1f, (2.0f * mouseY) / Display.getHeight() - 1f);
		Vector4f clipCoords = new Vector4f(ndc.x / scaleX, ndc.y / scaleY, -1.0f, 1.0f);
		Vector4f eyeCoords = new Vector4f(new Vector2f(this.projectionMatrix.clone().invert().transform(clipCoords)), -1.0f, 0.0f);
		Vector3f mouseRay = new Vector3f(this.viewMatrix.clone().invert().transform(eyeCoords)).normalize();
		
		this.mouseRay.set(mouseRay);
	}
	
	public void update() {
		update(null, null, null);
	}
	
	public Vector3f getIntersection(float a, float b, float c, float d) {
		Vector3f planeNormal = new Vector3f(a, b, c).normalize();
		Vector3f planePoint = new Vector3f(-planeNormal.x * d, -planeNormal.y * d, -planeNormal.z * d);
		Vector3f lineDirection = new Vector3f(mouseRay).normalize();
		Vector3f linePoint = new Vector3f(position);
		
		float signedDist = planeNormal.dot(Vector3f.sub(linePoint, planePoint));
		if(signedDist == 0) return null;
		else if(signedDist > 0 && lineDirection.dot(planeNormal) >= 0) return null;
		else if(signedDist < 0 && lineDirection.dot(planeNormal) <= 0) return null;
		
		float q = Vector3f.sub(planePoint, linePoint).dot(planeNormal);
		float r = lineDirection.dot(planeNormal);
		
		if(r == 0) return null;
		else {
			float x = q / r;
			Vector3f intersection = lineDirection.scale(x).translate(linePoint);
			return intersection;
		}
	}
	
	public float getDistance(Vector3f point) {
		Vector3f a = new Vector3f(position);
		Vector3f b = Vector3f.add(a, mouseRay);
		float d = Vector3f.cross(Vector3f.sub(b, a), Vector3f.sub(a, point)).length() / Vector3f.sub(b, a).length();
		
		return d;
	}
	
}
