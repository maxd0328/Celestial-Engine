package mod.celestial.render;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.TreeMap;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.core.ObjectConstraints;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.ctrl.PropertyController;
import celestial.data.ColorDepth;
import celestial.data.FrameBufferCube;
import celestial.glutil.GLDisplayMode;
import celestial.render.RenderAbortException;
import celestial.render.RenderConstraints;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.render.RenderReiterationException;
import celestial.serialization.SerializerImpl;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import mod.celestial.misc.CameraModifier;
import mod.celestial.texture.AbstractSamplerModifier;

public final class CubicEnvironmentModifier extends AbstractSamplerModifier {
	
	private static final long serialVersionUID = 7199220843763845967L;
	
	public static final Factory<CubicEnvironmentModifier> FACTORY = () -> new CubicEnvironmentModifier(128, 0f, 1f, 1f, 0f, false, 1f, false, 0);
	
	private static final TreeMap<Float, EnvironmentMapUnit> FRAMEBUFFER_MAP = new TreeMap<Float, EnvironmentMapUnit>();
	private static boolean frameRendered = false;
	
	private final FrameBufferCube buffer = (FrameBufferCube) super.sampler.get();
	private final EnvironmentMapUnit unit = new EnvironmentMapUnit(buffer);
	
	private final Property<Float> updateFrequency;
	private final Property<Float> blendFactor;
	private final Property<Float> refractiveRatio;
	private final Property<Float> refractiveBlend;
	private final Property<Boolean> fresnelBlending;
	private final Property<Float> fresnelModifier;
	
	public CubicEnvironmentModifier(int size, float updateFrequency, float blendFactor, float refractiveRatio, float refractiveBlend,
			boolean fresnelBlending, float fresnelModifier, boolean distortEnabled, int distortUnit) {
		super(false, true, "cubicEnvironment", 1, FrameBufferCube.create(size, false, true, ColorDepth.RGBA8_LDR), 0, distortEnabled, distortUnit);
		this.updateFrequency = Properties.createFloatProperty(updateFrequency);
		this.blendFactor = Properties.createFloatProperty(blendFactor);
		this.refractiveRatio = Properties.createFloatProperty(refractiveRatio);
		this.refractiveBlend = Properties.createFloatProperty(refractiveBlend);
		this.fresnelBlending = Properties.createBooleanProperty(fresnelBlending);
		this.fresnelModifier = Properties.createFloatProperty(fresnelModifier);
	}
	
	private CubicEnvironmentModifier(CubicEnvironmentModifier src, int size) {
		super(src, FrameBufferCube.create(size, false, true, ColorDepth.RGBA8_LDR));
		this.updateFrequency = src.updateFrequency.clone();
		this.blendFactor = src.blendFactor.clone();
		this.refractiveRatio = src.refractiveRatio.clone();
		this.refractiveBlend = src.refractiveBlend.clone();
		this.fresnelBlending = src.fresnelBlending.clone();
		this.fresnelModifier = src.fresnelModifier.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("mat4 viewMatrix"), ShaderAttribute.$("mat4 transform"),
				ShaderAttribute.$("vec3 cubicEnvironmentRefractBlend"), ShaderAttribute.$("float cubicEnvironmentRefractRatio"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = fUniforms("samplerCube", ShaderModule.attribs(ShaderAttribute.$("float cubicEnvironmentBlend")));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("cubicEnvironment",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("render.glsl"), "cubicEnvironmentV"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("render.glsl"), "cubicEnvironmentF"), fUniforms, fGlobals),
				getID(), inputs(ShaderModule.attribs(ShaderAttribute.$("vec3 position"), ShaderAttribute.$("vec3 normal"))),
				pvAttribs(ShaderModule.attribs(ShaderAttribute.$("vec3 cubicEnvironmentReflect"), ShaderAttribute.$("vec3 cubicEnvironmentRefract"),
						ShaderAttribute.$("float cubicEnvironmentRefractBlend")))).withDependencies("map");
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(packet.isFboRender()) throw new RenderAbortException();
		
		boolean stopRender = false;
		EnvironmentMapUnit bufferUnit = FRAMEBUFFER_MAP.firstEntry().getValue();
		if(!bufferUnit.needsRedraw) stopRender = true;
		
		int limit = 6;
		float updateFrequency = this.updateFrequency.get() * 6;
		if(updateFrequency > 0 && updateFrequency < 1 && ++bufferUnit.framesSinceDraw >= bufferUnit.framesPerFace(updateFrequency)) {
			bufferUnit.framesSinceDraw = 0;
			stopRender = false;
			limit = 1;
		}
		else if(updateFrequency >= 1) {
			stopRender = false;
			limit = bufferUnit.facesPerFrame(updateFrequency);
		}
		
		if(bufferUnit.needsRedraw) {
			stopRender = false;
			limit = 6;
		}
		if(!frameRendered && !stopRender) {
			frameRendered = true;
			
			CEObject camera = new CEObject("CubicEnvironment_TMP-Camera", bufferUnit.obj.getPosition(), new Vector3f(), new Vector3f(1),
					new ObjectConstraints(), new CubeFaceCameraModifier(packet.getCamera().getModifier(CameraModifier.class)));
			bufferUnit.obj.getConstraints().tempCull();
			int target = limit + bufferUnit.currentFace;
			
			for(int i = bufferUnit.currentFace ; i < target ; ++i) {
				bufferUnit.currentFace++;
				while(bufferUnit.currentFace >= 6) bufferUnit.currentFace -= 6;
				
				int face = i;
				while(face >= 6) face -= 6;
				
				bufferUnit.buffer.setCubeFace(face);
				camera.getModifier(CubeFaceCameraModifier.class).setFace(face, camera);
				camera.getModifier(CubeFaceCameraModifier.class).updateViewMatrix(camera);
				packet.getRenderer().render(packet, camera, true, bufferUnit.buffer.toRenderOutput(), new RenderConstraints());
			}
			bufferUnit.obj.getConstraints().tempUncull();
			throw new RenderReiterationException();
		}
		bufferUnit.needsRedraw = false;
		for(EnvironmentMapUnit unit : FRAMEBUFFER_MAP.values()) if(unit != bufferUnit) unit.needsRedraw = true;
		
		bufferUnit.buffer.sampleTexture();
		super.setSampler(bufferUnit.buffer);
		super.preRender(packet, obj);
		super.setSampler(this.buffer);
		
		packet.getShader().getCommunicator().store1f("cubicEnvironmentRefractRatio", refractiveRatio.get());
		packet.getShader().getCommunicator().store3f("cubicEnvironmentRefractBlend", new Vector3f(refractiveBlend.get(), fresnelBlending.get() ? 1f : 0f, fresnelModifier.get()));
		packet.getShader().getCommunicator().store1f("cubicEnvironmentBlend", blendFactor.get());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		super.render(packet, obj);
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		super.postRender(packet, obj);
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		super.update0(packet, obj);
		unit.obj = obj;
		FRAMEBUFFER_MAP.put(Vector3f.sub(obj.getPosition(), packet.getCamera().getPosition()).length(), unit);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		super.update1(packet, obj);
		updateFrequency.update(!packet.isPaused());
		blendFactor.update(!packet.isPaused());
		refractiveRatio.update(!packet.isPaused());
		refractiveBlend.update(!packet.isPaused());
		fresnelBlending.update(!packet.isPaused());
		fresnelModifier.update(!packet.isPaused());
		
		if(FRAMEBUFFER_MAP.size() > 0) FRAMEBUFFER_MAP.clear();
		frameRendered = false;
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Display Size", new SelectiveProperty<GLDisplayMode>(Properties.createProperty(GLDisplayMode.class, () -> new GLDisplayMode
				(buffer.getSize(), buffer.getSize(), false), s -> system.reinstantiateModifier(this, duplicate(s.getWidth()))), FrameBufferCube.RECOMMENDED_DISPLAY_MODES));
		ctrl.withProperty("Update Frequency", updateFrequency)
				.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 0f), 1);
		ctrl.withProperty("Blend Factor", blendFactor);
		ctrl.withProperty("Refractive Ratio", refractiveRatio);
		ctrl.withProperty("Refractive Blend", refractiveBlend);
		ctrl.withProperty("Fresnel Blending", fresnelBlending);
		ctrl.withProperty("Fresnel Modifier", fresnelModifier);
		
		ctrl.withProperty("Distort Enabled", distortEnabled);
		ctrl.withProperty("Distort Unit", distortUnit).withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 0, IntervalType.INCLUSIVE, 3, IntervalType.INCLUSIVE, 0), 1);
		return ctrl;
	}
	
	public FrameBufferCube getBuffer() {
		return buffer;
	}
	
	public float getUpdateFrequency() {
		return updateFrequency.get();
	}
	
	public void setUpdateFrequency(float updateFrequency) {
		this.updateFrequency.set(updateFrequency);
	}
	
	public Property<Float> updateFrequencyProperty() {
		return updateFrequency;
	}
	
	public float getBlendFactor() {
		return blendFactor.get();
	}
	
	public void setBlendFactor(float blendFactor) {
		this.blendFactor.set(blendFactor);
	}
	
	public Property<Float> blendFactorProperty() {
		return blendFactor;
	}
	
	public float getRefractiveRatio() {
		return refractiveRatio.get();
	}
	
	public void setRefractiveRatio(float refractiveRatio) {
		this.refractiveRatio.set(refractiveRatio);
	}
	
	public Property<Float> refractiveRatioProperty() {
		return refractiveRatio;
	}
	
	public float getRefractiveBlend() {
		return refractiveBlend.get();
	}
	
	public void setRefractiveBlend(float refractiveBlend) {
		this.refractiveBlend.set(refractiveBlend);
	}
	
	public Property<Float> refractiveBlendProperty() {
		return refractiveBlend;
	}
	
	public boolean isFresnelBlending() {
		return fresnelBlending.get();
	}
	
	public void setFresnelBlending(boolean fresnelBlending) {
		this.fresnelBlending.set(fresnelBlending);
	}
	
	public Property<Boolean> fresnelBlendingProperty() {
		return fresnelBlending;
	}
	
	public float getFresnelModifier() {
		return fresnelModifier.get();
	}
	
	public void setFresnelModifier(float fresnelModifier) {
		this.fresnelModifier.set(fresnelModifier);
	}
	
	public Property<Float> fresnelModifierProperty() {
		return fresnelModifier;
	}

	public Modifier duplicate() {
		return new CubicEnvironmentModifier(this, buffer.getSize());
	}
	
	private Modifier duplicate(int size) {
		return new CubicEnvironmentModifier(this, size);
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("cubicEnvironment", 1);
	}
	
	private static final class CubeFaceCameraModifier extends CameraModifier {

		private static final long serialVersionUID = 3307607295396454118L;
		
		public CubeFaceCameraModifier(CameraModifier src) {
			super(90.0f, src.getOrthoWidth(), src.getOrthoHeight(), src.getNearPlane(), src.getFarPlane(), 1f, 1f);
		}
		
		public void setFace(int index, CEObject cameraObj) {
			switch(index) {
				case 0: cameraObj.setRotation(new Vector3f(   0,  90, 0));
						break;
				case 1: cameraObj.setRotation(new Vector3f(   0, -90, 0));
						break;
				case 2: cameraObj.setRotation(new Vector3f( -90, 180, 0));
						break;
				case 3: cameraObj.setRotation(new Vector3f(  90, 180, 0));
						break;
				case 4: cameraObj.setRotation(new Vector3f(   0, 180, 0));
						break;
				case 5: cameraObj.setRotation(new Vector3f(   0,   0, 0));
						break;
			}
		}
		
		@Override
		public void updateViewMatrix(CEObject obj) {
			super.getViewMatrix().setIdentity();
			super.getViewMatrix().rotate((float) Math.toRadians(180.0f), new Vector3f(0, 0, 1));
			super.getViewMatrix().rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
			super.getViewMatrix().rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
			Vector3f negativeCameraPos = new Vector3f(obj.getPosition()).negate();
			super.getViewMatrix().translate(negativeCameraPos);
		}
		
		@Override
		protected Matrix4f createProjectionMatrix() {
			final float aspectRatio = 1f; // cube face
			float y_scale = (float) ((1f / Math.tan(Math.toRadians(super.getFOV() / 2f))));
			float x_scale = y_scale / aspectRatio;
			float frustum_length = super.getFarPlane() - super.getNearPlane();

			Matrix4f projectionMat = new Matrix4f();
			projectionMat.m00 = x_scale;
			projectionMat.m11 = y_scale;
			projectionMat.m22 = -((super.getFarPlane() + super.getNearPlane()) / frustum_length);
			projectionMat.m23 = -1;
			projectionMat.m32 = -((2 * super.getNearPlane() * super.getFarPlane()) / frustum_length);
			projectionMat.m33 = 0;
			return projectionMat;
		}
		
	}
	
	private static final class EnvironmentMapUnit implements java.io.Serializable {
		
		private static final long serialVersionUID = -5562405394935150060L;
		
		private final FrameBufferCube buffer;
		private CEObject obj;
		private boolean needsRedraw;
		private int framesSinceDraw;
		private int currentFace;
		
		private EnvironmentMapUnit(FrameBufferCube buffer) {
			this.buffer = buffer;
			this.needsRedraw = true;
			this.framesSinceDraw = Integer.MAX_VALUE - 1;
			this.currentFace = 0;
		}
		
		private int facesPerFrame(float updateFrequency) {
			if(updateFrequency < 1) return 1;
			return (int) Math.floor(updateFrequency);
		}
		
		private int framesPerFace(float updateFrequency) {
			if(updateFrequency == 0) return 0;
			if(updateFrequency >= 1) return 1;
			return (int) Math.floor(1f / updateFrequency);
		}
		
	}
	
}
