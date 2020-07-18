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

public final class SourceColorInputNode extends ShadingNode {
	
	public SourceColorInputNode() {
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
		
		private static final long serialVersionUID = 7350390378544890341L;
		
		@Override
		public Sampler build() {
			EngineRuntime.getPostProcessingBuffer().sampleTexture();
			return EngineRuntime.getPostProcessingBuffer();
		}
		
	}
	
}
