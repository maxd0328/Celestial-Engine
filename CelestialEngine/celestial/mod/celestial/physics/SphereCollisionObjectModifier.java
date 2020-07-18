package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;

public final class SphereCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = -1457111464204343919L;
	
	public static final Factory<SphereCollisionObjectModifier> FACTORY = () -> new SphereCollisionObjectModifier(0.5f);
	
	public SphereCollisionObjectModifier(float friction) {
		super(friction);
	}
	
	private SphereCollisionObjectModifier(SphereCollisionObjectModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createSphere(1f);
	}
	
	@Override
	public Modifier duplicate() {
		return new SphereCollisionObjectModifier(this);
	}
	
}
