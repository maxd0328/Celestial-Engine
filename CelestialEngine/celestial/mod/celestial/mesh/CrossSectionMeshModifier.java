package mod.celestial.mesh;

import celestial.core.Modifier;
import celestial.data.DataReader;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class CrossSectionMeshModifier extends AbstractMeshModifier {
	
	private static final long serialVersionUID = -2765413845362275762L;
	
	public static final Factory<CrossSectionMeshModifier> FACTORY = () -> new CrossSectionMeshModifier(false, true, new Vector3f());
	
	private static VertexArray vao = null;
	
	public CrossSectionMeshModifier(boolean cullBackFace, boolean depthTest, Vector3f offset) {
		super(false, cullBackFace, depthTest, offset);
	}
	
	private CrossSectionMeshModifier(CrossSectionMeshModifier src) {
		super(src);
	}
	
	protected void createVAO(Shader shader) {
		final String path = "C:\\Celestial\\resources\\line.obj";
		vao = VertexArray.create(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei(path, DataReader.DATA_FORMAT_OBJ, 0, 4),
				shader.sortAttribs(
						new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(path, DataReader.DATA_FORMAT_OBJ, 0, 0)),
						new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(path, DataReader.DATA_FORMAT_OBJ, 0, 1)),
						new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(path, DataReader.DATA_FORMAT_OBJ, 0, 2)),
						new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(path, DataReader.DATA_FORMAT_OBJ, 0, 3))
				));
	}
	
	protected VertexArray getVAO() {
		return vao;
	}
	
	public Modifier duplicate() {
		return new CrossSectionMeshModifier(this);
	}
	
}