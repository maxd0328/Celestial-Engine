package studio.celestial.media;

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
import studio.celestial.media.Media.MediaType;
import studio.celestial.dialog.MessageDialog.MessageType;

public class MediaBox extends HBox {
	
	private final MediaNode node;
	private final Label label;
	
	private String identifier;
	
	public MediaBox(MediaNode node) {
		this(node, null);
	}
	
	public MediaBox(MediaNode node, MediaType type) {
		this.node = node;
		this.identifier = node.getName();
		this.label = new Label((node instanceof Media ? "(Media " + type + ")  " : !(node instanceof MediaFolder) ?  "(Resource)  " : "") + identifier);
		
		ImageView renameView = new ImageView(StudioViewRepository.IMAGE_RENAME);
		renameView.setFitWidth(12);
		renameView.setFitHeight(12);
		Button rename = new Button();
		rename.setGraphic(renameView);
		rename.setId("invisible-button");
		rename.setPadding(new Insets(0, 5, 0, 5));
		rename.setTooltip(new Tooltip("Rename Resource"));
		rename.setOnAction(event -> {
			String name = MessageHandler.showAndWait("Rename", "Rename resource:", node.getName());
			if(name != null) GLRequestSystem.request(() -> node.setName(name));
		});
		
		ImageView deleteView = new ImageView(StudioViewRepository.IMAGE_DELETE);
		deleteView.setFitWidth(12);
		deleteView.setFitHeight(12);
		Button delete = new Button();
		delete.setGraphic(deleteView);
		delete.setId("invisible-button");
		delete.setPadding(new Insets(0, 5, 0, 5));
		delete.setTooltip(new Tooltip("Delete Resource"));
		delete.setOnAction(event -> {
			boolean confirm = MessageHandler.showAndWait(new MessageDialog(MessageType.MESSAGE_TYPE_CONFIRMATION, "Delete resource?"));
			if(confirm) GLRequestSystem.request(() -> node.delete());
		});
		
		Region region = new Region();
		HBox.setHgrow(region, Priority.ALWAYS);
		
		super.getChildren().addAll(label, region, rename, delete);
		
		super.setSpacing(0);
		super.setAlignment(Pos.CENTER_LEFT);
		super.setPadding(new Insets(0));
	}
	
	public MediaNode getNode() {
		return node;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		this.label.setText((node instanceof Media ? "(Media " + ((Media) node).getType() + ")  " : !(node instanceof MediaFolder) ?  "(Resource)  " : "") + identifier);
	}
	
}
