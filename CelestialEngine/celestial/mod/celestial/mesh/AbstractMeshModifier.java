package mod.celestial.mesh;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.beans.property.SelectiveProperty.PropertySelection;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.ctrl.PropertyController;
import celestial.data.VertexArray;
import celestial.glutil.RotationPattern;
import celestial.data.GLData;
import celestial.render.GraphicUtil;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.misc.CameraModifier;

public abstract class AbstractMeshModifier extends Modifier {
	
	private static final long serialVersionUID = 7602019858112886148L;
	
	public static final int BLEND_MODE_NONE		= 0x0;
	public static final int BLEND_MODE_CEL		= 0x1;
	public static final int BLEND_MODE_ALPHA	= 0x2;
	public static final int BLEND_MODE_ADDITIVE = 0x3;
	
	private final boolean hasLOD;
	private final Property<Boolean> cullBackFace;
	private final Property<Boolean> depthTest;
	private final Property<Vector3f> offset;
	private final Property<Integer> blendMode;
	private final Property<RotationPattern> rotationPattern;
	
	public AbstractMeshModifier(boolean hasLOD, boolean cullBackFace, boolean depthTest, Vector3f offset) {
		super(true, false, false);
		this.hasLOD = hasLOD;
		this.cullBackFace = Properties.createBooleanProperty(cullBackFace);
		this.depthTest = Properties.createBooleanProperty(depthTest);
		this.offset = Properties.createVec3Property(offset);
		this.blendMode = Properties.createIntegerProperty(BLEND_MODE_NONE);
		this.rotationPattern = Properties.createProperty(RotationPattern.class, RotationPattern.ROTATION_PATTERN_XYZ);
	}
	
	protected AbstractMeshModifier(AbstractMeshModifier src) {
		super(true, false, false);
		this.hasLOD = src.hasLOD;
		this.cullBackFace = src.cullBackFace.clone();
		this.depthTest = src.depthTest.clone();
		this.offset = src.offset.clone();
		this.blendMode = src.blendMode.clone();
		this.rotationPattern = src.rotationPattern.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("mat4 transform"),
				ShaderAttribute.$("mat4 viewMatrix"), ShaderAttribute.$("mat4 projMatrix"), ShaderAttribute.$("vec4 clipPlanes", 8));
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs(ShaderAttribute.$("vec3 vertDistort").withDefaultValue("vec3(0)"));
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("float discardEnabled"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("mesh",
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("mesh.glsl"), "meshV"),
						Shader.getProgramSegment(getClass().getResource("mesh.glsl"), "meshV_glb"), vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("mesh.glsl"), "meshF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(ShaderAttribute.$("vec3 position")),
				ShaderModule.attribs(ShaderAttribute.$("vec4 clipSpacePosition")));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(getVAO() == null) createVAO(packet.getShader());
		packet.getShader().getCommunicator().store4x4f("viewMatrix", packet.getCamera().getModifier(CameraModifier.class).getViewMatrix().clone());
		packet.getShader().getCommunicator().store4x4f("projMatrix", packet.getCamera().getModifier(CameraModifier.class).getProjectionMatrix().clone());
		packet.getShader().getCommunicator().store1f("discardEnabled", blendMode.get() == BLEND_MODE_CEL
				? 0.75f : blendMode.get() == BLEND_MODE_ALPHA || blendMode.get() == BLEND_MODE_ADDITIVE ? 1 : 0);
		if(!hasLOD) getVAO().bind(getVAO().getAttribs());
		if(depthTest.get()) GraphicUtil.enable(GraphicUtil.GL_DEPTH_TESTING);
		else GraphicUtil.disable(GraphicUtil.GL_DEPTH_TESTING);
		if(blendMode.get() == BLEND_MODE_ADDITIVE) GraphicUtil.enable(GraphicUtil.GL_ADDITIVE_BLENDING);
		else if(blendMode.get() == BLEND_MODE_ALPHA) GraphicUtil.enable(GraphicUtil.GL_ALPHA_BLENDING);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		updateVAO(packet, obj);
		if(getVAO() == null || !getVAO().isAllocated()) return;
		if(hasLOD) getVAO().bind(getVAO().getAttribs());
		boolean frontface = packet.getCamera().getModifier(CameraModifier.class).isReflected();
		if(obj.getScale().x < 0) frontface = !frontface;
		if(obj.getScale().y < 0) frontface = !frontface;
		if(obj.getScale().z < 0) frontface = !frontface;
		if(cullBackFace.get()) GraphicUtil.enable(frontface ? GraphicUtil.GL_CULL_FRONTFACE : GraphicUtil.GL_CULL_BACKFACE);
		packet.getShader().getCommunicator().store4x4f("transform", getTransformation(obj));
		packet.getRenderer().loadClipPlanes();
		for(int i = 0 ; i < packet.getRenderer().getClipPlaneCount() ; ++i)
			packet.getShader().getCommunicator().storearr4f("clipPlanes", i, packet.getRenderer().getClipPlane(i).getEquationVector());
		EngineRuntime.draw(EngineRuntime.DATATYPE_ARRAY_ELEMENTS, EngineRuntime.DRAWTYPE_TRIANGLES, 0, getVAO().getIndexCount());
		if(hasLOD) getVAO().unbind(getVAO().getAttribs());
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		GraphicUtil.disable(GraphicUtil.GL_BLENDING);
		GraphicUtil.disable(GraphicUtil.GL_CULLING);
		GraphicUtil.disable(GraphicUtil.GL_DEPTH_TESTING);
		if(!hasLOD) getVAO().unbind(getVAO().getAttribs());
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		cullBackFace.update(!packet.isPaused());
		depthTest.update(!packet.isPaused());
		offset.update(!packet.isPaused());
		blendMode.update(!packet.isPaused());
		rotationPattern.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Cull Backface", cullBackFace);
		ctrl.withProperty("Depth Testing", depthTest);
		ctrl.withProperty("Offset", offset);
		
		ctrl.withProperty("Blend Mode", new SelectiveProperty<Integer>(blendMode, new PropertySelection<>("None", BLEND_MODE_NONE),
				new PropertySelection<>("Cel", BLEND_MODE_CEL), new PropertySelection<>("Alpha", BLEND_MODE_ALPHA), new PropertySelection<>("Additive", BLEND_MODE_ADDITIVE)));
		ctrl.withProperty("Rotation Pattern", new SelectiveProperty<RotationPattern>(rotationPattern, RotationPattern.toSelectionList()));
		return ctrl;
	}
	
	@Override
	public boolean containsData(GLData data) {
		return data == getVAO();
	}
	
	protected abstract void createVAO(Shader shader);
	
	protected abstract VertexArray getVAO();
	
	protected void updateVAO(RenderPacket packet, CEObject obj) {}
	
	public boolean isCullBackFace() {
		return cullBackFace.get();
	}
	
	public void setCullBackFace(boolean cullBackFace) {
		this.cullBackFace.set(cullBackFace);
	}
	
	public Property<Boolean> cullBackFaceProperty() {
		return cullBackFace;
	}
	
	public boolean isDepthTest() {
		return depthTest.get();
	}
	
	public void setDepthTest(boolean depthTest) {
		this.depthTest.set(depthTest);
	}
	
	public Property<Boolean> depthTestProperty() {
		return depthTest;
	}
	
	public int getBlendMode() {
		return blendMode.get();
	}
	
	public void setBlendMode(int blendMode) {
		this.blendMode.set(blendMode);
	}
	
	public Property<Integer> blendModeProperty() {
		return blendMode;
	}
	
	public RotationPattern getRotationPattern() {
		return rotationPattern.get();
	}
	
	public void setRotationPattern(RotationPattern rotationPattern) {
		this.rotationPattern.set(rotationPattern);
	}
	
	public Property<RotationPattern> rotationPatternProperty() {
		return rotationPattern;
	}
	
	public Vector3f getOffset() {
		return offset.get();
	}
	
	public void setOffset(Vector3f offset) {
		this.offset.set(offset);
	}
	
	public Property<Vector3f> offsetProperty() {
		return offset;
	}
	
	private Matrix4f getTransformation(CEObject obj) {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(new Vector3f(obj.getTransformation(rotationPattern.get()).transform(new Vector4f(offset.get(), 1))));
		switch(rotationPattern.get()) {
		case ROTATION_PATTERN_ZYX:
			matrix.rotate((float) Math.toRadians(obj.getRotation().z), new Vector3f(0, 0, 1));
			matrix.rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
			matrix.rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
			break;
		case ROTATION_PATTERN_YZX:
			matrix.rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
			matrix.rotate((float) Math.toRadians(obj.getRotation().z), new Vector3f(0, 0, 1));
			matrix.rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
			break;
		case ROTATION_PATTERN_ZXY:
			matrix.rotate((float) Math.toRadians(obj.getRotation().z), new Vector3f(0, 0, 1));
			matrix.rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
			matrix.rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
			break;
		default:
			matrix.rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
			matrix.rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
			matrix.rotate((float) Math.toRadians(obj.getRotation().z), new Vector3f(0, 0, 1));
			break;
		}
		matrix.scale(obj.getScale());
		return matrix;
	}
	
}
