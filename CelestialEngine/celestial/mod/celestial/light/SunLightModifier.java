package mod.celestial.light;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

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
import celestial.shadow.ShadowCuboid;
import celestial.shadow.ShadowMapSystem;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix3f;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;

public final class SunLightModifier extends Modifier {
	
	private static final long serialVersionUID = -2202640890742620346L;
	
	public static final Factory<SunLightModifier> FACTORY = () -> new SunLightModifier(new Vector3f(), new Vector3f(), 0f, false, 1f, 1f, 0f, 1);
	
	protected static final ArrayList<SunLight> ACTIVE_LIGHTS;
	
	static {
		ACTIVE_LIGHTS = new ArrayList<SunLight>();
	}
	
	private final Property<Vector3f> direction;
	private final Property<Vector3f> color;
	private final Property<Float> intensity;
	private final Property<Boolean> castsShadows;
	private final Property<Float> shadowBoxDepth;
	private final Property<Float> shadowBoxExpansion;
	private final Property<Float> shadowBackcast;
	private final Property<Integer> shadowResolution;
	
	private final Map<CEObject, SunLight> lights = new HashMap<CEObject, SunLight>();
	
	public SunLightModifier(Vector3f direction, Vector3f color, float intensity, boolean castsShadows,
			float shadowBoxDepth, float shadowBoxExpansion, float shadowBackcast, int shadowResolution) {
		super(false, true, false);
		this.direction = Properties.createVec3Property(direction);
		this.color = Properties.createVec3Property(color);
		this.intensity = Properties.createFloatProperty(intensity);
		this.castsShadows = Properties.createBooleanProperty(castsShadows);
		this.shadowBoxDepth = Properties.createFloatProperty(shadowBoxDepth);
		this.shadowBoxExpansion = Properties.createFloatProperty(shadowBoxExpansion);
		this.shadowBackcast = Properties.createFloatProperty(shadowBackcast);
		this.shadowResolution = Properties.createIntegerProperty(shadowResolution);
	}
	
	private SunLightModifier(SunLightModifier src) {
		super(false, true, false);
		this.direction = src.direction.clone();
		this.color = src.color.clone();
		this.intensity = src.intensity.clone();
		this.castsShadows = src.castsShadows.clone();
		this.shadowBoxDepth = src.shadowBoxDepth.clone();
		this.shadowBoxExpansion = src.shadowBoxExpansion.clone();
		this.shadowBackcast = src.shadowBackcast.clone();
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
			lights.put(obj, new SunLight());
		SunLight light = lights.get(obj);
		light.direction = new Matrix3f(obj.getRotationTransformation()).transform(direction.get());
		light.color = new Vector3f(color.get()).gammaCorrect().scale(intensity.get());
		light.shadowBoxDepth = shadowBoxDepth.get();
		light.shadowBoxExpansion = shadowBoxExpansion.get();
		light.shadowBackcast = shadowBackcast.get();
		light.resolution = shadowResolution.get();
		
		ACTIVE_LIGHTS.add(light);
		if(castsShadows.get())
			ShadowMapSystem.INSTANCE.registerLight(light);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(ACTIVE_LIGHTS.size() > 0) ACTIVE_LIGHTS.clear();
		direction.update(!packet.isPaused());
		color.update(!packet.isPaused());
		intensity.update(!packet.isPaused());
		castsShadows.update(!packet.isPaused());
		shadowBoxDepth.update(!packet.isPaused());
		shadowBoxExpansion.update(!packet.isPaused());
		shadowBackcast.update(!packet.isPaused());
		shadowResolution.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Direction", direction).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		ctrl.withProperty("Color", color);
		ctrl.withProperty("Intensity", intensity);
		ctrl.withProperty("Casts Shadows", castsShadows);
		ctrl.withProperty("Shadow Box Depth", shadowBoxDepth);
		ctrl.withProperty("Shadow Box Expansion", shadowBoxExpansion);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.EXCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 2);
		ctrl.withProperty("Shadow Backcast", shadowBackcast);
		ctrl.withProperty("Shadow Resolution", shadowResolution)
			.withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 1, IntervalType.INCLUSIVE, Integer.MAX_VALUE, IntervalType.INCLUSIVE, 1), 1);
		return ctrl;
	}
	
	public Modifier duplicate() {
		return new SunLightModifier(this);
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
	
	public float getShadowBoxDepth() {
		return shadowBoxDepth.get();
	}
	
	public void setShadowBoxDepth(float shadowBoxDepth) {
		this.shadowBoxDepth.set(shadowBoxDepth);
	}
	
	public Property<Float> shadowBoxDepthProperty() {
		return shadowBoxDepth;
	}
	
	public float getShadowBoxExpansion() {
		return shadowBoxExpansion.get();
	}
	
	public void setShadowBoxExpansion(float shadowBoxExpansion) {
		this.shadowBoxExpansion.set(shadowBoxExpansion);
	}
	
	public Property<Float> shadowBoxExpansionProperty() {
		return shadowBoxExpansion;
	}
	
	public float getShadowBackcast() {
		return shadowBackcast.get();
	}
	
	public void setShadowBackcast(float shadowBackcast) {
		this.shadowBackcast.set(shadowBackcast);
	}
	
	public Property<Float> shadowBackcastProperty() {
		return shadowBackcast;
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
	
	public static final class SunLight implements Light, java.io.Serializable {
		
		private static final long serialVersionUID = 5631124503786658387L;
		
		private Vector3f direction;
		private Vector3f color;
		private float shadowBoxDepth;
		private float shadowBoxExpansion;
		private float shadowBackcast;
		private int resolution;
		
		private final ShadowCuboid cuboid = new ShadowCuboid();
		private final Matrix4f lightViewMatrix = new Matrix4f();
		
		private SunLight() {
		}
		
		@Override
		public Vector3f getPosition() {
			return new Vector3f();
		}
		
		public Vector3f getDirection() {
			return direction;
		}
		
		public Vector3f getColor() {
			return color;
		}
		
		public float getShadowBoxDepth() {
			return shadowBoxDepth;
		}
		
		public float getShadowBoxExpansion() {
			return shadowBoxExpansion;
		}
		
		public float getShadowBackcast() {
			return shadowBackcast;
		}
		
		@Override
		public float absDistance() {
			return 0;
		}
		
		@Override
		public Matrix4f getViewMatrix(RenderPacket pckt) {
			cuboid.update(pckt, shadowBoxDepth, shadowBoxExpansion, shadowBackcast, lightViewMatrix);
			lightViewMatrix.setIdentity();
			Vector3f dir = direction.clone().normalize();
			float pitch = (float) Math.acos(new Vector2f(dir.x, dir.z).length());
			lightViewMatrix.rotate(pitch, new Vector3f(1, 0, 0));
			float yaw = (float) Math.toDegrees(((float) Math.atan(dir.x / dir.z)));
			yaw = dir.z > 0 ? yaw - 180 : yaw;
			lightViewMatrix.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0));
			Vector3f center = cuboid.getCenter(lightViewMatrix).negate();
			lightViewMatrix.translate(center);
			return lightViewMatrix;
		}
		
		@Override
		public Matrix4f getProjectionMatrix(RenderPacket pckt) {
			cuboid.update(pckt, shadowBoxDepth, shadowBoxExpansion, shadowBackcast, lightViewMatrix);
			Matrix4f projectionMatrix = new Matrix4f();
			projectionMatrix.m00 = 2f / cuboid.getWidth();
			projectionMatrix.m11 = 2f / cuboid.getHeight();
			projectionMatrix.m22 = -2f / cuboid.getLength();
			projectionMatrix.m33 = 1;
			return projectionMatrix;
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
