package studio.celestial.impl;

import java.util.ArrayList;
import celestial.beans.property.Property;
import celestial.data.DataManager;
import celestial.data.DataManager.ClusterDataManager;
import celestial.data.DataManager.DynamicDataManager;
import celestial.data.DataManager.StaticDataManager;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.util.Factory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import studio.celestial.binding.DirectBinding;
import studio.celestial.binding.PropertyBinding;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.ProjectConfigurations.ProjectConfigurationBean;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.ComplexDialog;
import studio.celestial.dialog.DialogOptionBuilder;
import studio.celestial.dialog.DialogOptionBuilder.ButtonType;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.util.AdvancedIntegerSpinner;
import studio.celestial.util.AdvancedSpinner;

public final class ProjectConfigurationPanel extends StudioComponentPanel implements ComplexDialog<Object> {
	
	public ProjectConfigurationPanel(StudioInterface studio) {
		super(studio, "Project Configurations", 5, 5, 10, 5, 5);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		Label configTitle = new Label("Project Configurations");
		configTitle.setFont(new Font(16));
		super.getPane().add(configTitle, 0, 0, 5, 2);
		
		TextField projectName = new TextField();
		projectName.setPromptText("Project Name");
		projectName.setTooltip(new Tooltip("Project Name"));
		projectName.setMinWidth(150);
		projectName.setMaxWidth(150);
		super.getPane().add(projectName, 3, 2, 4, 1);
		super.getPane().add(new Label("Project Name"), 0, 2, 3, 1);
		
		AdvancedSpinner clearColorR, clearColorG, clearColorB;
		Rectangle clearColor = new Rectangle(60, 25);
		clearColor.setFill(Color.BLUE);
		super.getPane().add(createDriverInterface(clearColorR = new AdvancedSpinner(0, 1, 0, 0.1f, "Clear Color R"), 0), 3, 4, 5, 1);
		super.getPane().add(createDriverInterface(clearColorG = new AdvancedSpinner(0, 1, 0, 0.1f, "Clear Color G"), 1), 3, 5, 5, 1);
		super.getPane().add(createDriverInterface(clearColorB = new AdvancedSpinner(0, 1, 0, 0.1f, "Clear Color B"), 2), 3, 6, 5, 1);
		super.getPane().add(clearColor, 0, 5, 2, 1);
		super.getPane().add(new Label("Clear Color"), 0, 4, 3, 1);
		
		AdvancedIntegerSpinner displayWidth = new AdvancedIntegerSpinner(0, Integer.MAX_VALUE, 800, 1, "Display Width");
		AdvancedIntegerSpinner displayHeight = new AdvancedIntegerSpinner(0, Integer.MAX_VALUE, 600, 1, "Display Height");
		CheckBox fullscreen = new CheckBox("Fullscreen");
		CheckBox resizable = new CheckBox("Resizable");
		super.getPane().add(displayWidth.getFXSpinner(), 3, 8, 4, 1);
		super.getPane().add(displayHeight.getFXSpinner(), 3, 9, 4, 1);
		super.getPane().add(fullscreen, 3, 10, 4, 1);
		super.getPane().add(resizable, 3, 11, 4, 1);
		super.getPane().add(new Label("Display"), 0, 8, 3, 1);
		
		AdvancedSpinner viewportMinX = new AdvancedSpinner(0, 1, 0, 0.1f, "Viewport X Minimum");
		AdvancedSpinner viewportMinY = new AdvancedSpinner(0, 1, 1, 0.1f, "Viewport Y Maximum");
		AdvancedSpinner viewportMaxX = new AdvancedSpinner(0, 1, 0, 0.1f, "Viewport X Minimum");
		AdvancedSpinner viewportMaxY = new AdvancedSpinner(0, 1, 1, 0.1f, "Viewport Y Maximum");
		super.getPane().add(viewportMinX.getFXSpinner(), 3, 13, 4, 1);
		super.getPane().add(viewportMinY.getFXSpinner(), 3, 14, 4, 1);
		super.getPane().add(viewportMaxX.getFXSpinner(), 3, 15, 4, 1);
		super.getPane().add(viewportMaxY.getFXSpinner(), 3, 16, 4, 1);
		super.getPane().add(new Label("Viewport"), 0, 13, 3, 1);
		
		TextField displayTitle = new TextField();
		displayTitle.setPromptText("Display Title");
		displayTitle.setTooltip(new Tooltip("Display Title"));
		displayTitle.setMinWidth(150);
		displayTitle.setMaxWidth(150);
		super.getPane().add(displayTitle, 12, 2, 4, 1);
		super.getPane().add(new Label("Display Title"), 9, 2, 3, 1);
		
		ComboBox<Factory<? extends DataManager>> dataManager = new ComboBox<Factory<? extends DataManager>>();
		dataManager.getItems().addAll(StaticDataManager.FACTORY, ClusterDataManager.FACTORY, DynamicDataManager.FACTORY);
		dataManager.setConverter(new StringConverter<Factory<? extends DataManager>>() {
			
			@Override
			public Factory<? extends DataManager> fromString(String str) {
				if(str.equals("Static Manager")) return StaticDataManager.FACTORY;
				else if(str.equals("Cluster Manager")) return ClusterDataManager.FACTORY;
				else if(str.equals("Dynamic Manager")) return DynamicDataManager.FACTORY;
				else return null;
			}
			
			@Override
			public String toString(Factory<? extends DataManager> factory) {
				if(factory == null) return "null";
				if(StaticDataManager.class.isInstance(factory.build())) return "Static Manager";
				else if(ClusterDataManager.class.isInstance(factory.build())) return "Cluster Manager";
				else if(DynamicDataManager.class.isInstance(factory.build())) return "Dynamic Manager";
				else return "Unknown";
			}
			
		});
		dataManager.setMinWidth(150);
		dataManager.setMaxWidth(150);
		super.getPane().add(dataManager, 12, 4, 4, 1);
		super.getPane().add(new Label("Data Manager"), 9, 4, 3, 1);
		
		AdvancedIntegerSpinner glMajVer = new AdvancedIntegerSpinner(1, 5, 3, 1, "GL Major Version");
		AdvancedIntegerSpinner glMinVer = new AdvancedIntegerSpinner(0, 9, 2, 1, "GL Minor Version");
		CheckBox forwardCompatible = new CheckBox("Forward Compatibility");
		CheckBox profileCore = new CheckBox("Use Profile Core");
		super.getPane().add(glMajVer.getFXSpinner(), 12, 6, 4, 1);
		super.getPane().add(glMinVer.getFXSpinner(), 12, 7, 4, 1);
		super.getPane().add(forwardCompatible, 12, 8, 4, 1);
		super.getPane().add(profileCore, 12, 9, 4, 1);
		super.getPane().add(new Label("GL Version"), 9, 6, 3, 1);
		
		AdvancedIntegerSpinner sampleCount = new AdvancedIntegerSpinner(1, 128, 1, 1, "Sample Count");
		super.getPane().add(sampleCount.getFXSpinner(), 12, 11, 4, 1);
		super.getPane().add(new Label("Sample Count"), 9, 11, 3, 1);
		
		AdvancedIntegerSpinner bitDepth = new AdvancedIntegerSpinner(1, 128, 1, 1, "Bit Depth");
		super.getPane().add(bitDepth.getFXSpinner(), 12, 12, 4, 1);
		super.getPane().add(new Label("Bit Depth"), 9, 12, 3, 1);
		
		AdvancedIntegerSpinner fpsSync = new AdvancedIntegerSpinner(1, Integer.MAX_VALUE, 1, 1, "FPS Sync");
		super.getPane().add(fpsSync.getFXSpinner(), 12, 13, 4, 1);
		super.getPane().add(new Label("FPS Sync"), 9, 13, 3, 1);
		
		CheckBox vsync = new CheckBox("Use Vsync");
		CheckBox showMouse = new CheckBox("Show Cursor");
		super.getPane().add(vsync, 12, 15, 4, 1);
		super.getPane().add(showMouse, 12, 16, 4, 1);
		super.getPane().add(new Label("Miscellaneous"), 9, 15, 3, 1);
		
		// Bindings
		
		super.addBinding(new DirectBinding<Color>(() -> (Color) clearColor.getFill(), s -> clearColor.setFill(s),
				() -> Color.color(clearColorR.get(), clearColorG.get(), clearColorB.get()), s -> {}).withGLThread(PropertyBinding.CONTROL_THREAD_ID));
		
		super.addBinding(new DirectBinding<String>(() -> projectName.getText(), s -> projectName.setText(s), () -> bean().getProjectName(), s -> bean().setProjectName(s)));
		
		super.addBinding(new DirectBinding<Float>(() -> clearColorR.get(), s -> clearColorR.set(s), () -> bean().getClearColor().getBase().x, s -> bean().getClearColor().getBase().x = s));
		super.addBinding(new DirectBinding<Float>(() -> clearColorG.get(), s -> clearColorG.set(s), () -> bean().getClearColor().getBase().y, s -> bean().getClearColor().getBase().y = s));
		super.addBinding(new DirectBinding<Float>(() -> clearColorB.get(), s -> clearColorB.set(s), () -> bean().getClearColor().getBase().z, s -> bean().getClearColor().getBase().z = s));
		
		super.addBinding(new DirectBinding<Integer>(() -> displayWidth.get(), s -> displayWidth.set(s),
				() -> bean().getDisplayMode().getWidth(), s -> bean().setDisplayMode(GLDisplayMode.changeWidth(bean().getDisplayMode(), s))));
		super.addBinding(new DirectBinding<Integer>(() -> displayHeight.get(), s -> displayHeight.set(s),
				() -> bean().getDisplayMode().getHeight(), s -> bean().setDisplayMode(GLDisplayMode.changeHeight(bean().getDisplayMode(), s))));
		super.addBinding(new DirectBinding<Boolean>(() -> fullscreen.isSelected(), s -> fullscreen.setSelected(s),
				() -> bean().getDisplayMode().isFullscreen(), s -> bean().setDisplayMode(GLDisplayMode.changeFullscreen(bean().getDisplayMode(), s))));
		super.addBinding(new DirectBinding<Boolean>(() -> resizable.isSelected(), s -> resizable.setSelected(s), () -> bean().isResizable(), s -> bean().setResizable(s)));
		
		super.addBinding(new DirectBinding<Float>(() -> viewportMinX.get(), s -> viewportMinX.set(s),
				() -> bean().getViewport().getMinX(), s -> bean().setViewport(GLViewport.changeMinX(bean().getViewport(), s))));
		super.addBinding(new DirectBinding<Float>(() -> viewportMinY.get(), s -> viewportMinY.set(s),
				() -> bean().getViewport().getMinY(), s -> bean().setViewport(GLViewport.changeMinY(bean().getViewport(), s))));
		super.addBinding(new DirectBinding<Float>(() -> viewportMaxX.get(), s -> viewportMaxX.set(s),
				() -> bean().getViewport().getMaxX(), s -> bean().setViewport(GLViewport.changeMaxX(bean().getViewport(), s))));
		super.addBinding(new DirectBinding<Float>(() -> viewportMaxY.get(), s -> viewportMaxY.set(s),
				() -> bean().getViewport().getMaxY(), s -> bean().setViewport(GLViewport.changeMaxY(bean().getViewport(), s))));
		
		super.addBinding(new DirectBinding<String>(() -> displayTitle.getText(), s -> displayTitle.setText(s), () -> bean().getTitle(), s -> bean().setTitle(s)));
		
		super.addBinding(new DirectBinding<Factory<? extends DataManager>>(() -> dataManager.getSelectionModel().getSelectedItem(),
				s -> dataManager.getSelectionModel().select(s), () -> bean().getDataManager(), s -> bean().setDataManager(s)));
		
		super.addBinding(new DirectBinding<Integer>(() -> glMajVer.get(), s -> glMajVer.set(s), () -> bean().getGlMajVer(), s -> bean().setGlMajVer(s)));
		super.addBinding(new DirectBinding<Integer>(() -> glMinVer.get(), s -> glMinVer.set(s), () -> bean().getGlMinVer(), s -> bean().setGlMinVer(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> forwardCompatible.isSelected(), s -> forwardCompatible.setSelected(s),
				() -> bean().isForwardCompatibility(), s -> bean().setForwardCompatibility(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> profileCore.isSelected(), s -> profileCore.setSelected(s), () -> bean().isProfileCore(), s -> bean().setProfileCore(s)));
		
		super.addBinding(new DirectBinding<Integer>(() -> sampleCount.get(), s -> sampleCount.set(s), () -> bean().getSampleCount(), s -> bean().setSampleCount(s)));
		super.addBinding(new DirectBinding<Integer>(() -> fpsSync.get(), s -> fpsSync.set(s), () -> bean().getFpsSync(), s -> bean().setFpsSync(s)));
		super.addBinding(new DirectBinding<Integer>(() -> bitDepth.get(), s -> bitDepth.set(s), () -> bean().getBitDepth(), s -> bean().setBitDepth(s)));
		
		super.addBinding(new DirectBinding<Boolean>(() -> vsync.isSelected(), s -> vsync.setSelected(s), () -> bean().isVsync(), s -> bean().setVsync(s)));
		super.addBinding(new DirectBinding<Boolean>(() -> showMouse.isSelected(), s -> showMouse.setSelected(s), () -> bean().isShowMouse(), s -> bean().setShowMouse(s)));
		
		super.calculateConstraints(StudioInterface.getInstantiation(), 8, 9);
	}
	
	private ProjectConfigurationBean bean() {
		return GLRequestSystem.getSceneManager().getConfigs().getConfigBean();
	}
	
	@Override
	protected void update() {
	}
	
	@Override
	public GridPane getPane() {
		return super.getPane();
	}
	
	@Override
	public String getTitle() {
		return super.getName();
	}
	
	@Override
	public int getDialogWidth() {
		return 680;
	}
	
	@Override
	public int getDialogHeight() {
		return 550;
	}
	
	@Override
	public Object getResult() {
		return null;
	}
	
	private HBox createDriverInterface(AdvancedSpinner spinner, int index) {
		HBox box = createGraphicButton(StudioViewRepository.IMAGE_DRIVER, true, 10, 10, spinner.getTooltip() + " Driver", (event) -> GLRequestSystem.request(() -> {
			ArrayList<Property<?>> drivers = new ArrayList<Property<?>>();
			drivers.add(bean().getClearColor().subProperty(index));
			GLRequestSystem.getSceneManager().getDriverSystem().open(spinner.getTooltip(), drivers);
		}), spinner.getFXSpinner());
		this.addBinding(new DirectBinding<Boolean>(() -> box.getChildren().get(1).getId().equals("highlight-button"), s -> box.getChildren().get(1)
				.setId(s ? "highlight-button" : "invisible-button"), () -> GLRequestSystem.getSceneManager().getDriverSystem().getCurrentDriverSubjects()
				.contains(bean().getClearColor().subProperty(index)), s -> {}));
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
	
	public static MenuItem toMenuItem() {
		MenuItem item = new MenuItem("Project Configurations");
		item.setOnAction(event -> MessageHandler.showAndWait(new ProjectConfigurationPanel
				(StudioInterface.getInstantiation()), new DialogOptionBuilder().withButtonType(ButtonType.OK)));
		return item;
	}
	
}
