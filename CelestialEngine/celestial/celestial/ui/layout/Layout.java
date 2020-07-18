package celestial.ui.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import celestial.collections.ObservableLinkedHashMap;
import celestial.collections.ObservableMap;
import celestial.ui.Component;
import celestial.ui.CompoundComponent;
import celestial.util.Factory;
import celestial.util.KVEntry;
import celestial.vecmath.Vector2f;

public abstract class Layout<__ConstraintType> {
	
	private final Factory<__ConstraintType> factory;
	private final ObservableMap<Component, __ConstraintType> constraints;
	
	protected Layout(Factory<__ConstraintType> factory) {
		this.factory = factory;
		this.constraints = new ObservableLinkedHashMap<Component, __ConstraintType>();
	}
	
	public ObservableMap<Component, __ConstraintType> getConstraints() {
		return constraints;
	}
	
	public void registerComponent(Component comp) {
		if(factory != null && !constraints.containsKey(comp))
			constraints.put(comp, factory.build());
	}
	
	public void updateConstraintOrder(List<Component> children) {
		ArrayList<Entry<Component, __ConstraintType>> entries = new ArrayList<>(constraints.entrySet());
		Collections.sort(entries, (s0, s1) -> Integer.compare(children.indexOf(s0.getKey()), children.indexOf(s1.getKey())));
		
		constraints.clear();
		for(Entry<Component, __ConstraintType> e : entries)
			constraints.put(e.getKey(), e.getValue());
	}
	
	public abstract KVEntry<Vector2f, Vector2f> restrainComponent(Component comp, CompoundComponent container);
	
	public abstract void update();
	
}
