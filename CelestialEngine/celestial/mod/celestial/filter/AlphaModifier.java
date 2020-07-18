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

public final class AlphaModifier extends Modifier {
	
	private static final long serialVersionUID = 1881736179670785019L;
	
	public static final Factory<AlphaModifier> FACTORY = () -> new AlphaModifier(0, 0);
	
	private final Property<Float> alpha;
	private final Property<Float> blendFactor;
	
	public AlphaModifier(float alpha, float blendFactor) {
		super(false, false, false);
		this.alpha = Properties.createFloatProperty(alpha);
		this.blendFactor = Properties.createFloatProperty(blendFactor);
	}
	
	private AlphaModifier(AlphaModifier src) {
		super(false, false, false);
		this.alpha = src.alpha.clone();
		this.blendFactor = src.blendFactor.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("float alpha"), ShaderAttribute.$("float alphaBlendFactor"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("alpha",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "alphaF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store1f("alpha", alpha.get());
		packet.getShader().getCommunicator().store1f("alphaBlendFactor", blendFactor.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		alpha.update(!packet.isPaused());
		blendFactor.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Alpha", alpha);
		ctrl.withProperty("Blend Factor", blendFactor);
		return ctrl;
	}
	
	public float getAlpha() {
		return alpha.get();
	}
	
	public void setAlpha(float alpha) {
		this.alpha.set(alpha);
	}
	
	public Property<Float> alphaProperty() {
		return alpha;
	}
	
	public float getBlendFactor() {
		return blendFactor.get();
	}
	
	public void setBlendFactor(float blendFactor) {
		this.blendFactor.set(blendFactor);
	}
	
	public Property<Float> blendFactorProperty(){
		return blendFactor;
	}
	
	public Modifier duplicate() {
		return new AlphaModifier(this);
	}
	
}
