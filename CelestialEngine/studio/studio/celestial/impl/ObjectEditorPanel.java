package studio.celestial.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import celestial.ctrl.PropertyController.BoundsInfo;
import celestial.beans.property.CompoundProperty;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.modifier.ModifierEditor;
import studio.celestial.modifier.ModifierProperty;
import studio.celestial.modifier.StudioModifier;

public final class ObjectEditorPanel extends StudioComponentPanel {
	
	public ObjectEditorPanel(StudioInterface studio) {
		super(studio, "Object Editor", 5, 5, 0, 5, 0);
	}
	
	private final LinkedHashMap<StudioModifier, TreeItem<HBox>> modifiers = new LinkedHashMap<StudioModifier, TreeItem<HBox>>();
	
	private Label objectTitle;
	private TreeItem<HBox> objectEditor;
	private TreeView<HBox> objectEditorView;
	
	@Override
	protected void initialize() {
		objectTitle = new Label("No Object Selected");
		objectTitle.setAlignment(Pos.CENTER);
		objectTitle.setMaxWidth(Double.MAX_VALUE);
		super.getPane().add(objectTitle, 0, 0, 16, 1);
		
		objectEditor = new TreeItem<HBox>();
		objectEditorView = new TreeView<HBox>();
		objectEditorView.setRoot(objectEditor);
		objectEditorView.setShowRoot(false);
		objectEditorView.setFixedCellSize(25);
		super.getPane().add(objectEditorView, 0, 1, 16, 32);
	}
	
	private int selectionCount;
	
	@Override
	protected void update() {
		objectEditorView.getSelectionModel().select(null);
		if(selectionCount != GLRequestSystem.selectedObjects().size()) {
			modifiers.clear();
			objectEditor.getChildren().clear();
		}
		
		CEObject selectedObject = GLRequestSystem.selectedObjects().size() == 1 ? GLRequestSystem.selectedObjects().get(0) : null;
		selectionCount = GLRequestSystem.selectedObjects().size();
		
		if(selectedObject != null) {
			objectTitle.setText(selectedObject.getIdentifier() + " - Modifier Settings");
			for(Modifier nativeModifier : selectedObject.getModifiers()) {
				if(!isLoaded(nativeModifier)) {
					StudioModifier modifier = new StudioModifier(nativeModifier);
					if(modifier.getEditor() != null) {
						ModifierEditor editor = modifier.getEditor();
						
						TreeItem<HBox> item = new TreeItem<HBox>(editor.getHeader());
						item.setExpanded(true);
						item.getValue().setUserData(nativeModifier);
						for(ModifierProperty property : editor.getProperties()) {
							TreeItem<HBox> subItem = new TreeItem<HBox>(property.getPane());
							item.getChildren().add(subItem);
						}
						objectEditor.getChildren().add(item);
						modifiers.put(modifier, item);
					}
				}
			}
			for(StudioModifier mod : new ArrayList<StudioModifier>(modifiers.keySet())) {
				if(!selectedObject.getModifiers().contains(mod.getModifiers().get(0))) {
					objectEditor.getChildren().remove(modifiers.get(mod));
					modifiers.remove(mod);
				}
			}
			
			ArrayList<TreeItem<HBox>> tmpList = new ArrayList<TreeItem<HBox>>(objectEditor.getChildren());
			Collections.sort(tmpList, Comparator.comparing(item -> selectedObject.getModifiers().indexOf((Modifier) item.getValue().getUserData())));
			if(tmpList.size() == objectEditor.getChildren().size())
				for(int i = 0 ; i < tmpList.size() ; ++i)
					if(tmpList.get(i) != objectEditor.getChildren().get(i))
						objectEditor.getChildren().set(i, tmpList.get(i));
			// Sorts modifier list into the order of modifiers in the actual object
		}
		else if(GLRequestSystem.selectedObjects().size() > 1) {
			objectTitle.setText(GLRequestSystem.selectedObjects().size() + " Objects Selected");
			ArrayList<ArrayList<Modifier>> sharedModifiers = getSharedModifiers(GLRequestSystem.selectedObjects());
			
			for(ArrayList<Modifier> modRep : sharedModifiers) {
				if(!isLoaded(modRep)) {
					StudioModifier modifier = new StudioModifier(modRep, createCompoundController(modRep));
					if(modifier.getEditor() != null) {
						ModifierEditor editor = modifier.getEditor();
						
						TreeItem<HBox> item = new TreeItem<HBox>(editor.getHeader());
						item.setExpanded(true);
						for(ModifierProperty property : editor.getProperties()) {
							TreeItem<HBox> subItem = new TreeItem<HBox>(property.getPane());
							item.getChildren().add(subItem);
						}
						objectEditor.getChildren().add(item);
						modifiers.put(modifier, item);
					}
				}
			}
			
			for(StudioModifier mod : new ArrayList<StudioModifier>(modifiers.keySet())) {
				if(!sharedModifiers.contains(mod.getModifiers())) {
					objectEditor.getChildren().remove(modifiers.get(mod));
					modifiers.remove(mod);
				}
			}
		}
		else {
			objectTitle.setText("No Object Selected");
			objectEditor.getChildren().clear();
			modifiers.clear();
		}
		
		for(StudioModifier modifier : modifiers.keySet()) modifier.update();
	}
	
	public void deactivateAll() {
		for(StudioModifier modifier : modifiers.keySet())
			modifier.deactivateAll();
	}
	
	public void validateBounds() {
		for(StudioModifier mod : modifiers.keySet()) mod.getCtrl().validateBounds();
	}
	
	private boolean isLoaded(Modifier modifier) {
		for(StudioModifier mod : modifiers.keySet()) if(mod.getModifiers().get(0).equals(modifier)) return true;
		return false;
	}
	
	private boolean isLoaded(ArrayList<Modifier> modifiers) {
		for(StudioModifier mod : this.modifiers.keySet()) if(mod.getModifiers().equals(modifiers)) return true;
		return false;
	}
	
	private ArrayList<ArrayList<Modifier>> getSharedModifiers(ArrayList<CEObject> objects) {
		ArrayList<Class<? extends Modifier>> types = new ArrayList<Class<? extends Modifier>>();
		for(Modifier mod : objects.get(0).getModifiers()) if(!types.contains(mod.getClass())) types.add(mod.getClass());
		for(int i = 1 ; i < objects.size() ; ++i) {
			ArrayList<Class<? extends Modifier>> retainTypes = new ArrayList<Class<? extends Modifier>>();
			for(Modifier mod : objects.get(i).getModifiers()) if(!retainTypes.contains(mod.getClass())) retainTypes.add(mod.getClass());
			types.retainAll(retainTypes);
		}
		
		ArrayList<ArrayList<Modifier>> shared = new ArrayList<ArrayList<Modifier>>();
		for(Class<? extends Modifier> type : types) {
			ArrayList<Modifier> modList = new ArrayList<Modifier>();
			for(CEObject obj : objects) for(Modifier mod : obj.getModifiers()) if(type.equals(mod.getClass())) modList.add(mod);
			if(modList.size() > 0) shared.add(modList);
		}
		
		return shared;
	}
	
	private PropertyController createCompoundController(ArrayList<Modifier> mods) {
		ArrayList<PropertyController> ctrls = new ArrayList<PropertyController>();
		for(Modifier mod : mods) ctrls.add(mod.getPropertyController(GLRequestSystem.getSceneManager()));
		
		PropertyController ctrl = ctrls.get(0);
		ctrls.remove(0);
		
		PropertyController outCtrl = new PropertyController();
		
		for(String propID : ctrl.getProperties().keySet()) {
			CompoundProperty<?> newProp = new CompoundProperty<>(ctrl.getProperties().get(propID));
			for(PropertyController _ctrl : ctrls) {
				Property<?> _prop = _ctrl.getProperty(propID);
				if(_prop != null) newProp.addProperty(_prop);
			}
			outCtrl.withProperty(propID, newProp);
			
			for(BoundsInfo info : ctrl.getBoundsInfo())
				if(info.getLocation() == outCtrl.getProperties().size())
					outCtrl.withPropertyBounds(info.getBounds().duplicate(), info.getSize());
		}
		
		return outCtrl;
	}
	
}
