package celestial.shading;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import celestial.core.EngineRuntime;
import celestial.ctrl.PropertyController;
import celestial.data.FrameBuffer;
import celestial.data.GLData;
import celestial.data.Sampler;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.render.GraphicUtil;
import celestial.render.UpdatePacket;
import celestial.shader.Shader;
import celestial.shader.ShaderAttribute;

public final class ShadingSystem {
	
	private static final VertexArray VAO;
	private static final int INACTIVITY_THRESHOLD = 120;
	
	static {
		float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};
		VAO = VertexArray.create(null, VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(positions));
	}
	
	public static VertexArray getVAO() {
		return VAO;
	}
	
	private final int texWidth;
	private final int texHeight;
	private final ShadingNode outputNode;
	
	private boolean activeThisFrame = false;
	private int inactivityCounter = 0;
	
	private boolean inUse = false;
	
	public ShadingSystem(int texWidth, int texHeight) {
		this.texWidth = texWidth;
		this.texHeight = texHeight;
		this.outputNode = new OutputShadingNode();
	}
	
	public int getTexWidth() {
		return texWidth;
	}
	
	public int getTexHeight() {
		return texHeight;
	}
	
	public ShadingNode getOutputNode() {
		return outputNode;
	}
	
	public Sampler obtainResult() {
		activeThisFrame = true;
		VAO.bind(0);
		GraphicUtil.disable(GraphicUtil.GL_DEPTH_TESTING);
		
		Sampler out = null;
		try {
			out = outputNode.getInputs().get(0).obtainInput(this);
		}
		catch(InputAvailabilityException ex) {
		}
		
		GraphicUtil.enable(GraphicUtil.GL_DEPTH_TESTING);
		VAO.unbind(0);
		
		return out;
	}
	
	public void renderResult() {
		activeThisFrame = true;
		VAO.bind(0);
		GraphicUtil.disable(GraphicUtil.GL_DEPTH_TESTING);
		
		try {
			Sampler out = outputNode.getInputs().get(0).obtainInput(this);
			
			OutputShadingNode.SHADER.activate();
			int textureUnit = OutputShadingNode.SHADER.getCommunicator().load1i("color");
			out.bind(textureUnit);
			
			EngineRuntime.draw(EngineRuntime.DATATYPE_VERTEX_ARRAYS, EngineRuntime.DRAWTYPE_TRIANGLE_STRIP, 0, VAO.getIndexCount());
			OutputShadingNode.SHADER.deactivate();
		}
		catch(InputAvailabilityException ex) {
		}
		
		GraphicUtil.enable(GraphicUtil.GL_DEPTH_TESTING);
		VAO.unbind(0);
	}
	
	public void update(UpdatePacket pckt) {
		outputNode.update(pckt);
		if(!activeThisFrame) {
			if(inactivityCounter < INACTIVITY_THRESHOLD) inactivityCounter++;
			else inUse = false;
		}
		else {
			inactivityCounter = 0;
			inUse = true;
		}
		activeThisFrame = false;
	}
	
	public boolean containsData(GLData data) {
		return inUse && outputNode.containsData(data, this);
	}
	
	protected void renderNode(ShadingNode node, Output output, FrameBuffer texture) {
		Map<String, Sampler> inputs = new LinkedHashMap<String, Sampler>();
		for(Input input : node.getInputs())
			inputs.put(input.getAttribName(), input.obtainInput(this));
		
		Shader shader = node.getShader(output);
		shader.activate();
		
		for(String attrib : inputs.keySet()) {
			int textureUnit = shader.getCommunicator().load1i(attrib);
			Sampler sampler = inputs.get(attrib);
			sampler.bind(textureUnit);
		}
		node.preRender(shader, this);
		texture.fboBind();
		
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		EngineRuntime.draw(EngineRuntime.DATATYPE_VERTEX_ARRAYS, EngineRuntime.DRAWTYPE_TRIANGLE_STRIP, 0, VAO.getIndexCount());
		
		texture.fboUnbind();
		shader.deactivate();
	}
	
	private static final class OutputShadingNode extends ShadingNode {
		
		private static final Shader SHADER = new Shader(Shader.getProgramSegment(OutputShadingNode.class.getResource("output.glsl"), "outputV"), Shader.getProgramSegment
				(OutputShadingNode.class.getResource("output.glsl"), "outputF"), Arrays.asList(ShaderAttribute.$("vec2 position")), Arrays.asList(ShaderAttribute.$("sampler2D color")));
		
		private OutputShadingNode() {
			super.registerInputs(new Input("input", ConnectorType.VEC4));
			super.registerOutputs();
		}
		
		@Override
		public Shader getShader(Output output) {
			return null;
		}
		
		@Override
		public void preRender(Shader shader, ShadingSystem system) {
		}
		
		@Override
		public void implUpdate(UpdatePacket pckt) {
		}
		
		@Override
		public PropertyController getPropertyController() {
			return new PropertyController();
		}
		
	}
	
}
