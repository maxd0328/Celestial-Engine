package node.celestial.color;

import java.util.Arrays;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
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

public class ReinhardToneMapNode extends ShadingNode {
	
	private static final Shader SHADER = new Shader(
			Shader.getProgramSegment(ReinhardToneMapNode.class.getResource("color.glsl"), "reinhardToneMapV"),
			Shader.getProgramSegment(ReinhardToneMapNode.class.getResource("color.glsl"), "reinhardToneMapF"),
			Arrays.asList(ShaderAttribute.$("vec2 position")),
			Arrays.asList(ShaderAttribute.$("sampler2D color"), ShaderAttribute.$("float exposure"))
	);
	
	private final Property<Float> exposure;
	
	public ReinhardToneMapNode(float exposure) {
		super.registerInputs(new Attribute("color", ConnectorType.createConstant(new Vector4f(0, 0, 0, 1))));
		super.registerOutputs(new Output(ConnectorType.VEC4, this));
		this.exposure = Properties.createFloatProperty(exposure);
	}
	
	@Override
	public Shader getShader(Output output) {
		return SHADER;
	}
	
	@Override
	public void preRender(Shader shader, ShadingSystem system) {
		shader.getCommunicator().store1f("exposure", exposure.get());
	}
	
	@Override
	protected void implUpdate(UpdatePacket pckt) {
		exposure.update(!pckt.isPaused());
	}
	
	@Override
	public PropertyController getPropertyController() {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Exposure", exposure);
		return ctrl;
	}
	
	public float getExposure() {
		return exposure.get();
	}
	
	public void setExposure(float exposure) {
		this.exposure.set(exposure);
	}
	
	public Property<Float> exposureProperty() {
		return exposure;
	}
	
}
