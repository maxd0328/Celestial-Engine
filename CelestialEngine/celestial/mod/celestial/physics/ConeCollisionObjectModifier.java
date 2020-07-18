package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;

public final class ConeCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = -5876683190113873925L;
	
	public static final Factory<ConeCollisionObjectModifier> FACTORY = () -> new ConeCollisionObjectModifier(0.5f);
	
	public ConeCollisionObjectModifier(float friction) {
		super(friction);
	}
	
	private ConeCollisionObjectModifier(ConeCollisionObjectModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createCone(1f, 2f);
	}
	
	@Override
	public Modifier duplicate() {
		return new ConeCollisionObjectModifier(this);
	}
	
}
