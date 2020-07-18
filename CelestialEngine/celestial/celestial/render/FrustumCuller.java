package celestial.render;

import celestial.core.CEObject;
import celestial.vecmath.Vector3f;

public final class FrustumCuller {
	
	private ProjectionDataPacket frustum = null;
	
	protected FrustumCuller() {
	}
	
	public void update(CEObject camera) {
		frustum = new ProjectionDataPacket(camera);
		frustum.generateVertices();
		frustum.generatePlanes();
	}
	
	public boolean isInFrustum(CEObject object) {
		return isInFrustum(object, 10);
	}
	
	public boolean isInFrustum(CEObject object, float bias) {
		Vector3f pos = object.getPosition();
		float radius = object.getConstraints().getFrustumRadius() * object.getMaxScale();
		
		float signedDistTop = frustum.getTopPlane().signedDistanceTo(pos);
		if(signedDistTop < -bias) {
			float signedDistSphere = frustum.getTopPlane().signedDistanceTo(Vector3f.add(pos, mul(frustum.getTopPlane().normal, radius + 5), null));
			if(signedDistSphere < -bias) {
				return false;
			}
		}
		
		float signedDistBottom = frustum.getBottomPlane().signedDistanceTo(pos);
		if(signedDistBottom < -bias) {
			float signedDistSphere = frustum.getBottomPlane().signedDistanceTo(Vector3f.add(pos, mul(frustum.getBottomPlane().normal, radius + 5), null));
			if(signedDistSphere < -bias) {
				return false;
			}
		}
		
		float signedDistLeft = frustum.getLeftPlane().signedDistanceTo(pos);
		if(signedDistLeft < -bias) {
			float signedDistSphere = frustum.getLeftPlane().signedDistanceTo(Vector3f.add(pos, mul(frustum.getLeftPlane().normal, radius + 5), null));
			if(signedDistSphere < -bias) {
				return false;
			}
		}
		
		float signedDistRight = frustum.getRightPlane().signedDistanceTo(pos);
		if(signedDistRight < -bias) {
			float signedDistSphere = frustum.getRightPlane().signedDistanceTo(Vector3f.add(pos, mul(frustum.getRightPlane().normal, radius + 5), null));
			if(signedDistSphere < -bias) {
				return false;
			}
		}
		
		float signedDistNear = -frustum.getNearPlane().signedDistanceTo(pos);
		if(signedDistNear < -bias) {
			float signedDistSphere = frustum.getNearPlane().signedDistanceTo(Vector3f.add(pos, mul(frustum.getNearPlane().normal, radius + 5), null));
			if(signedDistSphere < -bias) {
				return false;
			}
		}
		
		float signedDistFar = frustum.getFarPlane().signedDistanceTo(pos);
		if(signedDistFar < -bias) {
			float signedDistSphere = frustum.getFarPlane().signedDistanceTo(Vector3f.add(pos, mul(frustum.getFarPlane().normal, radius + 5), null));
			if(signedDistSphere < -bias) {
				return false;
			}
		}
		return true;
	}
	
	private static Vector3f mul(Vector3f left, float right) {
		return new Vector3f(left.x*right,left.y*right,left.z*right);
	}
	
}
