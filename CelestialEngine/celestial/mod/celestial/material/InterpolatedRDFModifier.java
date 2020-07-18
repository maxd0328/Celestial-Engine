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
import mod.celestial.light.PointLightModifier;

public final class InterpolatedRDFModifier extends Modifier {
	
	private static final long serialVersionUID = 1314673860805726822L;
	
	public static final Factory<InterpolatedRDFModifier> FACTORY = () -> new InterpolatedRDFModifier(1f, 0.1f, 2.2f, false);
	
	private final Property<Float> ambientOcclusion;
	private final Property<Float> ambient;
	private final Property<Float> gamma;
	private final Property<Boolean> noCosTheta;
	
	public InterpolatedRDFModifier(float ambientOcclusion, float ambient, float gamma, boolean noCosTheta) {
		super(false, false, true);
		this.ambientOcclusion = Properties.createFloatProperty(ambientOcclusion);
		this.ambient = Properties.createFloatProperty(ambient);
		this.gamma = Properties.createFloatProperty(gamma);
		this.noCosTheta = Properties.createBooleanProperty(noCosTheta);
	}
	
	private InterpolatedRDFModifier(InterpolatedRDFModifier src) {
		super(false, false, true);
		this.ambientOcclusion = src.ambientOcclusion.clone();
		this.ambient = src.ambient.clone();
		this.gamma = src.gamma.clone();
		this.noCosTheta = src.noCosTheta.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs($("mat4 transform"), $("float materialLightMax"),
				$("vec3 materialPos", LIGHT_MAX), $("vec3 materialDir", LIGHT_MAX), $("float materialAtt", LIGHT_MAX),
				$("vec3 materialArgs", LIGHT_MAX), $("vec3 materialCol", LIGHT_MAX), $("float materialNoCosTheta"),
				$("mat4 materialShadowMatrix", SHADOW_MAX), $("float materialShadowMaxDistance", SHADOW_MAX),
				$("float materialShadowCount"), $("float materialFarPlane"), $("float materialPointShadowIndex"),
				$("float materialHasPointShadow"), $("sampler2D materialShadowMap", SHADOW_MAX),
				$("float materialShadowIndices", SHADOW_MAX), $("float materialMapSize", SHADOW_MAX),
				$("float materialShadowCount"), $("samplerCube materialPointShadowMap"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs($("float materialLightMax"), $("float materialAmbient"), $("float materialGamma"), $("float materialAO"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs($("float materialAO").withDefaultValue("1.0"));
		return new ShaderModule("interpolatedRDF",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("material.glsl"), "interpolatedRDF_V"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("material.glsl"), "interpolatedRDF_F"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs($("vec3 normal")),
				ShaderModule.attribs($("vec3 materialInfluenceMul", LIGHT_MAX)));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store1f("materialAO", ambientOcclusion.get());
		packet.getShader().getCommunicator().store1f("materialAmbient", ambient.get());
		packet.getShader().getCommunicator().store1f("materialGamma", gamma.get());
		packet.getShader().getCommunicator().store1f("materialNoCosTheta", noCosTheta.get() ? 1f : 0f);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		PointLightModifier.storeLights(packet, obj);
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		ambientOcclusion.update(!packet.isPaused());
		ambient.update(!packet.isPaused());
		gamma.update(!packet.isPaused());
		noCosTheta.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Ambient Occlusion", ambientOcclusion);
		ctrl.withProperty("Ambient", ambient);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, 1f, IntervalType.INCLUSIVE, 0f), 2);
		ctrl.withProperty("Gamma", gamma);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.EXCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 1);
		ctrl.withProperty("No CosTheta", noCosTheta);
		return ctrl;
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
		return new InterpolatedRDFModifier(this);
	}
	
}
