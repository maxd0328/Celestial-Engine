package mod.celestial.texture;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.ctrl.PropertyController;
import celestial.data.GLData;
import celestial.data.Sampler;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.serialization.SerializerImpl;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector2f;
import studio.celestial.media.Media.MediaType;

public final class BlendMapModifier extends AbstractSamplerModifier {
	
	private static final long serialVersionUID = -6144494425552332824L;
	
	public static final Factory<BlendMapModifier> FACTORY = () -> new BlendMapModifier(null, 1, false, 0, new Vector2f(), false, false);
	
	public static final int BLEND_TEXTURE_0 = 0x0;
	public static final int BLEND_TEXTURE_R = 0x1;
	public static final int BLEND_TEXTURE_G = 0x2;
	public static final int BLEND_TEXTURE_B = 0x3;
	
	private final Property<Float> tileFactor;
	private final Property<Vector2f> offset;
	private final Property<Boolean> additiveMirrorX;
	private final Property<Boolean> additiveMirrorY;
	
	public BlendMapModifier(Sampler sampler, float tileFactor, boolean distortEnabled, int distortUnit, Vector2f offset, boolean additiveMirrorX, boolean additiveMirrorY) {
		super(false, true, "blendMap", 1, sampler, 0, distortEnabled, distortUnit);
		this.tileFactor = Properties.createFloatProperty(tileFactor);
		this.offset = Properties.createVec2Property(offset);
		this.additiveMirrorX = Properties.createBooleanProperty(additiveMirrorX);
		this.additiveMirrorY = Properties.createBooleanProperty(additiveMirrorY);
	}
	
	private BlendMapModifier(BlendMapModifier src) {
		super(src);
		this.tileFactor = src.tileFactor.clone();
		this.offset = src.offset.clone();
		this.additiveMirrorX = src.additiveMirrorX.clone();
		this.additiveMirrorY = src.additiveMirrorY.clone();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = fUniforms(ShaderModule.attribs());
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("blendMap",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule("", fUniforms, fGlobals),
				getID(), inputs(ShaderModule.attribs()),
				pvAttribs(ShaderModule.attribs()));
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		super.preRender(packet, obj);
		packet.getShader().getCommunicator().store1f("blendMapTile", tileFactor.get());
		packet.getShader().getCommunicator().store2f("blendMapOffset", offset.get());
		packet.getShader().getCommunicator().store2f("blendMapMirror", new Vector2f(additiveMirrorX.get() ? 1f : 0f, additiveMirrorY.get() ? 1f : 0f));
		int texID = packet.getShader().getCommunicator().load1i("blendMap");
		if(sampler.get() != null) sampler.get().bind(texID);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		super.render(packet, obj);
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		super.postRender(packet, obj);
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		super.update0(packet, obj);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		super.update1(packet, obj);
		tileFactor.update(!packet.isPaused());
		offset.update(!packet.isPaused());
		additiveMirrorX.update(!packet.isPaused());
		additiveMirrorY.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Sampler Media", Properties.createProperty(GLData[].class, () -> new GLData[] {sampler.get()}, s -> sampler.set((Sampler) s[0])));
		ctrl.getProperty("Sampler Media").setUserPointer(new MediaType[] {MediaType.PNG});
		ctrl.withProperty("Tile Factor", tileFactor);
		ctrl.withProperty("Offset", offset);
		ctrl.withProperty("Additive Mirror X", additiveMirrorX);
		ctrl.withProperty("Additive Mirror Y", additiveMirrorY);
		
		ctrl.withProperty("Distort Enabled", distortEnabled);
		ctrl.withProperty("Distort Unit", distortUnit).withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 0, IntervalType.INCLUSIVE, 3, IntervalType.INCLUSIVE, 0), 1);
		return ctrl;
	}
	
	public float getTileFactor() {
		return tileFactor.get();
	}
	
	public void setTileFactor(float tileFactor) {
		this.tileFactor.set(tileFactor);
	}
	
	public Property<Float> tileFactorProperty() {
		return tileFactor;
	}
	
	public Vector2f getOffset() {
		return offset.get();
	}
	
	public void setOffset(Vector2f offset) {
		this.offset.set(offset);
	}
	
	public Property<Vector2f> offsetProperty() {
		return offset;
	}
	
	public boolean isAdditiveMirrorX() {
		return additiveMirrorX.get();
	}
	
	public void setAdditiveMirrorX(boolean additiveMirrorX) {
		this.additiveMirrorX.set(additiveMirrorX);
	}
	
	public Property<Boolean> additiveMirrorXProperty() {
		return additiveMirrorX;
	}
	
	public boolean isAdditiveMirrorY() {
		return additiveMirrorY.get();
	}
	
	public void setAdditiveMirrorY(boolean additiveMirrorY) {
		this.additiveMirrorY.set(additiveMirrorY);
	}
	
	public Property<Boolean> additiveMirrorYProperty() {
		return additiveMirrorY;
	}
	
	public Modifier duplicate() {
		return new BlendMapModifier(this);
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("blendMap", 1);
	}
	
}
