package studio.celestial.impl;

import java.util.ArrayList;
import celestial.core.SystemInput;
import celestial.core.CEObject;
import celestial.core.ObjectConstraints;
import celestial.core.CEObjectReference;
import celestial.scene.Layer;
import celestial.scene.Scene;
import celestial.vecmath.Vector3f;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import mod.celestial.misc.CameraModifier;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.util.AdvancedSpinner;
import studio.celestial.util.StudioUtil;

public final class PlacementSettingsPanel extends StudioComponentPanel {
	
	public PlacementSettingsPanel(StudioInterface studio) {
		super(studio, "Placement Settings", 10, 8, 12, 12, 12);
	}
	
	private final ObservableList<RootTableEntry> data = FXCollections.observableArrayList();
	private final FilteredList<RootTableEntry> filteredData = new FilteredList<RootTableEntry>(data, (RootTableEntry t) -> true);
	
	private TableView<RootTableEntry> rootTable;
	private AdvancedSpinner normX, normY, normZ, offset;
	private AdvancedSpinner rotX, rotY, rotZ;
	private CheckBox randomRotX, randomRotY, randomRotZ;
	private AdvancedSpinner sclX, sclY, sclZ;
	private TextField objectNameField;
	private RadioButton placeReference;
	
	private CEObject draggingObj = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		final TextField rootTableSearch = new TextField();
		rootTableSearch.setPromptText("Search Root Objects");
		rootTableSearch.textProperty().addListener(obs -> {
			if(rootTableSearch.getText() == null || rootTableSearch.getText().length() == 0) filteredData.setPredicate((RootTableEntry t) -> true);
			else filteredData.setPredicate((RootTableEntry t) -> t.getIdentifier().toLowerCase().contains(rootTableSearch.getText().toLowerCase().trim()));
		});
		super.getPane().add(rootTableSearch, 0, 13, 10, 1);
		
		rootTable = new TableView<RootTableEntry>();
		rootTable.getColumns().addAll(
				StudioUtil.<RootTableEntry, String>generateTableColumn("identifier", "Identifier", 142, 50, 150),
				StudioUtil.<RootTableEntry, String>generateTableColumn("hashcode", "Hashcode", 60, 100, 150),
				StudioUtil.<RootTableEntry, String>generateTableColumn("position", "Position", 110, 50, 150));
		rootTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
			if(placeReference.isSelected() && newV != null) {
				objectNameField.setText(newV.object.getIdentifier() + ".000");
				rotX.set(newV.object.getBaseRotation().x);
				rotY.set(newV.object.getBaseRotation().y);
				rotZ.set(newV.object.getBaseRotation().z);
				sclX.set(newV.object.getBaseScale().x);
				sclY.set(newV.object.getBaseScale().y);
				sclZ.set(newV.object.getBaseScale().z);
				randomRotX.setSelected(false);
				randomRotY.setSelected(false);
				randomRotZ.setSelected(false);
			}
		});
		rootTable.getSortOrder().addAll(rootTable.getColumns());
		rootTable.setItems(filteredData);
		super.getPane().add(rootTable, 0, 14, 10, 6);
		
		Label title = new Label("Placement Configurations");
		title.setFont(new Font("Ariel", 13));
		super.getPane().add(title, 0, 0, 10, 1);
		
		super.getPane().add(new Label("Object Type"), 0, 6, 4, 1);
		ToggleGroup grp0 = new ToggleGroup();
		RadioButton grp0obj = new RadioButton("New Object");
		grp0obj.setSelected(true);
		grp0obj.setToggleGroup(grp0);
		placeReference = new RadioButton("Object Reference");
		placeReference.selectedProperty().addListener((obs, oldV, newV) -> {
			if(newV) {
				RootTableEntry item = rootTable.getSelectionModel().getSelectedItem();
				rootTable.getSelectionModel().select(null);
				rootTable.getSelectionModel().select(item);
			}
		});
		placeReference.setToggleGroup(grp0);
		super.getPane().add(grp0obj, 1, 7, 3, 1);
		super.getPane().add(placeReference, 4, 7, 6, 1);
		
		super.getPane().add(new Label("Placement Type"), 0, 8, 4, 1);
		ToggleGroup grp1 = new ToggleGroup();
		RadioButton grp1plane = new RadioButton("Place on Plane");
		grp1plane.setSelected(true);
		grp1plane.setToggleGroup(grp1);
		RadioButton grp1dynamic = new RadioButton("Place on Heightmap");
		grp1dynamic.setToggleGroup(grp1);
		super.getPane().add(grp1plane, 1, 9, 3, 1);
		super.getPane().add(grp1dynamic, 4, 9, 6, 1);
		
		objectNameField = new TextField();
		objectNameField.setPromptText("Object Name");
		super.getPane().add(new Label("Object Name"), 0, 1, 4, 1);
		super.getPane().add(objectNameField, 4, 1, 6, 1);
		
		super.getPane().add(new Label("Placement Plane Normal"), 0, 10, 4, 1);
		normX = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0, 0.1f, "Plane Normal X");
		super.getPane().add(normX.getFXSpinner(), 1, 11, 3, 1);
		normY = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1, 0.1f, "Plane Normal Y");
		super.getPane().add(normY.getFXSpinner(), 4, 11, 3, 1);
		normZ = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0, 0.1f, "Plane Normal Z");
		super.getPane().add(normZ.getFXSpinner(), 7, 11, 3, 1);
		
		super.getPane().add(new Label("Placement Plane Offset"), 0, 12, 4, 1);
		offset = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0, 1f, "Plane Offset");
		super.getPane().add(offset.getFXSpinner(), 7, 12, 3, 1);
		
		super.getPane().add(new Label("Euler Rotation"), 0, 2, 4, 1);
		rotX = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0, 1f, "Rotation X");
		super.getPane().add(rotX.getFXSpinner(), 1, 3, 3, 1);
		rotY = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0, 1f, "Rotation Y");
		super.getPane().add(rotY.getFXSpinner(), 4, 3, 3, 1);
		rotZ = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 0, 1f, "Rotation Z");
		super.getPane().add(rotZ.getFXSpinner(), 7, 3, 3, 1);
		
		randomRotX = new CheckBox();
		randomRotX.setTooltip(new Tooltip("Randomize Rotation X"));
		super.getPane().add(randomRotX, 3, 2, 1, 1);
		randomRotY = new CheckBox();
		randomRotY.setTooltip(new Tooltip("Randomize Rotation Y"));
		super.getPane().add(randomRotY, 6, 2, 1, 1);
		randomRotZ = new CheckBox();
		randomRotZ.setTooltip(new Tooltip("Randomize Rotation Z"));
		super.getPane().add(randomRotZ, 9, 2, 1, 1);
		
		super.getPane().add(new Label("Scale"), 0, 4, 4, 1);
		sclX = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1, 0.1f, "Scale X");
		super.getPane().add(sclX.getFXSpinner(), 1, 5, 3, 1);
		sclY = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1, 0.1f, "Scale Y");
		super.getPane().add(sclY.getFXSpinner(), 4, 5, 3, 1);
		sclZ = new AdvancedSpinner(-Float.MAX_VALUE, Float.MAX_VALUE, 1, 0.1f, "Scale Z");
		super.getPane().add(sclZ.getFXSpinner(), 7, 5, 3, 1);
	}
	
	@Override
	protected void update() {
		ArrayList<RootTableEntry> objects = new ArrayList<RootTableEntry>();
		for(Scene scene : GLRequestSystem.getSceneManager().getScenes())
			for(Layer layer : scene.getLayers())
				if(!layer.getIdentifier().endsWith(".studio"))
					for(CEObject obj : layer.getObjects())
						if(!(obj instanceof CEObjectReference))
							objects.add(new RootTableEntry(obj, obj.getIdentifier(), "#" + Integer.toHexString(obj.hashCode()), obj.getPosition().toNotation("#.###")));
		for(RootTableEntry entry : new ArrayList<RootTableEntry>(data)) if(!objects.contains(entry)) data.remove(entry);
		for(RootTableEntry obj : objects) if(!data.contains(obj)) data.add(obj);
		for(RootTableEntry entry : data) {
			if(entry.object != null && !entry.identifier.equals(entry.object.getIdentifier())) entry.identifier = entry.object.getIdentifier();
			if(entry.object != null && !entry.position.equals(entry.object.getPosition().toNotation("#.###"))) entry.position = entry.object.getPosition().toNotation("#.###");
			if(entry.object != null && !entry.hashcode.equals("#" + Integer.toHexString(entry.object.hashCode()))) entry.hashcode = "#" + Integer.toHexString(entry.object.hashCode());
		}
		
		if(randomRotX.isSelected()) {
			rotX.getFXSpinner().increment((int) (Math.random() * 6));
			if(rotX.get() > 360) rotX.set(0);
		}
		if(randomRotY.isSelected()) {
			rotY.getFXSpinner().increment((int) (Math.random() * 6));
			if(rotY.get() > 360) rotY.set(0);
		}
		if(randomRotZ.isSelected()) {
			rotZ.getFXSpinner().increment((int) (Math.random() * 6));
			if(rotZ.get() > 360) rotZ.set(0);
		}
		
		GLRequestSystem.request("ObjectMode.u00", () -> {
			if(normX.get() == 0 && normY.get() == 0 && normZ.get() == 0) normY.set(1);
			Vector3f norm = new Vector3f(normX.get(), normY.get(), normZ.get()).normalize();
			if(norm.x == 0 && norm.y == 0 && norm.z == 0) norm.y = 1;
			GLRequestSystem.getSceneManager().getCurrentScene().getLayer(".studio").getObject("plane.studio").setPosition(GLRequestSystem.getSceneManager().
					getCurrentScene().getLayer("X.studio").getObject("camera.studio").closestPointOnPlane(normX.get(), normY.get(), normZ.get(), offset.get(), 400));
			GLRequestSystem.getSceneManager().getCurrentScene().getLayer(".studio").getObject("plane.studio").setRotation(new Vector3f
					((float) -Math.toDegrees(Math.asin(norm.y)) + 90, (float) -Math.toDegrees(Math.atan2(norm.z, norm.x)) + 90, 0)); // it's magic
			
			if(!objectNameField.isFocused() && (objectNameField.getText().endsWith(".studio") || GLRequestSystem
					.getSceneManager().getObject(objectNameField.getText()) != null || objectNameField.getText().length() == 0)) {
				if(placeReference.isSelected() && rootTable.getSelectionModel().getSelectedItem() != null)
					StudioInterface.getInstantiation().request(s -> objectNameField.setText(GLRequestSystem.getSceneManager()
							.nextObjectIdentifier(rootTable.getSelectionModel().getSelectedItem().object.getIdentifier())));
				else StudioInterface.getInstantiation().request(s -> objectNameField.setText(GLRequestSystem.getSceneManager().nextObjectIdentifier("Object")));
			}
			if(SystemInput.isButtonPressed(0)) {
				Vector3f intersection = calcIntersection(norm);
				if(intersection != null && GLRequestSystem.getSceneManager().getObject(objectNameField.getText()) == null) {
					if(placeReference.isSelected()) {
						RootTableEntry root = rootTable.getSelectionModel().getSelectedItem();
						if(root != null) {
							CEObject obj = new CEObjectReference(objectNameField.getText(), intersection, new Vector3f
									(rotX.get(), rotY.get(), rotZ.get()), new Vector3f(sclX.get(), sclY.get(), sclZ.get()), root.object);
							GLRequestSystem.getSceneManager().addObject(obj);
							GLRequestSystem.getSceneManager().clearAndSelect(obj);
							draggingObj = obj;
						}
					}
					else {
						CEObject obj = new CEObject(objectNameField.getText(), intersection, new Vector3f(rotX.get(),
								rotY.get(), rotZ.get()), new Vector3f(sclX.get(), sclY.get(), sclZ.get()), new ObjectConstraints(5, 1000));
						GLRequestSystem.getSceneManager().addObject(obj);
						GLRequestSystem.getSceneManager().clearAndSelect(obj);
						draggingObj = obj;
					}
				}
			}
			if(draggingObj != null) {
				if(!SystemInput.isButtonDown(0))
					draggingObj = null;
				else {
					Vector3f intersection = calcIntersection(norm);
					if(intersection != null)
						draggingObj.setPosition(intersection);
				}
			}
		});
	}
	
	private Vector3f calcIntersection(Vector3f norm) {
		return GLRequestSystem.getSceneManager().getCurrentScene().getLayer("X.studio").getObject("camera.studio")
				.getModifier(CameraModifier.class).getMousePicker().getIntersection(norm.x, norm.y, norm.z, offset.get());
	}
	
	public static final class RootTableEntry {
		
		private CEObject object;
		private String identifier;
		private String hashcode;
		private String position;
		
		public RootTableEntry(CEObject object, String identifier, String hashcode, String position) {
			this.object = object;
			this.identifier = identifier;
			this.hashcode = hashcode;
			this.position = position;
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
		
		public String getHashcode() {
			return hashcode;
		}
		
		public void setHashcode(String hashcode) {
			this.hashcode = hashcode;
		}
		
		public String getPosition() {
			return position;
		}
		
		public void setPosition(String position) {
			this.position = position;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof RootTableEntry)) return false;
			RootTableEntry r = (RootTableEntry) o;
			return r.object == object && r.identifier.equals(identifier) && r.position.equals(position) && r.hashcode.equals(hashcode);
		}
		
	}
	
}
