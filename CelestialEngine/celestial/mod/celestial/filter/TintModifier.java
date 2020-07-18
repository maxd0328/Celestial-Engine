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

public final class TintModifier extends Modifier {
	
	private static final long serialVersionUID = 7824834718345673321L;
	
	public static final Factory<TintModifier> FACTORY = () -> new TintModifier(new Vector3f(), 0);
	
	private final Property<Vector3f> color;
	private final Property<Float> blendFactor;
	
	public TintModifier(Vector3f color, float blendFactor) {
		super(false, false, true);
		this.color = Properties.createVec3Property(color);
		this.blendFactor = Properties.createFloatProperty(blendFactor);
	}
	
	private TintModifier(TintModifier src) {
		super(false, false, true);
		this.color = src.color.clone();
		this.blendFactor = src.blendFactor.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 tintColor"), ShaderAttribute.$("float tintBlendFactor"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("tint",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "tintF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store3f("tintColor", color.get());
		packet.getShader().getCommunicator().store1f("tintBlendFactor", blendFactor.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		color.update(!packet.isPaused());
		blendFactor.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Color", color);
		ctrl.withProperty("Blend Factor", blendFactor);
		return ctrl;
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
	
	public float getBlendFactor() {
		return blendFactor.get();
	}
	
	public void setBlendFactor(float blendFactor) {
		this.blendFactor.set(blendFactor);
	}
	
	public Property<Float> blendFactorProperty() {
		return blendFactor;
	}
	
	public Modifier duplicate() {
		return new TintModifier(this);
	}
	
}
