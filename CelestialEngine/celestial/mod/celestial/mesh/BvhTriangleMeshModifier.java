package mod.celestial.mesh;

import java.util.ArrayList;
import java.util.Collection;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.data.GLData;
import celestial.data.VertexArray;
import celestial.data.VertexBuffer;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;
import studio.celestial.media.Media.MediaType;

public final class BvhTriangleMeshModifier extends AbstractMeshModifier {
	
	private static final long serialVersionUID = 4863107951705208431L;
	
	private static final BvhTriangleVAO EMPTY_VAO = new BvhTriangleVAO(
			VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei(new int[] {}),
			new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(new float[] {})),
			new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(new float[] {})),
			new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(new float[] {})),
			new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(new float[] {}))
	);
	
	public static final Factory<BvhTriangleMeshModifier> FACTORY = () -> new BvhTriangleMeshModifier(false, true, new Vector3f(), 10, EMPTY_VAO);
	
	private final BvhTriangleVAO[] rawVaos;
	
	private final ArrayList<VertexArray> vaos;
	private final Property<Float> lodThreshold;
	private int activeIndex = 0;
	
	public BvhTriangleMeshModifier(boolean cullBackFace, boolean depthTest, Vector3f offset, float lodThreshold, BvhTriangleVAO... vaos) {
		super(vaos.length > 1, cullBackFace, depthTest, offset);
		this.rawVaos = vaos;
		this.vaos = new ArrayList<VertexArray>();
		this.lodThreshold = Properties.createFloatProperty(lodThreshold);
	}
	
	private BvhTriangleMeshModifier(BvhTriangleMeshModifier src) {
		super(src);
		this.rawVaos = src.rawVaos;
		this.vaos = new ArrayList<VertexArray>(src.vaos);
		this.lodThreshold = src.lodThreshold.clone();
	}
	
	private BvhTriangleMeshModifier(BvhTriangleMeshModifier src, Collection<VertexArray> vaos) {
		super(src);
		this.rawVaos = new BvhTriangleVAO[] {};
		this.vaos = new ArrayList<VertexArray>(vaos);
		this.lodThreshold = src.lodThreshold.clone();
	}
	
	protected void createVAO(Shader shader) {
		if(vaos.size() > 0) return;
		for(int i = 0 ; i < rawVaos.length ; ++i) {
			VertexArray vao = VertexArray.create(rawVaos[i].indexBuffer, shader.sortAttribs(rawVaos[i].attribs));
			this.vaos.add(vao);
		}
	}
	
	protected VertexArray getVAO() {
		if(activeIndex < 0 || activeIndex >= vaos.size()) return null;
		return vaos.get(activeIndex);
	}
	
	public float getLODThreshold() {
		return lodThreshold.get();
	}
	
	public void setLODThreshold(float lodThreshold) {
		this.lodThreshold.set(lodThreshold);
	}
	
	public Property<Float> lodThresholdProperty() {
		return lodThreshold;
	}
	
	@Override
	protected void update1(UpdatePacket packet, CEObject obj) {
		lodThreshold.update(!packet.isPaused());
	}
	
	@Override
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = super.getPropertyController(system);
		ctrl.withProperty("Model Media", Properties.createProperty(GLData[].class, () -> getVAOS(), s -> setVAOS(s, system)));
		ctrl.getProperty("Model Media").setUserPointer(new MediaType[] {MediaType.OBJ, MediaType.VAO, MediaType.HMAP});
		return ctrl;
	}
	
	public Modifier duplicate() {
		return new BvhTriangleMeshModifier(this);
	}
	
	private Modifier duplicate(ArrayList<VertexArray> vaos) {
		return new BvhTriangleMeshModifier(this, vaos);
	}
	
	@Override
	protected void updateVAO(RenderPacket packet, CEObject obj) {
		float distance = Vector3f.sub(packet.getCamera().getPosition(), obj.getPosition()).length();
		activeIndex = Math.min((int) (distance / (lodThreshold.get() * Math.max(Math.max(obj.getScale().x, obj.getScale().y), obj.getScale().z))), vaos.size() - 1);
	}
	
	private GLData[] getVAOS() {
		VertexArray[] arr = new VertexArray[vaos.size()];
		vaos.toArray(arr);
		return arr;
	}
	
	private void setVAOS(GLData[] data, ISceneSystem system) {
		ArrayList<VertexArray> vaos = new ArrayList<VertexArray>();
		for(int i = 0 ; i < data.length ; ++i) vaos.add((VertexArray) data[i]);
		system.reinstantiateModifier(this, duplicate(vaos));
	}
	
	public static final class BvhTriangleVAO implements java.io.Serializable {
		
		private static final long serialVersionUID = -1297395657453078606L;
		
		private final VertexBuffer indexBuffer;
		private final UnsortedAttrib[] attribs;
		
		public BvhTriangleVAO(VertexBuffer indexBuffer, UnsortedAttrib... attribs) {
			this.indexBuffer = indexBuffer;
			this.attribs = attribs;
		}
		
		public VertexBuffer getIndexBuffer() {
			return indexBuffer;
		}
		
		public UnsortedAttrib[] getAttribs() {
			return attribs;
		}
		
	}
	
}
