package studio.celestial.impl;

import java.util.ArrayList;
import celestial.core.CEObject;
import celestial.scene.Layer;
import celestial.scene.Scene;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.dialog.MessageDialog.MessageType;

public final class HierarchyViewPanel extends StudioComponentPanel {
	
	public HierarchyViewPanel(StudioInterface studio) {
		super(studio, "Hierarchy View", 5, 5, 5, 5, 5);
	}
	
	private TextField search;
	private TreeView<HierarchyViewItem> tree;
	
	@Override
	protected void initialize() {
		search = new TextField();
		search.setPromptText("Search Objects");
		super.getPane().add(search, 0, 0, 22, 2);
		
		tree = new TreeView<HierarchyViewItem>();
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		super.getPane().add(tree, 0, 2, 22, 10);
		
		Button selectAll = new Button("Select All");
		selectAll.setMaxWidth(Double.MAX_VALUE);
		selectAll.setOnAction((event) -> GLRequestSystem.request(() -> {
			GLRequestSystem.getSceneManager().deselectAll();
			for(TreeItem<HierarchyViewItem> item : tree.getSelectionModel().getSelectedItems()) {
				if(item.getValue().object != null) {
					GLRequestSystem.getSceneManager().addSelection(item.getValue().object);
				}
			}
		}));
		super.getPane().add(selectAll, 0, 12, 6, 2);
		
		Button add = new Button("Add Selection");
		add.setMaxWidth(Double.MAX_VALUE);
		add.setOnAction((event) -> GLRequestSystem.request(() -> {
			for(TreeItem<HierarchyViewItem> item : tree.getSelectionModel().getSelectedItems()) {
				if(item.getValue().object != null) {
					GLRequestSystem.getSceneManager().addSelection(item.getValue().object);
				}
			}
		}));
		super.getPane().add(add, 6, 12, 6, 2);
		
		Button remove = new Button("Remove Selection");
		remove.setMaxWidth(Double.MAX_VALUE);
		remove.setOnAction((event) -> GLRequestSystem.request(() -> {
			for(TreeItem<HierarchyViewItem> item : tree.getSelectionModel().getSelectedItems()) {
				if(item.getValue().object != null) {
					GLRequestSystem.getSceneManager().removeSelection(item.getValue().object);
				}
			}
		}));
		super.getPane().add(remove, 12, 12, 7, 2);
		
		ImageView deleteView = new ImageView(StudioViewRepository.IMAGE_DELETE);
		deleteView.setFitWidth(15);
		deleteView.setFitHeight(17);
		Button delete = new Button();
		delete.setMaxWidth(Double.MAX_VALUE);
		delete.setOnAction((event) -> {
			boolean confirm = MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete object(s)?"));
			if(confirm) GLRequestSystem.request(() -> {
				for(TreeItem<HierarchyViewItem> item : tree.getSelectionModel().getSelectedItems()) {
					if(item.getValue().object != null) {
						GLRequestSystem.getSceneManager().removeObject(item.getValue().object);
					}
				}
			});
		});
		delete.setGraphic(deleteView);
		delete.setTooltip(new Tooltip("Delete Objects"));
		super.getPane().add(delete, 19, 12, 3, 2);
	}
	
	@Override
	protected void update() {
		if(tree.getRoot() == null || tree.getRoot().getValue().scene != GLRequestSystem.getSceneManager().getCurrentScene()) {
			TreeItem<HierarchyViewItem> root = new TreeItem<HierarchyViewItem>(new HierarchyViewItem
					(HierarchyItemType.SCENE, GLRequestSystem.getSceneManager().getCurrentScene(), null, null));
			root.setExpanded(true);
			tree.setRoot(root);
		}
		tree.getRoot().getValue().update();
		
		ArrayList<Layer> layers = GLRequestSystem.getSceneManager().getCurrentScene().getLayers();
		for(Layer layer : layers) {
			ArrayList<CEObject> objects = layer.getObjects();
			for(CEObject obj : new ArrayList<CEObject>(objects)) if(!obj.getIdentifier().toLowerCase().contains(search.getText().trim().toLowerCase())) objects.remove(obj);
			
			if(!layer.getIdentifier().endsWith(".studio") && !containsLayer(tree.getRoot(), layer) && (objects.size() > 0 || search.getText().length() == 0)) {
				TreeItem<HierarchyViewItem> item = new TreeItem<>(new HierarchyViewItem(HierarchyItemType.LAYER, null, layer, null));
				item.setExpanded(true);
				tree.getRoot().getChildren().add(item);
			}
		}
		for(TreeItem<HierarchyViewItem> layerItem : new ArrayList<TreeItem<HierarchyViewItem>>(tree.getRoot().getChildren())) {
			if(!layers.contains(layerItem.getValue().layer)) tree.getRoot().getChildren().remove(layerItem);
			else {
				layerItem.getValue().visible.setSelected(layerItem.getValue().layer.isEnabled());
				layerItem.getValue().update();
			}
		}
		
		for(TreeItem<HierarchyViewItem> layer : new ArrayList<TreeItem<HierarchyViewItem>>(tree.getRoot().getChildren())) {
			ArrayList<CEObject> objects = layer.getValue().layer.getObjects();
			for(CEObject obj : new ArrayList<CEObject>(objects)) if(!obj.getIdentifier().toLowerCase().contains(search.getText().trim().toLowerCase())) objects.remove(obj);
			for(CEObject object : objects)
				if(!containsObject(layer, object))
					layer.getChildren().add(new TreeItem<>(new HierarchyViewItem(HierarchyItemType.OBJECT, null, null, object)));
			for(TreeItem<HierarchyViewItem> objectItem : new ArrayList<TreeItem<HierarchyViewItem>>(layer.getChildren())) {
				if(!objects.contains(objectItem.getValue().object)) layer.getChildren().remove(objectItem);
				else {
					objectItem.getValue().visible.setSelected(objectItem.getValue().object.isEnabled());
					objectItem.getValue().update();
				}
			}
			if(layer.getChildren().size() == 0 && search.getText().length() > 0) tree.getRoot().getChildren().remove(layer);
		}
		if(tree.getRoot().getChildren().size() == 0 && search.getText().length() > 0) tree.setRoot(null);
	}
	
	private boolean containsLayer(TreeItem<HierarchyViewItem> parentItem, Layer layer) {
		for(TreeItem<HierarchyViewItem> child : parentItem.getChildren()) {
			if(child.getValue() != null && child.getValue().layer == layer) return true;
		}
		return false;
	}
	
	private boolean containsObject(TreeItem<HierarchyViewItem> parentItem, CEObject object) {
		for(TreeItem<HierarchyViewItem> child : parentItem.getChildren()) {
			if(child.getValue() != null && child.getValue().object == object) return true;
		}
		return false;
	}
	
	private static final class HierarchyViewItem extends HBox {
		
		private final Scene scene;
		private final Layer layer;
		private final CEObject object;
		
		private Label label;
		private ToggleButton visible;
		
		public HierarchyViewItem(HierarchyItemType type, Scene scene, Layer layer, CEObject object) {
			this.scene = scene;
			this.layer = layer;
			this.object = object;
			
			this.label = new Label(scene != null ? scene.getIdentifier() : layer != null ? layer.getIdentifier() : object != null ? object.getIdentifier() : "");
			switch(type) {
			case SCENE: {
				super.getChildren().addAll(label);
				break;
			}
			case LAYER: {
				HBox box = new HBox();
				box.setAlignment(Pos.CENTER);
				box.setSpacing(0);
				box.setPadding(new Insets(0));
				
				ImageView visibleView = new ImageView();
				visibleView.setFitWidth(16);
				visibleView.setFitHeight(16);
				visible = new ToggleButton();
				visibleView.imageProperty().bind(Bindings.when(visible.selectedProperty()).then(StudioViewRepository.IMAGE_VISIBLE_SIMPLE).otherwise((Image) null));
				visible.setGraphic(visibleView);
				visible.setId("invisible-button");
				visible.setPadding(new Insets(0, 5, 0, 5));
				visible.setTooltip(new Tooltip("Show Layer"));
				visible.setSelected(true);
				visible.setOnAction((event) -> GLRequestSystem.request(() -> this.layer.setEnabled(!this.layer.isEnabled())));
				
				Region region = new Region();
				HBox.setHgrow(region, Priority.ALWAYS);
				
				box.getChildren().addAll(visible);
				super.getChildren().addAll(label, region, box);
				break;
			}
			case OBJECT:
				HBox box = new HBox();
				box.setAlignment(Pos.CENTER_RIGHT);
				box.setSpacing(0);
				box.setPadding(new Insets(0));
				
				ImageView visibleView = new ImageView();
				visibleView.setFitWidth(16);
				visibleView.setFitHeight(16);
				visible = new ToggleButton();
				visibleView.imageProperty().bind(Bindings.when(visible.selectedProperty()).then(StudioViewRepository.IMAGE_VISIBLE_SIMPLE).otherwise((Image) null));
				visible.setGraphic(visibleView);
				visible.setId("invisible-button");
				visible.setPadding(new Insets(0, 5, 0, 5));
				visible.setTooltip(new Tooltip("Show Object"));
				visible.setSelected(true);
				visible.setOnAction((event) -> GLRequestSystem.request(() -> this.object.setEnabled(!this.object.isEnabled())));
				
				ImageView selectView = new ImageView(StudioViewRepository.IMAGE_SELECT);
				selectView.setFitWidth(12);
				selectView.setFitHeight(12);
				Button select = new Button();
				select.setGraphic(selectView);
				select.setId("invisible-button");
				select.setPadding(new Insets(0, 5, 0, 5));
				select.setTooltip(new Tooltip("Select Object"));
				select.setOnAction((event) -> GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().clearAndSelect(this.object)));
				super.setOnMouseClicked(event -> { if(event.getClickCount() == 2) GLRequestSystem.getSceneManager().clearAndSelect(this.object); });
				
				ImageView addView = new ImageView(StudioViewRepository.IMAGE_ADD);
				addView.setFitWidth(12);
				addView.setFitHeight(12);
				Button add = new Button();
				add.setGraphic(addView);
				add.setId("invisible-button");
				add.setPadding(new Insets(0, 5, 0, 5));
				add.setTooltip(new Tooltip("Add Object to Selection"));
				add.setOnAction((event) -> GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().addSelection(this.object)));
				
				ImageView subtractView = new ImageView(StudioViewRepository.IMAGE_SUBTRACT);
				subtractView.setFitWidth(12);
				subtractView.setFitHeight(12);
				Button subtract = new Button();
				subtract.setGraphic(subtractView);
				subtract.setId("invisible-button");
				subtract.setPadding(new Insets(0, 5, 0, 5));
				subtract.setTooltip(new Tooltip("Remove Object from Selection"));
				subtract.setOnAction((event) -> GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().removeSelection(this.object)));
				
				ImageView deleteView = new ImageView(StudioViewRepository.IMAGE_DELETE);
				deleteView.setFitWidth(12);
				deleteView.setFitHeight(12);
				Button delete = new Button();
				delete.setGraphic(deleteView);
				delete.setId("invisible-button");
				delete.setPadding(new Insets(0, 5, 0, 5));
				delete.setTooltip(new Tooltip("Delete Object"));
				delete.setOnAction((event) -> {
					boolean confirm = MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete object?"));
					if(confirm) GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().removeObject(this.object));
				});
				
				Region region = new Region();
				HBox.setHgrow(region, Priority.ALWAYS);
				
				box.getChildren().addAll(visible, select, add, subtract, delete);
				super.getChildren().addAll(label, region, box);
				break;
			}
			
			super.setSpacing(0);
			super.setAlignment(Pos.CENTER_LEFT);
			super.setPadding(new Insets(0));
		}
		
		public void update() {
			this.label.setText(scene != null ? scene.getIdentifier() : layer != null ? layer.getIdentifier() : object != null ? object.getIdentifier() : "");
		}
		
		@Override
		public String toString() {
			return scene != null ? scene.getIdentifier() : layer != null ? layer.getIdentifier() : object != null ? object.getIdentifier() : "";
		}
		
	}
	
	public static enum HierarchyItemType {
		SCENE,
		LAYER,
		OBJECT;
	}
	
}
