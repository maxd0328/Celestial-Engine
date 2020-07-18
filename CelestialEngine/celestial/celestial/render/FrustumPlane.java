package celestial.render;

import celestial.vecmath.Vector3f;

public final class FrustumPlane {
	
	public float[] constructionData = new float[4];
	public float[] constructionDataNegated = new float[4];
	public Vector3f normal;
	public Vector3f origin;
	
	public FrustumPlane(Vector3f p1, Vector3f p2, Vector3f p3){
		
		normal = Vector3f.cross(Vector3f.sub(p3, p2, null), Vector3f.sub(p1, p2, null), normal);
		if(normal.length() > 0) normal.normalize();
		normal.negate();
		origin = p1;
		
		constructionData[0] = normal.x;
		constructionData[1] = normal.y;
		constructionData[2] = normal.z;
		constructionData[3] = -(normal.x*origin.x+normal.y*origin.y+normal.z*origin.z);
		
	}
	
	public FrustumPlane(Vector3f origin, Vector3f normal){
		
		this.normal = normal;
		this.origin = origin;
		
		constructionData[0] = normal.x;
		constructionData[1] = normal.y;
		constructionData[2] = normal.z;
		constructionData[3] = -(normal.x*origin.x+normal.y*origin.y+normal.z*origin.z);
		
	}
	
	public float signedDistanceTo(Vector3f point){
		return (Vector3f.dot(point, normal)) + constructionData[3];
	}
	
}
