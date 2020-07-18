package nonEuclidianDemo;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import celestial.core.CEObject;
import celestial.physics.PhysicsSimulation;
import celestial.render.UpdatePacket;
import celestial.vecmath.Vector3f;
import mod.celestial.misc.ScriptableModifier.ObjectScript;
import mod.celestial.physics.AbstractRigidBodyModifier;

public class PlayerScript implements ObjectScript {
	
	private static final float RUN_SPEED = 1.8f;
	private static final float SPRINT_SPEED = 3.0f;
	private static final float JUMP_POWER = 7f;
	
	private static final float SENSITIVITY = 0.1f;
	
	private Map<CEObject, Vector3f> prevPositions = new HashMap<CEObject, Vector3f>();
	
	@Override
	public void resolve(UpdatePacket pckt, CEObject obj) {
		
		if(!prevPositions.containsKey(obj))
			prevPositions.put(obj, obj.getPosition());
		
		if(PhysicsSimulation.DEFAULT.isResting(obj.getModifier(AbstractRigidBodyModifier.class).getBody(obj))) {
			
			float speed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? SPRINT_SPEED : RUN_SPEED;
			
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
		
		float dRY = Mouse.getDX() * SENSITIVITY, dRX = -Mouse.getDY() * SENSITIVITY;
		if(obj.getBaseRotation().x + dRX > 89) dRX = 89 - obj.getBaseRotation().x;
		else if(obj.getBaseRotation().x + dRX < -89) dRX = -89 - obj.getBaseRotation().x;
		
		if(obj.getBaseRotation().z != 0) obj.getBaseRotation().z *= 0.96f;
		obj.increaseRotation(dRX, dRY, 0);
		
		prevPositions.put(obj, obj.getPosition());
		
	}
	
}
