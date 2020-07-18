package studio.celestial.impl;

import java.util.ArrayList;
import java.util.List;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.util.Factory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;

public final class ModifierRepositoryPanel extends StudioComponentPanel {
	
	public ModifierRepositoryPanel(StudioInterface studio) {
		super(studio, "Modifier Repository", 5, 5, 6, 2, 6);
	}
	
	private final TreeItem<ModifierItem> modifierRoot = new TreeItem<>(new ModifierItem("Dummy Item"));
	private TextField modifierSearch;
	
	@Override
	protected void initialize() {
		modifierSearch = new TextField();
		modifierSearch.setPromptText("Search Modifiers");
		super.getPane().add(modifierSearch, 0, 0, 16, 2);
		
		TreeView<ModifierItem> modifierList = new TreeView<ModifierItem>();
		super.getPane().add(modifierList, 0, 2, 16, 32);
		
		modifierList.setRoot(modifierRoot);
		modifierList.setShowRoot(false);
	}
	
	@Override
	protected void update() {
		for(ModifierItem category : StudioInterface.getInstantiation().getModifierCategories().keySet()) {
			if(!contains(modifierRoot.getChildren(), category)) {
				ArrayList<ModifierItem> modifiers = new ArrayList<ModifierItem>();
				for(ModifierItem modifier : StudioInterface.getInstantiation().getModifierCategories().get(category))
					if(modifier.getIdentifier().toLowerCase().contains(modifierSearch.getText().trim().toLowerCase()))
						modifiers.add(modifier);
				
				if(modifiers.size() > 0 || modifierSearch.getText().length() == 0) modifierRoot.getChildren().add(new TreeItem<>(category));
			}
		}
		for(TreeItem<ModifierItem> item : new ArrayList<>(modifierRoot.getChildren())) {
			if(!StudioInterface.getInstantiation().getModifierCategories().containsKey(item.getValue())) modifierRoot.getChildren().remove(item);
			else {
				ArrayList<ModifierItem> modifiers = new ArrayList<ModifierItem>();
				for(ModifierItem modifier : StudioInterface.getInstantiation().getModifierCategories().get(item.getValue()))
					if(modifier.getIdentifier().toLowerCase().contains(modifierSearch.getText().trim().toLowerCase()))
						modifiers.add(modifier);
				
				for(ModifierItem modifier : modifiers) if(!contains(item.getChildren(), modifier))
					item.getChildren().add(new TreeItem<>(modifier));
				for(TreeItem<ModifierItem> subItem : new ArrayList<>(item.getChildren()))
					if(!modifiers.contains(subItem.getValue())) item.getChildren().remove(subItem);
				
				if(modifiers.size() == 0 && modifierSearch.getText().length() > 0) {
					modifierRoot.getChildren().remove(item);
				}
			}
		}
	}
	
	private boolean contains(List<TreeItem<ModifierItem>> items, ModifierItem item) {
		for(TreeItem<ModifierItem> treeItem : items) if(item.equals(treeItem.getValue())) return true;
		return false;
	}
	
	public static final class ModifierItem extends HBox {
		
		private final String identifier;
		private final String description;
		private final Factory<? extends Modifier> modifier;
		private final Class<? extends Modifier> type;
		
		public ModifierItem(String identifier) {
			this(identifier, "", null, null);
		}
		
		public ModifierItem(String identifier, String description, Factory<? extends Modifier> modifier, Class<? extends Modifier> type) {
			this.identifier = identifier;
			this.description = description;
			this.modifier = modifier;
			this.type = type;
			
			Label lbl = new Label(identifier);
			
			if(modifier != null) {
				HBox box = new HBox();
				box.setAlignment(Pos.CENTER);
				box.setSpacing(0);
				box.setPadding(new Insets(0));
				
				ImageView addView = new ImageView(StudioViewRepository.IMAGE_ADD);
				addView.setFitWidth(12);
				addView.setFitHeight(12);
				Button add = new Button();
				add.setGraphic(addView);
				add.setId("invisible-button");
				add.setPadding(new Insets(0, 5, 0, 5));
				add.setTooltip(new Tooltip("Add Modifier to Selected Object"));
				add.setOnAction(event -> addModifier());
				
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
				
				Region region = new Region();
				HBox.setHgrow(region, Priority.ALWAYS);
				
				box.getChildren().addAll(add, info);
				super.getChildren().addAll(lbl, region, box);
			}
			else super.getChildren().add(lbl);
			
			super.setSpacing(0);
			super.setAlignment(Pos.CENTER_LEFT);
			super.setPadding(new Insets(0));
			
			if(modifier != null) {
				super.setOnMouseClicked(event -> {
					if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
						addModifier();
				});
			}
		}
		
		public String getIdentifier() {
			return identifier;
		}
		
		public String getDescription() {
			return description;
		}
		
		public Factory<?> getModifier() {
			return modifier;
		}
		
		public Class<? extends Modifier> getType() {
			return type;
		}
		
		@Override
		public String toString() {
			return identifier;
		}
		
		private void addModifier() {
			GLRequestSystem.request(() -> {
				for(CEObject obj : GLRequestSystem.selectedObjects()) obj.addModifier(modifier.build());
			});
		}
		
	}
	
}
