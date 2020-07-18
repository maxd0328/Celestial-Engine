package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class ConvexHullRigidBodyModifier extends AbstractRigidBodyModifier {
	
	private static final long serialVersionUID = 6958284356921653652L;
	
	public static final Factory<ConvexHullRigidBodyModifier> FACTORY = () -> new ConvexHullRigidBodyModifier(new float[] {}, 1f, 0.5f, new Vector3f(), 1f);
	
	private final float[] vertexData;
	
	public ConvexHullRigidBodyModifier(float[] vertexData, float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(mass, friction, gravityAcceleration, angularFactor);
		this.vertexData = vertexData;
	}
	
	private ConvexHullRigidBodyModifier(ConvexHullRigidBodyModifier src) {
		super(src);
		this.vertexData = src.vertexData;
	}
	
	public float[] getVertexData() {
		return vertexData;
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createConvexHull(vertexData);
	}
	
	@Override
	public Modifier duplicate() {
		return new ConvexHullRigidBodyModifier(this);
	}
	
}
