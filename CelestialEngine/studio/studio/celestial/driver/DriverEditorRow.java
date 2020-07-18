package studio.celestial.driver;

import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class DriverEditorRow {
	
	private final ArrayList<DriverEditorComponent> components;
	private int height = -1;
	private Pos alignment = Pos.CENTER_LEFT;
	
	public DriverEditorRow(DriverEditorComponent... components) {
		this.components = new ArrayList<DriverEditorComponent>();
		for(DriverEditorComponent component : components) this.components.add(component);
	}
	
	public ArrayList<DriverEditorComponent> getComponents() {
		return components;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public Pos getAlignment() {
		return alignment;
	}
	
	public void setAlignment(Pos alignment) {
		this.alignment = alignment;
	}
	
	public void update() {
		for(DriverEditorComponent component : components)
			component.update();
	}
	
	public HBox toFXComponent() {
		HBox box = new HBox();
		
		box.setSpacing(0);
		box.setAlignment(alignment);
		box.setPadding(new Insets(0, 0, 0, 4));
		if(height > 0) {
			box.setMaxHeight(height);
			box.setMinHeight(height);
		}
		
		for(DriverEditorComponent comp : components) {
			if(comp.getTitle() != null) {
				Label lbl = new Label(comp.getTitle());
				lbl.setWrapText(true);
				box.getChildren().add(lbl);
			}
			if(comp.isGrow()) {
				Region region = new Region();
				HBox.setHgrow(region, Priority.ALWAYS);
				box.getChildren().add(region);
			}
			if(comp.getNode() != null) box.getChildren().add(comp.getNode());
		}
		
		return box;
	}
	
}
