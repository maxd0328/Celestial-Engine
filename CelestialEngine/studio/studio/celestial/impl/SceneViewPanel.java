package studio.celestial.impl;

import java.util.ArrayList;

import celestial.scene.Layer;
import celestial.scene.Scene;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import mod.celestial.misc.CameraModifier;
import mod.celestial.misc.CameraModifier.ProjectionState;
import studio.celestial.binding.DirectBinding;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.SceneManager;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioView;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.dialog.MessageDialog.MessageType;

public final class SceneViewPanel extends StudioComponentPanel {
	
	private static final int GAP = 5;
	
	public SceneViewPanel(StudioInterface studio) {
		super(studio, "Scene View", GAP, GAP, 10, 10, 4);
	}
	
	private ComboBox<SceneItem> scenes;
	private ComboBox<LayerItem> layers;
	private ComboBox<StudioView> modes;
	private ToggleButton visible;
	private ToggleButton axisX, axisY, axisZ, grid, placeGrid, studioOverlay, orthographic, playing;
	
	@Override
	protected void initialize() {
		Label sceneLbl = new Label("Scene");
		super.getPane().add(sceneLbl, 0, 32, 2, 2);
		
		scenes = new ComboBox<SceneItem>();
		scenes.setPrefWidth(72 * GAP);
		scenes.getSelectionModel().select(0);
		scenes.setTooltip(new Tooltip("Current Scene"));
		super.getPane().add(scenes, 2, 32, 6, 2);
		
		Image renameImage = StudioViewRepository.IMAGE_RENAME;
		ImageView renameView = new ImageView(renameImage);
		renameView.setFitWidth(12);
		renameView.setFitHeight(17);
		ImageView renameView0 = new ImageView(renameImage);
		renameView0.setFitWidth(12);
		renameView0.setFitHeight(17);
		
		HBox sceneButtons = new HBox(0);
		sceneButtons.setAlignment(Pos.CENTER);
		Button sceneAdd = new Button("+");
		sceneAdd.setOnAction((event) -> GLRequestSystem.request(() -> {
			SceneItem scene = new SceneItem(SceneManager.createStudioScene(GLRequestSystem.getSceneManager().nextSceneIdentifier()));
			GLRequestSystem.getSceneManager().addScene(scene.scene);
			scenes.getItems().add(scene);
			StudioInterface.getInstantiation().request((studio) -> scenes.getSelectionModel().select(scene));
		}));
		sceneAdd.setTooltip(new Tooltip("Add New Scene"));
		Button sceneRemove = new Button("-");
		sceneRemove.setOnAction((event) -> {
			if(GLRequestSystem.getSceneManager().getScenes().size() <= 1) {
				MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "At least one scene is required"));
				return;
			}
			if(MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete scene?")))
				GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().removeScene(scenes.getSelectionModel().getSelectedItem().scene));
		});
		sceneRemove.setTooltip(new Tooltip("Remove Current Scene"));
		Button sceneRename = new Button();
		sceneRename.setGraphic(renameView);
		sceneRename.setTooltip(new Tooltip("Rename Current Scene"));
		sceneRename.setOnAction((event) -> {
			String name = MessageHandler.showAndWait("Rename", "Rename scene:", scenes.getSelectionModel().getSelectedItem().identifier);
			if(name != null) {
				if(name.endsWith(".studio") || GLRequestSystem.getSceneManager().getScene(name) != null) return;
				scenes.getSelectionModel().getSelectedItem().identifier = name;
				scenes.getSelectionModel().getSelectedItem().scene.setIdentifier(name);
				SceneItem item = scenes.getSelectionModel().getSelectedItem();
				scenes.getSelectionModel().select(null);
				scenes.getSelectionModel().select(item);
			}
		});
		sceneButtons.getChildren().addAll(sceneAdd, sceneRemove, sceneRename);
		super.getPane().add(sceneButtons, 8, 32, 3, 2);
		
		Label layerLbl = new Label("Layer");
		super.getPane().add(layerLbl, 12, 32, 2, 2);
		
		layers = new ComboBox<LayerItem>();
		layers.setPrefWidth(72 * GAP);
		layers.getSelectionModel().select(0);
		layers.setTooltip(new Tooltip("Current Layer"));
		super.getPane().add(layers, 16, 32, 6, 2);
		
		HBox layerButtons = new HBox(0);
		layerButtons.setAlignment(Pos.CENTER);
		Button layerAdd = new Button("+");
		layerAdd.setOnAction((event) -> GLRequestSystem.request(() -> {
			LayerItem layer = new LayerItem(new Layer(GLRequestSystem.getSceneManager().nextLayerIdentifier()));
			GLRequestSystem.getSceneManager().getCurrentScene().addLayer(layer.layer);
			layers.getItems().add(layer);
			StudioInterface.getInstantiation().request((studio) -> layers.getSelectionModel().select(layer));
		}));
		layerAdd.setTooltip(new Tooltip("Add New Layer"));
		Button layerRemove = new Button("-");
		layerRemove.setOnAction((event) -> {
			if(GLRequestSystem.getSceneManager().getCurrentScene().getLayers().size() <= 3) {
				MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "At least one layer is required"));
				return;
			}
			if(MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete layer?")))
				GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().getCurrentScene().removeLayer(layers.getSelectionModel().getSelectedItem().layer));
		});
		layerRemove.setTooltip(new Tooltip("Remove Current Layer"));
		Button layerRename = new Button();
		layerRename.setOnAction((event) -> {
			String name = MessageHandler.showAndWait("Rename", "Rename layer:", layers.getSelectionModel().getSelectedItem().identifier);
			if(name != null) {
				if(name.endsWith(".studio") || GLRequestSystem.getSceneManager().getLayer(name) != null) return;
				layers.getSelectionModel().getSelectedItem().identifier = name;
				layers.getSelectionModel().getSelectedItem().layer.setIdentifier(name);
				LayerItem item = layers.getSelectionModel().getSelectedItem();
				layers.getSelectionModel().select(null);
				layers.getSelectionModel().select(item);
			}
		});
		layerRename.setGraphic(renameView0);
		layerRename.setTooltip(new Tooltip("Rename Current Layer"));
		layerButtons.getChildren().addAll(layerAdd, layerRemove, layerRename);
		super.getPane().add(layerButtons, 22, 32, 3, 2);
		
		ImageView visibleImage = new ImageView();
		visibleImage.setFitWidth(22);
		visibleImage.setFitHeight(22);
		visible = new ToggleButton();
		visibleImage.imageProperty().bind(Bindings.when(visible.selectedProperty()).then(StudioViewRepository.IMAGE_VISIBLE).otherwise((Image) null));
		visible.setPadding(new Insets(2));
		visible.setGraphic(visibleImage);
		visible.setTooltip(new Tooltip("Show Layer"));
		visible.setSelected(true);
		super.getPane().add(visible, 14, 32, 2, 2);
		
		modes = new ComboBox<StudioView>();
		modes.setPrefWidth(72 * GAP);
		modes.getItems().add(StudioViewRepository.STUDIO_VIEW_OBJECT_MODE);
		modes.getItems().add(StudioViewRepository.STUDIO_VIEW_EDIT_MODE);
		modes.getItems().add(StudioViewRepository.STUDIO_VIEW_VIEW_MODE);
		modes.getSelectionModel().select(0);
		modes.setTooltip(new Tooltip("Current Mode"));
		super.getPane().add(modes, 42, 32, 6, 2);
		
		HBox axisBox = new HBox(0);
		axisBox.setAlignment(Pos.CENTER);
		axisX = new ToggleButton("X");
		axisX.setMinWidth(25);
		axisX.setMinHeight(25);
		axisX.setSelected(true);
		axisX.selectedProperty().addListener((obs, oldV, newV) -> axisX.setText(newV ? "X" : ""));
		axisX.setTooltip(new Tooltip("Show X-Axis"));
		axisY = new ToggleButton("Y");
		axisY.setMinWidth(25);
		axisY.setMinHeight(25);
		axisY.setSelected(true);
		axisY.selectedProperty().addListener((obs, oldV, newV) -> axisY.setText(newV ? "Y" : ""));
		axisY.setTooltip(new Tooltip("Show Y-Axis"));
		axisZ = new ToggleButton("Z");
		axisZ.setMinWidth(25);
		axisZ.setMinHeight(25);
		axisZ.setSelected(true);
		axisZ.selectedProperty().addListener((obs, oldV, newV) -> axisZ.setText(newV ? "Z" : ""));
		axisZ.setTooltip(new Tooltip("Show Z-Axis"));
		axisBox.getChildren().addAll(axisX, axisY, axisZ);
		super.getPane().add(axisBox, 38, 32, 4, 2);
		
		ImageView gridImage = new ImageView();
		gridImage.setFitWidth(20);
		gridImage.setFitHeight(20);
		ImageView placeGridImage = new ImageView();
		placeGridImage.setFitWidth(20);
		placeGridImage.setFitHeight(20);
		
		HBox gridBox = new HBox(0);
		gridBox.setAlignment(Pos.CENTER);
		grid = new ToggleButton();
		gridImage.imageProperty().bind(Bindings.when(grid.selectedProperty()).then(StudioViewRepository.IMAGE_GRID).otherwise((Image) null));
		grid.setPadding(new Insets(2));
		grid.setGraphic(gridImage);
		grid.setTooltip(new Tooltip("Show Grid"));
		grid.setSelected(true);
		placeGrid = new ToggleButton();
		placeGridImage.imageProperty().bind(Bindings.when(placeGrid.selectedProperty()).then(StudioViewRepository.IMAGE_PLACE).otherwise((Image) null));
		placeGrid.setPadding(new Insets(2));
		placeGrid.setGraphic(placeGridImage);
		placeGrid.setTooltip(new Tooltip("Show Placement Plane"));
		placeGrid.setSelected(false);
		gridBox.getChildren().addAll(grid, placeGrid);
		super.getPane().add(gridBox, 35, 32, 4, 2);
		
		ImageView studioOverlayImage = new ImageView();
		studioOverlayImage.setFitWidth(20);
		studioOverlayImage.setFitHeight(20);
		ImageView orthographicView = new ImageView();
		orthographicView.setFitWidth(20);
		orthographicView.setFitHeight(20);
		ImageView playingView = new ImageView();
		playingView.setFitWidth(20);
		playingView.setFitHeight(20);
		
		HBox overlayBox = new HBox(0);
		overlayBox.setAlignment(Pos.CENTER);
		studioOverlay = new ToggleButton();
		studioOverlayImage.imageProperty().bind(Bindings.when(studioOverlay.selectedProperty()).then(StudioViewRepository.IMAGE_OVERLAY).otherwise((Image) null));
		studioOverlay.setPadding(new Insets(2));
		studioOverlay.setGraphic(studioOverlayImage);
		studioOverlay.setTooltip(new Tooltip("Show Studio Overlays"));
		studioOverlay.setSelected(true);
		
		orthographic = new ToggleButton();
		orthographicView.imageProperty().bind(Bindings.when(orthographic.selectedProperty()).then(StudioViewRepository.IMAGE_ORTHO).otherwise((Image) null));
		orthographic.setPadding(new Insets(2));
		orthographic.setGraphic(orthographicView);
		orthographic.setTooltip(new Tooltip("Show Orthographic View"));
		orthographic.setSelected(false);
		
		playing = new ToggleButton();
		playingView.imageProperty().bind(Bindings.when(playing.selectedProperty()).then(StudioViewRepository.IMAGE_PLAY).otherwise(StudioViewRepository.IMAGE_PAUSE));
		playing.setPadding(new Insets(2));
		playing.setGraphic(playingView);
		playing.setTooltip(new Tooltip("Play Simulation"));
		playing.setSelected(false);
		
		overlayBox.getChildren().addAll(studioOverlay, orthographic, playing);
		super.getPane().add(overlayBox, 32, 32, 4, 2);
		
		// Property bindings
		
		super.addBinding(new DirectBinding<Boolean>(() -> visible.isSelected(), s -> visible.setSelected(s),
				() -> GLRequestSystem.currentLayer().isEnabled(), s -> GLRequestSystem.currentLayer().setEnabled(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> studioOverlay.isSelected(), s -> studioOverlay.setSelected(s),
				() -> GLRequestSystem.currentScene().getLayer(".studio").isEnabled(), s -> GLRequestSystem.currentScene().getLayer(".studio").setEnabled(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> orthographic.isSelected(), s -> orthographic.setSelected(s),
				() -> GLRequestSystem.currentScene().getLayer("X.studio").getObject("camera.studio").getModifier(CameraModifier.class).getProjectionState()
				== ProjectionState.ORTHOGRAPHIC, s -> GLRequestSystem.currentScene().getLayer("X.studio").getObject("camera.studio")
				.getModifier(CameraModifier.class).setProjectionState(s ? ProjectionState.ORTHOGRAPHIC : ProjectionState.PERSPECTIVE)));
		super.addBinding(new DirectBinding<Boolean>(() -> !playing.isSelected(), s -> playing.setSelected(!s),
				() -> GLRequestSystem.getSceneManager().getConfigs().isPaused(), s -> GLRequestSystem.getSceneManager().getConfigs().setPaused(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> axisX.isSelected(), s -> axisX.setSelected(s), () -> GLRequestSystem.currentScene().getLayer
				(".studio").getObject("axisX.studio").isEnabled(), s -> GLRequestSystem.currentScene().getLayer(".studio").getObject("axisX.studio").setEnabled(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> axisY.isSelected(), s -> axisY.setSelected(s), () -> GLRequestSystem.currentScene().getLayer
				(".studio").getObject("axisY.studio").isEnabled(), s -> GLRequestSystem.currentScene().getLayer(".studio").getObject("axisY.studio").setEnabled(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> axisZ.isSelected(), s -> axisZ.setSelected(s), () -> GLRequestSystem.currentScene().getLayer
				(".studio").getObject("axisZ.studio").isEnabled(), s -> GLRequestSystem.currentScene().getLayer(".studio").getObject("axisZ.studio").setEnabled(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> grid.isSelected(), s -> grid.setSelected(s), () -> GLRequestSystem.currentScene().getLayer
				(".studio").getObject("0grid.studio").isEnabled(), s -> {
					for(int i = SceneManager.GRID_MIN ; i <= SceneManager.GRID_MAX ; ++i) {
						GLRequestSystem.currentScene().getLayer(".studio").getObject(i + "grid.studio").setEnabled(s);
						GLRequestSystem.currentScene().getLayer(".studio").getObject(i + "vert-grid.studio").setEnabled(s);
					}
				}));
		
		super.addBinding(new DirectBinding<StudioView>(() -> modes.getValue(), s -> modes.setValue(s), () -> StudioInterface.getInstantiation().getCurrentView(),
				s -> StudioInterface.getInstantiation().setView(s)).withGLThread(DirectBinding.CONTROL_THREAD_ID));
		super.addBinding(new DirectBinding<Scene>(() -> scenes.getValue() == null ? null : scenes.getValue().scene, s -> setScene(s),
				() -> GLRequestSystem.currentScene(), s -> GLRequestSystem.getSceneManager().setCurrentScene(s)));
		super.addBinding(new DirectBinding<Layer>(() -> layers.getValue() == null ? null : layers.getValue().layer, s -> setLayer(s),
				() -> GLRequestSystem.currentLayer(), s -> GLRequestSystem.getSceneManager().setCurrentLayer(s == null ? "" : s.getIdentifier())));
	}
	
	@Override
	protected void update() {
		ArrayList<SceneItem> sceneItems = new ArrayList<SceneItem>();
		ArrayList<LayerItem> layerItems = new ArrayList<LayerItem>();
		for(Scene scene : GLRequestSystem.getSceneManager().getScenes()) {
			sceneItems.add(new SceneItem(scene));
			if(scene == GLRequestSystem.getSceneManager().getCurrentScene())
				for(Layer layer : scene.getLayers()) if(!layer.getIdentifier().endsWith(".studio")) layerItems.add(new LayerItem(layer));
		}
		for(SceneItem item : new ArrayList<SceneItem>(scenes.getItems())) if(!sceneItems.contains(item)) scenes.getItems().remove(item);
		for(SceneItem item : sceneItems) if(!scenes.getItems().contains(item)) scenes.getItems().add(item);
		for(LayerItem item : new ArrayList<LayerItem>(layers.getItems())) if(!layerItems.contains(item)) layers.getItems().remove(item);
		for(LayerItem item : layerItems) if(!layers.getItems().contains(item)) layers.getItems().add(item);
		if(scenes.getSelectionModel().getSelectedItem() == null) scenes.getSelectionModel().select(0);
		if(layers.getSelectionModel().getSelectedItem() == null) layers.getSelectionModel().select(0);
		
		GLRequestSystem.request(() -> GLRequestSystem.currentScene().getLayer(".studio").getObject("plane.studio").setEnabled(isObjectMode() && placeGrid.isSelected()));
		
		if(StudioInterface.getInstantiation().getCurrentView() != modes.getValue() && modes.getValue() != null) StudioInterface.getInstantiation().setView(modes.getValue());
	}
	
	public int getMode() {
		return modes.getSelectionModel().getSelectedIndex();
	}
	
	public void setMode(int mode) {
		if(Thread.currentThread().getName().equals("JavaFX Application Thread"))
			this.modes.getSelectionModel().select(mode);
		else StudioInterface.getInstantiation().request(studio -> this.modes.getSelectionModel().select(mode));
	}
	
	private boolean isObjectMode() {
		return StudioInterface.getInstantiation().getCurrentView() != null && StudioInterface.getInstantiation().getCurrentView().getIdentifier().equals("Object Mode");
	}
	
	private void setScene(Scene scene) {
		for(SceneItem item : scenes.getItems()) if(item.scene == scene) scenes.getSelectionModel().select(item);
	}
	
	private void setLayer(Layer layer) {
		for(LayerItem item : layers.getItems()) if(item.layer == layer) layers.getSelectionModel().select(item);
	}
	
	private static final class LayerItem {
		
		public Layer layer;
		public String identifier;
		
		public LayerItem(Layer layer) {
			this.layer = layer;
			this.identifier = layer.getIdentifier();
		}
		
		@Override
		public String toString() {
			return identifier;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof LayerItem)) return false;
			LayerItem l = (LayerItem) o;
			return l.identifier.equals(identifier) && l.layer == layer;
		}
		
	}
	
	private static final class SceneItem {
		
		public Scene scene;
		public String identifier;
		
		public SceneItem(Scene scene) {
			this.scene = scene;
			this.identifier = scene.getIdentifier();
		}
		
		@Override
		public String toString() {
			return identifier;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof SceneItem)) return false;
			SceneItem s = (SceneItem) o;
			return s.identifier.equals(identifier) && s.scene == scene;
		}
		
	}
	
}
