package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class BoxCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = -5499368520637100714L;
	
	public static final Factory<BoxCollisionObjectModifier> FACTORY = () -> new BoxCollisionObjectModifier(0.5f);
	
	public BoxCollisionObjectModifier(float friction) {
		super(friction);
	}
	
	private BoxCollisionObjectModifier(BoxCollisionObjectModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createBox(new Vector3f(1));
	}
	
	@Override
	public Modifier duplicate() {
		return new BoxCollisionObjectModifier(this);
	}
	
}
