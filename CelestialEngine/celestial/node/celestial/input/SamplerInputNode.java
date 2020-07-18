package node.celestial.input;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.render.UpdatePacket;
import celestial.shader.Shader;
import celestial.shading.ConnectorType;
import celestial.shading.FactoryOutput;
import celestial.shading.Output;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;

public final class SamplerInputNode extends ShadingNode {
	
	private final Property<Sampler> sampler;
	
	public SamplerInputNode(Sampler sampler) {
		this.sampler = Properties.createProperty(Sampler.class, sampler);
		super.registerInputs();
		super.registerOutputs(new FactoryOutput(ConnectorType.VEC4, () -> this.sampler.get()));
	}
	
	@Override
	public Shader getShader(Output output) {
		return null;
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
	
	public Sampler getSampler() {
		return sampler.get();
	}
	
	public void setSamler(Sampler sampler) {
		this.sampler.set(sampler);
	}
	
	public Property<Sampler> samplerProperty() {
		return sampler;
	}
	
}
