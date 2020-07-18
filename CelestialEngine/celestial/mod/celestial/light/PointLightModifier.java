package mod.celestial.light;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.PropertyController;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.render.RenderPacket;
import celestial.render.RenderReiterationException;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.shadow.ShadowMap;
import celestial.shadow.ShadowMap2D;
import celestial.shadow.ShadowMapCube;
import celestial.shadow.ShadowMapSystem;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.light.AreaLightModifier.AreaLight;
import mod.celestial.light.DirectionalLightModifier.DirectionalLight;
import mod.celestial.light.SpotLightModifier.SpotLight;
import mod.celestial.light.SunLightModifier.SunLight;
import mod.celestial.misc.CameraModifier;

public final class PointLightModifier extends Modifier {
	
	private static final long serialVersionUID = -6809049279325264787L;
	
	public static final Factory<PointLightModifier> FACTORY = () -> new PointLightModifier(new Vector3f(), new Vector3f(), 0f, false, 0f, false, false, 0f, 1);
	
	public static final int LIGHT_MAX = 8;
	public static final int SHADOW_MAX = 3;
	/* POINT SHADOW MAXIMUM IS 1 - CANNOT CHANGE */
	
	protected static final ArrayList<PointLight> ACTIVE_LIGHTS;
	protected static final Vector3f CUR_OBJPOS = new Vector3f();
	
	static {
		ACTIVE_LIGHTS = new ArrayList<PointLight>();
	}
	
	private static final int FLAG_NO_MODIFICATION		= 0;
	private static final int FLAG_NULLIFY_ATTENUATION	= 1;
	private static final int FLAG_NULLIFY_COSTHETA		= 2;
	private static final int FLAG_RADIAL_CONSTRAINTS	= 4;
	private static final int FLAG_AREA_CONSTRAINTS		= 8;
	
	/*
	 * Value masking key (materialArgs.x):
	 * 
	 * VALUE		DESCRIPTION
	 * -----        -----------
	 * 0.0			No modification
	 * 1.0			Nullify attenuation
	 * 2.0			Nullify cosTheta
	 * 3.0			1.0 and 2.0
	 * 4.0			Use materialArgs.yz as radial constraints
	 * 8.0			Use materialArgs.yz as area constraints
	 * 16.0			Treat light as infinite sun-light
	 * 5.0			1.0 and 4.0
	 * 6.0			2.0 and 4.0
	 * 7.0			3.0 and 4.0
	 * 9.0			1.0 and 8.0
	 * 10.0			2.0 and 8.0
	 * 11.0			3.0 and 8.0
	 * 17.0			1.0 and 16.0
	 * 18.0			2.0 and 16.0
	 * 19.0			3.0	and 16.0
	 */
	
	public static void storeLights(RenderPacket packet, CEObject obj) {
		int count = 0;
		int shadowCount = 0;
		boolean hasPointShadow = false;
		for(SunLight light : SunLightModifier.ACTIVE_LIGHTS) {
			if(++count > LIGHT_MAX) break;
			packet.getShader().getCommunicator().storearr3f("materialDir", count - 1, light.getDirection());
			packet.getShader().getCommunicator().storearr3f("materialCol", count - 1, light.getColor());
			packet.getShader().getCommunicator().storearr3f("materialArgs", count - 1, new Vector3f(17.0f, 0f, 0f));
			
			ShadowMap shadow = ShadowMapSystem.INSTANCE.getShadowMap(light);
			if(shadow != null && shadowCount < SHADOW_MAX) {
				packet.getShader().getCommunicator().storearr4x4f("materialShadowMatrix", shadowCount, shadow.toShadowSpaceMatrix(packet));
				packet.getShader().getCommunicator().storearr1f("materialShadowIndices", shadowCount, count - 1);
				packet.getShader().getCommunicator().storearr1f("materialShadowMaxDistance", shadowCount,
						light.getShadowBoxDepth() * packet.getCamera().getModifier(CameraModifier.class).getFarPlane());
				packet.getShader().getCommunicator().storearr1f("materialMapSize", shadowCount, shadow.getLight().getShadowMapResolution());
				int texID = packet.getShader().getCommunicator().loadarr1i("materialShadowMap", shadowCount);
				shadow.getShadowMap().bind(texID);
				shadowCount++;
			}
		}
		
		CUR_OBJPOS.set(obj.getPosition());
		List<Light> lights = new ArrayList<Light>();
		lights.addAll(ACTIVE_LIGHTS);
		lights.addAll(AreaLightModifier.ACTIVE_LIGHTS);
		lights.addAll(DirectionalLightModifier.ACTIVE_LIGHTS);
		lights.addAll(SpotLightModifier.ACTIVE_LIGHTS);
		Collections.sort(lights);
		
		for(Light _light : lights) {
			if(_light instanceof DirectionalLight) {
				DirectionalLight light = (DirectionalLight) _light;
				if(++count > LIGHT_MAX) break;
				
				packet.getShader().getCommunicator().storearr3f("materialPos", count - 1, light.getPosition());
				packet.getShader().getCommunicator().storearr3f("materialDir", count - 1, light.getDirection().clone().normalize());
				packet.getShader().getCommunicator().storearr3f("materialCol", count - 1, light.getColor());
				if(light.isAttenuating()) packet.getShader().getCommunicator().storearr1f("materialAtt", count - 1, light.getAttenuationFactor());
				packet.getShader().getCommunicator().storearr3f("materialArgs", count - 1, new Vector3f
						(createFlags(light.isAttenuating(), light.isNoCosTheta(), FLAG_NO_MODIFICATION), light.getNarrowing(), 0f));
			}
			else if(_light instanceof PointLight) {
				PointLight light = (PointLight) _light;
				if(++count > LIGHT_MAX) break;
				
				packet.getShader().getCommunicator().storearr3f("materialPos", count - 1, light.getPosition());
				packet.getShader().getCommunicator().storearr3f("materialDir", count - 1, new Vector3f());
				packet.getShader().getCommunicator().storearr3f("materialCol", count - 1, light.getColor());
				if(light.isAttenuating()) packet.getShader().getCommunicator().storearr1f("materialAtt", count - 1, light.getAttenuationFactor());
				packet.getShader().getCommunicator().storearr3f("materialArgs", count - 1, new Vector3f
						(createFlags(light.isAttenuating(), light.isNoCosTheta(), FLAG_NO_MODIFICATION), 0f, 0f));
			}
			else if(_light instanceof SpotLight) {
				SpotLight light = (SpotLight) _light;
				if(++count > LIGHT_MAX) break;
				
				packet.getShader().getCommunicator().storearr3f("materialPos", count - 1, light.getPosition());
				packet.getShader().getCommunicator().storearr3f("materialDir", count - 1, light.getDirection().clone().normalize());
				packet.getShader().getCommunicator().storearr3f("materialCol", count - 1, light.getColor());
				if(light.isAttenuating()) packet.getShader().getCommunicator().storearr1f("materialAtt", count - 1, light.getAttenuationFactor());
				packet.getShader().getCommunicator().storearr3f("materialArgs", count - 1, new Vector3f(createFlags
						(light.isAttenuating(), false, FLAG_RADIAL_CONSTRAINTS), light.getSize(), light.getFeather()));
			}
			else if(_light instanceof AreaLight) {
				AreaLight light = (AreaLight) _light;
				if(++count > LIGHT_MAX) break;
				
				packet.getShader().getCommunicator().storearr3f("materialPos", count - 1, light.getPosition());
				packet.getShader().getCommunicator().storearr3f("materialDir", count - 1, light.getDirection().clone().normalize());
				packet.getShader().getCommunicator().storearr3f("materialCol", count - 1, light.getColor());
				if(light.isAttenuating()) packet.getShader().getCommunicator().storearr1f("materialAtt", count - 1, light.getAttenuationFactor());
				packet.getShader().getCommunicator().storearr3f("materialArgs", count - 1, new Vector3f(createFlags
						(light.isAttenuating(), false, FLAG_AREA_CONSTRAINTS), light.getAreaX(), light.getAreaY()));
			}
			
			ShadowMap shadow = ShadowMapSystem.INSTANCE.getShadowMap(_light);
			if(shadow != null) {
				if(shadow instanceof ShadowMap2D && shadowCount < SHADOW_MAX) {
					packet.getShader().getCommunicator().storearr4x4f("materialShadowMatrix", shadowCount, shadow.toShadowSpaceMatrix(packet));
					packet.getShader().getCommunicator().storearr1f("materialShadowIndices", shadowCount, count - 1);
					packet.getShader().getCommunicator().storearr1f("materialShadowMaxDistance", shadowCount, 0f);
					packet.getShader().getCommunicator().storearr1f("materialMapSize", shadowCount, shadow.getLight().getShadowMapResolution());
					int texID = packet.getShader().getCommunicator().loadarr1i("materialShadowMap", shadowCount);
					shadow.getShadowMap().bind(texID);
					shadowCount++;
				}
				else if(shadow instanceof ShadowMapCube && !hasPointShadow) {
					packet.getShader().getCommunicator().store1f("materialFarPlane", packet.getCamera().getModifier(CameraModifier.class).getFarPlane());
					packet.getShader().getCommunicator().store1f("materialPointShadowIndex", count - 1);
					int texID = packet.getShader().getCommunicator().load1i("materialPointShadowMap");
					shadow.getShadowMap().bind(texID);
					hasPointShadow = true;
				}
			}
		}
		
		packet.getShader().getCommunicator().store1f("materialLightMax", count - 1);
		packet.getShader().getCommunicator().store1f("materialShadowCount", shadowCount);
		packet.getShader().getCommunicator().store1f("materialHasPointShadow", hasPointShadow ? 1f : 0f);
	}
	
	private static float createFlags(boolean attenuate, boolean noCosTheta, int additionalFlag) {
		return (float) ((!attenuate ? FLAG_NULLIFY_ATTENUATION : 0) | (noCosTheta ? FLAG_NULLIFY_COSTHETA : 0) | additionalFlag);
	}
	
	private final Property<Vector3f> offset;
	private final Property<Vector3f> color;
	private final Property<Float> intensity;
	private final Property<Boolean> attenuating;
	private final Property<Float> attenuationFactor;
	private final Property<Boolean> noCosTheta;
	private final Property<Boolean> castsShadows;
	private final Property<Float> shadowDistance;
	private final Property<Integer> shadowResolution;
	
	private final Map<CEObject, PointLight> lights = new HashMap<CEObject, PointLight>();
	
	public PointLightModifier(Vector3f offset, Vector3f color, float intensity, boolean attenuating, float attenuationFactor,
			boolean noCosTheta, boolean castsShadows, float shadowDistance, int shadowResolution) {
		super(false, true, false);
		this.offset = Properties.createVec3Property(offset);
		this.color = Properties.createVec3Property(color);
		this.intensity = Properties.createFloatProperty(intensity);
		this.attenuating = Properties.createBooleanProperty(attenuating);
		this.attenuationFactor = Properties.createFloatProperty(attenuationFactor);
		this.noCosTheta = Properties.createBooleanProperty(noCosTheta);
		this.castsShadows = Properties.createBooleanProperty(castsShadows);
		this.shadowDistance = Properties.createFloatProperty(shadowDistance);
		this.shadowResolution = Properties.createIntegerProperty(shadowResolution);
	}
	
	private PointLightModifier(PointLightModifier src) {
		super(false, true, false);
		this.offset = src.offset.clone();
		this.color = src.color.clone();
		this.intensity = src.intensity.clone();
		this.attenuating = src.attenuating.clone();
		this.attenuationFactor = src.attenuationFactor.clone();
		this.noCosTheta = src.noCosTheta.clone();
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
			lights.put(obj, new PointLight());
		PointLight light = lights.get(obj);
		light.position = new Vector3f(obj.getTransformation().transform(new Vector4f(offset.get(), 1)));
		light.color = new Vector3f(color.get()).gammaCorrect().scale(intensity.get());
		light.attenuating = attenuating.get();
		light.attenuationFactor = attenuationFactor.get();
		light.noCosTheta = noCosTheta.get();
		light.resolution = shadowResolution.get();
		
		ACTIVE_LIGHTS.add((PointLight) light);
		if(castsShadows.get() && Vector3f.sub(packet.getCamera().getPosition(), light.getPosition()).length() <= shadowDistance.get())
			ShadowMapSystem.INSTANCE.registerLight(light);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(ACTIVE_LIGHTS.size() > 0) ACTIVE_LIGHTS.clear();
		offset.update(!packet.isPaused());
		color.update(!packet.isPaused());
		intensity.update(!packet.isPaused());
		attenuating.update(!packet.isPaused());
		attenuationFactor.update(!packet.isPaused());
		noCosTheta.update(!packet.isPaused());
		castsShadows.update(!packet.isPaused());
		shadowDistance.update(!packet.isPaused());
		shadowResolution.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Offset", offset);
		ctrl.withProperty("Color", color);
		ctrl.withProperty("Intensity", intensity);
		ctrl.withProperty("Attenuating", attenuating);
		ctrl.withProperty("Attenuation Factor", attenuationFactor);
		ctrl.withProperty("No CosTheta", noCosTheta);
		ctrl.withProperty("Casts Shadows", castsShadows);
		ctrl.withProperty("Shadow Distance", shadowDistance)
			.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 0f), 1);
		ctrl.withProperty("Shadow Resolution", shadowResolution)
			.withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 1, IntervalType.INCLUSIVE, Integer.MAX_VALUE, IntervalType.INCLUSIVE, 1), 1);
		return ctrl;
	}
	
	public Modifier duplicate() {
		return new PointLightModifier(this);
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
	
	public boolean isNoCosTheta() {
		return noCosTheta.get();
	}
	
	public void setNoCosTheta(boolean noCosTheta) {
		this.noCosTheta.set(noCosTheta);
	}
	
	public Property<Boolean> noCosThetaProperty() {
		return noCosTheta;
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
	
	public static final class PointLight implements Light, java.io.Serializable {
		
		private static final long serialVersionUID = -1516562519185895344L;
		
		private Vector3f position, color;
		private boolean attenuating;
		private float attenuationFactor;
		private boolean noCosTheta;
		private int resolution;
		
		private PointLight() {
		}
		
		@Override
		public Vector3f getPosition() {
			return position;
		}
		
		public Vector3f getColor() {
			return color;
		}
		
		public boolean isAttenuating() {
			return attenuating;
		}
		
		public float getAttenuationFactor() {
			return attenuationFactor;
		}
		
		public boolean isNoCosTheta() {
			return noCosTheta;
		}
		
		@Override
		public Matrix4f getViewMatrix(RenderPacket pckt) {
			return new Matrix4f();
		}
		
		@Override
		public Matrix4f getProjectionMatrix(RenderPacket pckt) {
			return new Matrix4f();
		}
		
		@Override
		public boolean castsPointShadows() {
			return true;
		}
		
		@Override
		public int getShadowMapResolution() {
			return resolution;
		}
		
	}
	
}
