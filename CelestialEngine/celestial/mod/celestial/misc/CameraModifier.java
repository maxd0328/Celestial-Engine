package mod.celestial.misc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.Display;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.beans.property.SelectiveProperty.PropertySelection;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.FloatConverter;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.NonZeroBounds;
import celestial.ctrl.PropertyController;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.glutil.MousePicker;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.serialization.SerializerImpl;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public class CameraModifier extends Modifier {
	
	private static final long serialVersionUID = -2551196924169499980L;
	
	public static final Factory<CameraModifier> FACTORY = () -> new CameraModifier(70, 1, 1, 1, 1000, 1f, 1f);
	
	public static final ProjectionState STATE_PERSPECTIVE = ProjectionState.PERSPECTIVE;
	public static final ProjectionState STATE_ORTHOGRAPHIC = ProjectionState.ORTHOGRAPHIC;
	
	private final Matrix4f viewMatrix, projectionMatrix;
	private Matrix4f reflectionMatrix = null;
	private final Property<Float> FOV, orthoWidth, orthoHeight, nearPlane, farPlane;
	private final Property<Float> mouseScaleX, mouseScaleY;
	private final Property<ProjectionState> state;
	private transient FloatBuffer listenerOri;
	private final MousePicker mousePicker;
	
	public CameraModifier(float FOV, float orthoWidth, float orthoHeight, float nearPlane, float farPlane, float mouseScaleX, float mouseScaleY) {
		super(false, false, false);
		this.FOV = Properties.createFloatProperty(FOV);
		this.orthoWidth = Properties.createFloatProperty(orthoWidth);
		this.orthoHeight = Properties.createFloatProperty(orthoHeight);
		this.nearPlane = Properties.createFloatProperty(nearPlane);
		this.farPlane = Properties.createFloatProperty(farPlane);
		this.mouseScaleX = Properties.createFloatProperty(mouseScaleX);
		this.mouseScaleY = Properties.createFloatProperty(mouseScaleY);
		this.state = Properties.createProperty(ProjectionState.class, STATE_PERSPECTIVE);
		this.viewMatrix = new Matrix4f();
		Matrix4f proj = createProjectionMatrix();
		this.projectionMatrix = proj == null ? new Matrix4f() : proj;
		this.listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f});
		this.listenerOri.flip();
		this.mousePicker = new MousePicker(mouseScaleX, mouseScaleY, new Vector3f(), projectionMatrix, viewMatrix);
		
		this.state.addListener((obs, _old, _new) -> this.projectionMatrix.set(createProjectionMatrix()));
	}
	
	public CameraModifier(CameraModifier src) {
		super(false, false, false);
		this.FOV = src.FOV.clone();
		this.orthoWidth = src.orthoWidth.clone();
		this.orthoHeight = src.orthoHeight.clone();
		this.nearPlane = src.nearPlane.clone();
		this.farPlane = src.farPlane.clone();
		this.mouseScaleX = src.mouseScaleX.clone();
		this.mouseScaleY = src.mouseScaleY.clone();
		this.state = src.state.clone();
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = createProjectionMatrix();
		this.listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f});
		this.listenerOri.flip();
		this.mousePicker = new MousePicker(1f, 1f, new Vector3f(), projectionMatrix, viewMatrix);
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(packet.getCamera() != obj) return;
		updateViewMatrix(obj);
	}
	
	private float pFOV, pNearPlane, pFarPlane, pOrthoWidth, pOrthoHeight, pWidth, pHeight;
	private Vector3f pPosition = new Vector3f();
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(packet.getCamera() != obj) return;
		if(!(pFOV == FOV.get() && pNearPlane == nearPlane.get() && pFarPlane == farPlane.get() && pOrthoWidth == orthoWidth.get() &&
				pOrthoHeight == orthoHeight.get() && pWidth == Display.getWidth() && pHeight == Display.getHeight())) {
			projectionMatrix.set(createProjectionMatrix());
			pFOV = FOV.get();
			pNearPlane = nearPlane.get();
			pFarPlane = farPlane.get();
			pOrthoWidth = orthoWidth.get();
			pOrthoHeight = orthoHeight.get();
			pWidth = Display.getWidth();
			pHeight = Display.getHeight();
		}
		
		FOV.update(!packet.isPaused());
		nearPlane.update(!packet.isPaused());
		farPlane.update(!packet.isPaused());
		orthoWidth.update(!packet.isPaused());
		orthoHeight.update(!packet.isPaused());
		mouseScaleX.update(!packet.isPaused());
		mouseScaleY.update(!packet.isPaused());
		state.update(!packet.isPaused());
		
		Vector3f velocity = Vector3f.sub(obj.getPosition(), pPosition);
		AL10.alListener3f(AL10.AL_POSITION, obj.getPosition().x, obj.getPosition().y, obj.getPosition().z);
		AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
		pPosition = obj.getPosition();
		
		Vector3f viewVector = obj.getForwardVector(), upVector = obj.getUpVector();
		listenerOri.put(0, viewVector.x);
		listenerOri.put(1, viewVector.y);
		listenerOri.put(2, viewVector.z);
		listenerOri.put(3, upVector.x);
		listenerOri.put(4, upVector.y);
		listenerOri.put(5, upVector.z);
		AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
		
		mousePicker.setScaleX(mouseScaleX.get());
		mousePicker.setScaleY(mouseScaleY.get());
		mousePicker.update(obj.getPosition(), projectionMatrix, viewMatrix);
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Camera FOV", FOV);
		ctrl.withProperty("Near Plane", nearPlane);
		ctrl.withProperty("Far Plane", farPlane);
		ctrl.withProperty("Ortho Width", orthoWidth);
		ctrl.withProperty("Ortho Height", orthoHeight);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.EXCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 5);
		
		ctrl.withProperty("Mouse Scale X", mouseScaleX).withPropertyBounds(new NonZeroBounds<Float>(Float.class, FloatConverter.FLOAT_CONVERTER), 1);
		ctrl.withProperty("Mouse Scale Y", mouseScaleY).withPropertyBounds(new NonZeroBounds<Float>(Float.class, FloatConverter.FLOAT_CONVERTER), 1);
		
		ctrl.withProperty("Projection Type", new SelectiveProperty<ProjectionState>(state, ProjectionState.toSelectionList()));
		return ctrl;
	}
	
	public Modifier duplicate() {
		return new CameraModifier(this);
	}
	
	public float getFOV() {
		return FOV.get();
	}
	
	public void setFOV(float FOV) {
		this.FOV.set(FOV);
	}
	
	public Property<Float> FOVProperty() {
		return FOV;
	}
	
	public float getOrthoWidth() {
		return orthoWidth.get();
	}
	
	public void setOrthoWidth(float orthoWidth) {
		this.orthoWidth.set(orthoWidth);
	}
	
	public Property<Float> orthoWidthProperty() {
		return orthoWidth;
	}
	
	public float getOrthoHeight() {
		return orthoHeight.get();
	}
	
	public void setOrthoHeight(float orthoHeight) {
		this.orthoHeight.set(orthoHeight);
	}
	
	public Property<Float> orthoHeightProperty() {
		return orthoHeight;
	}
	
	public float getNearPlane() {
		return nearPlane.get();
	}
	
	public void setNearPlane(float nearPlane) {
		this.nearPlane.set(nearPlane);
	}
	
	public Property<Float> nearPlaneProperty() {
		return nearPlane;
	}
	
	public float getFarPlane() {
		return farPlane.get();
	}
	
	public void setFarPlane(float farPlane) {
		this.farPlane.set(farPlane);
	}
	
	public Property<Float> farPlaneProperty() {
		return farPlane;
	}
	
	public float getMouseScaleX() {
		return mouseScaleX.get();
	}
	
	public void setMouseScaleX(float mouseScaleX) {
		this.mouseScaleX.set(mouseScaleX);
	}
	
	public Property<Float> mouseScaleXProperty() {
		return mouseScaleX;
	}
	
	public float getMouseScaleY() {
		return mouseScaleY.get();
	}
	
	public void setMouseScaleY(float mouseScaleY) {
		this.mouseScaleY.set(mouseScaleY);
	}
	
	public Property<Float> mouseScaleYProperty() {
		return mouseScaleY;
	}
	
	public ProjectionState getProjectionState() {
		return state.get();
	}
	
	public void setProjectionState(ProjectionState state) {
		this.state.set(state);
	}
	
	public Property<ProjectionState> stateProperty() {
		return state;
	}
	
	public void updateViewMatrix(CEObject obj) {
		viewMatrix.setIdentity();
		viewMatrix.rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
		viewMatrix.rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
		viewMatrix.rotate((float) Math.toRadians(obj.getRotation().z), new Vector3f(0, 0, 1));
		Vector3f negativeCameraPos = new Vector3f(obj.getPosition()).negate();
		viewMatrix.translate(negativeCameraPos);
		if(reflectionMatrix != null) viewMatrix.mul(reflectionMatrix);
	}
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	public MousePicker getMousePicker() {
		return mousePicker;
	}
	
	public void reflect(Vector4f plane, CEObject cameraObj) {
		if(plane == null) {
			reflectionMatrix = null;
			updateViewMatrix(cameraObj);
			return;
		}
		Vector4f p = new Vector4f(new Vector3f(plane).normalize(), plane.w);
		reflectionMatrix = new Matrix4f(
				-2 * p.x * p.x + 1, -2 * p.y * p.x, -2 * p.z * p.x, 0,
				-2 * p.x * p.y, -2 * p.y * p.y + 1, -2 * p.z * p.y, 0,
				-2 * p.x * p.z, -2 * p.y * p.z, -2 * p.z * p.z + 1, 0,
				-2 * p.x * p.w, -2 * p.y * p.w, -2 * p.z * p.w, 1
		);
				
		updateViewMatrix(cameraObj);
	}
	
	public boolean isReflected() {
		return reflectionMatrix != null;
	}
	
	protected Matrix4f createProjectionMatrix() {
		if(this.state.get() == STATE_ORTHOGRAPHIC)
			return createOrthographicProjectionMatrix(nearPlane.get(), farPlane.get(), orthoWidth.get(), orthoHeight.get(), (float) Display.getWidth() / (float) Display.getHeight());
		else
			return createPerspectiveProjectionMatrix(nearPlane.get(), farPlane.get(), FOV.get(), (float) Display.getWidth() / (float) Display.getHeight());
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f});
		this.listenerOri.flip();
	}
	
	public static enum ProjectionState {
		
		PERSPECTIVE,
		
		ORTHOGRAPHIC;
		
		@SuppressWarnings("unchecked")
		public static PropertySelection<ProjectionState>[] toSelectionList() {
			return (PropertySelection<ProjectionState>[]) new PropertySelection[] {
					new PropertySelection<ProjectionState>("Perspective", PERSPECTIVE),
					new PropertySelection<ProjectionState>("Orthographic", ORTHOGRAPHIC)
			};
		}
		
	}
	
	public static Matrix4f createOrthographicProjectionMatrix(float nearDistance, float farDistance, float orthoWidth, float orthoHeight, float aspectRatio) {
		float left = -orthoWidth * aspectRatio;
		float right = orthoWidth * aspectRatio;
		float bottom = -orthoHeight;
		float top = orthoHeight;
		float near = nearDistance;
		float far = farDistance;
		float x_orth = 2f / (right - left);
		float y_orth = 2f / (top - bottom);
		float z_orth = -2f / (far - near);
		
		Matrix4f projectionMat = new Matrix4f();
		projectionMat.setIdentity();
		projectionMat.m00 = x_orth;
		projectionMat.m11 = y_orth;
		projectionMat.m22 = z_orth;
		projectionMat.m33 = 1;
		return projectionMat;
	}
	
	public static Matrix4f createPerspectiveProjectionMatrix(float nearDistance, float farDistance, float FOV, float aspectRatio) {
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = farDistance - nearDistance;
		
		Matrix4f projectionMat = new Matrix4f();
		projectionMat.m00 = x_scale;
		projectionMat.m11 = y_scale;
		projectionMat.m22 = -((farDistance + nearDistance) / frustum_length);
		projectionMat.m23 = -1;
		projectionMat.m32 = -((2 * nearDistance * farDistance) / frustum_length);
		projectionMat.m33 = 0;
		
		return projectionMat;
	}
	
}
