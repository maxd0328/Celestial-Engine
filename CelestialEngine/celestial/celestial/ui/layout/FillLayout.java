package celestial.ui.layout;

import celestial.core.EngineRuntime;
import celestial.ui.Component;
import celestial.ui.CompoundComponent;
import celestial.util.KVEntry;
import celestial.vecmath.Vector2f;

public final class FillLayout extends Layout<Object> {
	
	public FillLayout() {
		super(() -> new Object());
	}
	
	@Override
	public KVEntry<Vector2f, Vector2f> restrainComponent(Component comp, CompoundComponent container) {
		return new KVEntry<>(new Vector2f(0, 0), new Vector2f(EngineRuntime.dispGetDisplayMode().getWidth(), EngineRuntime.dispGetDisplayMode().getHeight()));
	}

	@Override
	public void update() {
	}

}
