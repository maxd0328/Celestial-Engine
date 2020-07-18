package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class SphereRigidBodyModifier extends AbstractRigidBodyModifier {
	
	private static final long serialVersionUID = 1371224003247412701L;
	
	public static final Factory<SphereRigidBodyModifier> FACTORY = () -> new SphereRigidBodyModifier(1f, 0.5f, new Vector3f(), 1f);
	
	public SphereRigidBodyModifier(float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(mass, friction, gravityAcceleration, angularFactor);
	}
	
	private SphereRigidBodyModifier(SphereRigidBodyModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createSphere(1f);
	}
	
	@Override
	public Modifier duplicate() {
		return new SphereRigidBodyModifier(this);
	}
	
}
