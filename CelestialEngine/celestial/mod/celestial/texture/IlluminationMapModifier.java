package mod.celestial.texture;

import java.io.IOException;
import java.io.ObjectInputStream;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.render.UpdatePacket;
import celestial.serialization.SerializerImpl;
import celestial.shader.ShaderAttribute;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector2f;

public final class IlluminationMapModifier extends AbstractMapModifier {
	
	private static final long serialVersionUID = -4359141355157054961L;
	
	public static final Factory<IlluminationMapModifier> FACTORY = () -> new IlluminationMapModifier(null, 0, 1, 1, 1, 0, false, false, 0, new Vector2f(), false, false);
	
	private final Property<Float> blendFactor;
	private final Property<Boolean> singleChannel;
	
	public IlluminationMapModifier(Sampler sampler, int textureUnit, float tileFactor, int atlasWidth, int atlasHeight,
			float blendFactor, boolean singleChannel, boolean distortEnabled, int distortUnit, Vector2f offset, boolean additiveMirrorX, boolean additiveMirrorY) {
		super("illuminationMap", true, sampler, textureUnit, tileFactor, atlasWidth, atlasHeight, distortEnabled, distortUnit, offset, additiveMirrorX, additiveMirrorY);
		this.blendFactor = Properties.createFloatProperty(blendFactor);
		this.singleChannel = Properties.createBooleanProperty(singleChannel);
		putProperties();
	}
	
	private IlluminationMapModifier(IlluminationMapModifier src) {
		super(src);
		this.blendFactor = src.blendFactor.clone();
		this.singleChannel = src.singleChannel.clone();
		putProperties();
	}
	
	@Override
	protected void update1(UpdatePacket packet, CEObject obj) {
		super.update1(packet, obj);
		singleChannel.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = super.getPropertyController(system);
		ctrl.withProperty("Blend Factor", blendFactor);
		ctrl.withProperty("Single Channel", singleChannel);
		return ctrl;
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
	
	public boolean isSingleChannel() {
		return singleChannel.get();
	}
	
	public void setSingleChannel(boolean singleChannel) {
		this.singleChannel.set(singleChannel);
	}
	
	public Property<Boolean> singleChannelProperty() {
		return singleChannel;
	}
	
	public Modifier duplicate() {
		return new IlluminationMapModifier(this);
	}
	
	private void putProperties() {
		super.putdf(() -> blendFactor, ShaderAttribute.$("float illuminationMapBlend"));
		super.putf(() -> singleChannel.get() ? 1f : 0f, ShaderAttribute.$("float illuminationMapSingleChannel"));
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("illuminationMap");
		putProperties();
	}
	
}
