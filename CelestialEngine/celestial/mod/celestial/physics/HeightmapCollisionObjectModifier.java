package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.data.Heightmap;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;

public final class HeightmapCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = 8187488540278595321L;
	
	public static final Factory<HeightmapCollisionObjectModifier> FACTORY = () -> new HeightmapCollisionObjectModifier(Heightmap.createEmpty(1, 1), 0.5f);
	
	private final Heightmap heightmap;
	
	public HeightmapCollisionObjectModifier(Heightmap heightmap, float friction) {
		super(friction);
		this.heightmap = heightmap;
	}
	
	private HeightmapCollisionObjectModifier(HeightmapCollisionObjectModifier src) {
		super(src);
		this.heightmap = src.heightmap;
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createBvh(heightmap.getVertexArray(0), heightmap.getIndexArray(0));
	}
	
	@Override
	public Modifier duplicate() {
		return new HeightmapCollisionObjectModifier(this);
	}
	
}
