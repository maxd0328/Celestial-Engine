package celestial.ui.layout;

import java.util.ArrayList;
import java.util.List;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.ui.Component;
import celestial.ui.CompoundComponent;
import celestial.ui.geometry.HAlignment;
import celestial.ui.geometry.ColumnConstraints;
import celestial.util.KVEntry;
import celestial.util.Priority;
import celestial.vecmath.Vector2f;

public final class HorizontalLayout extends Layout<ColumnConstraints> {
	
	private final Property<Integer> gap;
	private final Property<HAlignment> alignment;
	
	private List<KVEntry<Integer, Integer>> widthData = null;
	
	public HorizontalLayout() {
		super(() -> new ColumnConstraints(Priority.LOW));
		this.gap = Properties.createIntegerProperty();
		this.alignment = Properties.createProperty(HAlignment.class, HAlignment.NONE);
	}
	
	@Override
	public KVEntry<Vector2f, Vector2f> restrainComponent(Component comp, CompoundComponent container) {
		int boundTop = (int) container.getConstrainedPosition().y;
		int boundBottom = (int) container.getConstrainedPosition().y + (int) container.getConstrainedScale().y;
		int locationLeft = (int) container.getConstrainedPosition().x;
		int locationRight = (int) container.getConstrainedPosition().x + (int) container.getConstrainedScale().x;
		
		if(widthData == null) {
			List<KVEntry<ColumnConstraints, Integer>> constraintData = new ArrayList<>();
			for(Component _comp : super.getConstraints().keySet())
				constraintData.add(new KVEntry<>(super.getConstraints().get(_comp), (int) _comp.getPrefScale().x));
			widthData = ColumnConstraints.calculateWidths((int) container.getConstrainedScale().x, gap.get(), constraintData);
		}
		
		int highestBounds = Integer.MIN_VALUE;
		for(KVEntry<Integer, Integer> entry : widthData)
			if(entry.getValue() > highestBounds)
				highestBounds = entry.getValue();
		int totalWidth = highestBounds - locationLeft, location = locationLeft;
		switch(alignment.get()) {
		case CENTER:
			location = (locationLeft + locationRight) / 2 - totalWidth / 2;
			break;
		case RIGHT:
			location = locationRight - totalWidth;
			break;
		default:
		}
		
		KVEntry<Integer, Integer> bounds = widthData.get(super.getConstraints().indexOf(comp));
		return comp.constrainTo(location + bounds.getKey(), location + bounds.getValue(), boundTop, boundBottom, super.getConstraints().get(comp).getAlignment());
	}
	
	@Override
	public void update() {
		for(Component comp : super.getConstraints().keySet())
			super.getConstraints().get(comp).update();
		gap.update();
		alignment.update();
		widthData = null;
	}
	
	public int getGap() {
		return gap.get();
	}
	
	public void setGap(int gap) {
		this.gap.set(gap);
	}
	
	public Property<Integer> gapProperty() {
		return gap;
	}
	
	public HAlignment getAlignment() {
		return alignment.get();
	}
	
	public void setAlignment(HAlignment alignment) {
		this.alignment.set(alignment);
	}
	
	public Property<HAlignment> alignmentProperty() {
		return alignment;
	}
	
}
