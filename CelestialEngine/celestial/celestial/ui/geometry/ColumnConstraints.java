package celestial.ui.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.util.KVEntry;
import celestial.util.Priority;

public final class ColumnConstraints {
	
	private final Property<Integer> minWidth;
	private final Property<Boolean> widthPreferred;
	private final Property<Integer> prefWidth;
	private final Property<Float> percentWidth;
	private final Property<Boolean> hgrow;
	private final Property<Alignment> alignment;
	private final Property<Priority> priority;
	
	public ColumnConstraints(int prefWidth, Priority priority) {
		this.minWidth = Properties.createIntegerProperty();
		this.widthPreferred = Properties.createBooleanProperty(true);
		this.prefWidth = Properties.createIntegerProperty(prefWidth);
		this.percentWidth = Properties.createFloatProperty();
		this.hgrow = Properties.createBooleanProperty();
		this.alignment = Properties.createProperty(Alignment.class, Alignment.NONE);
		this.priority = Properties.createProperty(Priority.class, priority);
	}
	
	public ColumnConstraints(Priority priority) {
		this.minWidth = Properties.createIntegerProperty();
		this.widthPreferred = Properties.createBooleanProperty();
		this.prefWidth = Properties.createIntegerProperty();
		this.percentWidth = Properties.createFloatProperty();
		this.hgrow = Properties.createBooleanProperty();
		this.alignment = Properties.createProperty(Alignment.class, Alignment.NONE);
		this.priority = Properties.createProperty(Priority.class, priority);
	}
	
	public int getMinWidth() {
		return minWidth.get();
	}
	
	public void setMinWidth(int minWidth) {
		this.minWidth.set(minWidth);
	}
	
	public Property<Integer> minWidthProperty() {
		return minWidth;
	}
	
	public boolean isWidthPreferred() {
		return widthPreferred.get();
	}
	
	public void setWidthPreferred(boolean widthPreferred) {
		this.widthPreferred.set(widthPreferred);
	}
	
	public Property<Boolean> widthPreferredProperty() {
		return widthPreferred;
	}
	
	public int getPrefWidth() {
		return prefWidth.get();
	}
	
	public void setPrefWidth(int prefWidth) {
		widthPreferred.set(true);
		this.prefWidth.set(prefWidth);
	}
	
	public Property<Integer> prefWidthProperty() {
		return prefWidth;
	}
	
	public float getPercentWidth() {
		return percentWidth.get();
	}
	
	public void setPercentWidth(float percentWidth) {
		if(percentWidth > 0 && percentWidth <= 100)
			widthPreferred.set(true);
		this.percentWidth.set(percentWidth);
	}
	
	public Property<Float> percentWidthProperty() {
		return percentWidth;
	}
	
	public boolean isHgrow() {
		return hgrow.get();
	}
	
	public void setHgrow(boolean hgrow) {
		if(hgrow) widthPreferred.set(true);
		this.hgrow.set(hgrow);
	}
	
	public Property<Boolean> hgrowProperty() {
		return hgrow;
	}
	
	public Alignment getAlignment() {
		return alignment.get();
	}
	
	public void setAlignment(Alignment alignment) {
		this.alignment.set(alignment);
	}
	
	public Property<Alignment> alignmentProperty() {
		return alignment;
	}
	
	public Priority getPriority() {
		return priority.get();
	}
	
	public void setPriority(Priority priority) {
		this.priority.set(priority);
	}
	
	public Property<Priority> priorityProperty() {
		return priority;
	}
	
	public int calculateDesiredWidth(int componentWidth, int totalWidth) {
		if(widthPreferred.get()) {
			if(hgrow.get())
				return Integer.MAX_VALUE;
			else if(percentWidth.get() > 0 && percentWidth.get() <= 100)
				return (int) ((float) totalWidth * (percentWidth.get() / 100f));
			else
				return prefWidth.get();
		}
		else return componentWidth;
	}
	
	public void update() {
		widthPreferred.update();
		prefWidth.update();
		percentWidth.update();
		hgrow.update();
		alignment.update();
		priority.update();
	}
	
	@SuppressWarnings("unchecked")
	public static List<KVEntry<Integer, Integer>> calculateWidths(int totalWidth, int gap, List<KVEntry<ColumnConstraints, Integer>> constraints) {
		ColumnConstraints[] constraintsArr = new ColumnConstraints[constraints.size()];
		int[] heightArr = new int[constraints.size()];
		for(int i = 0 ; i < constraints.size() ; ++i) {
			constraintsArr[i] = constraints.get(i).getKey();
			heightArr[i] = constraints.get(i).getValue();
		}
		
		long desiredTotal = 0;
		KVEntry<Integer, Priority>[] desiredWidths = new KVEntry[constraintsArr.length];
		for(int i = 0 ; i < desiredWidths.length ; ++i) {
			desiredWidths[i] = new KVEntry<>(constraintsArr[i].calculateDesiredWidth((int) heightArr[i], totalWidth), constraintsArr[i].getPriority());
			desiredTotal += (long) desiredWidths[i].getKey();
		}
		if(desiredWidths.length > 1)
			desiredTotal += gap * (desiredWidths.length - 1);
		
		long excess;
		
		for(int i = 0 ; i <= Priority.getRange() ; ++i) {
			excess = desiredTotal - (long) totalWidth;
			if(excess <= 0) break;
			
			Collection<Integer> indices = getIndicesByPriority(desiredWidths, i);
			if(indices.size() == 0) continue;
			long interval = excess / indices.size();
			
			for(int index : indices) {
				desiredTotal -= desiredWidths[index].getKey();
				desiredWidths[index].setKey((int) Math.max(constraintsArr[index].getMinWidth(), desiredWidths[index].getKey() - interval));
				desiredTotal += desiredWidths[index].getKey();
			}
		}
		
		List<KVEntry<Integer, Integer>> widthData = new ArrayList<KVEntry<Integer, Integer>>();
		int coord = 0;
		for(int i = 0 ; i < desiredWidths.length ; ++i) {
			widthData.add(new KVEntry<>(coord, coord + desiredWidths[i].getKey()));
			coord += desiredWidths[i].getKey() + gap;
		}
		
		return widthData;
	}
	
	private static Collection<Integer> getIndicesByPriority(KVEntry<Integer, Priority>[] desiredWidths, int priorityLvl){
		Collection<Integer> indices = new ArrayList<>();
		
		for(int i = 0 ; i < desiredWidths.length ; ++i)
			if(desiredWidths[i].getValue().asInteger() == priorityLvl)
				indices.add(i);
		
		return indices;
	}
	
}
