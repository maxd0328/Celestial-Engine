package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class ConeRigidBodyModifier extends AbstractRigidBodyModifier {
	
	private static final long serialVersionUID = -9103787566271691651L;
	
	public static final Factory<ConeRigidBodyModifier> FACTORY = () -> new ConeRigidBodyModifier(1f, 0.5f, new Vector3f(), 1f);
	
	public ConeRigidBodyModifier(float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(mass, friction, gravityAcceleration, angularFactor);
	}
	
	private ConeRigidBodyModifier(ConeRigidBodyModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createCone(1f, 2f);
	}
	
	@Override
	public Modifier duplicate() {
		return new ConeRigidBodyModifier(this);
	}
	
}
