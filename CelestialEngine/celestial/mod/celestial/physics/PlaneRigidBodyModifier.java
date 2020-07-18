package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class PlaneRigidBodyModifier extends AbstractRigidBodyModifier {
	
	private static final long serialVersionUID = -1021046237455036963L;
	
	public static final Factory<PlaneRigidBodyModifier> FACTORY = () -> new PlaneRigidBodyModifier(1f, 0.5f, new Vector3f(), 1f);
	
	public PlaneRigidBodyModifier(float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(mass, friction, gravityAcceleration, angularFactor);
	}
	
	private PlaneRigidBodyModifier(PlaneRigidBodyModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createBox(new Vector3f(1, 0, 1));
	}
	
	@Override
	public Modifier duplicate() {
		return new PlaneRigidBodyModifier(this);
	}
	
}
