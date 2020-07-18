package mod.celestial.light;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.FloatConverter;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.NonZeroBounds;
import celestial.ctrl.PropertyController;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.render.RenderPacket;
import celestial.render.RenderReiterationException;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.shadow.ShadowMapSystem;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix3f;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.misc.CameraModifier;

public final class SpotLightModifier extends Modifier {
	
	private static final long serialVersionUID = -2202640890742620346L;
	
	public static final Factory<SpotLightModifier> FACTORY = () -> new SpotLightModifier(new Vector3f(), new Vector3f(), 0f, 0f, false, 1f, new Vector3f(), 0f, false, 0f, 1);
	
	protected static final ArrayList<SpotLight> ACTIVE_LIGHTS;
	
	static {
		ACTIVE_LIGHTS = new ArrayList<SpotLight>();
	}
	
	private final Property<Vector3f> offset;
	private final Property<Vector3f> direction;
	private final Property<Float> size;
	private final Property<Float> feather;
	private final Property<Boolean> attenuating;
	private final Property<Float> attenuationFactor;
	private final Property<Vector3f> color;
	private final Property<Float> intensity;
	private final Property<Boolean> castsShadows;
	private final Property<Float> shadowDistance;
	private final Property<Integer> shadowResolution;
	
	private final Map<CEObject, SpotLight> lights = new HashMap<CEObject, SpotLight>();
	
	public SpotLightModifier(Vector3f offset, Vector3f direction, float size, float feather, boolean attenuating, float attenuationFactor,
			Vector3f color, float intensity, boolean castsShadows, float shadowDistance, int shadowResolution) {
		super(false, true, false);
		this.offset = Properties.createVec3Property(offset);
		this.direction = Properties.createVec3Property(direction);
		this.size = Properties.createFloatProperty(size);
		this.feather = Properties.createFloatProperty(feather);
		this.attenuating = Properties.createBooleanProperty(attenuating);
		this.attenuationFactor = Properties.createFloatProperty(attenuationFactor);
		this.color = Properties.createVec3Property(color);
		this.intensity = Properties.createFloatProperty(intensity);
		this.castsShadows = Properties.createBooleanProperty(castsShadows);
		this.shadowDistance = Properties.createFloatProperty(shadowDistance);
		this.shadowResolution = Properties.createIntegerProperty(shadowResolution);
	}
	
	private SpotLightModifier(SpotLightModifier src) {
		super(false, true, false);
		this.offset = src.offset.clone();
		this.direction = src.direction.clone();
		this.size = src.size.clone();
		this.feather = src.feather.clone();
		this.attenuating = src.attenuating.clone();
		this.attenuationFactor = src.attenuationFactor.clone();
		this.color = src.color.clone();
		this.intensity = src.intensity.clone();
		this.castsShadows = src.castsShadows.clone();
		this.shadowDistance = src.shadowDistance.clone();
		this.shadowResolution = src.shadowResolution.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(ShadowMapSystem.INSTANCE.needsToRender()) {
			ShadowMapSystem.INSTANCE.registerOut(packet);
			throw new RenderReiterationException();
		}
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	public Light getLight(CEObject obj) {
		return lights.get(obj);
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(!lights.containsKey(obj))
			lights.put(obj, new SpotLight());
		SpotLight light = lights.get(obj);
		light.position = new Vector3f(obj.getTransformation().transform(new Vector4f(offset.get(), 1)));
		light.direction = new Matrix3f(obj.getRotationTransformation()).transform(direction.get());
		light.size = size.get();
		light.feather = feather.get();
		light.attenuating = attenuating.get();
		light.attenuationFactor = attenuationFactor.get();
		light.color = new Vector3f(color.get()).gammaCorrect().scale(intensity.get());
		light.resolution = shadowResolution.get();
		
		ACTIVE_LIGHTS.add(light);
		if(castsShadows.get() && Vector3f.sub(packet.getCamera().getPosition(), light.getPosition()).length() <= shadowDistance.get())
			ShadowMapSystem.INSTANCE.registerLight(light);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(ACTIVE_LIGHTS.size() > 0) ACTIVE_LIGHTS.clear();
		offset.update(!packet.isPaused());
		direction.update(!packet.isPaused());
		size.update(!packet.isPaused());
		feather.update(!packet.isPaused());
		attenuating.update(!packet.isPaused());
		attenuationFactor.update(!packet.isPaused());
		color.update(!packet.isPaused());
		intensity.update(!packet.isPaused());
		castsShadows.update(!packet.isPaused());
		shadowDistance.update(!packet.isPaused());
		shadowResolution.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Offset", offset);
		ctrl.withProperty("Direction", direction).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		ctrl.withProperty("Size", size);
		ctrl.withProperty("Feather", feather);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 0f), 2);
		ctrl.withProperty("Attenuating", attenuating);
		ctrl.withProperty("Attenuation Factor", attenuationFactor);
		ctrl.withProperty("Color", color);
		ctrl.withProperty("Intensity", intensity);
		ctrl.withProperty("Casts Shadows", castsShadows);
		ctrl.withProperty("Shadow Distance", shadowDistance)
			.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 0f), 1);
		ctrl.withProperty("Shadow Resolution", shadowResolution)
			.withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 1, IntervalType.INCLUSIVE, Integer.MAX_VALUE, IntervalType.INCLUSIVE, 1), 1);
		return ctrl;
	}
	
	public Modifier duplicate() {
		return new SpotLightModifier(this);
	}
	
	public Vector3f getOffset() {
		return offset.get();
	}
	
	public void setOffset(Vector3f offset) {
		this.offset.set(offset);
	}
	
	public Property<Vector3f> offsetProperty() {
		return offset;
	}
	
	public Vector3f getDirection() {
		return direction.get();
	}
	
	public void setDirection(Vector3f direction) {
		this.direction.set(direction);
	}
	
	public Property<Vector3f> directionProperty() {
		return direction;
	}
	
	public float getSize() {
		return size.get();
	}
	
	public void setSize(float size) {
		this.size.set(size);
	}
	
	public Property<Float> sizeProperty() {
		return size;
	}
	
	public float getFeather() {
		return feather.get();
	}
	
	public void setFeather(float feather) {
		this.feather.set(feather);
	}
	
	public Property<Float> featherProperty() {
		return feather;
	}
	
	public boolean isAttenuating() {
		return attenuating.get();
	}
	
	public void setAttenuating(boolean attenuating) {
		this.attenuating.set(attenuating);
	}
	
	public Property<Boolean> attenuatingProperty() {
		return attenuating;
	}
	
	public float getAttenuationFactor() {
		return attenuationFactor.get();
	}
	
	public void setAttenuationFactor(float attenuationFactor) {
		this.attenuationFactor.set(attenuationFactor);
	}
	
	public Property<Float> attenuationFactorProperty() {
		return attenuationFactor;
	}
	
	public Vector3f getColor() {
		return color.get();
	}
	
	public void setColor(Vector3f color) {
		this.color.set(color);
	}
	
	public Property<Vector3f> colorProperty() {
		return color;
	}
	
	public float getIntensity() {
		return intensity.get();
	}
	
	public void setIntensity(float intensity) {
		this.intensity.set(intensity);
	}
	
	public Property<Float> intensityProperty() {
		return intensity;
	}
	
	public boolean isCastsShadows() {
		return castsShadows.get();
	}
	
	public void setCastsShadows(boolean castsShadows) {
		this.castsShadows.set(castsShadows);
	}
	
	public Property<Boolean> castsShadowsProperty() {
		return castsShadows;
	}
	
	public float getShadowDistance() {
		return shadowDistance.get();
	}
	
	public void setShadowDistance(float shadowDistance) {
		this.shadowDistance.set(shadowDistance);
	}
	
	public Property<Float> shadowDistanceProperty() {
		return shadowDistance;
	}
	
	public int getShadowResolution() {
		return shadowResolution.get();
	}
	
	public void setShadowResolution(int shadowResolution) {
		this.shadowResolution.set(shadowResolution);
	}
	
	public Property<Integer> shadowResolutionProperty() {
		return shadowResolution;
	}
	
	public static final class SpotLight implements Light, java.io.Serializable {
		
		private static final long serialVersionUID = 5631124503786658387L;
		
		private Vector3f position;
		private Vector3f direction;
		private float size, feather;
		private boolean attenuating;
		private float attenuationFactor;
		private Vector3f color;
		private int resolution;
		
		private SpotLight() {
		}
		
		@Override
		public Vector3f getPosition() {
			return position;
		}
		
		public Vector3f getDirection() {
			return direction;
		}
		
		public float getSize() {
			return size;
		}
		
		public float getFeather() {
			return feather;
		}
		
		public boolean isAttenuating() {
			return attenuating;
		}
		
		public float getAttenuationFactor() {
			return attenuationFactor;
		}
		
		public Vector3f getColor() {
			return color;
		}
		
		@Override
		public Matrix4f getViewMatrix(RenderPacket pckt) {
			Matrix4f viewMatrix = new Matrix4f();
			Vector3f dir = direction.normalize();
			float pitch = (float) -Math.acos(new Vector2f(dir.x, dir.z).length()) * (dir.y < 0 ? -1 : 1);
			viewMatrix.rotate(pitch, new Vector3f(1, 0, 0));
			float yaw = (float) Math.toDegrees(((float) Math.atan(dir.x / dir.z)));
			yaw = direction.z > 0 ? yaw - 180 : yaw;
			viewMatrix.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0));
			viewMatrix.translate(position.clone().negate());
			return viewMatrix;
		}
		
		@Override
		public Matrix4f getProjectionMatrix(RenderPacket pckt) {
			CameraModifier src = pckt.getCamera().getModifier(CameraModifier.class);
			return CameraModifier.createPerspectiveProjectionMatrix(src.getNearPlane(), src.getFarPlane(), size * 2 + 5f /* angle bias */, 1f);
		}
		
		@Override
		public boolean castsPointShadows() {
			return false;
		}
		
		@Override
		public int getShadowMapResolution() {
			return resolution;
		}
		
	}
	
}
