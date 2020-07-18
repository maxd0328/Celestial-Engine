package celestial.ui;

import java.util.List;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.ui.layout.Layout;
import celestial.ui.layout.NullLayout;

public abstract class CompoundComponent extends Component {
	
	private final Property<Layout<?>> layout;
	
	@SuppressWarnings("unchecked")
	public CompoundComponent(String identifier, Graphic graphic, Layout<?> layout) {
		super(identifier, graphic);
		this.layout = Properties.<Layout<?>>createProperty((Class<Layout<?>>) (Object) Layout.class, layout == null ? new NullLayout() : layout);
		this.layout.addListener((obs, _old, _new) -> { if(_new == null) this.layout.set(new NullLayout()); });
	}
	
	public Layout<?> getLayout() {
		return layout.get();
	}
	
	public void setLayout(Layout<?> layout) {
		this.layout.set(layout);
	}
	
	public Property<Layout<?>> layoutProperty() {
		return layout;
	}
	
	@Override
	public boolean containsData(GLData data) {
		return super.containsData(data) || childrenContainsData(data);
	}
	
	private boolean childrenContainsData(GLData data) {
		for(Component child : subComponents())
			if(child.containsData(data))
				return true;
		return false;
	}
	
	@Override
	void internalUpdate0() {
		super.internalUpdate0();
		if(layout.get() != null)
			layout.get().updateConstraintOrder(subComponents());
		for(Component child : subComponents()) {
			child.registerComponent(this);
			if(layout.get() != null)
				layout.get().registerComponent(child);
		}
	}
	
	@Override
	void internalUpdate1() {
		super.internalUpdate1();
		layout.update();
		if(layout.get() != null)
			layout.get().update();
	}
	
	protected abstract List<Component> subComponents();
	
}
