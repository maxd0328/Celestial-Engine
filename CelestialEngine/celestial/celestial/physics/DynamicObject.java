package celestial.physics;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import celestial.vecmath.Quat4f;
import celestial.vecmath.Vector3f;

public final class DynamicObject extends RegisteredProfile {
	
	private final RigidBody body;
	private final CollisionMesh mesh;
	private boolean massChanged;
	
	public DynamicObject(CollisionMesh mesh, Vector3f position, Vector3f rotation) {
		super();
		javax.vecmath.Vector3f __inertia = new javax.vecmath.Vector3f();
		try {
			mesh.getShape().calculateLocalInertia(1f, __inertia);
		}
		catch(AssertionError err) {}
		Vector3f inertia = PhysicsUtil.toNative(__inertia);
		if(inertia.length() == 0) inertia.set(1f, 1f, 1f);
		
		Transform transform = new Transform();
		transform.basis.set(PhysicsUtil.toJavax(PhysicsUtil.toQuaternion(rotation).toRotation3Matrix()));
		transform.origin.set(PhysicsUtil.toJavax(position));
		MotionState motionState = new DefaultMotionState(transform);
		
		RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(1f, motionState, mesh.getShape(), PhysicsUtil.toJavax(inertia));
		this.body = new RigidBody(info);
		this.mesh = mesh;
		this.massChanged = false;
	}
	
	public MotionState getMotionState() {
		return body.getMotionState();
	}
	
	public RigidBody getBody() {
		return body;
	}
	
	public CollisionMesh getMesh() {
		return mesh;
	}
	
	public Vector3f getSimulationPosition() {
		return PhysicsUtil.toNative(body.getMotionState().getWorldTransform(new Transform()).origin);
	}
	
	public Vector3f getSimulationRotation() {
		return PhysicsUtil.toEuler(PhysicsUtil.toNative(body.getMotionState().getWorldTransform(new Transform()).getRotation(new javax.vecmath.Quat4f())));
	}
	
	public Quat4f getSimulationRotationQuat() {
		return PhysicsUtil.toNative(body.getMotionState().getWorldTransform(new Transform()).getRotation(new javax.vecmath.Quat4f()));
	}
	
	public void setSimulationPosition(Vector3f position) {
		Transform transform = new Transform();
		transform.basis.set(body.getMotionState().getWorldTransform(new Transform()).basis);
		transform.origin.set(PhysicsUtil.toJavax(position));
		this.body.setMotionState(new DefaultMotionState(transform));
	}
	
	public void setSimulationRotation(Vector3f rotation) {
		Transform transform = new Transform();
		transform.basis.set(PhysicsUtil.toJavax(PhysicsUtil.toQuaternion(rotation).toRotation3Matrix()));
		transform.origin.set(body.getMotionState().getWorldTransform(new Transform()).origin);
		this.body.setMotionState(new DefaultMotionState(transform));
	}
	
	public void setSimulationRotationQuat(Quat4f rotation) {
		Transform transform = new Transform();
		transform.basis.set(PhysicsUtil.toJavax(rotation.toRotation3Matrix()));
		transform.origin.set(body.getMotionState().getWorldTransform(new Transform()).origin);
		this.body.setMotionState(new DefaultMotionState(transform));
	}
	
	public float getFriction() {
		return body.getFriction();
	}
	
	public void setFriction(float friction) {
		this.body.setFriction(friction);
	}
	
	public float getMass() {
		return body.getInvMass() == 0f ? 0f : 1f / body.getInvMass();
	}
	
	public void setMass(float mass) {
		if(mass == getMass()) return;
		javax.vecmath.Vector3f invInertia = body.getInvInertiaDiagLocal(new javax.vecmath.Vector3f());
		if(invInertia.x != 0) invInertia.x = 1f / invInertia.x;
		if(invInertia.y != 0) invInertia.y = 1f / invInertia.y;
		if(invInertia.z != 0) invInertia.z = 1f / invInertia.z;
		this.body.setMassProps(mass, invInertia);
		this.massChanged = true;
	}
	
	public Vector3f getGravityAcceleration() {
		return PhysicsUtil.toNative(body.getGravity(new javax.vecmath.Vector3f()));
	}
	
	public void setGravityAcceleration(Vector3f gravityAcceleration) {
		this.body.setGravity(PhysicsUtil.toJavax(gravityAcceleration));
	}
	
	public float getAngularFactor() {
		return body.getAngularFactor();
	}
	
	public void setAngularFactor(float angularFactor) {
		this.body.setAngularFactor(angularFactor);
	}
	
	public Vector3f getLinearVelocity() {
		return PhysicsUtil.toNative(this.body.getLinearVelocity(PhysicsUtil.toJavax(new Vector3f())));
	}
	
	public void setLinearVelocity(Vector3f velocity) {
		this.body.setLinearVelocity(PhysicsUtil.toJavax(velocity));
	}
	
	public void applyNetForce(Vector3f direction, float force) {
		Vector3f cpy = new Vector3f(direction).normalize().scale(force);
		this.applyNetForce(cpy);
	}
	
	public void applyNetForce(Vector3f velVector) {
		this.body.applyCentralImpulse(PhysicsUtil.toJavax(velVector));
	}
	
	public void applyNetForce(Vector3f direction, float force, Vector3f relPos) {
		Vector3f cpy = new Vector3f(direction).normalize().scale(force);
		this.applyNetForce(cpy, relPos);
	}
	
	public void applyNetForce(Vector3f velVector, Vector3f relPos) {
		this.body.applyImpulse(PhysicsUtil.toJavax(velVector), PhysicsUtil.toJavax(relPos));
	}
	
	public void applyNetTorque(Vector3f orientation, float force) {
		Quat4f target = PhysicsUtil.toQuaternion(orientation);
		Quat4f current = getSimulationRotationQuat();
		current.invert();
		target.mul(current);
		Vector3f delta = PhysicsUtil.toEuler(target);
		Vector3f torque = new Vector3f(delta.x * force, delta.y * force, delta.z * force);
		this.body.applyTorqueImpulse(PhysicsUtil.toJavax(torque));
	}
	
	public void applyNetTorque(Vector3f torque) {
		this.body.applyTorque(PhysicsUtil.toJavax(torque));
	}
	
	public void capHorizontalVelocity(float length) {
		Vector3f vel = PhysicsUtil.toNative(this.body.getLinearVelocity(new javax.vecmath.Vector3f()));
		float vlen = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		if(vlen > length) {
			Vector3f horiz = new Vector3f(vel.x, 0, vel.z);
			horiz.normalize();
			horiz.scale(length);
			this.body.setLinearVelocity(new javax.vecmath.Vector3f(horiz.x, vel.y, horiz.z));
		}
	}
	
	protected void update() {
		body.activate(true);
		body.setCollisionFlags(0);
		if(massChanged || mesh.isInertiaChanged()) {
			this.massChanged = false;
			mesh.setInertiaChanged(false);
			
			float mass = body.getInvMass() == 0f ? 0f : 1f / body.getInvMass();
			javax.vecmath.Vector3f __inertia = new javax.vecmath.Vector3f();
			mesh.getShape().calculateLocalInertia(mass, __inertia);
			body.setMassProps(mass, __inertia);
		}
	}
	
}
