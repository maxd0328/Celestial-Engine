package celestial.physics;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.linearmath.Transform;
import celestial.vecmath.Quat4f;
import celestial.vecmath.Vector3f;

public final class StaticObject extends RegisteredProfile {
	
	private CollisionObject object;
	private CollisionMesh mesh;
	
	public StaticObject(CollisionMesh mesh, Vector3f position, Vector3f rotation) {
		super();
		this.object = new CollisionObject();
		this.object.setCollisionShape(mesh.getShape());
		Transform transform = new Transform();
		transform.basis.set(PhysicsUtil.toJavax(PhysicsUtil.toQuaternion(rotation).toRotation3Matrix()));
		transform.origin.set(PhysicsUtil.toJavax(position));
		this.object.setWorldTransform(transform);
		this.mesh = mesh;
	}
	
	public CollisionObject getCollisionObject() {
		return object;
	}
	
	public CollisionMesh getCollisionMesh() {
		return mesh;
	}
	
	public float getFriction() {
		return object.getFriction();
	}
	
	public void setFriction(float friction) {
		this.object.setFriction(friction);
	}
	
	public Vector3f getPosition() {
		return PhysicsUtil.toNative(object.getWorldTransform(new Transform()).origin);
	}
	
	public Vector3f getRotation() {
		return PhysicsUtil.toEuler(new Quat4f().set(PhysicsUtil.toNative(object.getWorldTransform(new Transform()).basis)));
	}
	
	public void setPosition(Vector3f position) {
		Transform transform = new Transform();
		transform.basis.set(object.getWorldTransform(new Transform()).basis);
		transform.origin.set(PhysicsUtil.toJavax(position));
		this.object.setWorldTransform(transform);
	}
	
	public void setRotation(Vector3f rotation) {
		Transform transform = new Transform();
		transform.basis.set(PhysicsUtil.toJavax(PhysicsUtil.toQuaternion(rotation).toRotation3Matrix()));
		transform.origin.set(object.getWorldTransform(new Transform()).origin);
		this.object.setWorldTransform(transform);
	}
	
}
