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
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.texture.AbstractSamplerModifier;

public final class PlanarRefractionModifier extends AbstractSamplerModifier {
	
	private static final long serialVersionUID = 7528782526629162472L;
	
	public static final Factory<PlanarRefractionModifier> FACTORY = () -> new PlanarRefractionModifier(320, 180, new Vector3f(0, 1, 0), 1f, false, 1f, false, 0);
	
	protected static final TreeMap<Float, KVEntry<FrameBuffer, CEObject>> FRAMEBUFFER_MAP = new TreeMap<Float, KVEntry<FrameBuffer, CEObject>>();
	protected static boolean frameRendered = false;
	
	private final FrameBuffer buffer = (FrameBuffer) super.sampler.get();
	private final Property<Vector3f> normal;
	private final Property<Float> blendFactor;
	private final Property<Boolean> fresnelBlending;
	private final Property<Float> fresnelModifier;
	
	public PlanarRefractionModifier(int width, int height, Vector3f normal, float blendFactor,
			boolean fresnelBlending, float fresnelModifier, boolean distortEnabled, int distortUnit) {
		super(false, true, "planarRefraction", 1, FrameBuffer.create(width, height, false, false, ColorDepth.RGBA8_LDR), 0, distortEnabled, distortUnit);
		this.normal = Properties.createVec3Property(normal);
		this.blendFactor = Properties.createFloatProperty(blendFactor);
		this.fresnelBlending = Properties.createBooleanProperty(fresnelBlending);
		this.fresnelModifier = Properties.createFloatProperty(fresnelModifier);
	}
	
	private PlanarRefractionModifier(PlanarRefractionModifier src, int width, int height) {
		super(src, FrameBuffer.create(width, height, false, false, ColorDepth.RGBA8_LDR));
		this.normal = src.normal.clone();
		this.blendFactor = src.blendFactor.clone();
		this.fresnelBlending = src.fresnelBlending.clone();
		this.fresnelModifier = src.fresnelModifier.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("mat4 transform"), ShaderAttribute.$("mat4 viewMatrix"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = fUniforms(ShaderModule.attribs(ShaderAttribute.$("vec3 planarRefractionBlend"),
				ShaderAttribute.$("vec3 planarRefractionNormal")));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("planarRefraction",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("render.glsl"), "planarV"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("render.glsl"), "planarRefractionF"), fUniforms, fGlobals),
				getID(), inputs(ShaderModule.attribs(ShaderAttribute.$("vec3 position"))),
				pvAttribs(ShaderModule.attribs(ShaderAttribute.$("vec3 planarCameraVec"), ShaderAttribute.$("vec4 clipSpacePosition")))).withDependencies("map");
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(packet.isFboRender()) throw new RenderAbortException();
		
		KVEntry<FrameBuffer, CEObject> entry = FRAMEBUFFER_MAP.firstEntry().getValue();
		FrameBuffer buf = entry.getKey();
		CEObject bufObj = entry.getValue();
		Vector3f normal = this.normal.get().clone().normalize().negate();
		if(!frameRendered) {
			frameRendered = true;
			float planeConstant = normal.x * bufObj.getPosition().x + normal.y * bufObj.getPosition().y + normal.z * bufObj.getPosition().z;
			Vector4f plane = new Vector4f(normal.x, normal.y, normal.z, -planeConstant + 0.05f);
			
			packet.getRenderer().getClipPlane(0).set(plane);
			packet.getRenderer().render(packet, null, false, buf.toRenderOutput(), new RenderConstraints());
			packet.getRenderer().getClipPlane(0).disable();
			throw new RenderReiterationException();
		}
		
		buf.sampleTexture();
		super.setSampler(buf);
		super.preRender(packet, obj);
		super.setSampler(this.buffer);
		
		packet.getShader().getCommunicator().store3f("planarRefractionBlend", new Vector3f(blendFactor.get(), fresnelBlending.get() ? 1f : 0f, fresnelModifier.get()));
		packet.getShader().getCommunicator().store3f("planarRefractionNormal", normal.negate());
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		super.render(packet, obj); 
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		super.postRender(packet, obj);
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		super.update0(packet, obj);
		FRAMEBUFFER_MAP.put(Vector3f.sub(obj.getPosition(), packet.getCamera().getPosition()).length(), new KVEntry<FrameBuffer, CEObject>(buffer, obj));
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		super.update1(packet, obj);
		if(!packet.isPaused()) {
			normal.update();
			blendFactor.update();
			fresnelModifier.update();
		}
		if(FRAMEBUFFER_MAP.size() > 0) FRAMEBUFFER_MAP.clear();
		frameRendered = false;
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Display Size", new SelectiveProperty<GLDisplayMode>(Properties.createProperty(GLDisplayMode.class, () -> new GLDisplayMode(buffer.getWidth(),
				buffer.getHeight(), false), s -> system.reinstantiateModifier(this, duplicate(s.getWidth(), s.getHeight()))), FrameBuffer.RECOMMENDED_DISPLAY_MODES));
		ctrl.withProperty("Normal", normal).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		
		ctrl.withProperty("Blend Factor", blendFactor);
		ctrl.withProperty("Fresnel Blending", fresnelBlending);
		ctrl.withProperty("Fresnel Modifier", fresnelModifier);
		
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

	public float getBlendFactor() {
		return blendFactor.get();
	}
	
	public void setBlendFactor(float blendFactor) {
		this.blendFactor.set(blendFactor);
	}
	
	public Property<Float> blendFactorProperty() {
		return blendFactor;
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
		return new PlanarRefractionModifier(this, buffer.getWidth(), buffer.getHeight());
	}
	
	private Modifier duplicate(int width, int height) {
		return new PlanarRefractionModifier(this, width, height);
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("planarRefraction", 1);
	}
	
}
