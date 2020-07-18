package celestial.ui.layout;

import celestial.ui.Component;
import celestial.ui.CompoundComponent;
import celestial.util.KVEntry;
import celestial.vecmath.Vector2f;

public final class NullLayout extends Layout<Object> {
	
	public NullLayout() {
		super(null);
	}
	
	@Override
	public KVEntry<Vector2f, Vector2f> restrainComponent(Component comp, CompoundComponent container) {
		return new KVEntry<Vector2f, Vector2f>(comp.getPrefPosition(), comp.getPrefScale());
	}
	
	@Override
	public void update() {
	}
	
}
