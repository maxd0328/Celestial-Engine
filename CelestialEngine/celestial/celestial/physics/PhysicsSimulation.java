package celestial.physics;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import celestial.core.EngineRuntime;
import celestial.vecmath.Vector3f;

public final class PhysicsSimulation extends RegisteredProfile {
	
	public static final PhysicsSimulation DEFAULT = new PhysicsSimulation(1f / 60f, 10);
	
	private final SimulationInterface simulationInterface;
	private final ArrayList<StaticObject> staticObjects;
	private final ArrayList<DynamicObject> dynamicObjects;
	private float dT;
	private int maxSubstep;
	
	public PhysicsSimulation(float dT, int maxSubstep) {
		this.simulationInterface = new SimulationInterface();
		this.staticObjects = new ArrayList<StaticObject>();
		this.dynamicObjects = new ArrayList<DynamicObject>();
		this.dT = dT;
		this.maxSubstep = maxSubstep;
	}
	
	public SimulationInterface getSimulationInterface() {
		return simulationInterface;
	}
	
	public float getDT() {
		return dT;
	}
	
	public void setDT(float dT) {
		this.dT = dT;
	}
	
	public int getMaxSubstep() {
		return maxSubstep;
	}
	
	public void setMaxSubstep(int maxSubstep) {
		this.maxSubstep = maxSubstep;
	}
	
	public void stepSimulation(float dT) {
		/* Remove duplicates */
		Set<CollisionObject> set = new LinkedHashSet<>(simulationInterface.getSimulation().getCollisionObjectArray());
		simulationInterface.getSimulation().getCollisionObjectArray().clear();
		simulationInterface.getSimulation().getCollisionObjectArray().addAll(set);
		
		simulationInterface.getSimulation().stepSimulation(dT * this.dT * EngineRuntime.frameTimeRelative(), maxSubstep);
	}
	
	public void registerIn() {
		if(super.isActive()) return;
		super.register();
	}
	
	public void registerOut() {
		if(!super.isActive()) return;
		super.await();
		
		for(StaticObject obj : new ArrayList<StaticObject>(staticObjects)) {
			if(!obj.isActive()) {
				simulationInterface.getSimulation().removeCollisionObject(obj.getCollisionObject());
				staticObjects.remove(obj);
			}
			else obj.await();
		}
		for(DynamicObject obj : new ArrayList<DynamicObject>(dynamicObjects)) {
			obj.update();
			if(!obj.isActive()) {
				simulationInterface.getSimulation().removeRigidBody(obj.getBody());
				dynamicObjects.remove(obj);
			}
			else obj.await();
		}
		stepSimulation(1f);
	}
	
	public void registerObject(StaticObject obj) {
		if(!staticObjects.contains(obj)) {
			staticObjects.add(obj);
			simulationInterface.getSimulation().addCollisionObject(obj.getCollisionObject());
		}
		obj.register();
	}
	
	public void setChangeState(StaticObject obj, boolean state) {
		try {
			if(state) simulationInterface.getSimulation().removeCollisionObject(obj.getCollisionObject());
			else simulationInterface.getSimulation().addCollisionObject(obj.getCollisionObject());
		}
		catch(NullPointerException ex) {
			/* Bug in bullet physics (maybe?)
			 * Error when removing and re-adding collision object with modified local scaling */
		}
	}
	
	public void registerObject(DynamicObject obj) {
		if(!dynamicObjects.contains(obj)) {
			dynamicObjects.add(obj);
			simulationInterface.getSimulation().addRigidBody(obj.getBody());
		}
		obj.register();
	}
	
	public void setChangeState(DynamicObject obj, boolean state) {
		if(state) simulationInterface.getSimulation().removeRigidBody(obj.getBody());
		else simulationInterface.getSimulation().addRigidBody(obj.getBody());
	}
	
	public void setGravityAcceleration(Vector3f gravity) {
		simulationInterface.getSimulation().setGravity(PhysicsUtil.toJavax(gravity));
	}
	
	public boolean isResting(DynamicObject body) {
		int numManifolds = simulationInterface.getCollisionDispatcher().getNumManifolds();
		
		for(int i = 0 ; i < numManifolds ; ++i) {
			PersistentManifold contactManifold = simulationInterface.getSimulation().getDispatcher().getManifoldByIndexInternal(i);
			CollisionObject obj0 = (CollisionObject) contactManifold.getBody0();
			CollisionObject obj1 = (CollisionObject) contactManifold.getBody1();
			
			if(obj0 == body.getBody() || obj1 == body.getBody()) {
				int numContacts = contactManifold.getNumContacts();
				for(int j = 0 ; j < numContacts ; ++j) {
					ManifoldPoint point = contactManifold.getContactPoint(j);
					
					if(point.getDistance() < 0f) {
						javax.vecmath.Vector3f normal = point.normalWorldOnB;
						
						if(normal.y > 0.4f) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
}
