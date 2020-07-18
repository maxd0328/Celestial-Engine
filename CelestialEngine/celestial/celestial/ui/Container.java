package celestial.ui;

import java.util.List;
import celestial.collections.ObservableArrayList;
import celestial.collections.ObservableList;
import celestial.ui.layout.Layout;

public abstract class Container extends CompoundComponent {
	
	private final ObservableList<Component> children;
	
	public Container(String identifier, Graphic graphic, Layout<?> layout) {
		super(identifier, graphic, layout);
		this.children = new ObservableArrayList<>();
	}
	
	public ObservableList<Component> getChildren() {
		return children;
	}
	
	@Override
	protected List<Component> subComponents() {
		return getChildren();
	}
	
}
