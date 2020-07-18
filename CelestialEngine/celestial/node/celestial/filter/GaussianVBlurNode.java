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

public final class GaussianVBlurNode extends ShadingNode {
	
	private static final Shader SHADER = new Shader(
			Shader.getProgramSegment(GaussianVBlurNode.class.getResource("filter.glsl"), "gaussianVBlurV"),
			Shader.getProgramSegment(GaussianVBlurNode.class.getResource("filter.glsl"), "gaussianBlurF"),
			Arrays.asList(ShaderAttribute.$("vec2 position")),
			Arrays.asList(ShaderAttribute.$("float kernelRadius"), ShaderAttribute.$("float sigma"), ShaderAttribute.$("float targetHeight"), ShaderAttribute.$("sampler2D color"))
	);
	
	private final Property<Integer> kernelRadius;
	private final Property<Float> sigma;
	private final Property<Float> spread;
	
	public GaussianVBlurNode(int kernelRadius, float sigma, float spread) {
		super.registerInputs(new Input("color", ConnectorType.VEC4));
		super.registerOutputs(new Output(ConnectorType.VEC4, this));
		this.kernelRadius = Properties.createIntegerProperty(kernelRadius);
		this.sigma = Properties.createFloatProperty(sigma);
		this.spread = Properties.createFloatProperty(spread);
	}
	
	@Override
	public Shader getShader(Output output) {
		return SHADER;
	}
	
	@Override
	public void preRender(Shader shader, ShadingSystem system) {
		shader.getCommunicator().store1f("kernelRadius", kernelRadius.get());
		shader.getCommunicator().store1f("sigma", sigma.get());
		shader.getCommunicator().store1f("targetHeight", system.getTexHeight() / spread.get());
	}
	
	@Override
	protected void implUpdate(UpdatePacket pckt) {
		kernelRadius.update(!pckt.isPaused());
		sigma.update(!pckt.isPaused());
		spread.update(!pckt.isPaused());
	}
	
	@Override
	public PropertyController getPropertyController() {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Kernel Radius", kernelRadius);
		ctrl.withProperty("Sigma", sigma);
		ctrl.withProperty("Spread", spread).withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.EXCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 1);
		return ctrl;
	}
	
	public int getKernelRadius() {
		return kernelRadius.get();
	}
	
	public void setKernelRadius(int kernelRadius) {
		this.kernelRadius.set(kernelRadius);
	}
	
	public Property<Integer> kernelRadiusProperty() {
		return kernelRadius;
	}
	
	public float getSigma() {
		return sigma.get();
	}
	
	public void setSigma(float sigma) {
		this.sigma.set(sigma);
	}
	
	public Property<Float> sigmaProperty() {
		return sigma;
	}
	
	public float getSpread() {
		return spread.get();
	}
	
	public void setSpread(float spread) {
		this.spread.set(spread);
	}
	
	public Property<Float> spreadProperty() {
		return spread;
	}
	
}

