package mod.celestial.mesh;

import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.render.RenderPacket;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.misc.CameraModifier;

public final class PointMeshModifier extends AbstractMeshModifier {
	
	private static final long serialVersionUID = 4786019172810301444L;
	
	public static final Factory<PointMeshModifier> FACTORY = () -> new PointMeshModifier(false, true, new Vector3f());
	
	private static final float[] VERTICES = {
			-1.0f, -1.0f, 0.0f,
			1.0f, -1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f
	};
	
	private static final float[] TEXCOORDS = {
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f
	};
	
	private static final float[] NORMALS = {
			0.0f, -0.0f, 1.0f,
			0.0f, -0.0f, 1.0f,
			0.0f, -0.0f, 1.0f,
			0.0f, -0.0f, 1.0f
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
	
	public PointMeshModifier(boolean cullBackFace, boolean depthTest, Vector3f offset) {
		super(false, cullBackFace, depthTest, offset);
	}
	
	private PointMeshModifier(PointMeshModifier src) {
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
		return new PointMeshModifier(this);
	}
	
	@Override
	protected void render(RenderPacket packet, CEObject obj) {
		if(!getVAO().isAllocated()) return;
		Matrix4f transform = new Matrix4f(), viewMatrix = packet.getCamera().getModifier(CameraModifier.class).getViewMatrix();
		transform.translate(new Vector3f(obj.getTransformation().transform(new Vector4f(super.getOffset(), 1))));
		transform.m00 = viewMatrix.m00;
		transform.m01 = viewMatrix.m10;
		transform.m02 = viewMatrix.m20;
		transform.m10 = viewMatrix.m01;
		transform.m11 = viewMatrix.m11;
		transform.m12 = viewMatrix.m21;
		transform.m20 = viewMatrix.m02;
		transform.m21 = viewMatrix.m12;
		transform.m22 = viewMatrix.m22;
		transform.rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
		transform.rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
		transform.rotate((float) Math.toRadians(obj.getRotation().z), new Vector3f(0, 0, 1));
		transform.scale(obj.getScale());
		packet.getShader().getCommunicator().store4x4f("transform", transform);
		EngineRuntime.draw(EngineRuntime.DATATYPE_ARRAY_ELEMENTS, EngineRuntime.DRAWTYPE_TRIANGLES, 0, getVAO().getIndexCount());
	}
	
}