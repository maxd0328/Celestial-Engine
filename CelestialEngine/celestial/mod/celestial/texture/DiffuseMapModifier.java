package mod.celestial.texture;

import java.io.IOException;
import java.io.ObjectInputStream;
import celestial.core.Modifier;
import celestial.data.Sampler;
import celestial.serialization.SerializerImpl;
import celestial.util.Factory;
import celestial.vecmath.Vector2f;

public final class DiffuseMapModifier extends AbstractMapModifier {
	
	private static final long serialVersionUID = -1460498260632306024L;
	
	public static final Factory<DiffuseMapModifier> FACTORY = () -> new DiffuseMapModifier(null, 0, 1, 1, 1, false, 0, new Vector2f(), false, false);
	
	public DiffuseMapModifier(Sampler sampler, int textureUnit, float tileFactor, int atlasWidth, int atlasHeight,
			boolean distortEnabled, int distortUnit, Vector2f offset, boolean additiveMirrorX, boolean additiveMirrorY) {
		super("diffuseMap", false, sampler, textureUnit, tileFactor, atlasWidth, atlasHeight, distortEnabled, distortUnit, offset, additiveMirrorX, additiveMirrorY);
	}
	
	private DiffuseMapModifier(DiffuseMapModifier src) {
		super(src);
	}
	
	public Modifier duplicate() {
		return new DiffuseMapModifier(this);
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.loadImplementation("diffuseMap");
	}
	
}
