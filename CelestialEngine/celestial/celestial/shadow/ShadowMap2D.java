package celestial.shadow;

import celestial.core.CEObject;
import celestial.core.ObjectConstraints;
import celestial.data.ColorDepth;
import celestial.data.FrameBuffer;
import celestial.data.Sampler;
import celestial.render.RenderOutput;
import celestial.render.RenderPacket;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import mod.celestial.light.Light;

public final class ShadowMap2D implements ShadowMap {
	
	private final Light light;
	private final FrameBuffer buffer;
	
	private final Matrix4f viewMatrix;
	private final Matrix4f projectionMatrix;
	
	private boolean rendered = false;
	
	public ShadowMap2D(Light light) {
		this.light = light;
		this.buffer = FrameBuffer.create(light.getShadowMapResolution(), light.getShadowMapResolution(), true, false, ColorDepth.RGBA8_LDR);
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
	}
	
	@Override
	public Light getLight() {
		return light;
	}
	
	@Override
	public Sampler getShadowMap() {
		buffer.sampleDepth();
		return buffer;
	}
	
	@Override
	public RenderOutput toRenderOutput() {
		rendered = true;
		return buffer.toRenderOutput();
	}
	
	@Override
	public boolean containsFrameBuffer(Sampler frameBuffer) {
		return frameBuffer == buffer;
	}
	
	@Override
	public boolean isRendered() {
		return rendered;
	}
	
	@Override
	public void requestReset() {
		this.rendered = false;
	}
	
	@Override
	public boolean needsReinstantiation() {
		return buffer.getWidth() != light.getShadowMapResolution() || buffer.getHeight() != light.getShadowMapResolution();
	}
	
	@Override
	public CEObject createCamera(RenderPacket pckt) {
		this.viewMatrix.set(light.getViewMatrix(pckt));
		this.projectionMatrix.set(light.getProjectionMatrix(pckt));
		CEObject camera = new CEObject("ShadowMap2D_TMP-Camera", new Vector3f(), new Vector3f(), new Vector3f(1f), new ObjectConstraints());
		camera.addModifier(new ShadowMapSystem.CustomCameraModifier(viewMatrix, projectionMatrix));
		return camera;
	}
	
	@Override
	public Matrix4f toShadowSpaceMatrix(RenderPacket pckt) {
		Matrix4f shadowSpaceMatrix = Matrix4f.mul(projectionMatrix, viewMatrix);
		return shadowSpaceMatrix;
	}
	
}
