package celestial.shadow;

import org.lwjgl.opengl.Display;

import celestial.core.CEObject;
import celestial.render.RenderPacket;
import celestial.vecmath.Matrix3f;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.misc.CameraModifier;

public final class ShadowCuboid {
	
	private float minX, maxX;
	private float minY, maxY;
	private float minZ, maxZ;
	
	private final Vector3f pPosition = new Vector3f(-Float.MAX_VALUE);
	private final Vector3f pRotation = new Vector3f(-Float.MAX_VALUE);
	private float pFOV = -Float.MAX_VALUE;
	private float pNearPlane = -Float.MAX_VALUE;
	private float pShadowDepth = -Float.MAX_VALUE;
	private float pShadowExpansion = -Float.MAX_VALUE;
	private float pShadowBackcast = -Float.MAX_VALUE;
	
	/**
	 * Updates if any parameters have changed, with the exception of the light-view matrix. A change of this matrix
	 * will not necessarily trigger a recalculation. This is to prevent redundant updates as the light view matrix
	 * often changes many times within a frame.
	 */
	public void update(RenderPacket pckt, float shadowDepth, float shadowExpansion, float shadowBackcast, Matrix4f lightViewMatrix) {
		CEObject camera = pckt.getCamera();
		CameraModifier cameraData = camera.getModifier(CameraModifier.class);
		
		Vector3f position = camera.getPosition();
		Vector3f rotation = camera.getRotation();
		float FOV = cameraData.getFOV();
		float nearPlane = cameraData.getNearPlane();
		shadowDepth *= cameraData.getFarPlane();
		
		if(!pPosition.equals(position) || !pRotation.equals(rotation) || pFOV != FOV || pNearPlane != nearPlane
				|| pShadowDepth != shadowDepth || pShadowExpansion != shadowExpansion || pShadowBackcast != shadowBackcast) {
			
			pPosition.set(position);
			pRotation.set(rotation);
			pFOV = FOV;
			pNearPlane = nearPlane;
			pShadowDepth = shadowDepth;
			pShadowExpansion = shadowExpansion;
			pShadowBackcast = shadowBackcast;
			
			Vector3f forward = camera.getForwardVector();
			Vector3f up = camera.getUpVector();
			Vector3f centerNear = Vector3f.add(forward.clone().scale(nearPlane).translate(forward.clone().scale(shadowExpansion).negate()), position);
			Vector3f centerFar = Vector3f.add(forward.clone().scale(shadowDepth), position);
			
			float farWidth = (float) (shadowDepth * Math.tan(Math.toRadians(FOV))) * shadowExpansion;
			float nearWidth = (float) (nearPlane * Math.tan(Math.toRadians(FOV))) * shadowExpansion;
			float farHeight = farWidth / getAspectRatio() * shadowExpansion;
			float nearHeight = nearWidth / getAspectRatio() * shadowExpansion;
			
			Vector3f rightVector = Vector3f.cross(forward, up);
			Vector3f downVector = new Vector3f(-up.x, -up.y, -up.z);
			Vector3f leftVector = new Vector3f(-rightVector.x, -rightVector.y, -rightVector.z);
			Vector3f farTop = Vector3f.add(centerFar, new Vector3f(up.x * farHeight, up.y * farHeight, up.z * farHeight));
			Vector3f farBottom = Vector3f.add(centerFar, new Vector3f(downVector.x * farHeight, downVector.y * farHeight, downVector.z * farHeight));
			Vector3f nearTop = Vector3f.add(centerNear, new Vector3f(up.x * nearHeight, up.y * nearHeight, up.z * nearHeight));
			Vector3f nearBottom = Vector3f.add(centerNear, new Vector3f(downVector.x * nearHeight, downVector.y * nearHeight, downVector.z * nearHeight));
			Vector4f[] points = new Vector4f[8];
			points[0] = calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth, lightViewMatrix);
			points[1] = calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth, lightViewMatrix);
			points[2] = calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth, lightViewMatrix);
			points[3] = calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth, lightViewMatrix);
			points[4] = calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth, lightViewMatrix);
			points[5] = calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth, lightViewMatrix);
			points[6] = calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth, lightViewMatrix);
			points[7] = calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth, lightViewMatrix);
			
			boolean first = true;
			for (Vector4f point : points) {
				if (first) {
					minX = point.x;
					maxX = point.x;
					minY = point.y;
					maxY = point.y;
					minZ = point.z;
					maxZ = point.z;
					first = false;
					continue;
				}
				
				if(point.x > maxX) {
					maxX = point.x;
				}
				else if(point.x < minX) {
					minX = point.x;
				}
				
				if(point.y > maxY) {
					maxY = point.y;
				}
				else if(point.y < minY) {
					minY = point.y;
				}
				
				if(point.z > maxZ) {
					maxZ = point.z;
				}
				else if(point.z < minZ) {
					minZ = point.z;
				}
			}
			maxZ += shadowBackcast;
		}
	}
	
	private Vector4f calculateLightSpaceFrustumCorner(Vector3f startPoint, Vector3f direction, float width, Matrix4f lightViewMatrix) {
		Vector3f point = Vector3f.add(startPoint, new Vector3f(direction.x * width, direction.y * width, direction.z * width));
		Vector4f point4f = new Vector4f(point.x, point.y, point.z, 1f);
		lightViewMatrix.transform(point4f, point4f);
		return new Vector4f(new Matrix3f(lightViewMatrix).transform(point));
	}
	
	private float getAspectRatio() {
		return (float) Display.getWidth() / (float) Display.getHeight();
	}
	
	public Vector3f getCenter(Matrix4f lightViewMatrix) {
		float x = (minX + maxX) / 2f;
		float y = (minY + maxY) / 2f;
		float z = (minZ + maxZ) / 2f;
		Vector4f cen = new Vector4f(x, y, z, 1);
		Matrix4f invertedLight = lightViewMatrix.clone().invert();
		return new Vector3f(invertedLight.transform(cen));
	}
	
	public float getWidth() {
		return maxX - minX;
	}
	
	public float getHeight() {
		return maxY - minY;
	}
	
	public float getLength() {
		return maxZ - minZ;
	}
	
	public float getMinX() {
		return minX;
	}

	public float getMaxX() {
		return maxX;
	}

	public float getMinY() {
		return minY;
	}

	public float getMaxY() {
		return maxY;
	}

	public float getMinZ() {
		return minZ;
	}

	public float getMaxZ() {
		return maxZ;
	}
	
}
