package mod.celestial.mesh;

import celestial.core.Modifier;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class PlaneMeshModifier extends AbstractMeshModifier {
	
	private static final long serialVersionUID = -2765413845362275762L;
	
	public static final Factory<PlaneMeshModifier> FACTORY = () -> new PlaneMeshModifier(false, true, new Vector3f());
	
	private static final float[] VERTICES = {
			-1.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 1.0f,
			-1.0f, 0.0f, -1.0f,
			1.0f, 0.0f, -1.0f
	};
	
	private static final float[] TEXCOORDS = {
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f
	};
	
	private static final float[] NORMALS = {
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f
	};
	
	private static final float[] TANGENTS = {
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f
	};
	
	private static final int[] INDICES = {
			1, 2, 0,
			1, 3, 2
	};
	
	private static VertexArray vao = null;
	
	public PlaneMeshModifier(boolean cullBackFace, boolean depthTest, Vector3f offset) {
		super(false, cullBackFace, depthTest, offset);
	}
	
	private PlaneMeshModifier(PlaneMeshModifier src) {
		super(src);
	}
	
	protected void createVAO(Shader shader) {
		vao = VertexArray.create(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei(INDICES), shader.sortAttribs(
				new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(VERTICES)),
				new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(TEXCOORDS)),
				new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(NORMALS)),
				new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(TANGENTS))
				));
	}
	
	protected VertexArray getVAO() {
		return vao;
	}
	
	public Modifier duplicate() {
		return new PlaneMeshModifier(this);
	}
	
}