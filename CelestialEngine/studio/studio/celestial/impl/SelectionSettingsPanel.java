package studio.celestial.impl;

import java.util.ArrayList;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.scene.Layer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import studio.celestial.binding.ConditionalBinding;
import studio.celestial.binding.DeltaBinding;
import studio.celestial.binding.DirectBinding;
import studio.celestial.binding.PropertyBinding;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.dialog.MessageDialog.MessageType;
import studio.celestial.util.AdvancedIntegerSpinner;
import studio.celestial.util.AdvancedSpinner;
import studio.celestial.util.StudioUtil;

public final class SelectionSettingsPanel extends StudioComponentPanel {
	
	public SelectionSettingsPanel(StudioInterface studio) {
		super(studio, "Selection Settings", 5, 4, 12, 12, 12);
	}
	
	private final ObservableList<ParentTableEntry> parents = FXCollections.observableArrayList();
	private final FilteredList<ParentTableEntry> filteredParents = new FilteredList<ParentTableEntry>(parents, (ParentTableEntry t) -> true);
	
	private TextField objectNameField;
	private AdvancedSpinner posX, posY, posZ, rotX, rotY, rotZ, sclX, sclY, sclZ;
	private AdvancedSpinner radius, renderDistance, fboRenderDistance, limRenderDistance;
	private AdvancedIntegerSpinner confNumber;
	private AdvancedSpinner confValue;
	private TableView<ParentTableEntry> parentTable;
	private CheckBox translateOnly;
	
	private String objectName = new String();
	private boolean objectNameFieldActed = false;
	private boolean setVisibility = false;
	private int bindingStart; // Represents first index of pos/rot/scl/etc. bindings
	
	@Override
	protected void initialize() {
		objectNameField = new TextField();
		objectNameField.setPromptText("Object Name");
		objectNameField.setOnAction((event) -> {
			objectNameFieldActed = true;
			objectName = objectNameField.getText();
		});
		objectNameField.focusedProperty().addListener((obs, oldV, newV) -> objectName = objectNameField.getText());
		super.getPane().add(objectNameField, 0, 0, 11, 2);
		
		CheckBox enabled = new CheckBox();
		enabled.setTooltip(new Tooltip("Show Object"));
		enabled.setOnMouseClicked(event -> setVisibility = true);
		super.getPane().add(enabled, 11, 0, 1, 2);
		
		Button deselect = new Button("Deselect");
		deselect.setOnAction((event) -> GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().deselectAll()));
		deselect.setMaxWidth(Double.MAX_VALUE);
		super.getPane().add(deselect, 12, 0, 4, 2);
		Button duplicate = new Button("Duplicate");
		duplicate.setOnAction((event) -> GLRequestSystem.request(() -> {
			ArrayList<CEObject> selObjs = new ArrayList<CEObject>(GLRequestSystem.getSceneManager().getSelectedObjects());
			GLRequestSystem.getSceneManager().deselectAll();
			for(CEObject obj : selObjs) {
				CEObject newObj = new CEObject(obj);
				newObj.setIdentifier(GLRequestSystem.getSceneManager().nextObjectIdentifier(obj.getIdentifier()));
				GLRequestSystem.getSceneManager().addObject(newObj);
				GLRequestSystem.getSceneManager().addSelection(newObj);
			}
		}));
		duplicate.setMaxWidth(Double.MAX_VALUE);
		super.getPane().add(duplicate, 16, 0, 4, 2);
		HBox delete = createGraphicButton(StudioViewRepository.IMAGE_DELETE, false, 14, 18, "Delete Object", (event) -> {
			boolean confirm = MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete object(s)?"));
			if(confirm) GLRequestSystem.request(() -> {
				for(CEObject obj : GLRequestSystem.getSceneManager().getSelectedObjects()) GLRequestSystem.getSceneManager().removeObject(obj);
			});
		});
		super.getPane().add(delete, 20, 0, 2, 2);
		
		Label positionLbl = new Label("Position");
		super.getPane().add(positionLbl, 0, 2, 6, 2);
		Label rotationLbl = new Label("Euler Rotation");
		super.getPane().add(rotationLbl, 7, 2, 6, 2);
		Label scaleLbl = new Label("Scale");
		super.getPane().add(scaleLbl, 14, 2, 6, 2);
		
		super.getPane().add(createDriver(posX = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 0.1f, "Position X"), s -> s.positionProperty().subProperty(0)), 0, 4, 7, 2);
		super.getPane().add(createDriver(posY = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 0.1f, "Position Y"), s -> s.positionProperty().subProperty(1)), 0, 6, 7, 2);
		super.getPane().add(createDriver(posZ = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 0.1f, "Position Z"), s -> s.positionProperty().subProperty(2)), 0, 8, 7, 2);
		
		super.getPane().add(createDriver(rotX = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 1f, "Rotation X"), s -> s.rotationProperty().subProperty(0)), 7, 4, 7, 2);
		super.getPane().add(createDriver(rotY = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 1f, "Rotation Y"), s -> s.rotationProperty().subProperty(1)), 7, 6, 7, 2);
		super.getPane().add(createDriver(rotZ = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f, 1f, "Rotation Z"), s -> s.rotationProperty().subProperty(2)), 7, 8, 7, 2);
		
		super.getPane().add(createDriver(sclX = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1.0f, 0.1f, "Scale X"), s -> s.scaleProperty().subProperty(0)), 14, 4, 7, 2);
		super.getPane().add(createDriver(sclY = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1.0f, 0.1f, "Scale Y"), s -> s.scaleProperty().subProperty(1)), 14, 6, 7, 2);
		super.getPane().add(createDriver(sclZ = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1.0f, 0.1f, "Scale Z"), s -> s.scaleProperty().subProperty(2)), 14, 8, 7, 2);
		
		Label radiusLbl = new Label("Radius");
		super.getPane().add(radiusLbl, 6, 12, 6, 2);
		Label distLbl = new Label("Render Distance");
		super.getPane().add(distLbl, 6, 14, 6, 2);
		Label fboDistLbl = new Label("FBO-Render Distance");
		super.getPane().add(fboDistLbl, 17, 12, 6, 2);
		Label fboLimDistLbl = new Label("Lim-Render Distance");
		super.getPane().add(fboLimDistLbl, 17, 14, 6, 2);
		
		radius = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1.0f, 0.1f, "Object Radius");
		super.getPane().add(radius.getFXSpinner(), 0, 12, 6, 2);
		renderDistance = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1000.0f, 0.1f, "Render Distance");
		super.getPane().add(renderDistance.getFXSpinner(), 0, 14, 6, 2);
		fboRenderDistance = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1000.0f, 0.1f, "FBO Render Distance");
		super.getPane().add(fboRenderDistance.getFXSpinner(), 11, 12, 6, 2);
		limRenderDistance = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1000.0f, 0.1f, "Limited FBO Render Distance");
		super.getPane().add(limRenderDistance.getFXSpinner(), 11, 14, 6, 2);
		
		Label confNumberLbl = new Label("Configuration ID");
		super.getPane().add(confNumberLbl, 24, 12, 5, 2);
		Label confValueLbl = new Label("Configuration Value");
		super.getPane().add(confValueLbl, 29, 12, 6, 2);
		
		confNumber = new AdvancedIntegerSpinner(0, CEObject.CONFIGURATION_COUNT - 1, 0, 1, "Configuration ID");
		confNumber.getFXSpinner().valueProperty().addListener((obs, oldV, newV) -> GLRequestSystem.request(() -> {
			if(GLRequestSystem.getSceneManager().getSelectedObjects().size() > 1) confValue.set(0);
			else if(GLRequestSystem.getSceneManager().getSelectedObjects().size() == 1)
				confValue.set(GLRequestSystem.getSceneManager().getSelectedObjects().get(0).getConfiguration(newV));
		}));
		super.getPane().add(confNumber.getFXSpinner(), 24, 14, 5, 2);
		super.getPane().add(createDriver(confValue = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE,
				0.0f, 0.1f, "Configuration Value"), s -> s.configurationProperty(confNumber.get())), 29, 14, 7, 2);
		
		parentTable = new TableView<ParentTableEntry>();
		parentTable.getColumns().add(StudioUtil.<ParentTableEntry, String>generateTableColumn("identifier", "Set Object Parent", 240));
		parentTable.getSortOrder().addAll(parentTable.getColumns());
		parentTable.setItems(filteredParents);
		super.getPane().add(parentTable, 24, 2, 13, 10);
		
		TextField searchField = new TextField();
		searchField.setPromptText("Search");
		searchField.textProperty().addListener(obs -> {
			if(searchField.getText() == null || searchField.getText().length() == 0) filteredParents.setPredicate((ParentTableEntry t) -> true);
			else filteredParents.setPredicate((ParentTableEntry t) -> t.getIdentifier().toLowerCase().contains(searchField.getText().toLowerCase().trim()));
		});
		super.getPane().add(searchField, 24, 0, 11, 2);
		
		translateOnly = new CheckBox();
		translateOnly.setTooltip(new Tooltip("Parent Translation Only"));
		super.getPane().add(translateOnly, 35, 0, 2, 2);
		
		// Property bindings
		bindingStart = super.getPropertyBindings().size();
		
		// Position
		super.addBinding(new ConditionalBinding<Float>(() -> posX.get(), s -> posX.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBasePosition().x, s -> { for(CEObject obj : sel()) obj.getBasePosition().x = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBasePosition().x += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> posY.get(), s -> posY.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBasePosition().y, s -> { for(CEObject obj : sel()) obj.getBasePosition().y = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBasePosition().y += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> posZ.get(), s -> posZ.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBasePosition().z, s -> { for(CEObject obj : sel()) obj.getBasePosition().z = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBasePosition().z += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		
		// Rotation
		super.addBinding(new ConditionalBinding<Float>(() -> rotX.get(), s -> rotX.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBaseRotation().x, s -> { for(CEObject obj : sel()) obj.getBaseRotation().x = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBaseRotation().x += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> rotY.get(), s -> rotY.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBaseRotation().y, s -> { for(CEObject obj : sel()) obj.getBaseRotation().y = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBaseRotation().y += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> rotZ.get(), s -> rotZ.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBaseRotation().z, s -> { for(CEObject obj : sel()) obj.getBaseRotation().z = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBaseRotation().z += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		
		// Scale
		super.addBinding(new ConditionalBinding<Float>(() -> sclX.get(), s -> sclX.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBaseScale().x, s -> { for(CEObject obj : sel()) obj.getBaseScale().x = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBaseScale().x += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> sclY.get(), s -> sclY.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBaseScale().y, s -> { for(CEObject obj : sel()) obj.getBaseScale().y = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBaseScale().y += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> sclZ.get(), s -> sclZ.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getBaseScale().z, s -> { for(CEObject obj : sel()) obj.getBaseScale().z = s; }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) obj.getBaseScale().z += s; }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		
		// Constraints (I am dearly sorry)
		super.addBinding(new ConditionalBinding<Float>(() -> radius.get(), s -> radius.set(s), // Radius get/set - control side
				() -> sel().size() == 0 ? 0 : sel().get(0).getConstraints().getFrustumRadius(), // Radius get - GL side
				s -> { for(CEObject obj : sel()) obj.getConstraints().setFrustumRadius(s); }, DirectBinding.factory()) // Radius set - GL Side
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) // Delta binding increase setter
				obj.getConstraints().setFrustumRadius(obj.getConstraints().getFrustumRadius() + s); }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> renderDistance.get(), s -> renderDistance.set(s), // Cull distance get/set - control side
				() -> sel().size() == 0 ? 0 : sel().get(0).getConstraints().getCullDistance(), // Cull distance get - GL side
				s -> { for(CEObject obj : sel()) obj.getConstraints().setCullDistance(s); }, DirectBinding.factory()) // Cull distance set - GL side
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) // Delta binding increase setter
				obj.getConstraints().setCullDistance(obj.getConstraints().getCullDistance() + s); }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> fboRenderDistance.get(), s -> fboRenderDistance.set(s), // FBO cull distance get/set - control side
				() -> sel().size() == 0 ? 0 : sel().get(0).getConstraints().getFboCullDistance(), // FBO cull distance get - GL side
				s -> { for(CEObject obj : sel()) obj.getConstraints().setFboCullDistance(s); }, DirectBinding.factory()) // FBO cull distance set - GL side
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) // Delta binding increase setter
				obj.getConstraints().setFboCullDistance(obj.getConstraints().getFboCullDistance() + s); }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		super.addBinding(new ConditionalBinding<Float>(() -> limRenderDistance.get(), s -> limRenderDistance.set(s), // Limited cull distance get/set - control side
				() -> sel().size() == 0 ? 0 : sel().get(0).getConstraints().getLimitedFboCullDistance(), // Limited cull distance get - GL side
				s -> { for(CEObject obj : sel()) obj.getConstraints().setLimitedFboCullDistance(s); }, DirectBinding.factory()) // Limited cull distance set - GL side
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel()) // Delta binding increase setter
				obj.getConstraints().setLimitedFboCullDistance(obj.getConstraints().getLimitedFboCullDistance() + s); }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		
		// Configuration
		super.addBinding(new ConditionalBinding<Float>(() -> confValue.get(), s -> confValue.set(s),
				() -> sel().size() == 0 ? 0 : sel().get(0).getConfiguration(confNumber.get()),
				s -> { for(CEObject obj : sel()) obj.setConfiguration(confNumber.get(), s); }, DirectBinding.factory())
				.when(() -> sel().size() == 1).otherwise(DeltaBinding.factory(s -> { for(CEObject obj : sel())
				obj.setConfiguration(confNumber.get(), obj.getConfiguration(confNumber.get()) + s); }, DeltaBinding.FLOAT_DIFFERENTIATOR)));
		
		// Parents
		super.addBinding(new DirectBinding<CEObject>(() -> parentTable.getSelectionModel().getSelectedItem() == null ? null : parentTable.getSelectionModel().getSelectedItem().object,
				s -> setParent(s), () -> sel().size() == 0 ? null : sel().get(0).getParent(), s -> setObjectParent(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> translateOnly.isSelected(), s -> setTranslateOnly(s),
				() -> sel().size() == 0 ? false : sel().get(0).isTranslateOnly(), s -> setObjectTranslateOnly(s)));
		
		// Object Name
		super.addBinding(new DirectBinding<String>(() -> objectName, s -> objectName = s, () -> sel().size() == 1 ? sel().get(0).getIdentifier() : "", s -> { 
			if(sel().size() == 1) {
				if(GLRequestSystem.getSceneManager().getObject(s) != null) {
					objectName = sel().get(0).getIdentifier();
					StudioInterface.getInstantiation().request(studio -> objectNameField.setText(objectName));
				}
				else sel().get(0).setIdentifier(s);
			}
		}));
		
		// Object Visibility
		super.addBinding(new DirectBinding<Boolean>(() -> enabled.isSelected(), s -> enabled.setSelected(s), () -> GLRequestSystem.selectedObjects().size() == 0 ? false
				: GLRequestSystem.selectedObjects().get(0).isEnabled(), s -> { if(setVisibility) for(CEObject obj : GLRequestSystem.selectedObjects()) obj.setEnabled(s); }));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void update() {
		ArrayList<ParentTableEntry> objects = new ArrayList<ParentTableEntry>();
		objects.add(new ParentTableEntry(null, "No Parent"));
		for(Layer layer : GLRequestSystem.getSceneManager().getCurrentScene().getLayers()) if(!layer.getIdentifier().endsWith(".studio"))
			for(CEObject obj : layer.getObjects()) objects.add(new ParentTableEntry(obj, obj.getIdentifier()));
		for(ParentTableEntry parent : new ArrayList<ParentTableEntry>(parents)) if(!objects.contains(parent)) parents.remove(parent);
		for(ParentTableEntry obj : objects) if(!parents.contains(obj)) parents.add(obj);
		for(ParentTableEntry parent : parents) if(parent.object != null && !parent.identifier.equals(parent.object.getIdentifier())) parent.identifier = parent.object.getIdentifier();
		
		for(Node node : super.getPane().getChildren()) node.setDisable(GLRequestSystem.getSceneManager().getSelectedObjects().size() <= 0);
		if(GLRequestSystem.getSceneManager().getSelectedObjects().size() > 1) {
			objectNameField.setEditable(false);
			objectNameField.setText(GLRequestSystem.getSceneManager().getSelectedObjects().size() + " Objects Selected");
		}
		else if(GLRequestSystem.getSceneManager().getSelectedObjects().size() == 0) objectNameField.setText("No Object Selected");
		else {
			if((!objectNameField.isFocused() || objectNameFieldActed) && !objectNameField.getText().equals(objectName)) objectNameField.setText(objectName);
			objectNameFieldActed = false;
			objectNameField.setEditable(true);
		}
		
		GLRequestSystem.request("SelectionUpdate", () -> {
			if(GLRequestSystem.getSceneManager().isSelectionChanged()) {
				if(GLRequestSystem.getSceneManager().getSelectedObjects().size() > 1) {
					for(int i = bindingStart ; i < bindingStart + 14 ; ++i) ((PropertyBinding<Float>) super.getPropertyBindings().get(i)).set(0f);
					parentTable.getSelectionModel().select(null);
					translateOnly.setSelected(false);
					setVisibility = false;
				}
				else setVisibility = true;
			}
		});
	}
	
	/**
	 * Creates spinner and driver button
	 */
	private HBox createDriver(AdvancedSpinner spinner, DriverReceiver receiver) {
		HBox box = createGraphicButton(StudioViewRepository.IMAGE_DRIVER, true, 10, 10, spinner.getTooltip() + " Driver", (event) -> GLRequestSystem.request(() -> {
			ArrayList<Property<?>> drivers = new ArrayList<Property<?>>();
			for(CEObject obj : sel()) drivers.add(receiver.get(obj));
			GLRequestSystem.getSceneManager().getDriverSystem().open(sel().size() == 0 ? spinner.getTooltip() : sel().get(0).getIdentifier() + " " + spinner.getTooltip(), drivers);
		}), spinner.getFXSpinner());
		this.addBinding(new DirectBinding<Boolean>(() -> box.getChildren().get(1).getId().equals("highlight-button"), s -> box.getChildren().get(1)
				.setId(s ? "highlight-button" : "invisible-button"), () -> GLRequestSystem.getSceneManager().getDriverSystem()
				.getCurrentDriverSubjects().contains(sel().size()  == 0 ? null : receiver.get(sel().get(0))), s -> {}));
		return box;
	}
	
	private HBox createGraphicButton(Image graphic, boolean invisible, int width, int height, String tooltip, EventHandler<ActionEvent> action, Node... components) {
		HBox box = new HBox(0);
		box.setAlignment(Pos.CENTER_LEFT);
		
		ImageView view = new ImageView(graphic);
		view.setFitWidth(width);
		view.setFitHeight(height);
		
		Button btn = new Button();
		btn.setTooltip(new Tooltip(tooltip));
		btn.setGraphic(view);
		if(invisible) btn.setId("invisible-button");
		btn.setOnAction(action);
		
		box.getChildren().addAll(components);
		box.getChildren().add(btn);
		return box;
	}
	
	private ArrayList<CEObject> sel() {
		return GLRequestSystem.selectedObjects();
	}
	
	private void setParent(CEObject parent) {
		if(parentTable.getSelectionModel().getSelectedItem() != null || GLRequestSystem.selectedObjects().size() == 1)
			for(ParentTableEntry entry : parents) if(entry.object == parent) parentTable.getSelectionModel().select(entry);
	}
	
	private void setTranslateOnly(boolean translateOnly) {
		if(parentTable.getSelectionModel().getSelectedItem() != null || GLRequestSystem.selectedObjects().size() == 1)
			this.translateOnly.setSelected(translateOnly);
	}
	
	private void setObjectParent(CEObject parent) {
		if(parentTable.getSelectionModel().getSelectedItem() != null || GLRequestSystem.selectedObjects().size() == 1)
			for(CEObject obj : GLRequestSystem.selectedObjects()) if(obj.getParent() != parent) obj.setParent(parent, obj.isTranslateOnly(), obj.isUprightParent());
	}
	
	private void setObjectTranslateOnly(boolean translateOnly) {
		if(parentTable.getSelectionModel().getSelectedItem() != null || GLRequestSystem.selectedObjects().size() == 1)
			for(CEObject obj : GLRequestSystem.selectedObjects()) if(obj.getParent() != null && obj.isTranslateOnly() != translateOnly) obj.setParent(obj.getParent(), translateOnly, false);
	}
	
	public static final class ParentTableEntry {
		
		private CEObject object;
		private String identifier;
		
		public ParentTableEntry(CEObject object, String identifier) {
			this.object = object;
			this.identifier = identifier;
		}
		
		public CEObject getObject() {
			return object;
		}
		
		public void setObject(CEObject object) {
			this.object = object;
		}
		
		public String getIdentifier() {
			return identifier;
		}
		
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof ParentTableEntry)) return false;
			ParentTableEntry r = (ParentTableEntry) o;
			return r.object == object && r.identifier.equals(identifier);
		}
		
	}
	
	private static interface DriverReceiver {
		
		public Property<?> get(CEObject obj);
		
	}
	
}
