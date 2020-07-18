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

public final class StaticColorModifier extends Modifier {
	
	private static final long serialVersionUID = 8762184099508463883L;
	
	public static final Factory<StaticColorModifier> FACTORY = () -> new StaticColorModifier(new Vector3f());
	
	private final Property<Vector3f> color;
	
	public StaticColorModifier(Vector3f color) {
		super(false, false, true);
		this.color = Properties.createVec3Property(color);
	}
	
	private StaticColorModifier(StaticColorModifier src) {
		super(false, false, true);
		this.color = src.color.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 staticColor"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("staticColor",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "staticColorF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store3f("staticColor", color.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		color.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Color", color);
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
	
	public Modifier duplicate() {
		return new StaticColorModifier(this);
	}
	
}
