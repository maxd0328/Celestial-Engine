package studio.celestial.core;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import studio.celestial.binding.PropertyBinding;

public abstract class StudioComponentPanel {
	
	private final String name;
	private final GridPane pane;
	private boolean isInitialized = false;
	
	private final ArrayList<PropertyBinding<?>> propertyBindings = new ArrayList<PropertyBinding<?>>();
	
	protected StudioComponentPanel(StudioInterface studio, String name, int hgap, int vgap, int insetX, int insetY, int insetBottom) {
		this.name = name;
		this.pane = new GridPane();
		
		this.pane.setAlignment(Pos.TOP_LEFT);
		this.pane.setHgap(hgap);
		this.pane.setVgap(vgap);
		this.pane.setPadding(new Insets(insetY, insetX, insetBottom, insetX));
		this.pane.setId("panel");
		this.pane.setOnMouseClicked(event -> pane.requestFocus());
	}
	
	protected StudioComponentPanel(StudioInterface studio, String name, int hgap, int vgap, int insetX, int insetY) {
		this(studio, name, hgap, vgap, insetX, insetY, 0);
	}
	
	public String getName() {
		return name;
	}
	
	protected GridPane getPane() {
		return pane;
	}
	
	protected <T> void addBinding(PropertyBinding<T> binding) {
		this.propertyBindings.add(binding);
	}
	
	protected <T> void removeBinding(PropertyBinding<T> binding) {
		this.propertyBindings.remove(binding);
	}
	
	public ArrayList<PropertyBinding<?>> getPropertyBindings() {
		return new ArrayList<PropertyBinding<?>>(propertyBindings);
	}
	
	public void componentInitialize() {
		if(isInitialized) return;
		pane.getChildren().clear();
		isInitialized = true;
		initialize();
	}
	
	public void componentUpdate() {
		update();
		for(PropertyBinding<?> binding : propertyBindings) binding.update();
	}
	
	/**
	 * Inheritable
	 */
	public void close() {
	}
	
	private static final int COLLAPSE_X = 2, COLLAPSE_Y = 8;
	
	private boolean collapsedInsetsX = false, collapsedInsetsY = false;
	public void calculateConstraints(StudioInterface studio, int width, int height) {
		if(width > 27 && !collapsedInsetsX && (collapsedInsetsX = true))
			pane.setPadding(new Insets(pane.getPadding().getTop(), pane.getPadding().getRight() - COLLAPSE_X, pane.getPadding().getBottom(), pane.getPadding().getLeft() - COLLAPSE_X));
		else if(width <= 27 && collapsedInsetsX && !(collapsedInsetsX = false))
			pane.setPadding(new Insets(pane.getPadding().getTop(), pane.getPadding().getRight() + COLLAPSE_X, pane.getPadding().getBottom(), pane.getPadding().getLeft() + COLLAPSE_X));
		
		if(height > 20 && !collapsedInsetsY && (collapsedInsetsY = true))
			pane.setPadding(new Insets(pane.getPadding().getTop() - COLLAPSE_Y, pane.getPadding().getRight(), pane.getPadding().getBottom() - COLLAPSE_Y, pane.getPadding().getLeft()));
		else if(height <= 20 && collapsedInsetsY && !(collapsedInsetsY = false))
			pane.setPadding(new Insets(pane.getPadding().getTop() + COLLAPSE_Y, pane.getPadding().getRight(), pane.getPadding().getBottom() + COLLAPSE_Y, pane.getPadding().getLeft()));
		
		ObservableList<ColumnConstraints> colConstraints = pane.getColumnConstraints();
		ObservableList<RowConstraints> rowConstraints = pane.getRowConstraints();
		
		colConstraints.clear();
		rowConstraints.clear();
		
		for(int i = 0 ; i < pane.getColumnCount() ; ++i) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth((1 / (double) pane.getColumnCount()) * 100d);
			colConstraints.add(c);
		}
		for(int i = 0 ; i < pane.getRowCount() ; ++i) {
			RowConstraints c = new RowConstraints();
			c.setPercentHeight((1 / (double) pane.getRowCount()) * 100d);
			rowConstraints.add(c);
		}
	}
	
	protected abstract void initialize();
	
	protected abstract void update();
	
}
