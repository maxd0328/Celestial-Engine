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

public final class MeshDistortionModifier extends Modifier {
	
	private static final long serialVersionUID = -8833834089550562925L;
	
	public static final Factory<MeshDistortionModifier> FACTORY = () -> new MeshDistortionModifier(new Vector3f(), new Vector3f(0, 1, 0), 0f, 1f, false, 0f);
	
	private final Property<Vector3f> distortVector;
	private final Property<Vector3f> axis;
	private final Property<Float> min;
	private final Property<Float> max;
	private final Property<Boolean> sineWave;
	private final Property<Float> sineOffset;
	
	public MeshDistortionModifier(Vector3f distortVector, Vector3f axis, float min, float max, boolean sineWave, float sineOffset) {
		super(false, false, false);
		this.distortVector = Properties.createVec3Property(distortVector);
		this.axis = Properties.createVec3Property(axis);
		this.min = Properties.createFloatProperty(min);
		this.max = Properties.createFloatProperty(max);
		this.sineWave = Properties.createBooleanProperty(sineWave);
		this.sineOffset = Properties.createFloatProperty(sineOffset);
	}
	
	private MeshDistortionModifier(MeshDistortionModifier src) {
		super(false, false, false);
		this.distortVector = src.distortVector.clone();
		this.axis = src.axis.clone();
		this.min = src.min.clone();
		this.max = src.max.clone();
		this.sineWave = src.sineWave.clone();
		this.sineOffset = src.sineOffset.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 meshDistortionVector"),
				ShaderAttribute.$("vec3 meshDistortionAxis"), ShaderAttribute.$("vec2 meshDistortionBounds"),
				ShaderAttribute.$("vec2 meshDistortionSineWave"));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs(ShaderAttribute.$("vec3 vertDistort").withDefaultValue("vec3(0)"));
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("meshDistortion",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("filter.glsl"), "meshDistortionV"), vUniforms, vGlobals),
				new ProgramModule("", fUniforms, fGlobals),
				getID(), ShaderModule.attribs(ShaderAttribute.$("vec3 position")),
				ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		packet.getShader().getCommunicator().store3f("meshDistortionVector", distortVector.get());
		packet.getShader().getCommunicator().store3f("meshDistortionAxis", axis.get());
		packet.getShader().getCommunicator().store2f("meshDistortionBounds", new Vector2f(min.get(), max.get()));
		packet.getShader().getCommunicator().store2f("meshDistortionSineWave", new Vector2f(sineWave.get() ? 1f : 0f, sineOffset.get()));
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		distortVector.update(!packet.isPaused());
		axis.update(!packet.isPaused());
		min.update(!packet.isPaused());
		max.update(!packet.isPaused());
		sineWave.update(!packet.isPaused());
		sineOffset.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Distortion", distortVector);
		ctrl.withProperty("Axis", axis);
		ctrl.withProperty("Distort Min", min);
		ctrl.withProperty("Distort Max", max);
		ctrl.withProperty("Sine Function", sineWave);
		ctrl.withProperty("Sine Offset", sineOffset);
		return ctrl;
	}
	
	public boolean isSineWave() {
		return sineWave.get();
	}
	
	public void setSineWave(boolean sineWave) {
		this.sineWave.set(sineWave);
	}
	
	public Property<Boolean> sineWaveProperty() {
		return sineWave;
	}
	
	public Vector3f getDistortVector() {
		return distortVector.get();
	}
	
	public void setDistortVector(Vector3f distortVector) {
		this.distortVector.set(distortVector);
	}
	
	public Property<Vector3f> distortVectorProperty() {
		return distortVector;
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
	
	public float getSineOffset() {
		return sineOffset.get();
	}
	
	public void setSineOffset(float sineOffset) {
		this.sineOffset.set(sineOffset);
	}
	
	public Property<Float> sineOffsetProperty() {
		return sineOffset;
	}
	
	public Modifier duplicate() {
		return new MeshDistortionModifier(this);
	}
	
}
