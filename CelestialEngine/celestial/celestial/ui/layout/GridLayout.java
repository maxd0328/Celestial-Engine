package celestial.ui.layout;

import java.util.ArrayList;
import java.util.List;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.collections.ObservableHashMap;
import celestial.collections.ObservableMap;
import celestial.ui.Component;
import celestial.ui.CompoundComponent;
import celestial.ui.geometry.Alignment;
import celestial.ui.geometry.ColumnConstraints;
import celestial.ui.geometry.GridConstraints;
import celestial.ui.geometry.RowConstraints;
import celestial.util.KVEntry;
import celestial.util.Priority;
import celestial.vecmath.Vector2f;

public final class GridLayout extends Layout<GridConstraints> {
	
	private final ObservableMap<Integer, ColumnConstraints> columnConstraints;
	private final ObservableMap<Integer, RowConstraints> rowConstraints;
	
	private final Property<Integer> hgap;
	private final Property<Integer> vgap;
	private final Property<Alignment> alignment;
	
	private List<KVEntry<Integer, Integer>> widthData = null;
	private List<KVEntry<Integer, Integer>> heightData = null;
	
	public GridLayout() {
		super(() -> new GridConstraints(0, 0, 1, 1));
		this.columnConstraints = new ObservableHashMap<>();
		this.rowConstraints = new ObservableHashMap<>();
		this.hgap = Properties.createIntegerProperty();
		this.vgap = Properties.createIntegerProperty();
		this.alignment = Properties.createProperty(Alignment.class, Alignment.NONE);
	}
	
	public ObservableMap<Integer, ColumnConstraints> getColumnConstraints() {
		return columnConstraints;
	}
	
	public ObservableMap<Integer, RowConstraints> getRowConstraints() {
		return rowConstraints;
	}
	
	public int getHgap() {
		return hgap.get();
	}
	
	public void setHgap(int hgap) {
		this.hgap.set(hgap);
	}
	
	public Property<Integer> hgapProperty() {
		return hgap;
	}
	
	public int getVgap() {
		return vgap.get();
	}
	
	public void setVgap(int vgap) {
		this.vgap.set(vgap);
	}
	
	public Property<Integer> vgapProperty() {
		return vgap;
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
	
	private int minX, minY, maxX, maxY;
	
	@Override
	public synchronized KVEntry<Vector2f, Vector2f> restrainComponent(Component comp, CompoundComponent container) {
		int boundTop = (int) container.getConstrainedPosition().y;
		int boundBottom = (int) container.getConstrainedPosition().y + (int) container.getConstrainedScale().y;
		int boundLeft = (int) container.getConstrainedPosition().x;
		int boundRight = (int) container.getConstrainedPosition().x + (int) container.getConstrainedScale().x;
		
		minX = Integer.MAX_VALUE; minY = Integer.MAX_VALUE; maxX = Integer.MIN_VALUE; maxY = Integer.MIN_VALUE;
		super.getConstraints().values().forEach(s -> minX = Math.min(minX, s.getGridX()));
		super.getConstraints().values().forEach(s -> minY = Math.min(minY, s.getGridY()));
		super.getConstraints().values().forEach(s -> maxX = Math.max(maxX, s.getGridX() + s.getGridWidth() - 1));
		super.getConstraints().values().forEach(s -> maxY = Math.max(maxY, s.getGridY() + s.getGridHeight() - 1));
		
		if(widthData == null) {
			List<KVEntry<ColumnConstraints, Integer>> constraintData = new ArrayList<>();
			for(int i = minX ; i <= maxX ; ++i)
				constraintData.add(new KVEntry<>(getColumnConstraints(i), widthByColumn(i)));
			widthData = ColumnConstraints.calculateWidths((int) container.getConstrainedScale().x, hgap.get(), constraintData);
		}
		
		if(heightData == null) {
			List<KVEntry<RowConstraints, Integer>> constraintData = new ArrayList<>();
			for(int i = minY ; i <= maxY ; ++i)
				constraintData.add(new KVEntry<>(getRowConstraints(i), heightByRow(i)));
			heightData = RowConstraints.calculateHeights((int) container.getConstrainedScale().y, vgap.get(), constraintData);
		}
		
		int highestX = Integer.MIN_VALUE, highestY = Integer.MIN_VALUE;
		for(KVEntry<Integer, Integer> entry : widthData) if(entry.getValue() > highestX) highestX = entry.getValue();
		for(KVEntry<Integer, Integer> entry : heightData) if(entry.getValue() > highestY) highestY = entry.getValue();
		
		int totalWidth = highestX - boundLeft, totalHeight = highestY - boundTop;
		int locationX = boundLeft, locationY = boundTop;
		
		switch(alignment.get().toHoriz()) {
		case CENTER:
			locationX = (boundLeft + boundRight) / 2 - totalWidth / 2;
			break;
		case RIGHT:
			locationX = boundRight - totalWidth;
			break;
		default:
		}
		
		switch(alignment.get().toVertical()) {
		case CENTER:
			locationY = (boundTop + boundBottom) / 2 - totalHeight / 2;
			break;
		case BOTTOM:
			locationY = boundBottom - totalHeight;
			break;
		default:
		}
		
		GridConstraints grid = super.getConstraints().get(comp);
		int loX = grid.getGridX() - minX, hiX = grid.getGridX() + grid.getGridWidth() - 1 - minX;
		int loY = grid.getGridY() - minY, hiY = grid.getGridY() + grid.getGridHeight() - 1 - minY;
		
		KVEntry<Integer, Integer> loColumn = widthData.get(loX), hiColumn = widthData.get(hiX);
		KVEntry<Integer, Integer> loRow = heightData.get(loY), hiRow = heightData.get(hiY);
		return comp.constrainTo(locationX + loColumn.getKey(), locationX + hiColumn.getValue(), locationY + loRow.getKey(), locationY + hiRow.getValue(), grid.getAlignment());
	}
	
	@Override
	public void update() {
		for(int column : columnConstraints.keySet())
			columnConstraints.get(column).update();
		for(int row : rowConstraints.keySet())
			rowConstraints.get(row).update();
		hgap.update();
		vgap.update();
		alignment.update();
		widthData = null;
		heightData = null;
	}
	
	public synchronized int getColumnCount() {
		return getMaxColumn() - getMinColumn();
	}
	
	public synchronized int getRowCount() {
		return getMaxRow() - getMinRow();
	}
	
	public synchronized int getMinColumn() {
		minX = Integer.MAX_VALUE;
		super.getConstraints().values().forEach(s -> minX = Math.min(minX, s.getGridX()));
		return minX;
	}
	
	public synchronized int getMinRow() {
		minY = Integer.MAX_VALUE;
		super.getConstraints().values().forEach(s -> minY = Math.min(minY, s.getGridY()));
		return minY;
	}
	
	public synchronized int getMaxColumn() {
		maxX = Integer.MIN_VALUE;
		super.getConstraints().values().forEach(s -> maxX = Math.max(maxX, s.getGridX() + s.getGridWidth() - 1));
		return maxX;
	}
	
	public synchronized int getMaxRow() {
		maxY = Integer.MIN_VALUE;
		super.getConstraints().values().forEach(s -> maxY = Math.max(maxY, s.getGridY() + s.getGridHeight() - 1));
		return maxY;
	}
	
	private int maxWidth;
	private synchronized int widthByColumn(int column) {
		maxWidth = 0;
		super.getConstraints().forEach((k, v) -> {
			if(v.getGridX() <= column && v.getGridX() + v.getGridWidth() > column && k.getPrefScale().x / v.getGridWidth() > maxWidth)
				maxWidth = (int) (k.getPrefScale().x / v.getGridWidth());
		});
		return maxWidth;
	}
	
	private int maxHeight;
	private synchronized int heightByRow(int row) {
		maxHeight = 0;
		super.getConstraints().forEach((k, v) -> {
			if(v.getGridY() <= row && v.getGridY() + v.getGridHeight() > row && k.getPrefScale().y / v.getGridHeight() > maxHeight)
				maxHeight = (int) (k.getPrefScale().y / v.getGridHeight());
		});
		return maxHeight;
	}
	
	private ColumnConstraints getColumnConstraints(int column) {
		if(columnConstraints.get(column) == null)
			columnConstraints.put(column, new ColumnConstraints(Priority.LOW));
		return columnConstraints.get(column);
	}

	private RowConstraints getRowConstraints(int row) {
		if(rowConstraints.get(row) == null)
			rowConstraints.put(row, new RowConstraints(Priority.LOW));
		return rowConstraints.get(row);
	}
	
}
