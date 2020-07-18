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

public final class ProceduralRDFModifier extends Modifier {
	
	private static final long serialVersionUID = -4171379258510883001L;
	
	public static final Factory<ProceduralRDFModifier> FACTORY = () -> new ProceduralRDFModifier(1f, 0.1f, 2.2f, false, false);
	
	private final Property<Float> ambientOcclusion;
	private final Property<Float> ambient;
	private final Property<Float> gamma;
	private final Property<Boolean> noCosTheta;
	private final Property<Boolean> restrictTangents;
	
	public ProceduralRDFModifier(float ambientOcclusion, float ambient, float gamma, boolean noCosTheta, boolean restrictTangents) {
		super(false, false, true);
		this.ambientOcclusion = Properties.createFloatProperty(ambientOcclusion);
		this.ambient = Properties.createFloatProperty(ambient);
		this.gamma = Properties.createFloatProperty(gamma);
		this.noCosTheta = Properties.createBooleanProperty(noCosTheta);
		this.restrictTangents = Properties.createBooleanProperty(restrictTangents);
	}
	
	private ProceduralRDFModifier(ProceduralRDFModifier src) {
		super(false, false, true);
		this.ambientOcclusion = src.ambientOcclusion.clone();
		this.ambient = src.ambient.clone();
		this.gamma = src.gamma.clone();
		this.noCosTheta = src.noCosTheta.clone();
		this.restrictTangents = src.restrictTangents.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs($("mat4 transform"), $("float materialLightMax"),
				$("vec3 materialPos", LIGHT_MAX), $("vec3 materialDir", LIGHT_MAX), $("float materialRestrictTangents"),
				$("mat4 materialShadowMatrix", SHADOW_MAX), $("float materialShadowMaxDistance", SHADOW_MAX), $("float materialShadowCount"),
				$("float materialFarPlane"), $("float materialPointShadowIndex"), $("float materialHasPointShadow"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs($("float materialRestrictTangents"), $("float materialLightMax"),
				$("float materialAmbient"), $("float materialGamma"), $("vec3 materialCol", LIGHT_MAX), $("float materialAtt", LIGHT_MAX),
				$("vec3 materialArgs", LIGHT_MAX), $("float materialAO"), $("float materialNoCosTheta"), $("sampler2D materialShadowMap", SHADOW_MAX),
				$("float materialShadowIndices", SHADOW_MAX), $("float materialMapSize", SHADOW_MAX), $("float materialShadowCount"),
				$("samplerCube materialPointShadowMap"), $("float materialPointShadowIndex"), $("float materialHasPointShadow"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs($("vec3 surfaceNorm").withDefaultValue("vec3(0, 0, 1)"), $("float materialAO").withDefaultValue("1.0"));
		return new ShaderModule("proceduralRDF",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("material.glsl"), "proceduralRDF_V"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("material.glsl"), "proceduralRDF_F"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs($("vec3 normal"), $("vec3 tangent")),
				ShaderModule.attribs($("vec3 materialView"), $("vec3 materialNorm"), $("vec3 materialSAng", LIGHT_MAX), $("vec3 materialVDir", LIGHT_MAX),
						$("vec4 materialShadowCoords", SHADOW_MAX), $("float materialShadowTransition", SHADOW_MAX), $("float materialPointDistance"),
						$("vec3 materialPointDirection")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store1f("materialAO", ambientOcclusion.get());
		packet.getShader().getCommunicator().store1f("materialAmbient", ambient.get());
		packet.getShader().getCommunicator().store1f("materialGamma", gamma.get());
		packet.getShader().getCommunicator().store1f("materialNoCosTheta", noCosTheta.get() ? 1f : 0f);
		packet.getShader().getCommunicator().store1f("materialRestrictTangents", restrictTangents.get() ? 1f : 0f);
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
		restrictTangents.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Ambient Occlusion", ambientOcclusion);
		ctrl.withProperty("Ambient", ambient);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, 1f, IntervalType.INCLUSIVE, 0f), 2);
		ctrl.withProperty("Gamma", gamma);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.EXCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 1);
		ctrl.withProperty("No CosTheta", noCosTheta);
		ctrl.withProperty("Restrict Tangents", restrictTangents);
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
	
	public boolean isRestrictTangents() {
		return restrictTangents.get();
	}
	
	public void setRestrictTangents(boolean restrictTangents) {
		this.restrictTangents.set(restrictTangents);
	}
	
	public Property<Boolean> restrictTangentsProperty() {
		return restrictTangents;
	}
	
	public Modifier duplicate() {
		return new ProceduralRDFModifier(this);
	}
	
}
