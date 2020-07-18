package studio.celestial.impl;

import java.io.File;
import celestial.data.DataManager;
import celestial.data.DataManager.ClusterDataManager;
import celestial.data.DataManager.DynamicDataManager;
import celestial.data.DataManager.StaticDataManager;
import celestial.util.Factory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.ComplexDialog;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.util.AdvancedIntegerSpinner;
import studio.celestial.util.StudioUtil;

public class ProjectSetupPanel extends StudioComponentPanel implements ComplexDialog<File> {
	
	private TextField projectName = null;
	private TextField projectDir = null;
	
	public ProjectSetupPanel(StudioInterface studio) {
		super(studio, "New Project", 20, 10, 20, 20, 20);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		projectName = new TextField("New Project");
		projectName.setPromptText("Project Name");
		super.getPane().add(new Label("Project Name"), 0, 0, 3, 1);
		super.getPane().add(projectName, 3, 0, 10, 1);
		
		TextField displayTitle = new TextField("Game");
		displayTitle.setPromptText("Display Title");
		super.getPane().add(new Label("Display Title"), 0, 1, 3, 1);
		super.getPane().add(displayTitle, 3, 1, 10, 1);
		
		this.projectDir = new TextField(System.getProperty("user.home"));
		this.projectDir.setEditable(false);
		this.projectDir.setPromptText("Project Directory");
		super.getPane().add(new Label("Project Directory"), 0, 2, 3, 1);
		super.getPane().add(projectDir, 3, 2, 9, 1);
		
		ImageView browseView = new ImageView(StudioViewRepository.IMAGE_FOLDER);
		browseView.setFitWidth(20);
		browseView.setFitHeight(20);
		Button browse = new Button();
		browse.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			File file = chooser.showDialog(MessageHandler.getDialogs().get(this).getDialogPane().getScene().getWindow());
			if(file != null && file.exists() && file.isDirectory())
				projectDir.setText(file.toString());
		});
		GridPane.setMargin(browse, new Insets(0, 0, 0, -15));
		browse.setPadding(new Insets(3, 9, 3, 9));
		browse.setGraphic(browseView);
		super.getPane().add(browse, 12, 2, 1, 1);
		
		ImageView logoSmall = new ImageView(StudioViewRepository.IMAGE_LOGO_SMALL);
		logoSmall.setFitWidth(100);
		logoSmall.setFitHeight(100);
		super.getPane().add(logoSmall, 13, 0, 3, 3);
		
		Separator separator = new Separator();
		GridPane.setMargin(separator, new Insets(0, -5, 0, -5));
		super.getPane().add(separator, 0, 3, 16, 1);
		
		AdvancedIntegerSpinner displayWidth = new AdvancedIntegerSpinner(0, Integer.MAX_VALUE, 800, 1, "Display Width");
		AdvancedIntegerSpinner displayHeight = new AdvancedIntegerSpinner(0, Integer.MAX_VALUE, 600, 1, "Display Height");
		CheckBox fullscreen = new CheckBox("Fullscreen");
		CheckBox resizable = new CheckBox("Resizable");
		super.getPane().add(displayWidth.getFXSpinner(), 3, 4, 4, 1);
		super.getPane().add(displayHeight.getFXSpinner(), 3, 5, 4, 1);
		super.getPane().add(fullscreen, 3, 6, 4, 1);
		super.getPane().add(resizable, 3, 7, 4, 1);
		super.getPane().add(new Label("Display"), 0, 4, 3, 1);
		
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
		dataManager.getSelectionModel().select(0);
		super.getPane().add(dataManager, 3, 9, 4, 1);
		super.getPane().add(new Label("Data Manager"), 0, 9, 3, 1);
		
		AdvancedIntegerSpinner glMajVer = new AdvancedIntegerSpinner(1, 5, 3, 1, "GL Major Version");
		AdvancedIntegerSpinner glMinVer = new AdvancedIntegerSpinner(0, 9, 2, 1, "GL Minor Version");
		CheckBox forwardCompatible = new CheckBox("Forward Compatible");
		forwardCompatible.setSelected(true);
		CheckBox profileCore = new CheckBox("Use Profile Core");
		profileCore.setSelected(true);
		super.getPane().add(glMajVer.getFXSpinner(), 12, 4, 4, 1);
		super.getPane().add(glMinVer.getFXSpinner(), 12, 5, 4, 1);
		super.getPane().add(forwardCompatible, 12, 6, 4, 1);
		super.getPane().add(profileCore, 12, 7, 4, 1);
		super.getPane().add(new Label("GL Version"), 9, 4, 3, 1);
		
		AdvancedIntegerSpinner sampleCount = new AdvancedIntegerSpinner(1, 128, 4, 1, "Sample Count");
		super.getPane().add(sampleCount.getFXSpinner(), 12, 9, 4, 1);
		super.getPane().add(new Label("Sample Count"), 9, 9, 3, 1);
		
		AdvancedIntegerSpinner bitDepth = new AdvancedIntegerSpinner(1, 128, 24, 1, "Bit Depth");
		super.getPane().add(bitDepth.getFXSpinner(), 12, 10, 4, 1);
		super.getPane().add(new Label("Bit Depth"), 9, 10, 3, 1);
		
		AdvancedIntegerSpinner fpsSync = new AdvancedIntegerSpinner(1, Integer.MAX_VALUE, 60, 1, "FPS Sync");
		super.getPane().add(fpsSync.getFXSpinner(), 12, 12, 4, 1);
		super.getPane().add(new Label("FPS Sync"), 9, 12, 3, 1);
		
		CheckBox vsync = new CheckBox("Use Vsync");
		CheckBox showMouse = new CheckBox("Show Cursor");
		showMouse.setSelected(true);
		super.getPane().add(vsync, 3, 11, 4, 1);
		super.getPane().add(showMouse, 3, 12, 4, 1);
		super.getPane().add(new Label("Miscellaneous"), 0, 11, 3, 1);
		
		super.calculateConstraints(StudioInterface.getInstantiation(), 5, 5);
		super.getPane().setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #3A3A3A, #2A2A2A)");
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
		return 700;
	}
	
	@Override
	public int getDialogHeight() {
		return 510;
	}
	
	@Override
	public File getResult() {
		return new File(StudioUtil.subFile(projectDir.getText(), projectName.getText()));
	}
	
}
