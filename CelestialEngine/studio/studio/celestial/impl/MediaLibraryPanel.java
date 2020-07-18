package studio.celestial.impl;

import java.io.File;
import java.util.Collection;
import celestial.data.AudioBuffer;
import celestial.data.DataUtil;
import celestial.data.ImageBuffer;
import celestial.data.ImageSampler;
import celestial.data.VertexBuffer;
import celestial.util.KVEntry;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.dialog.ComplexDialog;
import studio.celestial.dialog.DoubleTextInputDialog;
import studio.celestial.dialog.MediaSelectorDialog;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageDialog.MessageType;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.dialog.SFInputDialog;
import studio.celestial.dialog.SSFInputDialog;
import studio.celestial.media.Media;
import studio.celestial.media.Media.MediaType;
import studio.celestial.media.MediaBox;
import studio.celestial.media.MediaFolder;
import studio.celestial.media.MediaNode;
import studio.celestial.media.Resource;
import studio.celestial.media.Resource.FileSource;
import studio.celestial.media.Resource.StringSource;

public final class MediaLibraryPanel extends StudioComponentPanel implements ComplexDialog<Resource> {
	
	public MediaLibraryPanel(StudioInterface studio) {
		super(studio, "Media Library", 5, 5, 6, 2, 6);
	}
	
	private TreeView<MediaBox> mediaList;
	private TreeItem<MediaBox> mediaRoot;
	private TextField mediaSearch;
	
	@Override
	protected void initialize() {
		mediaSearch = new TextField();
		mediaSearch.setPromptText("Search Media");
		super.getPane().add(mediaSearch, 0, 0, 16, 2);
		
		mediaList = new TreeView<MediaBox>();
		super.getPane().add(mediaList, 0, 2, 16, 28);
		
		mediaRoot = new TreeItem<MediaBox>(GLRequestSystem.getSceneManager().getMediaLibrary().getRoot().getMediaBox());
		mediaList.setRoot(mediaRoot);
		mediaList.setShowRoot(false);
		
		Button newFolder = new Button("Create Folder");
		newFolder.setMaxWidth(Double.MAX_VALUE);
		newFolder.setMaxHeight(Double.MAX_VALUE);
		newFolder.setOnAction(event -> {
			String name = MessageHandler.showAndWait("New Folder", "Enter folder name:", "New Folder");
			if(name == null) return;
			if(mediaList.getSelectionModel().getSelectedItem() != null && mediaList.getSelectionModel().getSelectedItem().getValue().getNode() instanceof MediaFolder)
				GLRequestSystem.request(() -> ((MediaFolder) mediaList.getSelectionModel().getSelectedItem().getValue().getNode()).getChildren().add(new MediaFolder(name)));
			else
				GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().getMediaLibrary().getRoot().getChildren().add(new MediaFolder(name)));
		});
		super.getPane().add(newFolder, 0, 30, 6, 4);
		
		Button importMedia = new Button("Import Media");
		importMedia.setMaxWidth(Double.MAX_VALUE);
		importMedia.setMaxHeight(Double.MAX_VALUE);
		importMedia.setOnAction(event -> {
			MediaType type = MessageHandler.showAndWait(new MediaSelectorDialog(StudioInterface.getInstantiation(), MediaType.toTypeArray()));
			if(type != null) {
				switch(type) {
				case DAE:
					// TODO DAE not yet supported
					break;
				case HMAP:
					KVEntry<String, KVEntry<String, Integer>> hmap = MessageHandler.showAndWait(new SSFInputDialog
							(StudioInterface.getInstantiation(), "New HMAP-Model", "Heightmap Name", "Heightmap Path", "Array Count", 1, Integer.MAX_VALUE));
					if(hmap != null) {
						if(!exists(hmap.getValue().getKey())) MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "No such file exists"));
						else addResource(new Media(hmap.getKey(), type, DataUtil.fromHMAP
								(hmap.getValue().getKey(), VertexBuffer.DRAW_TYPE_STATIC, hmap.getValue().getValue())));
					}
					break;
				case OBJ:
					KVEntry<String, String> obj = MessageHandler.showAndWait(new DoubleTextInputDialog(StudioInterface.getInstantiation(), "New OBJ-Model", "Model Name", "Model Path"));
					if(obj != null) {
						if(!exists(obj.getValue())) MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "No such file exists"));
						else addResource(new Media(obj.getKey(), type, DataUtil.fromOBJ(obj.getValue(), VertexBuffer.DRAW_TYPE_STATIC)));
					}
					break;
				case PNG:
					KVEntry<String, String> png = MessageHandler.showAndWait(new DoubleTextInputDialog(StudioInterface.getInstantiation(), "New Image", "Image Name", "Image Path"));
					if(png != null) {
						if(!exists(png.getValue())) MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "No such file exists"));
						else addResource(new Media(png.getKey(), type, ImageSampler.create(png.getValue())));
					}
					break;
				case VAO:
					KVEntry<String, KVEntry<String, Integer>> vao = MessageHandler.showAndWait(new SSFInputDialog
							(StudioInterface.getInstantiation(), "New VAO-Model", "Model Name", "Model Path", "Array Count", 1, Integer.MAX_VALUE));
					if(vao != null) {
						if(!exists(vao.getValue().getKey())) MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "No such file exists"));
						else addResource(new Media(vao.getKey(), type, DataUtil.fromVAO(vao.getValue().getKey(), VertexBuffer.DRAW_TYPE_STATIC, vao.getValue().getValue())));
					}
					break;
				case WAV:
					KVEntry<String, String> wav = MessageHandler.showAndWait(new DoubleTextInputDialog(StudioInterface.getInstantiation(), "New Wave Sound", "Sound Name", "Sound Path"));
					if(wav != null) {
						if(!exists(wav.getValue())) MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "No such file exists"));
						else addResource(new Media(wav.getKey(), type, AudioBuffer.create(wav.getValue())));
					}
					break;
				}
			}
		});
		super.getPane().add(importMedia, 6, 30, 5, 2);
		
		Button importResource = new Button("Import Resource");
		importResource.setMaxWidth(Double.MAX_VALUE);
		importResource.setMaxHeight(Double.MAX_VALUE);
		importResource.setOnAction(event -> {
			KVEntry<String, String> e = MessageHandler.showAndWait(new DoubleTextInputDialog(StudioInterface.getInstantiation(), "New Resource", "Resource Name", "Resource Path"));
			if(e != null) {
				if(!exists(e.getValue())) MessageHandler.show(new MessageDialog(MessageType.MESSAGE_TYPE_ERROR, "No such file exists"));
				else addResource(new Resource(e.getKey(), new FileSource(e.getValue())));
			}
		});
		super.getPane().add(importResource, 11, 30, 5, 2);
		
		Button newMedia = new Button("Create Media");
		newMedia.setMaxWidth(Double.MAX_VALUE);
		newMedia.setMaxHeight(Double.MAX_VALUE);
		newMedia.setOnAction(event -> {
			MediaType type = MessageHandler.showAndWait(new MediaSelectorDialog(StudioInterface.getInstantiation(), MediaType.toEditableTypeArray()));
			if(type != null) {
				switch(type) {
				case VAO:
					KVEntry<String, Integer[]> vao = MessageHandler.showAndWait(new SFInputDialog(StudioInterface
							.getInstantiation(), "New VAO-Model", "Model Name", 1, Integer.MAX_VALUE, "Array Length", "Index Count", "Vertex Count"));
					if(vao != null) addResource(new Media(vao.getKey(), type,
							DataUtil.newVAO(VertexBuffer.DRAW_TYPE_STATIC, vao.getValue()[0], vao.getValue()[1], vao.getValue()[2])));
					break;
				case HMAP:
					KVEntry<String, Integer[]> hmap = MessageHandler.showAndWait(new SFInputDialog(StudioInterface
							.getInstantiation(), "New VAO-Model", "Model Name", 1, Integer.MAX_VALUE, "Array Length", "Width", "Height"));
					if(hmap != null) addResource(new Media(hmap.getKey(), type,
							DataUtil.newHMAP(VertexBuffer.DRAW_TYPE_STATIC, hmap.getValue()[0], hmap.getValue()[1], hmap.getValue()[2])));
					break;
				case PNG:
					KVEntry<String, Integer[]> png = MessageHandler.showAndWait(new SFInputDialog(StudioInterface
							.getInstantiation(), "New Image", "Image Name", 1, Integer.MAX_VALUE, "Width", "Height"));
					if(png != null) addResource(new Media(png.getKey(), type, ImageSampler.create(new ImageBuffer(png.getValue()[0], png.getValue()[1]))));
					break;
				default: break;
				}
			}
		});
		super.getPane().add(newMedia, 6, 32, 5, 2);
		
		Button newResource = new Button("Create Resource");
		newResource.setMaxWidth(Double.MAX_VALUE);
		newResource.setMaxHeight(Double.MAX_VALUE);
		newResource.setOnAction(event -> {
			String name = MessageHandler.showAndWait("New Resource", "Enter resource name:", "New Resource");
			if(name != null) addResource(new Resource(name, new StringSource("")));
		});
		super.getPane().add(newResource, 11, 32, 5, 2);
		
		super.calculateConstraints(StudioInterface.getInstantiation(), 8, 17);
	}
	
	@Override
	protected void update() {
		syncMediaFolder(mediaRoot, GLRequestSystem.getSceneManager().getMediaLibrary().getRoot());
	}
	
	@Override
	public GridPane getPane() {
		return super.getPane();
	}
	
	@Override
	public String getTitle() {
		return "Select Media";
	}
	
	@Override
	public int getDialogWidth() {
		return 400;
	}
	
	@Override
	public int getDialogHeight() {
		return 700;
	}
	
	@Override
	public Resource getResult() {
		if(mediaList.getSelectionModel().getSelectedItem() != null && mediaList.getSelectionModel().getSelectedItem().getValue().getNode() instanceof Resource)
			return (Resource) mediaList.getSelectionModel().getSelectedItem().getValue().getNode();
		else return null;
	}
	
	private boolean syncMediaFolder(TreeItem<MediaBox> view, MediaFolder folder) {
		view.getChildren().removeIf(childView -> !folder.containsMediaBox(childView.getValue()));
		view.getChildren().removeIf(childView -> {
			if(childView.getValue().getNode() instanceof MediaFolder) return !syncMediaFolder(childView, (MediaFolder) childView.getValue().getNode());
			else return mediaSearch.getText().length() > 0 && !childView.getValue().getNode().getName().toLowerCase().contains(mediaSearch.getText().trim().toLowerCase());
		});
		
		for(MediaFolder childFolder : folder.getChildren()) {
			if(!contains(view.getChildren(), childFolder)) {
				TreeItem<MediaBox> newItem = new TreeItem<>(childFolder.getMediaBox());
				newItem.setExpanded(true);
				view.getChildren().add(newItem);
				boolean rem = !syncMediaFolder(newItem, childFolder);
				if(rem) view.getChildren().remove(newItem);
			}
		}
		
		for(Resource resource : folder.getResources()) {
			if((mediaSearch.getText().length() == 0 || resource.getName().toLowerCase()
					.contains(mediaSearch.getText().trim().toLowerCase())) && !contains(view.getChildren(), resource))
				view.getChildren().add(new TreeItem<>(resource.getMediaBox()));
		}
		
		return view.getChildren().size() > 0 || mediaSearch.getText().length() == 0;
	}
	
	private boolean contains(Collection<TreeItem<MediaBox>> viewChildren, MediaNode node) {
		for(TreeItem<MediaBox> box : viewChildren) if(box.getValue() == node.getMediaBox()) return true;
		return false;
	}
	
	private boolean exists(String path) {
		File f = new File(path);
		return f.exists() && f.isFile();
	}
	
	private void addResource(Resource resource) {
		if(mediaList.getSelectionModel().getSelectedItem() != null && mediaList.getSelectionModel().getSelectedItem().getValue().getNode() instanceof MediaFolder)
			GLRequestSystem.request(() -> ((MediaFolder) mediaList.getSelectionModel().getSelectedItem().getValue().getNode()).getResources().add(resource));
		else
			GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().getMediaLibrary().getRoot().getResources().add(resource));
	}
	
}
