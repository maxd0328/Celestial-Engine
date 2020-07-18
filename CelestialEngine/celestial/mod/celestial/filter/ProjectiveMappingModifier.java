package mod.celestial.filter;

import java.util.ArrayList;
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

public final class ProjectiveMappingModifier extends Modifier {
	
	private static final long serialVersionUID = 5608633647203478662L;
	
	public static final Factory<ProjectiveMappingModifier> FACTORY = () -> new ProjectiveMappingModifier();
	
	public ProjectiveMappingModifier() {
		super(false, false, true);
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("projectiveMapping",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "projectiveMappingF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs(ShaderAttribute.$("vec4 clipSpacePosition")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
	public Modifier duplicate() {
		return new ProjectiveMappingModifier();
	}
	
}
