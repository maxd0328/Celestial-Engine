package studio.celestial.impl;

import celestial.beans.driver.Driver;
import celestial.util.Factory;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import studio.celestial.binding.DirectBinding;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.driver.DriverEditorFactory;
import studio.celestial.driver.StudioDriverSystem.DriverSystemState;

public final class DriverSettingsPanel extends StudioComponentPanel {
	
	public DriverSettingsPanel(StudioInterface studio) {
		super(studio, "Driver Settings", 5, 5, 7, 5, 7);
	}
	
	private Label identifier;
	private CheckBox editMultiple;
	private ComboBox<Factory<? extends Driver>> driverSelector;
	
	private final Region region = new Region();
	
	private Node driverEditorComponent = null;
	
	@Override
	protected void initialize() {
		identifier = new Label("  No Active Driver");
		super.getPane().add(identifier, 0, 0, 10, 2);
		
		editMultiple = new CheckBox("Edit Multiple");
		editMultiple.setTooltip(new Tooltip("Select and edit multiple driver interfaces at once."));
		super.getPane().add(editMultiple, 12, 0, 5, 2);
		
		driverSelector = new ComboBox<Factory<? extends Driver>>();
		driverSelector.setConverter(new StringConverter<Factory<? extends Driver>>() {
			
			@Override
			public Factory<? extends Driver> fromString(String str) {
				for(DriverEditorFactory editFactory : GLRequestSystem.getSceneManager().getDriverSystem().getFactories())
					if(str.equals(editFactory.getName())) return editFactory.getDriverFactory();
				return Factory.nullFactory();
			}
			
			@Override
			public String toString(Factory<? extends Driver> factory) {
				for(DriverEditorFactory editFactory : GLRequestSystem.getSceneManager().getDriverSystem().getFactories())
					if(editFactory.getDriverFactory() == factory) return editFactory.getName();
				return "No Driver";
			}
			
		});
		driverSelector.setPromptText("(Select Driver)");
		driverSelector.setTooltip(new Tooltip("Driver Type"));
		driverSelector.setMaxWidth(Double.MAX_VALUE);
		driverSelector.setButtonCell(new ListCell<Factory<? extends Driver>>() {
			@Override
			protected void updateItem(Factory<? extends Driver> item, boolean empty) {
				super.updateItem(item, empty);
				if(empty || item == null) setText("(Select Driver)");
				else setText(driverSelector.getConverter().toString(item));
			}
		});
		
		driverSelector.getItems().add(Factory.nullFactory());
		for(DriverEditorFactory factory : GLRequestSystem.getSceneManager().getDriverSystem().getFactories())
			driverSelector.getItems().add(factory.getDriverFactory());
		
		driverSelector.setOnAction(event -> {
			if(driverSelector.getSelectionModel().getSelectedItem() != null) {
				Driver tmpDriver = driverSelector.getSelectionModel().getSelectedItem().build();
				if(tmpDriver != null && GLRequestSystem.getSceneManager().getDriverSystem().getPrimaryType() != null
						&& tmpDriver.getClass().isAssignableFrom(GLRequestSystem.getSceneManager().getDriverSystem().getPrimaryType()))
					return;
				GLRequestSystem.getSceneManager().getDriverSystem().reinstantiateDrivers(driverSelector.getSelectionModel().getSelectedItem());
			}
		});
		
		super.getPane().add(driverSelector, 17, 0, 7, 2);
		
		removeDriverEditor();
		
		super.addBinding(new DirectBinding<Boolean>(() -> editMultiple.isSelected(), s -> editMultiple.setSelected(s), () -> GLRequestSystem
				.getSceneManager().getDriverSystem().isEditMultiple(), s -> GLRequestSystem.getSceneManager().getDriverSystem().setEditMultiple(s)));
	}
	
	@Override
	protected void update() {
		DriverSystemState state = GLRequestSystem.getSceneManager().getDriverSystem().queryState();
		if(state != DriverSystemState.NO_CHANGE) {
			switch(state) {
			case INACTIVE:
				identifier.setText("  No Active Driver");
				driverSelector.getSelectionModel().select(null);
				driverSelector.setDisable(true);
				
				removeDriverEditor();
				break;
			case EDIT_DISABLED:
				identifier.setText("  " + getIdentifierText());
				driverSelector.getSelectionModel().select(null);
				driverSelector.setDisable(false);
				
				removeDriverEditor();
				break;
			case DRIVER_DISABLED:
				identifier.setText("  " + getIdentifierText());
				driverSelector.getSelectionModel().select(Factory.nullFactory());
				driverSelector.setDisable(false);
				
				removeDriverEditor();
				break;
			case DRIVER_ENABLED:
				identifier.setText("  " + getIdentifierText());
				driverSelector.getSelectionModel().select(GLRequestSystem.getSceneManager().getDriverSystem().getCurrentFactory());
				driverSelector.setDisable(false);
				
				if(driverEditorComponent != null) super.getPane().getChildren().remove(driverEditorComponent);
				driverEditorComponent = GLRequestSystem.getSceneManager().getDriverSystem().getCurrentEditor().toFXComponent();
				addDriverEditor();
				break;
			default: break;
			}
		}
	}
	
	private String getIdentifierText() {
		if(GLRequestSystem.getSceneManager().getDriverSystem().getCurrentDriverSubjects().size() == 0)
			return "No Active Driver";
		else if(GLRequestSystem.getSceneManager().getDriverSystem().getCurrentDriverSubjects().size() > 1)
			return GLRequestSystem.getSceneManager().getDriverSystem().getCurrentDriverSubjects().size() + " Active Drivers";
		else return GLRequestSystem.getSceneManager().getDriverSystem().getDriverSubjectName() + " Driver";
	}
	
	private void removeDriverEditor() {
		if(driverEditorComponent != region) {
			if(driverEditorComponent != null) super.getPane().getChildren().remove(driverEditorComponent);
			driverEditorComponent = region;
			addDriverEditor();
		}
	}
	
	private void addDriverEditor() {
		if(driverEditorComponent != null)
			super.getPane().add(driverEditorComponent, 0, 2, 24, 12);
	}
	
}
