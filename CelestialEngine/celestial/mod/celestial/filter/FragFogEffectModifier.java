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

public final class FragFogEffectModifier extends Modifier {
	
	private static final long serialVersionUID = 1765763450244508414L;
	
	public static final Factory<FragFogEffectModifier> FACTORY = () -> new FragFogEffectModifier(true, new Vector3f(), 1f);
	
	private final Property<Boolean> useScreenColor;
	private final Property<Vector3f> color;
	private final Property<Float> exponentiation;
	
	public FragFogEffectModifier(boolean useScreenColor, Vector3f color, float exponentiation) {
		super(false, false, true);
		this.useScreenColor = Properties.createBooleanProperty(useScreenColor);
		this.color = Properties.createVec3Property(color);
		this.exponentiation = Properties.createFloatProperty(exponentiation);
	}
	
	private FragFogEffectModifier(FragFogEffectModifier src) {
		super(false, false, true);
		this.useScreenColor = src.useScreenColor.clone();
		this.color = src.color.clone();
		this.exponentiation = src.exponentiation.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 fogEffectColor"), ShaderAttribute.$("float fogEffectExponentiation"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("fragFogEffect",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "fragFogEffectF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(), ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store1f("fogEffectExponentiation", exponentiation.get());
		packet.getShader().getCommunicator().store3f("fogEffectColor", useScreenColor.get() ? packet.getRenderer().getScreen().get() : color.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		useScreenColor.update(!packet.isPaused());
		color.update(!packet.isPaused());
		exponentiation.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Use Screen Color", useScreenColor);
		ctrl.withProperty("Color", color);
		ctrl.withProperty("Exponentiation", exponentiation);
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
	
	public float getExponentiation() {
		return exponentiation.get();
	}
	
	public void setExponentiation(float exponentiation) {
		this.exponentiation.set(exponentiation);
	}
	
	public Property<Float> exponentiationProperty() {
		return exponentiation;
	}
	
	public Modifier duplicate() {
		return new FragFogEffectModifier(this);
	}
	
}
