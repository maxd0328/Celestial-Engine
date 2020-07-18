package celestial.ui.geometry;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;

public final class GridConstraints {
	
	private final Property<Integer> gridX, gridY;
	private final Property<Integer> gridWidth, gridHeight;
	private final Property<Alignment> alignment;
	
	public GridConstraints(int gridX, int gridY, int gridWidth, int gridHeight) {
		this.gridX = Properties.createIntegerProperty(gridX);
		this.gridY = Properties.createIntegerProperty(gridY);
		this.gridWidth = Properties.createIntegerProperty(gridWidth);
		this.gridHeight = Properties.createIntegerProperty(gridHeight);
		this.alignment = Properties.createProperty(Alignment.class, Alignment.NONE);
	}
	
	public int getGridX() {
		return gridX.get();
	}
	
	public void setGridX(int gridX) {
		this.gridX.set(gridX);
	}
	
	public Property<Integer> gridXProperty() {
		return gridX;
	}
	
	public int getGridY() {
		return gridY.get();
	}
	
	public void setGridY(int gridY) {
		this.gridY.set(gridY);
	}
	
	public Property<Integer> gridYProperty() {
		return gridY;
	}
	
	public int getGridWidth() {
		return gridWidth.get();
	}
	
	public void setGridWidth(int gridWidth) {
		this.gridWidth.set(gridWidth);
	}
	
	public Property<Integer> gridWidthProperty() {
		return gridWidth;
	}
	
	public int getGridHeight() {
		return gridHeight.get();
	}
	
	public void setGridHeight(int gridHeight) {
		this.gridHeight.set(gridHeight);
	}
	
	public Property<Integer> gridHeightProperty() {
		return gridHeight;
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
	
}
