package celestial.shadow;

import celestial.core.CEObject;
import celestial.data.Sampler;
import celestial.render.RenderOutput;
import celestial.render.RenderPacket;
import celestial.vecmath.Matrix4f;
import mod.celestial.light.Light;

public interface ShadowMap {
	
	public Light getLight();
	
	public Sampler getShadowMap();
	
	public RenderOutput toRenderOutput();
	
	public boolean containsFrameBuffer(Sampler frameBuffer);
	
	public boolean isRendered();
	
	public void requestReset();
	
	public boolean needsReinstantiation();
	
	public CEObject createCamera(RenderPacket pckt);
	
	public Matrix4f toShadowSpaceMatrix(RenderPacket pckt);
	
}
