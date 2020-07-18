package mod.celestial.light;

import celestial.render.RenderPacket;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;

public interface Light extends Comparable<Light> {
	
	public static final Vector3f CUR_OBJPOS = new Vector3f();
	
	public Vector3f getPosition();
	
	public Matrix4f getViewMatrix(RenderPacket pckt);
	
	public Matrix4f getProjectionMatrix(RenderPacket pckt);
	
	public boolean castsPointShadows();
	
	public int getShadowMapResolution();
	
	@Override
	public default int compareTo(Light o) {
		float a = absDistance(), b = o.absDistance();
		return a < b ? -1 : a > b ? 1 : 0;
	}
	
	default float absDistance() {
		return Vector3f.sub(getPosition(), PointLightModifier.CUR_OBJPOS).length();
	}
	
}
