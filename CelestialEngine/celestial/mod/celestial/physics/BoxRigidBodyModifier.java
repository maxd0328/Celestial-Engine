package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class BoxRigidBodyModifier extends AbstractRigidBodyModifier {
	
	private static final long serialVersionUID = -1265940172381082861L;
	
	public static final Factory<BoxRigidBodyModifier> FACTORY = () -> new BoxRigidBodyModifier(1f, 0.5f, new Vector3f(), 1f);
	
	public BoxRigidBodyModifier(float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(mass, friction, gravityAcceleration, angularFactor);
	}
	
	private BoxRigidBodyModifier(BoxRigidBodyModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createBox(new Vector3f(1));
	}
	
	@Override
	public Modifier duplicate() {
		return new BoxRigidBodyModifier(this);
	}
	
}
