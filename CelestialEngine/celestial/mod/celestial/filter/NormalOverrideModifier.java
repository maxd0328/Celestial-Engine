package mod.celestial.filter;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.FloatConverter;
import celestial.ctrl.NonZeroBounds;
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

public final class NormalOverrideModifier extends Modifier {
	
	private static final long serialVersionUID = -7223574766453561436L;
	
	public static final Factory<NormalOverrideModifier> FACTORY = () -> new NormalOverrideModifier(new Vector3f(0, 1, 0));
	
	private final Property<Vector3f> normal;
	
	public NormalOverrideModifier(Vector3f normal) {
		super(false, false, true);
		this.normal = Properties.createVec3Property(normal);
	}
	
	private NormalOverrideModifier(NormalOverrideModifier src) {
		super(false, false, true);
		this.normal = src.normal.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 normalOverride"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("normalOverride",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "normalOverrideV"), vUniforms, vGlobals),
				new ProgramModule("", fUniforms, fGlobals),
				getID(), ShaderModule.attribs(ShaderAttribute.$("vec3 normal")),
				ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store3f("normalOverride", normal.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		normal.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Normal", normal).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		return ctrl;
	}
	
	public Vector3f getNormal() {
		return normal.get();
	}
	
	public void setNormal(Vector3f normal) {
		this.normal.set(normal);
	}
	
	public Property<Vector3f> normalProperty() {
		return normal;
	}
	
	public Modifier duplicate() {
		return new NormalOverrideModifier(this);
	}
	
}
