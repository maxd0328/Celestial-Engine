package studio.celestial.modifier;

import java.util.ArrayList;
import java.util.List;
import celestial.beans.property.CompoundProperty;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.beans.property.SelectiveProperty.PropertySelection;
import celestial.data.GLData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import studio.celestial.binding.DirectBinding;
import studio.celestial.binding.PropertyBinding;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageDialog.MessageType;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.impl.MediaLibraryPanel;
import studio.celestial.media.Media;
import studio.celestial.media.Media.MediaType;
import studio.celestial.media.Resource;
import studio.celestial.util.AdvancedIntegerSpinner;
import studio.celestial.util.AdvancedSpinner;

public abstract class ModifierProperty {
	
	private final String name;
	private final HBox pane;
	protected final Label title;
	
	private final ArrayList<PropertyBinding<?>> bindings;
	
	public ModifierProperty(String name) {
		this.name = name;
		this.pane = new HBox();
		this.title = new Label(name);
		this.bindings = new ArrayList<PropertyBinding<?>>();
		
		this.pane.setSpacing(0);
		this.pane.setAlignment(Pos.CENTER_LEFT);
		this.pane.setPadding(new Insets(0));
		
		Region region = new Region();
		HBox.setHgrow(region, Priority.ALWAYS);
		
		this.pane.getChildren().addAll(title, region);
	}
	
	public String getName() {
		return name;
	}
	
	public HBox getPane() {
		return pane;
	}
	
	public ArrayList<PropertyBinding<?>> getBindings() {
		return new ArrayList<PropertyBinding<?>>(bindings);
	}
	
	public void update() {
		for(PropertyBinding<?> binding : bindings) binding.update();
	}
	
	protected void addBinding(PropertyBinding<?> binding) {
		this.bindings.add(binding);
	}
	
	protected void addDriverAction(Property<?> property, String identifier) {
		if(property.getDriver() != null) {
			ImageView driverView = new ImageView(StudioViewRepository.IMAGE_DRIVER);
			driverView.setFitWidth(10);
			driverView.setFitHeight(10);
			Button driver = new Button();
			driver.setGraphic(driverView);
			driver.setId("invisible-button");
			driver.setPadding(new Insets(0, 5, 0, 5));
			driver.setTooltip(new Tooltip("Edit Driver"));
			addDriverAction(driver, property, identifier);
			getPane().getChildren().add(driver);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addDriverAction(CompoundProperty<?> property, String identifier) {
		if(property.getDriver() != null) {
			ImageView driverView = new ImageView(StudioViewRepository.IMAGE_DRIVER);
			driverView.setFitWidth(10);
			driverView.setFitHeight(10);
			Button driver = new Button();
			driver.setGraphic(driverView);
			driver.setId("invisible-button");
			driver.setPadding(new Insets(0, 5, 0, 5));
			driver.setTooltip(new Tooltip("Edit Driver"));
			addDriverAction(driver, (List<Property<?>>) (List<? super Property>) property.getProperties(), identifier); // Cheat way to cast <Object> to <?>
			getPane().getChildren().add(driver);
		}
	}
	
	protected void addDriverAction(Button btn, Property<?> property, String identifier) {
		ArrayList<Property<?>> properties = new ArrayList<Property<?>>();
		properties.add(property);
		addDriverAction(btn, properties, identifier);
	}
	
	private void addDriverAction(Button btn, List<Property<?>> properties, String identifier) {
		if(properties.size() == 0) return;
		
		btn.setOnAction((event) -> GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().getDriverSystem().open(identifier, properties)));
		this.addBinding(new DirectBinding<Boolean>(() -> btn.getId().equals("highlight-button"), s -> btn.setId(s ? "highlight-button" : "invisible-button"),
				() -> GLRequestSystem.getSceneManager().getDriverSystem().getCurrentDriverSubjects().contains(properties.get(0)), s -> {}));
	}
	
	public abstract void deactivate();
	
	public static final class FloatProperty extends ModifierProperty {
		
		private final Property<Float> property;
		
		public FloatProperty(String identifier, Property<Float> property, float min, float max, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			AdvancedSpinner spinner = new AdvancedSpinner(min, max, property.getBase(), 0.1f, super.getName() + " Property Value");
			super.addBinding(new DirectBinding<Float>(() -> spinner.get(), s -> spinner.set(s), () -> property.getBase(), s -> property.set(s)));
			
			super.getPane().getChildren().add(spinner.getFXSpinner());
		}
		
		public Property<Float> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
	public static final class IntegerProperty extends ModifierProperty {
		
		private final Property<Integer> property;
		
		public IntegerProperty(String identifier, Property<Integer> property, int min, int max, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			AdvancedIntegerSpinner spinner = new AdvancedIntegerSpinner(min, max, property.getBase(), 1, super.getName() + " Property Value");
			super.addBinding(new DirectBinding<Integer>(() -> spinner.get(), s -> spinner.set(s), () -> property.getBase(), s -> property.set(s)));
			
			super.getPane().getChildren().add(spinner.getFXSpinner());
		}
		
		public Property<Integer> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
	public static final class BooleanProperty extends ModifierProperty {
		
		private final Property<Boolean> property;
		
		public BooleanProperty(String identifier, Property<Boolean> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			CheckBox box = new CheckBox();
			box.setSelected(property.getBase());
			super.addBinding(new DirectBinding<Boolean>(() -> box.isSelected(), s -> box.setSelected(s), () -> property.getBase(), s -> property.set(s)));
			
			super.getPane().getChildren().add(box);
		}
		
		public Property<Boolean> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
	public static final class StringProperty extends ModifierProperty {
		
		private final Property<String> property;
		private String stringValue;
		
		public StringProperty(String identifier, Property<String> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			final TextField field = new TextField();
			field.setText(property.getBase());
			field.setOnAction((event) -> stringValue = field.getText());
			field.focusedProperty().addListener((obs, oldV, newV) -> { if(!newV) stringValue = field.getText(); });
			stringValue = property.getBase();
			super.addBinding(new DirectBinding<String>(() -> stringValue, s -> { field.setText(s); stringValue = s; }, () -> property.getBase(), s -> property.set(s)));
			
			super.getPane().getChildren().add(field);
		}
		
		public Property<String> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
	public static final class ChoiceProperty<T> extends ModifierProperty {
		
		private final SelectiveProperty<T> property;
		
		public ChoiceProperty(String identifier, SelectiveProperty<T> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			final ComboBox<PropertySelection<T>> box = new ComboBox<PropertySelection<T>>();
			box.setMinWidth(150);
			box.setPadding(new Insets(-1));
			box.getItems().addAll(property.getSelections());
			select(box, property.getBase());
			super.addBinding(new DirectBinding<T>(() -> box.getSelectionModel().getSelectedItem().getValue(), s -> select(box, s), () -> property.getBase(), s -> property.set(s)));
			
			super.getPane().getChildren().add(box);
		}
		
		public SelectiveProperty<T> getProperty() {
			return property;
		}
		
		private void select(ComboBox<PropertySelection<T>> box, T value) {
			for(PropertySelection<T> selection : box.getItems()) {
				if((selection.getValue() == null && value == null) || (selection.getValue() != null && selection.getValue().equals(value))) box.getSelectionModel().select(selection);
			}
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
	public static final class EditableMediaProperty extends ModifierProperty {
		
		private final Property<GLData[]> property;
		private Media media;
		
		private final Label mediaID;
		
		public EditableMediaProperty(String identifier, Property<GLData[]> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			mediaID = new Label("(No Media)  ");
			mediaID.setId("bad-text");
			updateMediaText();
			
			ImageView openView = new ImageView(StudioViewRepository.IMAGE_FOLDER);
			openView.setFitWidth(18);
			openView.setFitHeight(18);
			Button open = new Button();
			open.setGraphic(openView);
			open.setId("invisible-button");
			open.setPadding(new Insets(0, 5, 0, 5));
			open.setTooltip(new Tooltip("Select Media"));
			open.setOnAction(event -> {
				if(!(property.getUserPointer() instanceof MediaType[])) return;
				Resource resource = MessageHandler.showAndWait(new MediaLibraryPanel(StudioInterface.getInstantiation()));
				if(resource != null) {
					if(!(resource instanceof Media) || !isValidType(((Media) resource).getType()))
						MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "Invalid media type"));
					else {
						Media media = (Media) resource;
						property.set(media.getMediaSource().read());
					}
				}
			});
			
			super.getPane().getChildren().addAll(mediaID, open);
		}
		
		public Property<?> getProperty() {
			return property;
		}
		
		public Media getMedia() {
			return media;
		}
		
		@Override
		public void update() {
			super.update();
			updateMediaText();
		}
		
		private String getMediaIdentifier() {
			GLData[] data = property.getBase();
			if(data != null && data.length > 0 && data[0] != null && data[0].getUserPointer() instanceof String) return (String) data[0].getUserPointer();
			else return null;
		}
		
		private boolean isValidType(MediaType type) {
			for(MediaType mediaType : (MediaType[]) property.getUserPointer()) if(mediaType == type) return true;
			return false;
		}
		
		private void updateMediaText() {
			if(getMediaIdentifier() != null) {
				mediaID.setText(getMediaIdentifier() + "  ");
				mediaID.setId(null);
			}
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
	public static final class CompoundFloatProperty extends ModifierProperty {
		
		private final CompoundProperty<Float> property;
		private boolean active = false;
		
		public CompoundFloatProperty(String identifier, CompoundProperty<Float> property, float min, float max, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			AdvancedSpinner spinner = new AdvancedSpinner(min, max, property.getBase(), 0.1f, super.getName() + " Property Value");
			spinner.getFXSpinner().valueProperty().addListener((obs, oldV, newV) -> {
				active = true;
				super.title.setId("highlight-text");
			});
			super.addBinding(new DirectBinding<Float>(() -> spinner.get(), s -> {}, () -> property.getBase(), s -> { if(active) property.set(s); }));
			
			super.getPane().getChildren().addAll(spinner.getFXSpinner());
		}
		
		public CompoundProperty<Float> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
			active = false;
			super.title.setId(null);
		}
		
	}
	
	public static final class CompoundIntegerProperty extends ModifierProperty {
		
		private final CompoundProperty<Integer> property;
		private boolean active = false;
		
		public CompoundIntegerProperty(String identifier, CompoundProperty<Integer> property, int min, int max, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			AdvancedIntegerSpinner spinner = new AdvancedIntegerSpinner(min, max, property.getBase(), 1, super.getName() + " Property Value");
			spinner.getFXSpinner().valueProperty().addListener((obs, oldV, newV) -> {
				active = true;
				super.title.setId("highlight-text");
			});
			super.addBinding(new DirectBinding<Integer>(() -> spinner.get(), s -> {}, () -> property.getBase(), s -> { if(active) property.set(s); }));
			
			super.getPane().getChildren().addAll(spinner.getFXSpinner());
		}
		
		public CompoundProperty<Integer> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
			active = false;
			super.title.setId(null);
		}
		
	}
	
	public static final class CompoundBooleanProperty extends ModifierProperty {
		
		private final CompoundProperty<Boolean> property;
		private boolean active = false;
		
		public CompoundBooleanProperty(String identifier, CompoundProperty<Boolean> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			CheckBox box = new CheckBox();
			box.selectedProperty().addListener((obs, oldV, newV) -> {
				active = true;
				super.title.setId("highlight-text");
			});
			super.addBinding(new DirectBinding<Boolean>(() -> box.isSelected(), s -> {}, () -> property.getBase(), s -> { if(active) property.set(s); }));
			
			super.getPane().getChildren().addAll(box);
		}
		
		public CompoundProperty<Boolean> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
			active = false;
			super.title.setId(null);
		}
		
	}
	
	public static final class CompoundStringProperty extends ModifierProperty {
		
		private final CompoundProperty<String> property;
		private String stringValue;
		private boolean active = false;
		
		public CompoundStringProperty(String identifier, CompoundProperty<String> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			final TextField field = new TextField();
			field.setText(property.getBase());
			field.setOnAction((event) -> stringValue = field.getText());
			field.focusedProperty().addListener((obs, oldV, newV) -> { if(!newV) stringValue = field.getText(); });
			stringValue = property.getBase();
			field.textProperty().addListener((obs, oldV, newV) -> {
				active = true;
				super.title.setId("highlight-text");
			});
			super.addBinding(new DirectBinding<String>(() -> stringValue, s -> {}, () -> property.getBase(), s -> { if(active) property.set(s); }));
			
			super.getPane().getChildren().add(field);
		}
		
		public CompoundProperty<String> getProperty() {
			return property;
		}
		
		@Override
		public void deactivate() {
			active = false;
			super.title.setId(null);
		}
		
	}
	
	public static final class CompoundChoiceProperty<T> extends ModifierProperty {
		
		private final CompoundProperty<T> property;
		private boolean active = false;
		
		public CompoundChoiceProperty(String identifier, CompoundProperty<T> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			final ComboBox<PropertySelection<T>> box = new ComboBox<PropertySelection<T>>();
			box.setMinWidth(150);
			box.setPadding(new Insets(-1));
			box.getItems().addAll(((SelectiveProperty<T>) property.getProperties().get(0)).getSelections());
			select(box, property.getBase());
			box.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
				active = true;
				super.title.setId("highlight-text");
			});
			super.addBinding(new DirectBinding<T>(() -> box.getSelectionModel().getSelectedItem().getValue(), s -> {}, () -> property.getBase(), s -> { if(active) property.set(s); }));
			
			super.getPane().getChildren().add(box);
		}
		
		public CompoundProperty<T> getProperty() {
			return property;
		}
		
		private void select(ComboBox<PropertySelection<T>> box, T value) {
			for(PropertySelection<T> selection : box.getItems()) {
				if((selection.getValue() == null && value == null) || (selection.getValue() != null && selection.getValue().equals(value))) box.getSelectionModel().select(selection);
			}
		}
		
		@Override
		public void deactivate() {
			active = false;
			super.title.setId(null);
		}
		
	}
	
	public static final class CompoundEditableMediaProperty extends ModifierProperty {
		
		private final CompoundProperty<GLData[]> property;
		private Media media;
		
		public CompoundEditableMediaProperty(String identifier, CompoundProperty<GLData[]> property, String propertyID) {
			super(identifier);
			this.property = property;
			
			addDriverAction(property, propertyID);
			final Label mediaID = new Label("(No Media)  ");
			mediaID.setId("bad-text");
			ImageView openView = new ImageView(StudioViewRepository.IMAGE_FOLDER);
			openView.setFitWidth(18);
			openView.setFitHeight(18);
			Button open = new Button();
			open.setGraphic(openView);
			open.setId("invisible-button");
			open.setPadding(new Insets(0, 5, 0, 5));
			open.setTooltip(new Tooltip("Select Media"));
			open.setOnAction(event -> {
				if(!(property.getProperties().get(0).getUserPointer() instanceof MediaType[])) return;
				Resource resource = MessageHandler.showAndWait(new MediaLibraryPanel(StudioInterface.getInstantiation()));
				if(resource != null) {
					if(!(resource instanceof Media) || isValidType(((Media) resource).getType()))
						MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "Invalid media type"));
					else {
						Media media = (Media) resource;
						mediaID.setText(media.getName() + "  ");
						mediaID.setId(null);
						property.set(media.getMediaSource().read());
						setMediaIdentifier(media.getName());
						
						super.title.setId("highlight-text");
					}
				}
			});
			
			super.getPane().getChildren().addAll(mediaID, open);
		}
		
		public CompoundProperty<GLData[]> getProperty() {
			return property;
		}
		
		public Media getMedia() {
			return media;
		}
		
		private void setMediaIdentifier(String identifier) {
			for(Property<GLData[]> prop : property.getProperties()) {
				GLData[] data = prop.get();
				if(data != null) for(GLData d : data) d.setUserPointer(identifier);
			}
		}
		
		private boolean isValidType(MediaType type) {
			for(MediaType mediaType : (MediaType[]) property.getProperties().get(0).getUserPointer()) if(mediaType == type) return true;
			return false;
		}
		
		@Override
		public void deactivate() {
		}
		
	}
	
}
