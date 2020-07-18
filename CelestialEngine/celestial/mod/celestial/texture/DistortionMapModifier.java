package mod.celestial.texture;

import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.data.GLData;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector2f;
import studio.celestial.media.Media.MediaType;

public final class DistortionMapModifier extends Modifier {
	
	private static final long serialVersionUID = 5881923029472581400L;
	
	public static final Factory<DistortionMapModifier> FACTORY = () -> new DistortionMapModifier(null, 0, 1, 1, new Vector2f(), false, false);
	
	private final Property<Sampler> sampler;
	private final Property<Integer> textureUnit;
	private final Property<Float> tileFactor;
	private final Property<Float> intensity;
	private final Property<Vector2f> offset;
	private final Property<Boolean> additiveMirrorX;
	private final Property<Boolean> additiveMirrorY;
	
	public DistortionMapModifier(Sampler sampler, int textureUnit, float tileFactor, float intensity, Vector2f offset, boolean additiveMirrorX, boolean additiveMirrorY) {
		super(false, true, false);
		this.sampler = Properties.createProperty(Sampler.class, sampler);
		this.textureUnit = Properties.createIntegerProperty(textureUnit);
		this.tileFactor = Properties.createFloatProperty(tileFactor);
		this.intensity = Properties.createFloatProperty(intensity);
		this.offset = Properties.createVec2Property(offset);
		this.additiveMirrorX = Properties.createBooleanProperty(additiveMirrorX);
		this.additiveMirrorY = Properties.createBooleanProperty(additiveMirrorY);
	}
	
	private DistortionMapModifier(DistortionMapModifier src) {
		super(false, true, false);
		this.sampler = src.sampler.clone();
		this.textureUnit = src.textureUnit.clone();
		this.tileFactor = src.tileFactor.clone();
		this.intensity = src.intensity.clone();
		this.offset = src.offset.clone();
		this.additiveMirrorX = src.additiveMirrorX.clone();
		this.additiveMirrorY = src.additiveMirrorY.clone();
	}
	
	public ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("sampler2D distortionMap", 4),
				ShaderAttribute.$("float distortionMapTile", 4), ShaderAttribute.$("float distortionMapIntensity", 4),
				ShaderAttribute.$("vec2 distortionMapOffset", 4), ShaderAttribute.$("vec2 distortionMapAdditiveMirror", 4));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("distortionMap",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule("", fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs());
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		int textureUnit = this.textureUnit.get();
		while(textureUnit >= 4) textureUnit -= 4;
		while(textureUnit < 0) textureUnit += 4;
		int texID = packet.getShader().getCommunicator().load1i("distortionMap[" + textureUnit + "]");
		if(sampler.get() != null) sampler.get().bind(texID);
		packet.getShader().getCommunicator().store1f("distortionMapTile[" + textureUnit + "]", tileFactor.get());
		packet.getShader().getCommunicator().store1f("distortionMapIntensity[" + textureUnit + "]", intensity.get());
		packet.getShader().getCommunicator().store2f("distortionMapOffset[" + textureUnit + "]", offset.get());
		packet.getShader().getCommunicator().store2f("distortionMapAdditiveMirror[" + textureUnit + "]", new Vector2f(additiveMirrorX.get() ? 1f : 0f, additiveMirrorY.get() ? 1f : 0f));
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		sampler.update(!packet.isPaused());
		tileFactor.update(!packet.isPaused());
		intensity.update(!packet.isPaused());
		offset.update(!packet.isPaused());
		additiveMirrorX.update(!packet.isPaused());
		additiveMirrorY.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Sampler Media", Properties.createProperty(GLData[].class, () -> new GLData[] {sampler.get()}, s -> sampler.set((Sampler) s[0])));
		ctrl.getProperty("Sampler Media").setUserPointer(new MediaType[] {MediaType.PNG});
		ctrl.withProperty("Texture Unit", textureUnit).withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 0, IntervalType.INCLUSIVE, 3, IntervalType.INCLUSIVE, 0), 1);
		ctrl.withProperty("Tile Factor", tileFactor);
		ctrl.withProperty("Distort Intensity", intensity);
		ctrl.withProperty("Offset", offset);
		ctrl.withProperty("Additive Mirror X", additiveMirrorX);
		ctrl.withProperty("Additive Mirror Y", additiveMirrorY);
		return ctrl;
	}
	
	public Sampler getSampler() {
		return sampler.get();
	}
	
	public void setSampler(Sampler sampler) {
		this.sampler.set(sampler);
	}
	
	public Property<Sampler> samplerProperty() {
		return sampler;
	}
	
	public int getTextureUnit() {
		return textureUnit.get();
	}
	
	public void setTextureUnit(int textureUnit) {
		this.textureUnit.set(textureUnit);
	}
	
	public Property<Integer> textureUnitProperty() {
		return textureUnit;
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
	
	public float getIntensity() {
		return intensity.get();
	}
	
	public void setIntensity(float intensity) {
		this.intensity.set(intensity);
	}
	
	public Property<Float> intensityProperty() {
		return intensity;
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
		return new DistortionMapModifier(this);
	}
	
	@Override
	public boolean containsData(GLData data) {
		return data == sampler.get();
	}
	
}
