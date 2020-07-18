package celestial.shading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import celestial.ctrl.PropertyController;
import celestial.data.GLData;
import celestial.render.UpdatePacket;
import celestial.shader.Shader;

public abstract class ShadingNode {
	
	private final List<Input> inputs;
	private final List<Output> outputs;
	
	protected ShadingNode() {
		this.inputs = new ArrayList<Input>();
		this.outputs = new ArrayList<Output>();
	}
	
	protected void registerInputs(Input... inputs) {
		registerInputs(Arrays.asList(inputs));
	}
	
	protected void registerInputs(Collection<Input> inputs) {
		this.inputs.addAll(inputs);
	}
	
	protected void registerOutputs(Output... outputs) {
		registerOutputs(Arrays.asList(outputs));
	}
	
	protected void registerOutputs(Collection<Output> outputs) {
		this.outputs.addAll(outputs);
	}
	
	public List<Input> getInputs() {
		return Collections.unmodifiableList(inputs);
	}
	
	public List<Output> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}
	
	public void update(UpdatePacket pckt) {
		for(Input input : inputs)
			if(input.getLink() != null && input.getLink().getSrc() != null)
				input.getLink().getSrc().update(pckt);
		implUpdate(pckt);
		for(Output output : outputs)
			output.reset();
	}
	
	public boolean containsData(GLData data, ShadingSystem system) {
		boolean containsData = false;
		for(Input input : inputs) {
			if(input.getLink() != null && input.getLink().getSrc() != null)
				containsData = containsData || input.getLink().getSrc().containsData(data, system);
			containsData = containsData || input.containsData(data);
			if(containsData)
				break;
		}
		for(Output output : outputs) {
			if(containsData)
				break;
			containsData = containsData || output.containsData(data, system);
		}
		return containsData;
	}
	
	public abstract Shader getShader(Output output);
	
	public abstract void preRender(Shader shader, ShadingSystem system);
	
	protected abstract void implUpdate(UpdatePacket pckt);
	
	public abstract PropertyController getPropertyController();
	
}
