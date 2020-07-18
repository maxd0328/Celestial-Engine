package mod.celestial.render;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.FloatConverter;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.NonZeroBounds;
import celestial.ctrl.PropertyController;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.data.ColorDepth;
import celestial.data.FrameBuffer;
import celestial.data.GLData;
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
import celestial.util.KVEntry;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.misc.CameraModifier;
import mod.celestial.texture.AbstractSamplerModifier;

public final class DepthEffectModifier extends AbstractSamplerModifier {
	
	private static final long serialVersionUID = -2337972673593518865L;
	
	public static final Factory<DepthEffectModifier> FACTORY = () -> new DepthEffectModifier(320, 180,
			new Vector3f(0, 1, 0), 0, 1, false, new Vector3f(), 1, false, false, 1, false, false, 0);
	
	private final FrameBuffer buffer = (FrameBuffer) super.sampler.get();
	private final Property<Vector3f> normal;
	private final Property<Float> diffuseFactor;
	private final Property<Float> diffuseDivisor;
	private final Property<Boolean> blendEffect;
	private final Property<Vector3f> blendColor;
	private final Property<Float> blendDivisor;
	private final Property<Boolean> blendInverse;
	private final Property<Boolean> alphaEffect;
	private final Property<Float> alphaDivisor;
	private final Property<Boolean> alphaInverse;
	
	public DepthEffectModifier(int width, int height, Vector3f normal, float diffuseFactor, float diffuseDivisor,
			boolean blendEffect, Vector3f blendColor, float blendDivisor, boolean blendInverse, boolean alphaEffect,
			float alphaDivisor, boolean alphaInverse, boolean distortEnabled, int distortUnit) {
		super(false, false, "depthEffect", 1, FrameBuffer.create(width, height, false, false, ColorDepth.RGBA8_LDR), 0, distortEnabled, distortUnit);
		this.normal = Properties.createVec3Property(normal);
		this.diffuseFactor = Properties.createFloatProperty(diffuseFactor);
		this.diffuseDivisor = Properties.createFloatProperty(diffuseDivisor);
		this.blendEffect = Properties.createBooleanProperty(blendEffect);
		this.blendColor = Properties.createVec3Property(blendColor);
		this.blendDivisor = Properties.createFloatProperty(blendDivisor);
		this.blendInverse = Properties.createBooleanProperty(blendInverse);
		this.alphaEffect = Properties.createBooleanProperty(alphaEffect);
		this.alphaDivisor = Properties.createFloatProperty(alphaDivisor);
		this.alphaInverse = Properties.createBooleanProperty(alphaInverse);
	}
	
	private DepthEffectModifier(DepthEffectModifier src, int width, int height) {
		super(src, FrameBuffer.create(width, height, false, false, ColorDepth.RGBA8_LDR));
		this.normal = src.normal.clone();
		this.diffuseFactor = src.diffuseFactor.clone();
		this.diffuseDivisor = src.diffuseDivisor.clone();
		this.blendEffect = src.blendEffect.clone();
		this.blendColor = src.blendColor.clone();
		this.blendDivisor = src.blendDivisor.clone();
		this.blendInverse = src.blendInverse.clone();
		this.alphaEffect = src.alphaEffect.clone();
		this.alphaDivisor = src.alphaDivisor.clone();
		this.alphaInverse = src.alphaInverse.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = fUniforms(ShaderModule.attribs(ShaderAttribute.$("vec2 depthEffectPlane"),
				ShaderAttribute.$("vec2 depthEffectDiffuse"), ShaderAttribute.$("vec3 depthEffectBlend"),
				ShaderAttribute.$("vec3 depthEffectBlendColor"), ShaderAttribute.$("vec3 depthEffectAlpha")));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("depthEffect",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("render.glsl"), "depthEffectV"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("render.glsl"), "depthEffectF"), fUniforms, fGlobals),
				getID(), inputs(ShaderModule.attribs()),
				pvAttribs(ShaderModule.attribs(ShaderAttribute.$("vec4 clipSpacePosition")))).withDependencies("map");
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(packet.isFboRender()) throw new RenderAbortException();
		
		KVEntry<FrameBuffer, CEObject> entry = PlanarRefractionModifier.FRAMEBUFFER_MAP.firstEntry().getValue();
		FrameBuffer buf = entry.getKey();
		CEObject bufObj = entry.getValue();
		Vector3f normal = this.normal.get().clone().normalize().negate();
		if(!PlanarRefractionModifier.frameRendered) {
			PlanarRefractionModifier.frameRendered = true;
			float planeConstant = normal.x * bufObj.getPosition().x + normal.y * bufObj.getPosition().y + normal.z * bufObj.getPosition().z;
			Vector4f plane = new Vector4f(normal.x, normal.y, normal.z, -planeConstant + 0.05f);
			
			packet.getRenderer().getClipPlane(0).set(plane);
			packet.getRenderer().render(packet, null, false, buf.toRenderOutput(), new RenderConstraints());
			packet.getRenderer().getClipPlane(0).disable();
			throw new RenderReiterationException();
		}
		
		buf.sampleDepth();
		super.setSampler(buf);
		super.preRender(packet, obj);
		super.setSampler(this.buffer);
		
		packet.getShader().getCommunicator().store2f("depthEffectPlane", new Vector2f(packet.getCamera()
				.getModifier(CameraModifier.class).getNearPlane(), packet.getCamera().getModifier(CameraModifier.class).getFarPlane()));
		packet.getShader().getCommunicator().store2f("depthEffectDiffuse", new Vector2f(diffuseFactor.get(), diffuseDivisor.get()));
		packet.getShader().getCommunicator().store3f("depthEffectBlend", new Vector3f(blendEffect.get() ? 1f : 0f, blendDivisor.get(), blendInverse.get() ? 1f : 0f));
		packet.getShader().getCommunicator().store3f("depthEffectBlendColor", blendColor.get());
		packet.getShader().getCommunicator().store3f("depthEffectAlpha", new Vector3f(alphaEffect.get() ? 1f : 0f, alphaDivisor.get(), alphaInverse.get() ? 1f : 0f));
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		super.update0(packet, obj);
		PlanarRefractionModifier.FRAMEBUFFER_MAP.put(Vector3f.sub(obj.getPosition(), packet.getCamera().getPosition()).length(), new KVEntry<FrameBuffer, CEObject>(buffer, obj));
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		super.update1(packet, obj);
		normal.update(!packet.isPaused());
		diffuseFactor.update(!packet.isPaused());
		diffuseDivisor.update(!packet.isPaused());
		blendEffect.update(!packet.isPaused());
		blendColor.update(!packet.isPaused());
		blendDivisor.update(!packet.isPaused());
		blendInverse.update(!packet.isPaused());
		alphaEffect.update(!packet.isPaused());
		alphaDivisor.update(!packet.isPaused());
		alphaInverse.update(!packet.isPaused());
		
		if(PlanarRefractionModifier.FRAMEBUFFER_MAP.size() > 0) PlanarRefractionModifier.FRAMEBUFFER_MAP.clear();
		PlanarRefractionModifier.frameRendered = false;
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Display Size", new SelectiveProperty<GLDisplayMode>(Properties.createProperty(GLDisplayMode.class, () -> new GLDisplayMode(buffer.getWidth(),
				buffer.getHeight(), false), s -> system.reinstantiateModifier(this, duplicate(s.getWidth(), s.getHeight()))), FrameBuffer.RECOMMENDED_DISPLAY_MODES));
		ctrl.withProperty("Normal", normal).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		
		ctrl.withProperty("Diffuse Factor", diffuseFactor);
		ctrl.withProperty("Diffuse Divisor", diffuseDivisor).withPropertyBounds(new NonZeroBounds<Float>(Float.class, FloatConverter.FLOAT_CONVERTER), 1);
		
		ctrl.withProperty("Blend Effect", blendEffect);
		ctrl.withProperty("Blend Color", blendColor);
		ctrl.withProperty("Blend Divisor", blendDivisor).withPropertyBounds(new NonZeroBounds<Float>(Float.class, FloatConverter.FLOAT_CONVERTER), 1);
		ctrl.withProperty("Inverse Blend", blendInverse);
		
		ctrl.withProperty("Alpha Effect", alphaEffect);
		ctrl.withProperty("Alpha Divisor", alphaDivisor).withPropertyBounds(new NonZeroBounds<Float>(Float.class, FloatConverter.FLOAT_CONVERTER), 1);
		ctrl.withProperty("Inverse Alpha", alphaInverse);
		
		ctrl.withProperty("Distort Enabled", distortEnabled);
		ctrl.withProperty("Distort Unit", distortUnit).withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 0, IntervalType.INCLUSIVE, 3, IntervalType.INCLUSIVE, 0), 1);
		return ctrl;
	}
	
	@Override
	public boolean containsData(GLData data) {
		return data == buffer;
	}
	
	public FrameBuffer getBuffer() {
		return buffer;
	}
	
	public Vector3f getNormal() {
		return normal.get();
	}
	
	public void setNormal(Vector3f normal) {
		this.normal.set(normal);
	}
	
	public Property<Vector3f> normalProperty() {
		return normal;
	}
	
	public boolean isBlendEffect() {
		return blendEffect.get();
	}
	
	public void setBlendEffect(boolean blendEffect) {
		this.blendEffect.set(blendEffect);
	}
	
	public Property<Boolean> blendEffectProperty() {
		return blendEffect;
	}
	
	public boolean isBlendInverse() {
		return blendInverse.get();
	}
	
	public void setBlendInverse(boolean blendInverse) {
		this.blendInverse.set(blendInverse);
	}
	
	public Property<Boolean> blendInverseProperty() {
		return blendInverse;
	}
	
	public boolean isAlphaEffect() {
		return alphaEffect.get();
	}
	
	public void setAlphaEffect(boolean alphaEffect) {
		this.alphaEffect.set(alphaEffect);
	}
	
	public Property<Boolean> alphaEffectProperty() {
		return alphaEffect;
	}
	
	public boolean isAlphaInverse() {
		return alphaInverse.get();
	}
	
	public void setAlphaInverse(boolean alphaInverse) {
		this.alphaInverse.set(alphaInverse);
	}
	
	public Property<Boolean> alphaInverseProperty() {
		return alphaInverse;
	}
	
	public float getDiffuseFactor() {
		return diffuseFactor.get();
	}
	
	public void setDiffuseFactor(float diffuseFactor) {
		this.diffuseFactor.set(diffuseFactor);
	}
	
	public Property<Float> diffuseFactorProperty() {
		return diffuseFactor;
	}
	
	public float getDiffuseDivisor() {
		return diffuseDivisor.get();
	}
	
	public void setDiffuseDivisor(float diffuseDivisor) {
		this.diffuseDivisor.set(diffuseDivisor);
	}
	
	public Property<Float> diffuseDivisorProperty() {
		return diffuseDivisor;
	}
	
	public Vector3f getBlendColor() {
		return blendColor.get();
	}
	
	public void setBlendColor(Vector3f blendColor) {
		this.blendColor.set(blendColor);
	}
	
	public Property<Vector3f> blendColorProperty() {
		return blendColor;
	}
	
	public float getBlendDivisor() {
		return blendDivisor.get();
	}
	
	public void setBlendDivisor(float blendDivisor) {
		this.blendDivisor.set(blendDivisor);
	}
	
	public Property<Float> blendDivisorProperty() {
		return blendDivisor;
	}
	
	public float getAlphaDivisor() {
		return alphaDivisor.get();
	}
	
	public void setAlphaDivisor(float alphaDivisor) {
		this.alphaDivisor.set(alphaDivisor);
	}
	
	public Property<Float> alphaDivisorProperty() {
		return alphaDivisor;
	}
	
	public Modifier duplicate() {
		return new DepthEffectModifier(this, buffer.getWidth(), buffer.getHeight());
	}
	
	private Modifier duplicate(int width, int height) {
		return new DepthEffectModifier(this, width, height);
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("depthEffect", 1);
	}
	
}
