package testScene;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import celestial.beans.driver.SmoothDriver;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.physics.PhysicsSimulation;
import celestial.render.UpdatePacket;
import celestial.vecmath.Vector3f;
import mod.celestial.misc.ScriptableModifier.ObjectScript;
import mod.celestial.physics.AbstractRigidBodyModifier;

public class PlayerScript implements ObjectScript {
	
	private static final float RUN_SPEED = 2.6f;
	private static final float SPRINT_SPEED = 5.0f;
	private static final float CROUCH_SPEED = 1.2f;
	private static final float JUMP_POWER = 10f;
	
	private static final float PLAYER_HEIGHT = 6f;
	private static final float PLAYER_CROUCH_HEIGHT = 3f;
	
	private static final float FLASHLIGHT_INTENSITY = 30f;
	
	private static final float SENSITIVITY = 0.1f;
	
	private Map<CEObject, Vector3f> prevPositions = new HashMap<CEObject, Vector3f>();
	private final Property<Float> height = Properties.createFloatProperty(PLAYER_HEIGHT);
	private final Property<Float> flashlight = Properties.createFloatProperty(0f);
	
	public PlayerScript() {
		height.getDriver().set(new SmoothDriver(10f));
		flashlight.getDriver().set(new SmoothDriver(45f));
	}
	
	@Override
	public void resolve(UpdatePacket pckt, CEObject obj) {
		
		if(!prevPositions.containsKey(obj))
			prevPositions.put(obj, obj.getPosition());
		
		if(PhysicsSimulation.DEFAULT.isResting(obj.getModifier(AbstractRigidBodyModifier.class).getBody(obj))) {
			
			float speed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? SPRINT_SPEED : RUN_SPEED;
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
				speed = CROUCH_SPEED;
			
			Vector3f directionForward = obj.getForwardVector().scale(new Vector3f(1f, 0f, 1f));
			if(directionForward.length() > 0)
				directionForward.normalize();
			Vector3f directionRight = obj.getRightVector().scale(new Vector3f(1f, 0f, 1f));
			if(directionRight.length() > 0)
				directionRight.normalize();
			
			Vector3f velocityForward = Vector3f.mul(directionForward, speed);
			Vector3f velocityRight = Vector3f.mul(directionRight, speed);
			Vector3f totalVelocity = new Vector3f();
			
			Vector3f objVelocity = obj.getPosition().clone().translate(prevPositions.get(obj).clone().negate());
			
			if(Keyboard.isKeyDown(Keyboard.KEY_W)) totalVelocity.translate(velocityForward);
			if(!Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S))
				totalVelocity.translate(velocityForward.clone().negate().scale(Math.max(objVelocity.dot(directionForward), 0)).scale(7f));
			if(Keyboard.isKeyDown(Keyboard.KEY_S)) totalVelocity.translate(velocityForward.clone().negate());
			if(!Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_W))
				totalVelocity.translate(velocityForward.scale(Math.max(objVelocity.dot(directionForward.clone().negate()), 0)).scale(7f));
			if(Keyboard.isKeyDown(Keyboard.KEY_D)) totalVelocity.translate(velocityRight);
			if(!Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_A))
				totalVelocity.translate(velocityRight.clone().negate().scale(Math.max(objVelocity.dot(directionRight), 0)).scale(7f));
			if(Keyboard.isKeyDown(Keyboard.KEY_A)) totalVelocity.translate(velocityRight.clone().negate());
			if(!Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D))
				totalVelocity.translate(velocityRight.scale(Math.max(objVelocity.dot(directionRight.clone().negate()), 0)).scale(7f));
			if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) totalVelocity.translate(0f, JUMP_POWER, 0f);
			
			obj.getModifier(AbstractRigidBodyModifier.class).getBody(obj).applyNetForce(totalVelocity);
			obj.getModifier(AbstractRigidBodyModifier.class).getBody(obj).capHorizontalVelocity(speed * 6);
			
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
			height.setBase(PLAYER_CROUCH_HEIGHT);
		else
			height.setBase(PLAYER_HEIGHT);
		
		float dRY = Mouse.getDX() * SENSITIVITY, dRX = -Mouse.getDY() * SENSITIVITY;
		if(obj.getBaseRotation().x + dRX > 89) dRX = 89 - obj.getBaseRotation().x;
		else if(obj.getBaseRotation().x + dRX < -89) dRX = -89 - obj.getBaseRotation().x;
		
		if(obj.getBaseRotation().z != 0) obj.getBaseRotation().z *= 0.96f;
		obj.increaseRotation(dRX, dRY, 0);
		obj.configurationProperty(30).setBase(height.get());
		obj.configurationProperty(29).setBase(flashlight.get());
		
		while(Keyboard.next()) {
			if(Keyboard.isKeyDown(Keyboard.KEY_F))
				flashlight.setBase(flashlight.getBase() == FLASHLIGHT_INTENSITY ? 0f : FLASHLIGHT_INTENSITY);
		}
		
		prevPositions.put(obj, obj.getPosition());
		height.update();
		flashlight.update();
		
	}
	
}
