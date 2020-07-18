package node.celestial.filter;

import java.util.Arrays;
import celestial.ctrl.PropertyController;
import celestial.render.UpdatePacket;
import celestial.shader.Shader;
import celestial.shader.ShaderAttribute;
import celestial.shading.Attribute;
import celestial.shading.ConnectorType;
import celestial.shading.Output;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;
import celestial.vecmath.Vector4f;

public final class LuminanceExtractionNode extends ShadingNode {
	
	private static final Shader SHADER = new Shader(
			Shader.getProgramSegment(LuminanceExtractionNode.class.getResource("filter.glsl"), "luminanceExtractionV"),
			Shader.getProgramSegment(LuminanceExtractionNode.class.getResource("filter.glsl"), "luminanceExtractionF"),
			Arrays.asList(ShaderAttribute.$("vec2 position")),
			Arrays.asList(ShaderAttribute.$("sampler2D color"), ShaderAttribute.$("sampler2D threshold"))
	);
	
	public LuminanceExtractionNode() {
		super.registerInputs(new Attribute("color", ConnectorType.createConstant(new Vector4f(0, 0, 0, 1))), new Attribute("threshold", ConnectorType.createConstant(0f)));
		super.registerOutputs(new Output(ConnectorType.VEC4, this));
	}
	
	@Override
	public Shader getShader(Output output) {
		return SHADER;
	}
	
	@Override
	public void preRender(Shader shader, ShadingSystem system) {
	}
	
	@Override
	protected void implUpdate(UpdatePacket pckt) {
	}
	
	@Override
	public PropertyController getPropertyController() {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
}
