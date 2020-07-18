package mod.celestial.mesh;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.data.VertexArray;
import celestial.data.Heightmap;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class HeightmapMeshModifier extends AbstractMeshModifier {
	
	private static final long serialVersionUID = 5949363446535089078L;
	
	public static final Factory<HeightmapMeshModifier> FACTORY = () -> new HeightmapMeshModifier(false, true, new Vector3f(), false, Heightmap.createEmpty(1, 1), 1, 10);
	
	private final Heightmap heightmap;
	private final int lodCount;
	private final boolean dynamic;
	private final ArrayList<VertexArray> vaos;
	private final Property<Float> lodThreshold;
	private int activeIndex = 0;
	
	public HeightmapMeshModifier(boolean cullBackFace, boolean depthTest, Vector3f offset, boolean dynamic, Heightmap map, int lodCount, float lodThreshold) {
		super(lodCount > 1, cullBackFace, depthTest, offset);
		this.heightmap = map;
		this.lodCount = lodCount;
		this.dynamic = dynamic;
		this.vaos = new ArrayList<VertexArray>();
		this.lodThreshold = Properties.createFloatProperty(lodThreshold);
	}
	
	private HeightmapMeshModifier(HeightmapMeshModifier src) {
		super(src);
		this.heightmap = src.heightmap;
		this.lodCount = src.lodCount;
		this.dynamic = src.dynamic;
		this.vaos = new ArrayList<VertexArray>(src.vaos);
		this.lodThreshold = src.lodThreshold.clone();
	}
	
	protected void createVAO(Shader shader) {
		if(vaos.size() > 0) return;
		for(int i = 0 ; i < lodCount ; ++i) {
			VertexArray vao = VertexArray.create(heightmap.getIndexBuffer(dynamic, i), shader.sortAttribs(
					new UnsortedAttrib("position", heightmap.getVertexBuffer(dynamic, i)), new UnsortedAttrib("texCoords", heightmap.getTexCoordBuffer(dynamic, i)),
					new UnsortedAttrib("normal", heightmap.getNormalBuffer(dynamic, i)), new UnsortedAttrib("tangent", heightmap.getTangentBuffer(dynamic, i))));
			vaos.add(vao);
		}
	}
	
	protected VertexArray getVAO() {
		if(activeIndex < 0 || activeIndex >= vaos.size()) return null;
		return vaos.get(activeIndex);
	}
	
	public Heightmap getHeightmap() {
		return heightmap;
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
	
	public Modifier duplicate() {
		return new HeightmapMeshModifier(this);
	}
	
	@Override
	protected void updateVAO(RenderPacket packet, CEObject obj) {
		float distance = Vector3f.sub(packet.getCamera().getPosition(), obj.getPosition()).length();
		activeIndex = Math.min((int) (distance / (lodThreshold.get() * Math.max(Math.max(obj.getScale().x, obj.getScale().y), obj.getScale().z))), vaos.size() - 1);
	}
	
}
