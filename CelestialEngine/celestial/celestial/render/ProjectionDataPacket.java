package celestial.render;

import org.lwjgl.opengl.Display;
import celestial.core.CEObject;
import celestial.vecmath.Vector3f;
import mod.celestial.misc.CameraModifier;

public final class ProjectionDataPacket {
	
	private Vector3f cameraPos;
	private Vector3f cameraViewVector;
	private float nearDist;
	private float farDist;
	private float nearHeight;
	private float nearWidth;
	private float farHeight;
	private float farWidth;
	private Vector3f cameraUpVector;
	private Vector3f cameraRightVector;
	
	public ProjectionDataPacket(CEObject camera) {
		cameraPos = camera.getPosition();
		
		cameraViewVector = camera.getForwardVector();
		cameraUpVector = camera.getUpVector();
		cameraRightVector = Vector3f.cross(cameraUpVector, cameraViewVector);
		
		nearDist = camera.getModifier(CameraModifier.class).getNearPlane();
		farDist = camera.getModifier(CameraModifier.class).getFarPlane();
		nearHeight = (float) (2 * Math.tan((camera.getModifier(CameraModifier.class).getFOV()) / 2) * nearDist);
		nearWidth = nearHeight * ((float) Display.getWidth() / (float) Display.getHeight());
		farHeight = (float) (2 * Math.tan((camera.getModifier(CameraModifier.class).getFOV()) /2) * farDist);
		farWidth = farHeight * ((float) Display.getWidth() / (float) Display.getHeight());
	}
	
	private Vector3f ntl, ntr, nbl, nbr, ftl, ftr, fbl, fbr;
	
	public void generateVertices() {
		
		Vector3f nc = Vector3f.add(cameraPos, new Vector3f(cameraViewVector.x*nearDist,cameraViewVector.y*nearDist,cameraViewVector.z*nearDist), null);
		ntl = Vector3f.sub(Vector3f.add(nc, new Vector3f(cameraUpVector.x*nearHeight/2,cameraUpVector.y*nearHeight/2,cameraUpVector.z*nearHeight/2), 
				null),new Vector3f(cameraRightVector.x*nearWidth/2,cameraRightVector.y*nearWidth/2,cameraRightVector.z*nearWidth/2),null);
		ntr = Vector3f.add(Vector3f.add(nc, new Vector3f(cameraUpVector.x*nearHeight/2,cameraUpVector.y*nearHeight/2,cameraUpVector.z*nearHeight/2), 
				null),new Vector3f(cameraRightVector.x*nearWidth/2,cameraRightVector.y*nearWidth/2,cameraRightVector.z*nearWidth/2),null);
		nbl = Vector3f.sub(Vector3f.sub(nc, new Vector3f(cameraUpVector.x*nearHeight/2,cameraUpVector.y*nearHeight/2,cameraUpVector.z*nearHeight/2), 
				null),new Vector3f(cameraRightVector.x*nearWidth/2,cameraRightVector.y*nearWidth/2,cameraRightVector.z*nearWidth/2),null);
		nbr = Vector3f.add(Vector3f.sub(nc, new Vector3f(cameraUpVector.x*nearHeight/2,cameraUpVector.y*nearHeight/2,cameraUpVector.z*nearHeight/2), 
				null),new Vector3f(cameraRightVector.x*nearWidth/2,cameraRightVector.y*nearWidth/2,cameraRightVector.z*nearWidth/2),null);
		
		Vector3f fc = Vector3f.add(cameraPos, new Vector3f(cameraViewVector.x*farDist,cameraViewVector.y*farDist,cameraViewVector.z*farDist), null);
		ftl = Vector3f.sub(Vector3f.add(fc, new Vector3f(cameraUpVector.x*farHeight/2,cameraUpVector.y*farHeight/2,cameraUpVector.z*farHeight/2), 
				null),new Vector3f(cameraRightVector.x*farWidth/2,cameraRightVector.y*farWidth/2,cameraRightVector.z*farWidth/2),null);
		ftr = Vector3f.add(Vector3f.add(fc, new Vector3f(cameraUpVector.x*farHeight/2,cameraUpVector.y*farHeight/2,cameraUpVector.z*farHeight/2), 
				null),new Vector3f(cameraRightVector.x*farWidth/2,cameraRightVector.y*farWidth/2,cameraRightVector.z*farWidth/2),null);
		fbl = Vector3f.sub(Vector3f.sub(fc, new Vector3f(cameraUpVector.x*farHeight/2,cameraUpVector.y*farHeight/2,cameraUpVector.z*farHeight/2), 
				null),new Vector3f(cameraRightVector.x*farWidth/2,cameraRightVector.y*farWidth/2,cameraRightVector.z*farWidth/2),null);
		fbr = Vector3f.add(Vector3f.sub(fc, new Vector3f(cameraUpVector.x*farHeight/2,cameraUpVector.y*farHeight/2,cameraUpVector.z*farHeight/2), 
				null),new Vector3f(cameraRightVector.x*farWidth/2,cameraRightVector.y*farWidth/2,cameraRightVector.z*farWidth/2),null);
		
	}
	
	private FrustumPlane nearPlane, farPlane, leftPlane, rightPlane, topPlane, bottomPlane;
	
	public void generatePlanes() {
		
		nearPlane = new FrustumPlane(nbr,ntr,ntl);
		farPlane = new FrustumPlane(fbr,ftr,ftl);
		leftPlane = new FrustumPlane(fbl,ftl,ntl);
		rightPlane = new FrustumPlane(nbr,ntr,ftr);
		topPlane = new FrustumPlane(ftl,ftr,ntr);
		bottomPlane = new FrustumPlane(fbr,fbl,nbl);
		
	}

	public FrustumPlane getNearPlane() {
		return nearPlane;
	}

	public FrustumPlane getFarPlane() {
		return farPlane;
	}

	public FrustumPlane getLeftPlane() {
		return leftPlane;
	}

	public FrustumPlane getRightPlane() {
		return rightPlane;
	}

	public FrustumPlane getTopPlane() {
		return topPlane;
	}

	public FrustumPlane getBottomPlane() {
		return bottomPlane;
	}
	
}
