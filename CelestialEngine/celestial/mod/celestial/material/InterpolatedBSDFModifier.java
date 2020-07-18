package mod.celestial.material;

import celestial.core.Modifier;

import static celestial.shader.ShaderAttribute.$;
import static mod.celestial.light.PointLightModifier.LIGHT_MAX;
import static mod.celestial.light.PointLightModifier.SHADOW_MAX;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.PropertyController;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;
import mod.celestial.light.PointLightModifier;

public final class InterpolatedBSDFModifier extends Modifier {
	
	private static final long serialVersionUID = 4363565816136618503L;
	
	public static final Factory<InterpolatedBSDFModifier> FACTORY = () -> new InterpolatedBSDFModifier(0f, new Vector3f(),
			1f, 1f, 0.01f, 0.2f, 0f, 0.5f, 1f, 0f, 1f, 0.1f, 2.2f, 0.04f, false);
	
	private final Property<Float> subsurface;
	private final Property<Vector3f> subsurfaceRadius;
	private final Property<Float> subsurfaceDistortion;
	private final Property<Float> subsurfaceScale;
	private final Property<Float> subsurfaceThickness;
	private final Property<Float> subsurfaceAmbient;
	
	private final Property<Float> metallic;
	private final Property<Float> roughness;
	private final Property<Float> specular;
	private final Property<Float> specularTint;
	private final Property<Float> ambientOcclusion;
	private final Property<Float> ambient;
	private final Property<Float> gamma;
	private final Property<Float> iorF0;
	private final Property<Boolean> noCosTheta;
	
	public InterpolatedBSDFModifier(float subsurface, Vector3f subsurfaceRadius, float subsurfaceDistortion, float subsurfaceScale, float subsurfaceThickness,
			float subsurfaceAmbient, float metallic, float roughness, float specular, float specularTint, float ambientOcclusion, float ambient, float gamma, float iorF0,
			boolean noCosTheta) {
		super(false, false, true);
		this.subsurface = Properties.createFloatProperty(subsurface);
		this.subsurfaceRadius = Properties.createVec3Property(subsurfaceRadius);
		this.subsurfaceDistortion = Properties.createFloatProperty(subsurfaceDistortion);
		this.subsurfaceScale = Properties.createFloatProperty(subsurfaceScale);
		this.subsurfaceThickness = Properties.createFloatProperty(subsurfaceThickness);
		this.subsurfaceAmbient = Properties.createFloatProperty(subsurfaceAmbient);
		
		this.metallic = Properties.createFloatProperty(metallic);
		this.roughness = Properties.createFloatProperty(roughness);
		this.specular = Properties.createFloatProperty(specular);
		this.specularTint = Properties.createFloatProperty(specularTint);
		this.ambientOcclusion = Properties.createFloatProperty(ambientOcclusion);
		this.ambient = Properties.createFloatProperty(ambient);
		this.gamma = Properties.createFloatProperty(gamma);
		this.iorF0 = Properties.createFloatProperty(iorF0);
		this.noCosTheta = Properties.createBooleanProperty(noCosTheta);
	}
	
	private InterpolatedBSDFModifier(InterpolatedBSDFModifier src) {
		super(false, false, true);
		this.subsurface = src.subsurface.clone();
		this.subsurfaceRadius = src.subsurfaceRadius.clone();
		this.subsurfaceDistortion = src.subsurfaceDistortion.clone();
		this.subsurfaceScale = src.subsurfaceScale.clone();
		this.subsurfaceThickness = src.subsurfaceThickness.clone();
		this.subsurfaceAmbient = src.subsurfaceAmbient.clone();
		
		this.metallic = src.metallic.clone();
		this.roughness = src.roughness.clone();
		this.specular = src.specular.clone();
		this.specularTint = src.specularTint.clone();
		this.ambientOcclusion = src.ambientOcclusion.clone();
		this.ambient = src.ambient.clone();
		this.gamma = src.gamma.clone();
		this.iorF0 = src.iorF0.clone();
		this.noCosTheta = src.noCosTheta.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs($("mat4 transform"), $("float materialLightMax"), $("vec3 materialPos", LIGHT_MAX),
				$("vec3 materialDir", LIGHT_MAX), $("float materialRoughness"), $("float materialSpecular"), $("vec3 materialCol", LIGHT_MAX),
				$("float materialAtt", LIGHT_MAX), $("vec3 materialArgs", LIGHT_MAX), $("float materialSubsurface"), $("vec3 materialSubsurfaceRadius"),
				$("float materialSubsurfaceDistortion"), $("float materialSubsurfaceScale"), $("float materialSubsurfaceThickness"),
				$("float materialSubsurfaceAmbient"), $("float materialNoCosTheta"), $("mat4 materialShadowMatrix", SHADOW_MAX),
				$("float materialShadowMaxDistance", SHADOW_MAX), $("float materialShadowCount"), $("float materialFarPlane"),
				$("float materialPointShadowIndex"), $("float materialHasPointShadow"), $("sampler2D materialShadowMap", SHADOW_MAX),
				$("float materialShadowIndices", SHADOW_MAX), $("float materialMapSize", SHADOW_MAX), $("float materialShadowCount"),
				$("samplerCube materialPointShadowMap"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs($("float materialMetallic"), $("float materialAO"), $("float materialF0"),
				$("float materialSpecularTint"), $("float materialAmbient"), $("float materialGamma"), $("float materialLightMax"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs($("float materialMetallic").withDefaultValue("1.0"), $("float materialAO").withDefaultValue("1.0"));
		return new ShaderModule("interpolatedBSDF",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("material.glsl"), "interpolatedBSDF_V"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("material.glsl"), "interpolatedBSDF_F"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs($("vec3 normal")),
				ShaderModule.attribs($("float materialRatioMul", LIGHT_MAX), $("vec3 materialSpecularMul", LIGHT_MAX),
						$("vec3 materialInfluenceMul", LIGHT_MAX), $("vec3 materialSubsurface")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store1f("materialSubsurface", subsurface.get());
		packet.getShader().getCommunicator().store3f("materialSubsurfaceRadius", subsurfaceRadius.get());
		packet.getShader().getCommunicator().store1f("materialSubsurfaceDistortion", subsurfaceDistortion.get());
		packet.getShader().getCommunicator().store1f("materialSubsurfaceScale", subsurfaceScale.get());
		packet.getShader().getCommunicator().store1f("materialSubsurfaceThickness", subsurfaceThickness.get());
		packet.getShader().getCommunicator().store1f("materialSubsurfaceAmbient", subsurfaceAmbient.get());
		
		packet.getShader().getCommunicator().store1f("materialMetallic", metallic.get());
		packet.getShader().getCommunicator().store1f("materialRoughness", roughness.get());
		packet.getShader().getCommunicator().store1f("materialSpecular", specular.get());
		packet.getShader().getCommunicator().store1f("materialSpecularTint", specularTint.get());
		packet.getShader().getCommunicator().store1f("materialAO", ambientOcclusion.get());
		packet.getShader().getCommunicator().store1f("materialAmbient", ambient.get());
		packet.getShader().getCommunicator().store1f("materialGamma", gamma.get());
		packet.getShader().getCommunicator().store1f("materialF0", iorF0.get());
		packet.getShader().getCommunicator().store1f("materialNoCosTheta", noCosTheta.get() ? 1f : 0f);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		PointLightModifier.storeLights(packet, obj);
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		subsurface.update(!packet.isPaused());
		subsurfaceRadius.update(!packet.isPaused());
		subsurfaceDistortion.update(!packet.isPaused());
		subsurfaceScale.update(!packet.isPaused());
		subsurfaceThickness.update(!packet.isPaused());
		subsurfaceAmbient.update(!packet.isPaused());
		metallic.update(!packet.isPaused());
		roughness.update(!packet.isPaused());
		specular.update(!packet.isPaused());
		specularTint.update(!packet.isPaused());
		ambientOcclusion.update(!packet.isPaused());
		ambient.update(!packet.isPaused());
		gamma.update(!packet.isPaused());
		iorF0.update(!packet.isPaused());
		noCosTheta.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Subsurface", subsurface).withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, 1f, IntervalType.INCLUSIVE, 0f), 1);
		ctrl.withProperty("Subsurface Radius", subsurfaceRadius);
		ctrl.withProperty("Subsurface Distortion", subsurfaceDistortion);
		ctrl.withProperty("Subsurface Scale", subsurfaceScale);
		ctrl.withProperty("Subsurface Thickness", subsurfaceThickness);
		ctrl.withProperty("Subsurface Ambient", subsurfaceAmbient);
		
		ctrl.withProperty("Metallic", metallic);
		ctrl.withProperty("Roughness", roughness);
		ctrl.withProperty("Specular", specular);
		ctrl.withProperty("Specular Tint", specularTint);
		ctrl.withProperty("Ambient Occlusion", ambientOcclusion);
		ctrl.withProperty("Ambient", ambient);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, 1f, IntervalType.INCLUSIVE, 0f), 6);
		ctrl.withProperty("Gamma", gamma);
		ctrl.withProperty("IOR F0", iorF0);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.EXCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 2);
		ctrl.withProperty("No CosTheta", noCosTheta);
		return ctrl;
	}
	
	public float getSubsurface() {
		return subsurface.get();
	}
	
	public void setSubsurface(float subsurface) {
		this.subsurface.set(subsurface);
	}
	
	public Property<Float> subsurfaceProperty() {
		return subsurface;
	}
	
	public Vector3f getSubsurfaceRadius() {
		return subsurfaceRadius.get();
	}
	
	public void setSubsurfaceRadius(Vector3f subsurfaceRadius) {
		this.subsurfaceRadius.set(subsurfaceRadius);
	}
	
	public Property<Vector3f> subsurfaceRadiusProperty() {
		return subsurfaceRadius;
	}
	
	public float getSubsurfaceDistortion() {
		return subsurfaceDistortion.get();
	}
	
	public void setSubsurfaceDistortion(float subsurfaceDistortion) {
		this.subsurfaceDistortion.set(subsurfaceDistortion);
	}
	
	public Property<Float> subsurfaceDistortionProperty() {
		return subsurfaceDistortion;
	}
	
	public float getSubsurfaceScale() {
		return subsurfaceScale.get();
	}
	
	public void setSubsurfaceScale(float subsurfaceScale) {
		this.subsurfaceScale.set(subsurfaceScale);
	}
	
	public Property<Float> subsurfaceScaleProperty() {
		return subsurfaceScale;
	}
	
	public float getSubsurfaceThickness() {
		return subsurfaceThickness.get();
	}
	
	public void setSubsurfaceThickness(float subsurfaceThickness) {
		this.subsurfaceThickness.set(subsurfaceThickness);
	}
	
	public Property<Float> subsurfaceThicknessProperty() {
		return subsurfaceThickness;
	}
	
	public float getSubsurfaceAmbient() {
		return subsurfaceAmbient.get();
	}
	
	public void setSubsurfaceAmbient(float subsurfaceAmbient) {
		this.subsurfaceAmbient.set(subsurfaceAmbient);
	}
	
	public Property<Float> subsurfaceAmbientProperty() {
		return subsurfaceAmbient;
	}
	
	public float getMetallic() {
		return metallic.get();
	}
	
	public void setMetallic(float metallic) {
		this.metallic.set(metallic);
	}
	
	public Property<Float> metallicProperty() {
		return metallic;
	}
	
	public float getRoughness() {
		return roughness.get();
	}
	
	public void setRoughness(float roughness) {
		this.roughness.set(roughness);
	}
	
	public Property<Float> roughnessProperty() {
		return roughness;
	}
	
	public float getSpecular() {
		return specular.get();
	}
	
	public void setSpecular(float specular) {
		this.specular.set(specular);
	}
	
	public Property<Float> specularProperty() {
		return specular;
	}
	
	public float getSpecularTint() {
		return specularTint.get();
	}
	
	public void setSpecularTint(float specularTint) {
		this.specularTint.set(specularTint);
	}
	
	public Property<Float> specularTintProperty() {
		return specularTint;
	}
	
	public float getAmbientOcclusion() {
		return ambientOcclusion.get();
	}
	
	public void setAmbientOcclusion(float ambientOcclusion) {
		this.ambientOcclusion.set(ambientOcclusion);
	}
	
	public Property<Float> ambientOcclusionProperty() {
		return ambientOcclusion;
	}
	
	public float getAmbient() {
		return ambient.get();
	}
	
	public void setAmbient(float ambient) {
		this.ambient.set(ambient);
	}
	
	public Property<Float> ambientProperty() {
		return ambient;
	}
	
	public float getGamma() {
		return gamma.get();
	}
	
	public void setGamma(float gamma) {
		this.gamma.set(gamma);
	}
	
	public Property<Float> gammaProperty() {
		return gamma;
	}
	
	public float getIorF0() {
		return iorF0.get();
	}
	
	public void setIorF0(float iorF0) {
		this.iorF0.set(iorF0);
	}
	
	public Property<Float> iorF0Property() {
		return iorF0;
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
	
	public Modifier duplicate() {
		return new InterpolatedBSDFModifier(this);
	}
	
}
