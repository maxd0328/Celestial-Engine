package celestial.ui;

import celestial.core.EngineRuntime;
import celestial.data.GLData;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.shader.Shader;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;

public abstract class Graphic {
	
	private static final VertexArray DEFAULT_VAO;
	
	static {
		float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};
		DEFAULT_VAO = VertexArray.create(null, VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(positions));
	}
	
	public static void bindDefaultVAO() {
		DEFAULT_VAO.bind(0);
	}
	
	public static void unbindDefaultVAO() {
		DEFAULT_VAO.unbind(0);
	}
	
	public static VertexArray getDefaultVAO() {
		return DEFAULT_VAO;
	}
	
	private final Shader shader;
	
	private final VertexArray customVAO;
	private final int[] attribs;
	
	protected Graphic(Shader shader, VertexArray customVAO, int... attribs) {
		this.shader = shader;
		
		this.customVAO = customVAO;
		this.attribs = customVAO == null ? new int[] {0} : attribs;
	}
	
	protected Graphic(Shader shader) {
		this(shader, null);
	}
	
	public Shader getShader() {
		return shader;
	}
	
	public boolean hasCustomVAO() {
		return customVAO != null;
	}
	
	public VertexArray getVAO() {
		return hasCustomVAO() ? customVAO : DEFAULT_VAO;
	}
	
	public void bind(Graphic previous) {
		if(previous == null) {
			shader.activate();
			getVAO().bind(attribs);
		}
		else {
			if(previous.getShader() != shader) {
				previous.getShader().deactivate();
				shader.activate();
			}
			
			if(previous.getVAO() != getVAO()) {
				previous.getVAO().unbind(previous.attribs);
				getVAO().bind(attribs);
			}
		}
		
		preRender();
	}
	
	public void unbind(boolean hasNext) {
		postRender();
		
		if(!hasNext) {
			getVAO().unbind(attribs);
			shader.deactivate();
		}
	}
	
	public void render(Vector2f position, Vector2f scale) {
		scale = new Vector2f(scale.x / EngineRuntime.dispGetDisplayMode().getWidth(), scale.y / EngineRuntime.dispGetDisplayMode().getHeight());
		
		Matrix4f transform = new Matrix4f();
		transform.translate(new Vector2f(position.x / EngineRuntime.dispGetDisplayMode().getWidth() * 2f - 1f + scale.x,
				-(position.y / EngineRuntime.dispGetDisplayMode().getHeight() * 2f - 1f + scale.y)));
		transform.scale(new Vector3f(scale, 1.0f));
		shader.getCommunicator().store4x4f("transform", transform);
		
		EngineRuntime.draw(EngineRuntime.DATATYPE_VERTEX_ARRAYS, EngineRuntime.DRAWTYPE_TRIANGLE_STRIP, 0, getVAO().getIndexCount());
	}
	
	protected abstract void preRender();
	
	protected abstract void postRender();
	
	public abstract void update();
	
	public abstract boolean containsData(GLData data);
	
}
