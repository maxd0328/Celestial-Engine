package node.celestial.input;

import celestial.core.EngineRuntime;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.render.UpdatePacket;
import celestial.shader.Shader;
import celestial.shading.ConnectorType;
import celestial.shading.FactoryOutput;
import celestial.shading.Output;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;
import celestial.util.Factory;

public final class SourceDepthInputNode extends ShadingNode {
	
	public SourceDepthInputNode() {
		super.registerInputs();
		super.registerOutputs(new FactoryOutput(ConnectorType.VEC4, new SceneRetriever()));
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
	
	private static final class SceneRetriever implements Factory<Sampler> {
		
		private static final long serialVersionUID = -1338396315736526829L;
		
		@Override
		public Sampler build() {
			EngineRuntime.getPostProcessingBuffer().sampleDepth();
			return EngineRuntime.getPostProcessingBuffer();
		}
		
	}
	
}
