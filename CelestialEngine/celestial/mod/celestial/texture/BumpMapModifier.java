package mod.celestial.texture;

import java.io.IOException;
import java.io.ObjectInputStream;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.serialization.SerializerImpl;
import celestial.shader.ShaderAttribute;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector2f;

public final class BumpMapModifier extends AbstractMapModifier {
	
	private static final long serialVersionUID = 5399942738073027403L;
	
	public static final Factory<BumpMapModifier> FACTORY = () -> new BumpMapModifier(null, 0, 1, 1, 1, 1, false, 0, new Vector2f(), false, false);
	
	private final Property<Float> intensity;
	
	public BumpMapModifier(Sampler sampler, int textureUnit, float tileFactor, int atlasWidth, int atlasHeight,
			float intensity, boolean distortEnabled, int distortUnit, Vector2f offset, boolean additiveMirrorX, boolean additiveMirrorY) {
		super("bumpMap", true, sampler, textureUnit, tileFactor, atlasWidth, atlasHeight, distortEnabled, distortUnit, offset, additiveMirrorX, additiveMirrorY);
		this.intensity = Properties.createFloatProperty(intensity);
		putProperties();
	}
	
	private BumpMapModifier(BumpMapModifier src) {
		super(src);
		this.intensity = src.intensity.clone();
		putProperties();
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = super.getPropertyController(system);
		ctrl.withProperty("Bump Intensity", intensity);
		return ctrl;
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
	
	public Modifier duplicate() {
		return new BumpMapModifier(this);
	}
	
	private void putProperties() {
		super.putGlobal(ShaderAttribute.$("vec3 surfaceNorm").withDefaultValue("vec3(0, 0, 1)"));
		super.putdf(() -> intensity, ShaderAttribute.$("float bumpMapIntensity"));
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("bumpMap");
		putProperties();
	}
	
}
