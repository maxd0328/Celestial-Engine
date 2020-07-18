package mod.celestial.filter;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.util.ISceneSystem;

public final class CubeMapDiffuseModifier extends Modifier {
	
	private static final long serialVersionUID = 8762184099508463883L;
	
	public static final Factory<CubeMapDiffuseModifier> FACTORY = () -> new CubeMapDiffuseModifier((Sampler) null);
	
	private final Property<Sampler> sampler;
	
	public CubeMapDiffuseModifier(Sampler sampler) {
		super(false, false, true);
		this.sampler = Properties.createProperty(Sampler.class, sampler);
	}
	
	private CubeMapDiffuseModifier(CubeMapDiffuseModifier src) {
		super(false, false, true);
		this.sampler = src.sampler.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("samplerCube cubeMapDiffuseSampler"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("cubeMapDiffuse",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "cubeMapDiffuseV"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "cubeMapDiffuseF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(ShaderAttribute.$("vec3 position")),
				ShaderModule.attribs(ShaderAttribute.$("vec3 position")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		int texID = packet.getShader().getCommunicator().load1i("cubeMapDiffuseSampler");
		if(sampler.get() != null)
			sampler.get().bind(texID);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		sampler.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Sampler", sampler);
		return ctrl;
	}
	
	public Sampler getSampler() {
		return sampler.get();
	}
	
	public void setSampler(Sampler sampler) {
		this.sampler.set(sampler);
	}
	
	public Property<Sampler> samplerProperty() {
		return sampler;
	}
	
	public Modifier duplicate() {
		return new CubeMapDiffuseModifier(this);
	}
	
}
