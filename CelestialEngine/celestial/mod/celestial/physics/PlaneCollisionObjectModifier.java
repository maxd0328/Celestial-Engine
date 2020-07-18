package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class PlaneCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = 1112607933700010495L;
	
	public static final Factory<PlaneCollisionObjectModifier> FACTORY = () -> new PlaneCollisionObjectModifier(0.5f);
	
	public PlaneCollisionObjectModifier(float friction) {
		super(friction);
	}
	
	private PlaneCollisionObjectModifier(PlaneCollisionObjectModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createBox(new Vector3f(1, 0, 1));
	}
	
	@Override
	public Modifier duplicate() {
		return new PlaneCollisionObjectModifier(this);
	}
	
}
