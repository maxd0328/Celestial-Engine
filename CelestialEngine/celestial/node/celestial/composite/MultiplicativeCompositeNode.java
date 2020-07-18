package node.celestial.composite;

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
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public final class MultiplicativeCompositeNode extends ShadingNode {
	
	private static final Shader SHADER = new Shader(
			Shader.getProgramSegment(MultiplicativeCompositeNode.class.getResource("composite.glsl"), "compositeV"),
			Shader.getProgramSegment(MultiplicativeCompositeNode.class.getResource("composite.glsl"), "multiplicativeCompositeF"),
			Arrays.asList(ShaderAttribute.$("vec2 position")),
			Arrays.asList(ShaderAttribute.$("sampler2D bottom"), ShaderAttribute.$("sampler2D top"))
	);
	
	public MultiplicativeCompositeNode() {
		super.registerInputs(new Attribute("bottom", ConnectorType.createConstant(new Vector4f(0, 0, 0, 1))), new Attribute("top", ConnectorType.createConstant(new Vector3f())));
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