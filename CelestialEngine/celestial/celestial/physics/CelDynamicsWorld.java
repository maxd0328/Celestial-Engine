package celestial.physics;

import javax.vecmath.Vector3f;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;

public class CelDynamicsWorld extends DiscreteDynamicsWorld {
	
	public CelDynamicsWorld(Dispatcher dispatcher, BroadphaseInterface pairCache, ConstraintSolver constraintSolver, CollisionConfiguration collisionConfiguration) {
		super(dispatcher, pairCache, constraintSolver, collisionConfiguration);
	}
	
	@Override
	public void setGravity(Vector3f gravity) {
		this.gravity.set(gravity);
	}
	
	@Override
	public void addRigidBody(RigidBody body) {
		if(body.getCollisionShape() != null) {
			boolean isDynamic = !(body.isStaticObject() || body.isKinematicObject());
			short collisionFilterGroup = isDynamic ? (short) CollisionFilterGroups.DEFAULT_FILTER : (short) CollisionFilterGroups.STATIC_FILTER;
			short collisionFilterMask = isDynamic ? (short) CollisionFilterGroups.ALL_FILTER : (short) (CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.STATIC_FILTER);

			addCollisionObject(body, collisionFilterGroup, collisionFilterMask);
		}
	}
	
	public void addRigidBody(RigidBody body, short group, short mask) {
		if(body.getCollisionShape() != null) {
			addCollisionObject(body, group, mask);
		}
	}
	
}
