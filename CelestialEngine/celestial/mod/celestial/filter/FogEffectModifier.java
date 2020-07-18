package mod.celestial.filter;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;

public final class FogEffectModifier extends Modifier {
	
	private static final long serialVersionUID = 1765763450244508414L;
	
	public static final Factory<FogEffectModifier> FACTORY = () -> new FogEffectModifier(true, new Vector3f(), 0f, 1f);
	
	private final Property<Boolean> useScreenColor;
	private final Property<Vector3f> color;
	private final Property<Float> density;
	private final Property<Float> gradient;
	
	public FogEffectModifier(boolean useScreenColor, Vector3f color, float density, float gradient) {
		super(false, false, true);
		this.useScreenColor = Properties.createBooleanProperty(useScreenColor);
		this.color = Properties.createVec3Property(color);
		this.density = Properties.createFloatProperty(density);
		this.gradient = Properties.createFloatProperty(gradient);
	}
	
	private FogEffectModifier(FogEffectModifier src) {
		super(false, false, true);
		this.useScreenColor = src.useScreenColor.clone();
		this.color = src.color.clone();
		this.density = src.density.clone();
		this.gradient = src.gradient.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("float fogEffectDensity"),
				ShaderAttribute.$("float fogEffectGradient"), ShaderAttribute.$("mat4 viewMatrix"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 fogEffectColor"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("fogEffect",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "fogEffectV"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "fogEffectF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(ShaderAttribute.$("vec3 position")),
				ShaderModule.attribs(ShaderAttribute.$("float fogEffectVisibility")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store1f("fogEffectDensity", density.get());
		packet.getShader().getCommunicator().store1f("fogEffectGradient", gradient.get());
		packet.getShader().getCommunicator().store3f("fogEffectColor", useScreenColor.get() ? packet.getRenderer().getScreen().get() : color.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		useScreenColor.update(!packet.isPaused());
		color.update(!packet.isPaused());
		density.update(!packet.isPaused());
		gradient.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Use Screen Color", useScreenColor);
		ctrl.withProperty("Color", color);
		ctrl.withProperty("Fog Density", density);
		ctrl.withProperty("Fog Gradient", gradient);
		return ctrl;
	}
	
	public boolean isUseScreenColor() {
		return useScreenColor.get();
	}
	
	public void setUseScreenColor(boolean useScreenColor) {
		this.useScreenColor.set(useScreenColor);
	}
	
	public Property<Boolean> useScreenColorProperty() {
		return useScreenColor;
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
	
	public float getDensity() {
		return density.get();
	}
	
	public void setDensity(float density) {
		this.density.set(density);
	}
	
	public Property<Float> densityProperty() {
		return density;
	}
	
	public float getGradient() {
		return gradient.get();
	}
	
	public void setGradient(float gradient) {
		this.gradient.set(gradient);
	}
	
	public Property<Float> gradientProperty() {
		return gradient;
	}
	
	public Modifier duplicate() {
		return new FogEffectModifier(this);
	}
	
}
