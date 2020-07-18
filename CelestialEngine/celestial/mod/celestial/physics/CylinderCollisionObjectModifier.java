package mod.celestial.physics;

import celestial.core.Modifier;
import celestial.physics.CollisionMesh;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public final class CylinderCollisionObjectModifier extends AbstractCollisionObjectModifier {
	
	private static final long serialVersionUID = 5337844378603153399L;
	
	public static final Factory<CylinderCollisionObjectModifier> FACTORY = () -> new CylinderCollisionObjectModifier(0.5f);
	
	public CylinderCollisionObjectModifier(float friction) {
		super(friction);
	}
	
	private CylinderCollisionObjectModifier(CylinderCollisionObjectModifier src) {
		super(src);
	}
	
	@Override
	protected CollisionMesh getCollisionMesh() {
		return CollisionMesh.createCylinder(new Vector3f(1f));
	}
	
	@Override
	public Modifier duplicate() {
		return new CylinderCollisionObjectModifier(this);
	}
	
}
