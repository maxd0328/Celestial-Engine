package celestial.physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import celestial.vecmath.Vector3f;

public final class SimulationInterface {
	
	private final BroadphaseInterface broadphase;
	private final CollisionConfiguration config;
	private final CollisionDispatcher dispatcher;
	
	private final ConstraintSolver solver;
	private final DynamicsWorld simulation;
	
	public SimulationInterface() {
		this.broadphase = new DbvtBroadphase();
		this.config = new DefaultCollisionConfiguration();
		this.dispatcher = new CollisionDispatcher(config);
		
		this.solver = new SequentialImpulseConstraintSolver();
		this.simulation = new CelDynamicsWorld(dispatcher, broadphase, solver, config);
		this.simulation.setGravity(PhysicsUtil.toJavax(new Vector3f()));
	}
	
	public BroadphaseInterface getBroadphaseInterface() {
		return broadphase;
	}
	
	public CollisionConfiguration getCollisionConfiguration() {
		return config;
	}
	
	public CollisionDispatcher getCollisionDispatcher() {
		return dispatcher;
	}
	
	public ConstraintSolver getConstraintSolver() {
		return solver;
	}
	
	public DynamicsWorld getSimulation() {
		return simulation;
	}
	
}
