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
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.util.ISceneSystem;
import studio.celestial.media.Media.MediaType;

public abstract class AbstractSamplerModifier extends Modifier {
	
	private static final long serialVersionUID = 9011646200348149479L;
	
	protected transient String mapIdentifier;
	protected transient int samplerMax;
	
	protected final Property<Sampler> sampler;
	protected final Property<Integer> textureUnit;
	protected final Property<Boolean> distortEnabled;
	protected final Property<Integer> distortUnit;
	
	public AbstractSamplerModifier(boolean allowMultiple, boolean colorOnly, String mapIdentifier, int samplerMax,
			Sampler sampler, int textureUnit, boolean distortEnabled, int distortUnit) {
		super(false, allowMultiple, colorOnly);
		this.mapIdentifier = mapIdentifier;
		this.samplerMax = samplerMax;
		this.sampler = Properties.createProperty(Sampler.class, sampler);
		this.textureUnit = Properties.createIntegerProperty(textureUnit);
		this.distortEnabled = Properties.createBooleanProperty(distortEnabled);
		this.distortUnit = Properties.createIntegerProperty(distortUnit);
	}
	
	protected AbstractSamplerModifier(AbstractSamplerModifier src) {
		super(false, src.isAllowMultiple(), src.isColorOnly());
		this.mapIdentifier = src.mapIdentifier;
		this.samplerMax = src.samplerMax;
		this.sampler = src.sampler.clone();
		this.textureUnit = src.textureUnit.clone();
		this.distortEnabled = src.distortEnabled.clone();
		this.distortUnit = src.distortUnit.clone();
	}
	
	protected AbstractSamplerModifier(AbstractSamplerModifier src, Sampler sampler) {
		super(false, src.isAllowMultiple(), src.isColorOnly());
		this.mapIdentifier = src.mapIdentifier;
		this.samplerMax = src.samplerMax;
		this.sampler = Properties.createProperty(Sampler.class, sampler);
		this.textureUnit = src.textureUnit.clone();
		this.distortEnabled = src.distortEnabled.clone();
		this.distortUnit = src.distortUnit.clone();
	}
	
	protected ArrayList<ShaderAttribute> fUniforms(ArrayList<ShaderAttribute> fUniforms) {
		return fUniforms("sampler2D", fUniforms);
	}
	
	protected ArrayList<ShaderAttribute> fUniforms(String samplerType, ArrayList<ShaderAttribute> fUniforms) {
		fUniforms.addAll(ShaderModule.attribs(ShaderAttribute.$(samplerType + " " + mapIdentifier, samplerMax),
				ShaderAttribute.$("float " + mapIdentifier + "DistortUnit", samplerMax), ShaderAttribute.$("sampler2D distortionMap", 4),
				ShaderAttribute.$("float distortionMapTile", 4), ShaderAttribute.$("float distortionMapIntensity", 4),
				ShaderAttribute.$("vec2 distortionMapOffset", 4), ShaderAttribute.$("vec2 distortionMapAdditiveMirror", 4),
				ShaderAttribute.$("sampler2D blendMap"), ShaderAttribute.$("float blendMapTile"), ShaderAttribute.$("vec2 blendMapOffset"),
				ShaderAttribute.$("vec2 blendMapMirror")));
		return fUniforms;
	}
	
	protected ArrayList<ShaderAttribute> inputs(ArrayList<ShaderAttribute> inputs) {
		inputs.addAll(ShaderModule.attribs(ShaderAttribute.$("vec2 texCoords")));
		return inputs;
	}
	
	protected ArrayList<ShaderAttribute> pvAttribs(ArrayList<ShaderAttribute> pvAttribs) {
		pvAttribs.addAll(ShaderModule.attribs(ShaderAttribute.$("vec2 texCoords")));
		return pvAttribs;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		int textureUnit = this.textureUnit.get();
		while(textureUnit >= samplerMax) textureUnit -= samplerMax;
		while(textureUnit < 0) textureUnit += samplerMax;
		int texID = packet.getShader().getCommunicator().load1i(samplerMax == 1 ? mapIdentifier : mapIdentifier + "[" + textureUnit + "]");
		if(sampler.get() != null) sampler.get().bind(texID);
		packet.getShader().getCommunicator().store1f(mapIdentifier + "DistortUnit" + (samplerMax == 1
				? "" : "[" + textureUnit + "]"), distortEnabled.get() ? (float) distortUnit.get() : -1f);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		sampler.update(!packet.isPaused());
		textureUnit.update(!packet.isPaused());
		distortEnabled.update(!packet.isPaused());
		distortUnit.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Sampler Media", Properties.createProperty(GLData[].class, () -> new GLData[] {sampler.get()}, s -> sampler.set((Sampler) s[0])));
		ctrl.getProperty("Sampler Media").setUserPointer(new MediaType[] {MediaType.PNG});
		ctrl.withProperty("Texture Unit", textureUnit)
				.withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 0, IntervalType.INCLUSIVE, samplerMax - 1, IntervalType.INCLUSIVE, 0), 1);
		ctrl.withProperty("Distort Enabled", distortEnabled);
		ctrl.withProperty("Distort Unit", distortUnit).withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 0, IntervalType.INCLUSIVE, 3, IntervalType.INCLUSIVE, 0), 1);
		return ctrl;
	}
	
	@Override
	public boolean containsData(GLData data) {
		return sampler.get() == data;
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
	
	public boolean isDistortEnabled() {
		return distortEnabled.get();
	}
	
	public void setDistortEnabled(boolean distortEnabled) {
		this.distortEnabled.set(distortEnabled);
	}
	
	public Property<Boolean> distortEnabledProperty() {
		return distortEnabled;
	}
	
	public int getDistortUnit() {
		return distortUnit.get();
	}
	
	public void setDistortUnit(int distortUnit) {
		this.distortUnit.set(distortUnit);
	}
	
	public Property<Integer> distortUnitProperty() {
		return distortUnit;
	}
	
	protected void loadImplementation(String mapIdentifier, int samplerMax) {
		this.mapIdentifier = mapIdentifier;
		this.samplerMax = samplerMax;
	}
	
}
