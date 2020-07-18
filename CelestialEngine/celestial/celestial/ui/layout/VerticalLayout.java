package celestial.ui.layout;

import java.util.ArrayList;
import java.util.List;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.ui.Component;
import celestial.ui.CompoundComponent;
import celestial.ui.geometry.VAlignment;
import celestial.ui.geometry.RowConstraints;
import celestial.util.KVEntry;
import celestial.util.Priority;
import celestial.vecmath.Vector2f;

public final class VerticalLayout extends Layout<RowConstraints> {
	
	private final Property<Integer> gap;
	private final Property<VAlignment> alignment;
	
	private List<KVEntry<Integer, Integer>> heightData = null;
	
	public VerticalLayout() {
		super(() -> new RowConstraints(Priority.LOW));
		this.gap = Properties.createIntegerProperty();
		this.alignment = Properties.createProperty(VAlignment.class, VAlignment.NONE);
	}
	
	@Override
	public KVEntry<Vector2f, Vector2f> restrainComponent(Component comp, CompoundComponent container) {
		int boundLeft = (int) container.getConstrainedPosition().x;
		int boundRight = (int) container.getConstrainedPosition().x + (int) container.getConstrainedScale().x;
		int locationTop = (int) container.getConstrainedPosition().y;
		int locationBottom = (int) container.getConstrainedPosition().y + (int) container.getConstrainedScale().y;
		
		if(heightData == null) {
			List<KVEntry<RowConstraints, Integer>> constraintData = new ArrayList<>();
			for(Component _comp : super.getConstraints().keySet())
				constraintData.add(new KVEntry<>(super.getConstraints().get(_comp), (int) _comp.getPrefScale().y));
			heightData = RowConstraints.calculateHeights((int) container.getConstrainedScale().y, gap.get(), constraintData);
		}
		
		int highestBounds = Integer.MIN_VALUE;
		for(KVEntry<Integer, Integer> entry : heightData)
			if(entry.getValue() > highestBounds)
				highestBounds = entry.getValue();
		int totalHeight = highestBounds - locationTop, location = locationTop;
		switch(alignment.get()) {
		case CENTER:
			location = (locationTop + locationBottom) / 2 - totalHeight / 2;
			break;
		case BOTTOM:
			location = locationBottom - totalHeight;
			break;
		default:
		}
		
		KVEntry<Integer, Integer> bounds = heightData.get(super.getConstraints().indexOf(comp));
		return comp.constrainTo(boundLeft, boundRight, location + bounds.getKey(), location + bounds.getValue(), super.getConstraints().get(comp).getAlignment());
	}
	
	@Override
	public void update() {
		for(Component comp : super.getConstraints().keySet())
			super.getConstraints().get(comp).update();
		gap.update();
		alignment.update();
		heightData = null;
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
	
	public VAlignment getAlignment() {
		return alignment.get();
	}
	
	public void setAlignment(VAlignment alignment) {
		this.alignment.set(alignment);
	}
	
	public Property<VAlignment> alignmentProperty() {
		return alignment;
	}
	
}
