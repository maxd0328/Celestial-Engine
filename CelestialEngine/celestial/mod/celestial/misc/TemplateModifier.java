package mod.celestial.misc;

import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;

public final class TemplateModifier extends Modifier {
	
	private static final long serialVersionUID = -7607614641652263870L;
	
	public static final Factory<TemplateModifier> FACTORY = () -> new TemplateModifier();
	
	public TemplateModifier() {
		super(false, true, false);
	}
	
	private TemplateModifier(TemplateModifier src) {
		super(false, true, false);
	}
	
	protected ShaderModule getShaderModule() {
/*		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource(".glsl"), "vertex"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource(".glsl"), "fragment"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs());
*/		return null;
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
	
	/* Getters and setters */
	
	public Modifier duplicate() {
		return new TemplateModifier(this);
	}
	
}
