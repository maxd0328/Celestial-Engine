package node.celestial.filter;

import java.util.Arrays;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.PropertyController;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.render.UpdatePacket;
import celestial.shader.Shader;
import celestial.shader.ShaderAttribute;
import celestial.shading.ConnectorType;
import celestial.shading.Input;
import celestial.shading.Output;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;

public final class BrightnessContrastNode extends ShadingNode {
	
	private static final Shader SHADER = new Shader(
			Shader.getProgramSegment(BrightnessContrastNode.class.getResource("filter.glsl"), "brightnessContrastV"),
			Shader.getProgramSegment(BrightnessContrastNode.class.getResource("filter.glsl"), "brightnessContrastF"),
			Arrays.asList(ShaderAttribute.$("vec2 position")),
			Arrays.asList(ShaderAttribute.$("float brightness"), ShaderAttribute.$("float contrast"), ShaderAttribute.$("sampler2D color"))
	);
	
	private final Property<Float> brightness;
	private final Property<Float> contrast;
	
	public BrightnessContrastNode(float brightness, float contrast) {
		super.registerInputs(new Input("color", ConnectorType.VEC4));
		super.registerOutputs(new Output(ConnectorType.VEC4, this));
		this.brightness = Properties.createFloatProperty(brightness);
		this.contrast = Properties.createFloatProperty(contrast);
	}
	
	@Override
	public Shader getShader(Output output) {
		return SHADER;
	}
	
	@Override
	public void preRender(Shader shader, ShadingSystem system) {
		shader.getCommunicator().store1f("brightness", brightness.get());
		shader.getCommunicator().store1f("contrast", contrast.get());
	}
	
	@Override
	protected void implUpdate(UpdatePacket pckt) {
		brightness.update(!pckt.isPaused());
		contrast.update(!pckt.isPaused());
	}
	
	@Override
	public PropertyController getPropertyController() {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Brightness", brightness);
		ctrl.withProperty("Contrast", contrast);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 1);
		return ctrl;
	}
	
	public float getBrightness() {
		return brightness.get();
	}
	
	public void setBrightness(float brightness) {
		this.brightness.set(brightness);
	}
	
	public Property<Float> brightnessProperty() {
		return brightness;
	}
	
	public float getContrast() {
		return contrast.get();
	}
	
	public void setContrast(float contrast) {
		this.contrast.set(contrast);
	}
	
	public Property<Float> contrastProperty() {
		return contrast;
	}
	
}
