package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class CylinderRigidBodyModifier extends AbstractRigidBodyModifier {
	
	private static final long serialVersionUID = -2456069121172041449L;
	
	public static final Factory<CylinderRigidBodyModifier> FACTORY = () -> new CylinderRigidBodyModifier(1f, 0.5f, new Vector3f(), 1f);
	
	public CylinderRigidBodyModifier(float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(mass, friction, gravityAcceleration, angularFactor);
	}
	
	private CylinderRigidBodyModifier(CylinderRigidBodyModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createCylinder(new Vector3f(1f));
	}
	
	@Override
	public Modifier duplicate() {
		return new CylinderRigidBodyModifier(this);
	}
	
}
