package mod.celestial.filter;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public final class GradientModifier extends Modifier {
	
	private static final long serialVersionUID = 4981340398215762455L;
	
	public static final Factory<GradientModifier> FACTORY = () -> new GradientModifier(new Vector4f(), 0, new Vector4f(), 0, false, new Vector3f(0, 1, 0), 0, 1);
	
	private final Property<Vector4f> color0, color1;
	private final Property<Float> blendFactor0, blendFactor1;
	private final Property<Boolean> useAlpha;
	private final Property<Vector3f> axis;
	private final Property<Float> min, max;
	
	public GradientModifier(Vector4f color0, float blendFactor0, Vector4f color1, float blendFactor1, boolean useAlpha, Vector3f axis, float min, float max) {
		super(false, false, true);
		this.color0 = Properties.createVec4Property(color0);
		this.blendFactor0 = Properties.createFloatProperty(blendFactor0);
		this.color1 = Properties.createVec4Property(color1);
		this.blendFactor1 = Properties.createFloatProperty(blendFactor1);
		this.useAlpha = Properties.createBooleanProperty(useAlpha);
		this.axis = Properties.createVec3Property(axis);
		this.min = Properties.createFloatProperty(min);
		this.max = Properties.createFloatProperty(max);
	}
	
	private GradientModifier(GradientModifier src) {
		super(false, false, true);
		this.color0 = src.color0.clone();
		this.blendFactor0 = src.blendFactor0.clone();
		this.color1 = src.color1.clone();
		this.blendFactor1 = src.blendFactor1.clone();
		this.useAlpha = src.useAlpha.clone();
		this.axis = src.axis.clone();
		this.min = src.min.clone();
		this.max = src.max.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("vec4 gradientColor0"), ShaderAttribute.$("float gradientBlend0"),
				ShaderAttribute.$("vec4 gradientColor1"), ShaderAttribute.$("float gradientBlend1"),
				ShaderAttribute.$("vec3 gradientAxis"), ShaderAttribute.$("vec2 gradientBounds"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("float gradientUseAlpha"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("gradient",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "gradientV"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "gradientF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(ShaderAttribute.$("vec3 position")),
				ShaderModule.attribs(ShaderAttribute.$("vec4 gradientColor"), ShaderAttribute.$("float gradientBlend")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store4f("gradientColor0", color0.get());
		packet.getShader().getCommunicator().store1f("gradientBlend0", blendFactor0.get());
		packet.getShader().getCommunicator().store4f("gradientColor1", color1.get());
		packet.getShader().getCommunicator().store1f("gradientBlend1", blendFactor1.get());
		packet.getShader().getCommunicator().store3f("gradientAxis", axis.get());
		packet.getShader().getCommunicator().store2f("gradientBounds", new Vector2f(min.get(), max.get()));
		packet.getShader().getCommunicator().store1f("gradientUseAlpha", useAlpha.get() ? 1f : 0f);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		color0.update(!packet.isPaused());
		blendFactor0.update(!packet.isPaused());
		color1.update(!packet.isPaused());
		blendFactor1.update(!packet.isPaused());
		useAlpha.update(!packet.isPaused());
		axis.update(!packet.isPaused());
		min.update(!packet.isPaused());
		max.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Color-0", color0);
		ctrl.withProperty("Color-0 Blend", blendFactor0);
		ctrl.withProperty("Color-1", color1);
		ctrl.withProperty("Color-1 Blend", blendFactor1);
		ctrl.withProperty("Use Alpha", useAlpha);
		ctrl.withProperty("Axis", axis);
		ctrl.withProperty("Gradient Min", min);
		ctrl.withProperty("Gradient Max", max);
		return ctrl;
	}
	
	public boolean isUseAlpha() {
		return useAlpha.get();
	}
	
	public void setUseAlpha(boolean useAlpha) {
		this.useAlpha.set(useAlpha);
	}
	
	public Property<Boolean> useAlphaProperty() {
		return useAlpha;
	}
	
	public Vector4f getColor0() {
		return color0.get();
	}
	
	public void setColor0(Vector4f color0) {
		this.color0.set(color0);
	}
	
	public Property<Vector4f> color0Property() {
		return color0;
	}
	
	public Vector4f getColor1() {
		return color1.get();
	}
	
	public void setColor1(Vector4f color1) {
		this.color1.set(color1);
	}
	
	public Property<Vector4f> color1Property() {
		return color1;
	}
	
	public float getBlendFactor0() {
		return blendFactor0.get();
	}
	
	public void setBlendFactor0(float blendFactor0) {
		this.blendFactor0.set(blendFactor0);
	}
	
	public Property<Float> blendFactor0Property() {
		return blendFactor0;
	}
	
	public float getBlendFactor1() {
		return blendFactor1.get();
	}
	
	public void setBlendFactor1(float blendFactor1) {
		this.blendFactor1.set(blendFactor1);
	}
	
	public Property<Float> blendFactor1Property() {
		return blendFactor1;
	}
	
	public Vector3f getAxis() {
		return axis.get();
	}
	
	public void setAxis(Vector3f axis) {
		this.axis.set(axis);
	}
	
	public Property<Vector3f> axisProperty() {
		return axis;
	}
	
	public float getMin() {
		return min.get();
	}
	
	public void setMin(float min) {
		this.min.set(min);
	}
	
	public Property<Float> minProperty() {
		return min;
	}
	
	public float getMax() {
		return max.get();
	}
	
	public void setMax(float max) {
		this.max.set(max);
	}
	
	public Property<Float> maxProperty() {
		return max;
	}
	
	public Modifier duplicate() {
		return new GradientModifier(this);
	}
	
}
