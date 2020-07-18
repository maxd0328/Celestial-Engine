package studio.celestial.impl;

import java.io.File;
import celestial.util.KVEntry;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.dialog.ComplexDialog;
import studio.celestial.dialog.DialogOptionBuilder;
import studio.celestial.dialog.DialogOptionBuilder.ButtonType;
import studio.celestial.dialog.MessageHandler;

public class StudioSetupPanel extends StudioComponentPanel implements ComplexDialog<KVEntry<File, Boolean>> {
	
	private File result = null;
	private boolean exists = false;
	
	public StudioSetupPanel(StudioInterface studio) {
		super(studio, "Celestial Game Studio", 10, 10, 20, 5, 5);
	}
	
	@Override
	protected void initialize() {
		ImageView image = new ImageView(StudioViewRepository.IMAGE_LOGO);
		GridPane.setMargin(image, new Insets(-5, 0, -15, 70));
		image.setFitWidth(470);
		image.setFitHeight(200);
		super.getPane().add(image, 0, 0, 2, 1);
		super.getPane().setStyle("-fx-background-color: linear-gradient(from 0% 10% to 100% 50%, #4A4A4A, #222222)");
		
		Separator sep0 = new Separator(Orientation.HORIZONTAL);
		GridPane.setMargin(sep0, new Insets(0, -5, 0, -20));
		super.getPane().add(sep0, 0, 1, 2, 1);
		Separator sep1 = new Separator(Orientation.HORIZONTAL);
		GridPane.setMargin(sep1, new Insets(0, -20, 0, -5));
		super.getPane().add(sep1, 2, 1, 2, 1);
		
		Label project = new Label("Project");
		project.setFont(new Font(16.0));
		super.getPane().add(project, 0, 2, 1, 1);
		
		Label recents = new Label("Recents");
		recents.setFont(new Font(16.0));
		super.getPane().add(recents, 2, 2, 1, 1);
		
		ImageView newView = new ImageView(StudioViewRepository.IMAGE_ADD);
		newView.setFitWidth(30);
		newView.setFitHeight(30);
		ImageView openView = new ImageView(StudioViewRepository.IMAGE_FOLDER);
		openView.setFitWidth(30);
		openView.setFitHeight(30);
		ImageView exportView = new ImageView(StudioViewRepository.IMAGE_DRIVER);
		exportView.setFitWidth(30);
		exportView.setFitHeight(30);
		
		Button newProject = new Button("  New Project");
		newProject.setGraphic(newView);
		newProject.setId("invisible-button");
		newProject.setFont(new Font(13.0));
		newProject.setMaxWidth(Double.MAX_VALUE);
		newProject.setMaxHeight(Double.MAX_VALUE);
		newProject.setOnAction(event -> {
			File path = MessageHandler.showAndWait(new ProjectSetupPanel(StudioInterface.getInstantiation()),
					new DialogOptionBuilder().withButtonTypes(ButtonType.OK, ButtonType.CANCEL));
			if(path != null) {
				result = path;
				exists = false;
				MessageHandler.acceptAll();
			}
		});
		super.getPane().add(newProject, 0, 3, 2, 6);
		
		Button openProject = new Button("  Open Project");
		openProject.setGraphic(openView);
		openProject.setId("invisible-button");
		openProject.setFont(new Font(13.0));
		openProject.setMaxWidth(Double.MAX_VALUE);
		openProject.setMaxHeight(Double.MAX_VALUE);
		openProject.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Open Project");
			File path = chooser.showDialog(MessageHandler.getDialogs().get(this).getDialogPane().getScene().getWindow());
			if(path != null) {
				result = path;
				exists = true;
				MessageHandler.acceptAll();
			}
		});
		super.getPane().add(openProject, 0, 9, 2, 6);
		
		Button exportProject = new Button("  Export Project");
		exportProject.setGraphic(exportView);
		exportProject.setId("invisible-button");
		exportProject.setFont(new Font(13.0));
		exportProject.setMaxWidth(Double.MAX_VALUE);
		exportProject.setMaxHeight(Double.MAX_VALUE);
		super.getPane().add(exportProject, 0, 15, 2, 6);
		
		TreeView<String> recentTable = new TreeView<String>();
		recentTable.setMaxWidth(Double.MAX_VALUE);
		recentTable.setPrefWidth(Double.MAX_VALUE);
		super.getPane().add(recentTable, 2, 3, 2, 18);
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
		return 475;
	}
	
	@Override
	public KVEntry<File, Boolean> getResult() {
		return result == null ? null : new KVEntry<>(result, exists);
	}
	
}
