package mod.celestial.physics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.physics.CollisionMesh;
import celestial.physics.PhysicsSimulation;
import celestial.physics.StaticObject;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.serialization.SerializerImpl;
import celestial.shader.ShaderModule;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;

public abstract class AbstractCollisionObjectModifier extends Modifier {
	
	private static final long serialVersionUID = -5250353394428433482L;
	
	protected transient HashMap<CEObject, StaticObject> objectMap;
	
	protected final Property<Float> friction;
	
	public AbstractCollisionObjectModifier(float friction) {
		super(false, false, false);
		this.friction = Properties.createFloatProperty(friction);
		this.objectMap = new HashMap<CEObject, StaticObject>();
	}
	
	protected AbstractCollisionObjectModifier(AbstractCollisionObjectModifier src) {
		super(false, false, false);
		this.friction = src.friction.clone();
		this.objectMap = new HashMap<CEObject, StaticObject>();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	private transient HashMap<CEObject, Vector3f> pScales = new HashMap<>();
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(packet.isPaused()) return;
		PhysicsSimulation.DEFAULT.registerIn();
		StaticObject physObj;
		if(!objectMap.containsKey(obj)) {
			physObj = new StaticObject(getCollisionMesh(), obj.getPosition(), obj.getRotation());
			objectMap.put(obj, physObj);
		}
		else physObj = objectMap.get(obj);
		
		PhysicsSimulation.DEFAULT.registerObject(physObj);
		
		if(pScales.get(obj) == null || !pScales.get(obj).equals(obj.getScale())) {
			pScales.put(obj, obj.getScale());
			PhysicsSimulation.DEFAULT.setChangeState(physObj, true);
			physObj.getCollisionMesh().setScaling(obj.getScale());
			PhysicsSimulation.DEFAULT.setChangeState(physObj, false);
		}
		
		physObj.setFriction(friction.get());
		physObj.setPosition(obj.getPosition());
		physObj.setRotation(obj.getRotation());
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(!packet.isPaused()) PhysicsSimulation.DEFAULT.registerOut();
		friction.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Friction", friction);
		return ctrl;
	}
	
	public StaticObject getBody(CEObject obj) {
		if(!objectMap.containsKey(obj)) objectMap.put(obj, new StaticObject(getCollisionMesh(), obj.getPosition(), obj.getRotation()));
		return objectMap.get(obj);
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
	
	protected abstract CollisionMesh getCollisionMesh();
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.objectMap = new HashMap<CEObject, StaticObject>();
	}
	
}
