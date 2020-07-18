package mod.celestial.mesh;

import celestial.core.Modifier;
import celestial.data.DataReader;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class CylinderMeshModifier extends AbstractMeshModifier {
	
	private static final long serialVersionUID = 1691784420783767334L;
	
	public static final Factory<CylinderMeshModifier> FACTORY = () -> new CylinderMeshModifier(false, true, new Vector3f());
	
	private static VertexArray vao = null;
	
	public CylinderMeshModifier(boolean cullBackFace, boolean depthTest, Vector3f offset) {
		super(false, cullBackFace, depthTest, offset);
	}
	
	private CylinderMeshModifier(CylinderMeshModifier src) {
		super(src);
	}
	
	protected void createVAO(Shader shader) {
		String src = "C:\\Celestial\\resources\\cylinder.obj";
		
		vao = VertexArray.create(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei(src, DataReader.DATA_FORMAT_OBJ, 0, 4),
				shader.sortAttribs(
				new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 0)),
				new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 1)),
				new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 2)),
				new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 3))
				));
	}
	
	protected VertexArray getVAO() {
		return vao;
	}
	
	public Modifier duplicate() {
		return new CylinderMeshModifier(this);
	}
	
}