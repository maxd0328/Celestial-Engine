package mod.celestial.physics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.ctrl.PropertyController;
import celestial.physics.CollisionMesh;
import celestial.physics.DynamicObject;
import celestial.physics.PhysicsSimulation;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.serialization.SerializerImpl;
import celestial.shader.ShaderModule;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;

public abstract class AbstractRigidBodyModifier extends Modifier {
	
	private static final long serialVersionUID = 3339256524428616414L;
	
	protected transient HashMap<CEObject, DynamicObject> objectMap;
	
	protected final Property<Float> mass;
	protected final Property<Float> friction;
	protected final Property<Vector3f> gravityAcceleration;
	protected final Property<Float> angularFactor;
	
	protected final Collection<CEObject> restrictedObjects = new ArrayList<>();
	
	public AbstractRigidBodyModifier(float mass, float friction, Vector3f gravityAcceleration, float angularFactor) {
		super(false, false, false);
		this.mass = Properties.createFloatProperty(mass);
		this.friction = Properties.createFloatProperty(friction);
		this.gravityAcceleration = Properties.createVec3Property(gravityAcceleration);
		this.angularFactor = Properties.createFloatProperty(angularFactor);
		this.objectMap = new HashMap<CEObject, DynamicObject>();
	}
	
	protected AbstractRigidBodyModifier(AbstractRigidBodyModifier src) {
		super(false, false, false);
		this.mass = src.mass.clone();
		this.friction = src.friction.clone();
		this.gravityAcceleration = src.gravityAcceleration.clone();
		this.angularFactor = src.angularFactor.clone();
		this.objectMap = new HashMap<CEObject, DynamicObject>();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	// TODO put prev values in map
	
	private Vector3f pScale = new Vector3f(-Float.MAX_VALUE);
	private float pMass = -Float.MAX_VALUE;
	private float pFriction = -Float.MAX_VALUE;
	private float pAngularFactor = -Float.MAX_VALUE;
	private Vector3f pGravityAcceleration = new Vector3f(-Float.MAX_VALUE);
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(packet.isPaused() || restrictedObjects.contains(obj)) return;
		PhysicsSimulation.DEFAULT.registerIn();
		DynamicObject physObj;
		if(!objectMap.containsKey(obj)) {
			physObj = new DynamicObject(getCollisionMesh(), obj.getPosition(), obj.getRotation());
			objectMap.put(obj, physObj);
		}
		else physObj = objectMap.get(obj);
		
		PhysicsSimulation.DEFAULT.registerObject(physObj);
		
		boolean change = !pScale.equals(obj.getScale()) || pMass != mass.get() || pFriction != friction.get()
				|| pAngularFactor != angularFactor.get() || !pGravityAcceleration.equals(gravityAcceleration.get());
		
		if(change) {
			pScale.set(obj.getScale());
			pMass = mass.get();
			pFriction = friction.get();
			pAngularFactor = angularFactor.get();
			pGravityAcceleration = gravityAcceleration.get();
			
			PhysicsSimulation.DEFAULT.setChangeState(physObj, true);
			physObj.getMesh().setScaling(obj.getScale());
			physObj.setMass(mass.get() * ((obj.getScale().x + obj.getScale().y + obj.getScale().z) / 3f));
			physObj.setFriction(friction.get());
			physObj.setAngularFactor(angularFactor.get());
			physObj.setGravityAcceleration(gravityAcceleration.get());
		}
		
		obj.setPosition(physObj.getSimulationPosition());
		if(angularFactor.get() > 0)
			obj.setRotation(physObj.getSimulationRotation());
		
		if(change) {
			PhysicsSimulation.DEFAULT.setChangeState(physObj, false);
			physObj.setGravityAcceleration(gravityAcceleration.get());
		}
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(!packet.isPaused()) PhysicsSimulation.DEFAULT.registerOut();
		mass.update(!packet.isPaused());
		friction.update(!packet.isPaused());
		gravityAcceleration.update(!packet.isPaused());
		angularFactor.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Mass", mass).withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 1);
		ctrl.withProperty("Friction", friction);
		ctrl.withProperty("Gravity Accel", gravityAcceleration);
		ctrl.withProperty("Angular Factor", angularFactor);
		return ctrl;
	}
	
	public void updateTransform(CEObject obj) {
		DynamicObject physObj = objectMap.get(obj);
		if(physObj == null) return;
		obj.setPosition(physObj.getSimulationPosition());
		if(angularFactor.get() > 0)
			obj.setRotation(physObj.getSimulationRotation());
	}
	
	public DynamicObject getBody(CEObject obj) {
		if(!objectMap.containsKey(obj)) objectMap.put(obj, new DynamicObject(getCollisionMesh(), obj.getPosition(), obj.getRotation()));
		return objectMap.get(obj);
	}
	
	public Collection<CEObject> getRestrictedObjects() {
		return restrictedObjects;
	}
	
	public float getMass() {
		return mass.get();
	}
	
	public void setMass(float mass) {
		this.mass.set(mass);
	}
	
	public Property<Float> massProperty() {
		return mass;
	}
	
	public float getFriction() {
		return friction.get();
	}
	
	public void setFriction(float friction) {
		this.friction.set(friction);
	}
	
	public Property<Float> frictionProperty() {
		return friction;
	}
	
	public Vector3f getGravityAcceleration() {
		return gravityAcceleration.get();
	}
	
	public void setGravityAcceleration(Vector3f gravityAcceleration) {
		this.gravityAcceleration.set(gravityAcceleration);
	}
	
	public Property<Vector3f> gravityAccelerationProperty() {
		return gravityAcceleration;
	}
	
	public float getAngularFactor() {
		return angularFactor.get();
	}
	
	public void setAngularFactor(float angularFactor) {
		this.angularFactor.set(angularFactor);
	}
	
	public Property<Float> angularFactorProperty() {
		return angularFactor;
	}
	
	protected abstract CollisionMesh getCollisionMesh();
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.objectMap = new HashMap<CEObject, DynamicObject>();
	}
	
}
