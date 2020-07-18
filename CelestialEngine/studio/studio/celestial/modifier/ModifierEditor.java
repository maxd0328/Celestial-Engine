package studio.celestial.modifier;

import java.util.ArrayList;
import celestial.core.Modifier;
import celestial.beans.property.CompoundProperty;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.PropertyBounds;
import celestial.ctrl.PropertyController;
import celestial.data.GLData;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.error.CelestialGenericException;
import celestial.vecmath.GenericVector;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.dialog.MessageDialog.MessageType;
import studio.celestial.modifier.ModifierProperty.*;

public final class ModifierEditor {
	
	private final StudioModifier mod;
	private final String name;
	private final String description;
	private final HBox header;
	private final ArrayList<ModifierProperty> properties;
	
	public ModifierEditor(StudioModifier mod, String name, String description) {
		this.mod = mod;
		this.name = name;
		this.description = description;
		this.header = createHeader();
		this.properties = new ArrayList<ModifierProperty>();
	}
	
	public StudioModifier getMod() {
		return mod;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public HBox getHeader() {
		return header;
	}
	
	public ArrayList<ModifierProperty> getProperties() {
		return properties;
	}
	
	public void update() {
		for(ModifierProperty property : properties)
			property.update();
	}
	
	public void deactivateAll() {
		for(ModifierProperty property : properties)
			property.deactivate();
	}
	
	@SuppressWarnings("unchecked")
	public void initProperties(PropertyController ctrl, String modID) {
		for(String propID : ctrl.getProperties().keySet()) {
			String driverPropID = modID + " " + propID;
			Property<?> prop = ctrl.getProperties().get(propID);
			PropertyBounds<?> _bounds = ctrl.getBounds(prop);
			
			IntervalBounds<Float> fbounds = _bounds instanceof IntervalBounds && Float.class.isAssignableFrom(_bounds.getType()) ? (IntervalBounds<Float>) _bounds : null;
			float fmin = fbounds == null ? -Float.MAX_VALUE : fbounds.getMinType() == IntervalType.EXCLUSIVE ? fbounds.getMin() + 0.01f : fbounds.getMin();
			float fmax = fbounds == null ? Float.MAX_VALUE : fbounds.getMaxType() == IntervalType.EXCLUSIVE ? fbounds.getMax() - 0.01f : fbounds.getMax();
			
			IntervalBounds<Integer> ibounds = _bounds instanceof IntervalBounds && Integer.class.isAssignableFrom(_bounds.getType()) ? (IntervalBounds<Integer>) _bounds : null;
			int imin = ibounds == null ? Integer.MIN_VALUE : ibounds.getMinType() == IntervalType.EXCLUSIVE ? ibounds.getMin() + 1 : ibounds.getMin();
			int imax = ibounds == null ? Integer.MAX_VALUE : ibounds.getMaxType() == IntervalType.EXCLUSIVE ? ibounds.getMax() - 1 : ibounds.getMax();
			
			if(prop instanceof SelectiveProperty) {
				SelectiveProperty<?> property = (SelectiveProperty<?>) prop;
				
				properties.add(new ChoiceProperty<>(propID, property, driverPropID));
			}
			else if(prop instanceof CompoundProperty) {
				CompoundProperty<?> property = (CompoundProperty<?>) prop;
				
				if(property.getProperties().size() == 0) continue;
				Class<?> type = property.getProperties().get(0).getPropertyType(), propExtensionType = property.getProperties().get(0).getClass();
				
				for(Property<?> testProp : property.getProperties()) if(!type.isAssignableFrom(testProp.getPropertyType()) || !propExtensionType.isAssignableFrom(testProp.getClass()))
					throw new CelestialGenericException("Property not supported");
				
				if(SelectiveProperty.class.isAssignableFrom(propExtensionType)) {
					properties.add(new CompoundChoiceProperty<>(propID, property, driverPropID));
				}
				else {
					if(Float.class.isAssignableFrom(property.getPropertyType()))
						properties.add(new CompoundFloatProperty(propID, (CompoundProperty<Float>) property, fmin, fmax, driverPropID));
					else if(Integer.class.isAssignableFrom(property.getPropertyType()))
						properties.add(new CompoundIntegerProperty(propID, (CompoundProperty<Integer>) property, imin, imax, driverPropID));
					else if(Boolean.class.isAssignableFrom(property.getPropertyType()))
						properties.add(new CompoundBooleanProperty(propID, (CompoundProperty<Boolean>) property, driverPropID));
					else if(String.class.isAssignableFrom(property.getPropertyType()))
						properties.add(new CompoundStringProperty(propID, (CompoundProperty<String>) property, driverPropID));
					else if(GenericVector.class.isAssignableFrom(property.getPropertyType())) {
						for(int i = 0 ; i < ((GenericVector) property.get()).size() ; ++i)
							properties.add(new CompoundFloatProperty(propID + " " + createVectorText(propID, i),
									(CompoundProperty<Float>) property.subProperty(i), fmin, fmax, driverPropID + " " + createVectorText(propID, i)));
					}
					else if(GLData[].class.isAssignableFrom(property.getPropertyType()))
						properties.add(new CompoundEditableMediaProperty(propID, (CompoundProperty<GLData[]>) property, driverPropID));
					else throw new CelestialGenericException("Property not supported");
				}
			}
			else {
				if(Float.class.isAssignableFrom(prop.getPropertyType())) properties.add(new FloatProperty(propID, (Property<Float>) prop, fmin, fmax, driverPropID));
				else if(Integer.class.isAssignableFrom(prop.getPropertyType())) properties.add(new IntegerProperty(propID, (Property<Integer>) prop, imin, imax, driverPropID));
				else if(Boolean.class.isAssignableFrom(prop.getPropertyType())) properties.add(new BooleanProperty(propID, (Property<Boolean>) prop, driverPropID));
				else if(String.class.isAssignableFrom(prop.getPropertyType())) properties.add(new StringProperty(propID, (Property<String>) prop, driverPropID));
				else if(GenericVector.class.isAssignableFrom(prop.getPropertyType())) {
					for(int i = 0 ; i < ((GenericVector) prop.get()).size() ; ++i)
						properties.add(new FloatProperty(propID + " " + createVectorText(propID, i), prop.subProperty(i), fmin, fmax, driverPropID + " " + createVectorText(propID, i)));
				}
				else if(GLData[].class.isAssignableFrom(prop.getPropertyType()))
					properties.add(new EditableMediaProperty(propID, (Property<GLData[]>) prop, driverPropID));
				else throw new CelestialGenericException("Property not supported");
			}
		}
	}
	
	private HBox createHeader() {
		HBox header = new HBox();
		header.setSpacing(0);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0));
		
		Label title = new Label(name);
		Region region = new Region();
		HBox.setHgrow(region, Priority.ALWAYS);
		header.getChildren().addAll(title, region);
		
		ImageView upView = new ImageView(StudioViewRepository.IMAGE_UP);
		upView.setFitWidth(9);
		upView.setFitHeight(9);
		Button up = new Button();
		up.setGraphic(upView);
		up.setId("invisible-button");
		up.setPadding(new Insets(0, 5, 0, 5));
		up.setTooltip(new Tooltip("Move Up"));
		up.setOnAction(event -> GLRequestSystem.request(() -> {
			if(GLRequestSystem.selectedObjects().size() == 1) {
				CEObject obj = GLRequestSystem.selectedObjects().get(0);
				for(int i = 0 ; i < obj.getModifiers().size() ; ++i) {
					if(obj.getModifiers().get(i) == mod.getModifiers().get(0) && i > 0) {
						obj.removeModifier(i);
						obj.insertModifier(i - 1, mod.getModifiers().get(0));
					}
				}
			}
		}));
		
		ImageView downView = new ImageView(StudioViewRepository.IMAGE_DOWN);
		downView.setFitWidth(9);
		downView.setFitHeight(9);
		Button down = new Button();
		down.setGraphic(downView);
		down.setId("invisible-button");
		down.setPadding(new Insets(0, 5, 0, 5));
		down.setTooltip(new Tooltip("Move Down"));
		down.setOnAction(event -> GLRequestSystem.request(() -> {
			if(GLRequestSystem.selectedObjects().size() == 1) {
				CEObject obj = GLRequestSystem.selectedObjects().get(0);
				for(int i = obj.getModifiers().size() - 1 ; i >= 0 ; --i) {
					if(obj.getModifiers().get(i) == mod.getModifiers().get(0) && i < obj.getModifiers().size() - 1) {
						obj.removeModifier(i);
						obj.insertModifier(i + 1, mod.getModifiers().get(0));
					}
				}
			}
		}));
		
		ImageView deleteView = new ImageView(StudioViewRepository.IMAGE_DELETE);
		deleteView.setFitWidth(12);
		deleteView.setFitHeight(12);
		Button delete = new Button();
		delete.setGraphic(deleteView);
		delete.setId("invisible-button");
		delete.setPadding(new Insets(0, 5, 0, 5));
		delete.setTooltip(new Tooltip("Delete Modifier"));
		delete.setOnAction(event -> {
			boolean confirm = MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete modifier?"));
			if(confirm) GLRequestSystem.request(() -> {
				for(CEObject obj : GLRequestSystem.selectedObjects()) for(Modifier modifier : mod.getModifiers()) obj.removeModifier(modifier);
			});
		});
		
		Tooltip infoText = new Tooltip(description);
		ImageView infoView = new ImageView(StudioViewRepository.IMAGE_INFO);
		infoView.setFitWidth(12);
		infoView.setFitHeight(12);
		Button info = new Button();
		info.setGraphic(infoView);
		info.setId("invisible-button");
		info.setPadding(new Insets(0, 5, 0, 5));
		info.setOnMouseClicked(event -> {
			infoText.setAnchorX(event.getScreenX() + 10);
			infoText.setAnchorY(event.getScreenY() + 10);
			infoText.setHideOnEscape(true);
			infoText.setAutoHide(true);
			infoText.show(info.getScene().getWindow());
		});
		info.setOnMouseExited(event -> infoText.hide());
		
		header.getChildren().addAll(up, down, delete, info);
		return header;
	}
	
	private char createVectorText(String propID, int index) {
		if(propID.toLowerCase().contains("color") || propID.toLowerCase().contains("tint") || propID.toLowerCase().contains("blend")) {
			return new char[] {'R', 'G', 'B', 'A'} [index];
		}
		else if(propID.toLowerCase().contains("attenuation")) {
			return new char[] {'0', '1', '2', '3'} [index];
		}
		else {
			return new char[] {'X', 'Y', 'Z', 'W'} [index];
		}
	}
	
}
