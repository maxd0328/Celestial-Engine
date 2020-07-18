package celestial.shadow;

import celestial.core.CEObject;
import celestial.core.ObjectConstraints;
import celestial.data.ColorDepth;
import celestial.data.FrameBufferCube;
import celestial.data.Sampler;
import celestial.render.RenderOutput;
import celestial.render.RenderPacket;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import mod.celestial.light.Light;
import mod.celestial.misc.CameraModifier;

public final class ShadowMapCube implements ShadowMap {
	
	private final Light light;
	private final FrameBufferCube buffer;
	private final int cubeFace;
	
	private boolean rendered = false;
	
	private ShadowMapCube(Light light, FrameBufferCube buffer, int cubeFace) {
		this.light = light;
		this.buffer = buffer;
		this.cubeFace = cubeFace;
	}
	
	@Override
	public Light getLight() {
		return light;
	}
	
	@Override
	public Sampler getShadowMap() {
		buffer.sampleTexture();
		return buffer;
	}
	
	@Override
	public RenderOutput toRenderOutput() {
		rendered = true;
		buffer.setCubeFace(cubeFace);
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
		return buffer.getSize() != light.getShadowMapResolution();
	}
	
	@Override
	public CEObject createCamera(RenderPacket pckt) {
		CEObject camera = new CEObject("ShadowMap2D_TMP-Camera", new Vector3f(light.getPosition()), new Vector3f(), new Vector3f(1f), new ObjectConstraints());
		camera.addModifier(new ShadowMapSystem.CubeFaceCameraModifier(pckt.getCamera().getModifier(CameraModifier.class)));
		camera.getModifier(ShadowMapSystem.CubeFaceCameraModifier.class).setFace(cubeFace, camera);
		return camera;
	}
	
	@Override
	public Matrix4f toShadowSpaceMatrix(RenderPacket pckt) {
		return new Matrix4f();
	}
	
	public static ShadowMapCube[] createShadowCubeMap(Light light) {
		FrameBufferCube buffer = FrameBufferCube.create(light.getShadowMapResolution(), false, true, ColorDepth.RGBA8_LDR);
		ShadowMapCube[] faces = new ShadowMapCube[6];
		for(int i = 0 ; i < 6 ; ++i)
			faces[i] = new ShadowMapCube(light, buffer, i);
		return faces;
	}
	
}
