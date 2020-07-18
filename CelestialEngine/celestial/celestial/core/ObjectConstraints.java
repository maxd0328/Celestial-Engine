package celestial.core;

public class ObjectConstraints implements java.io.Serializable {
	
	private static final long serialVersionUID = 7481376061977978635L;
	
	private float frustumRadius;
	private float cullDistance;
	private float fboCullDistance;
	private float limitedFboCullDistance;
	private boolean culled, tmpCulled;
	
	public ObjectConstraints() {
		this(0f, 0f);
	}
	
	public ObjectConstraints(float frustumRadius, float cullDistance) {
		this(frustumRadius, cullDistance, cullDistance, cullDistance);
	}
	
	public ObjectConstraints(float frustumRadius, float cullDistance, float fboCullDistance, float limitedFboCullDistance) {
		this.frustumRadius = frustumRadius;
		this.cullDistance = cullDistance;
		this.fboCullDistance = fboCullDistance;
		this.limitedFboCullDistance = limitedFboCullDistance;
		this.culled = false;
		this.tmpCulled = false;
	}
	
	public float getFrustumRadius() {
		return frustumRadius;
	}
	
	public void setFrustumRadius(float frustumRadius) {
		this.frustumRadius = frustumRadius;
	}
	
	public float getCullDistance() {
		return cullDistance;
	}
	
	public void setCullDistance(float cullDistance) {
		this.cullDistance = cullDistance;
	}
	
	public float getFboCullDistance() {
		return fboCullDistance;
	}
	
	public void setFboCullDistance(float fboCullDistance) {
		this.fboCullDistance = fboCullDistance;
	}
	
	public float getLimitedFboCullDistance() {
		return limitedFboCullDistance;
	}
	
	public void setLimitedFboCullDistance(float limitedFboCullDistance) {
		this.limitedFboCullDistance = limitedFboCullDistance;
	}
	
	public boolean isCulled() {
		return culled || tmpCulled;
	}
	
	public void cull() {
		this.culled = true;
	}
	
	public void uncull() {
		this.culled = false;
	}
	
	public void tempCull() {
		this.tmpCulled = true;
	}
	
	public void tempUncull() {
		this.tmpCulled = false;
	}
	
}
