package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.util.ISceneSystem;

public final class BvhCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = 2994941660638229480L;
	
	public static final Factory<BvhCollisionObjectModifier> FACTORY = () -> new BvhCollisionObjectModifier(new int[] {}, new float[] {}, 0.5f);
	
	private final int[] indices;
	private final float[] vertexData;
	
	public BvhCollisionObjectModifier(int[] indices, float[] vertexData, float friction) {
		super(friction);
		this.indices = indices;
		this.vertexData = vertexData;
	}
	
	private BvhCollisionObjectModifier(BvhCollisionObjectModifier src) {
		super(src);
		this.indices = src.indices;
		this.vertexData = src.vertexData;
	}
	
	@Override
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = super.getPropertyController(system);
		return ctrl;
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createBvh(vertexData, indices);
	}
	
	@Override
	public Modifier duplicate() {
		return new BvhCollisionObjectModifier(this);
	}
	
}
