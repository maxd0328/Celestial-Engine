package celestial.ui.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.util.KVEntry;
import celestial.util.Priority;

public final class RowConstraints {
	
	private final Property<Integer> minHeight;
	private final Property<Boolean> heightPreferred;
	private final Property<Integer> prefHeight;
	private final Property<Float> percentHeight;
	private final Property<Boolean> vgrow;
	private final Property<Alignment> alignment;
	private final Property<Priority> priority;
	
	public RowConstraints(int prefHeight, Priority priority) {
		this.minHeight = Properties.createIntegerProperty();
		this.heightPreferred = Properties.createBooleanProperty(true);
		this.prefHeight = Properties.createIntegerProperty(prefHeight);
		this.percentHeight = Properties.createFloatProperty();
		this.vgrow = Properties.createBooleanProperty();
		this.alignment = Properties.createProperty(Alignment.class, Alignment.NONE);
		this.priority = Properties.createProperty(Priority.class, priority);
	}
	
	public RowConstraints(Priority priority) {
		this.minHeight = Properties.createIntegerProperty();
		this.heightPreferred = Properties.createBooleanProperty();
		this.prefHeight = Properties.createIntegerProperty();
		this.percentHeight = Properties.createFloatProperty();
		this.vgrow = Properties.createBooleanProperty();
		this.alignment = Properties.createProperty(Alignment.class, Alignment.NONE);
		this.priority = Properties.createProperty(Priority.class, priority);
	}
	
	public int getMinHeight() {
		return minHeight.get();
	}
	
	public void setMinHeight(int minHeight) {
		this.minHeight.set(minHeight);
	}
	
	public Property<Integer> minHeightProperty() {
		return minHeight;
	}
	
	public boolean isHeightPreferred() {
		return heightPreferred.get();
	}
	
	public void setHeightPreferred(boolean heightPreferred) {
		this.heightPreferred.set(heightPreferred);
	}
	
	public Property<Boolean> heightPreferredProperty() {
		return heightPreferred;
	}
	
	public int getPrefHeight() {
		return prefHeight.get();
	}
	
	public void setPrefHeight(int prefHeight) {
		heightPreferred.set(true);
		this.prefHeight.set(prefHeight);
	}
	
	public Property<Integer> prefHeightProperty() {
		return prefHeight;
	}
	
	public float getPercentHeight() {
		return percentHeight.get();
	}
	
	public void setPercentHeight(float percentHeight) {
		if(percentHeight > 0 && percentHeight <= 100)
			heightPreferred.set(true);
		this.percentHeight.set(percentHeight);
	}
	
	public Property<Float> percentHeightProperty() {
		return percentHeight;
	}
	
	public boolean isVgrow() {
		return vgrow.get();
	}
	
	public void setVgrow(boolean vgrow) {
		if(vgrow) heightPreferred.set(true);
		this.vgrow.set(vgrow);
	}
	
	public Property<Boolean> vgrowProperty() {
		return vgrow;
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
	
	public int calculateDesiredHeight(int componentHeight, int totalHeight) {
		if(heightPreferred.get()) {
			if(vgrow.get())
				return Integer.MAX_VALUE;
			else if(percentHeight.get() > 0 && percentHeight.get() <= 100)
				return (int) ((float) totalHeight * (percentHeight.get() / 100f));
			else
				return prefHeight.get();
		}
		else return componentHeight;
	}
	
	public void update() {
		heightPreferred.update();
		prefHeight.update();
		percentHeight.update();
		vgrow.update();
		alignment.update();
		priority.update();
	}
	
	@SuppressWarnings("unchecked")
	public static List<KVEntry<Integer, Integer>> calculateHeights(int totalHeight, int gap, List<KVEntry<RowConstraints, Integer>> constraints) {
		RowConstraints[] constraintsArr = new RowConstraints[constraints.size()];
		int[] heightArr = new int[constraints.size()];
		for(int i = 0 ; i < constraints.size() ; ++i) {
			constraintsArr[i] = constraints.get(i).getKey();
			heightArr[i] = constraints.get(i).getValue();
		}
		
		long desiredTotal = 0;
		KVEntry<Integer, Priority>[] desiredHeights = new KVEntry[constraintsArr.length];
		for(int i = 0 ; i < desiredHeights.length ; ++i) {
			desiredHeights[i] = new KVEntry<>(constraintsArr[i].calculateDesiredHeight((int) heightArr[i], totalHeight), constraintsArr[i].getPriority());
			desiredTotal += (long) desiredHeights[i].getKey();
		}
		if(desiredHeights.length > 1)
			desiredTotal += gap * (desiredHeights.length - 1);
		
		long excess;
		
		for(int i = 0 ; i <= Priority.getRange() ; ++i) {
			excess = desiredTotal - (long) totalHeight;
			if(excess <= 0) break;
			
			Collection<Integer> indices = getIndicesByPriority(desiredHeights, i);
			if(indices.size() == 0) continue;
			long interval = excess / indices.size();
			
			for(int index : indices) {
				desiredTotal -= desiredHeights[index].getKey();
				desiredHeights[index].setKey((int) Math.max(constraintsArr[index].getMinHeight(), desiredHeights[index].getKey() - interval));
				desiredTotal += desiredHeights[index].getKey();
			}
		}
		
		List<KVEntry<Integer, Integer>> heightData = new ArrayList<KVEntry<Integer, Integer>>();
		int coord = 0;
		for(int i = 0 ; i < desiredHeights.length ; ++i) {
			heightData.add(new KVEntry<>(coord, coord + desiredHeights[i].getKey()));
			coord += desiredHeights[i].getKey() + gap;
		}
		
		return heightData;
	}
	
	private static Collection<Integer> getIndicesByPriority(KVEntry<Integer, Priority>[] desiredHeights, int priorityLvl){
		Collection<Integer> indices = new ArrayList<>();
		
		for(int i = 0 ; i < desiredHeights.length ; ++i)
			if(desiredHeights[i].getValue().asInteger() == priorityLvl)
				indices.add(i);
		
		return indices;
	}
	
}
