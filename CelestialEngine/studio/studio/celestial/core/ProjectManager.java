package studio.celestial.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import celestial.serialization.Deserializer;
import celestial.serialization.Persistable;
import celestial.serialization.SerializationSystem;
import celestial.serialization.Serializer;
import celestial.serialization.SystemSerializers;
import celestial.util.Factory;
import celestial.util.ISetter;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.DirectoryChooser;
import studio.celestial.dialog.DialogOptionBuilder;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.dialog.DialogOptionBuilder.ButtonType;
import studio.celestial.impl.ProjectSetupPanel;
import studio.celestial.util.StudioUtil;

public class ProjectManager {
	
	private static final String INFO_FILE = "info.cel.gz";
	
	private final SceneManager sceneManager;
	private final SerializationSystem serializationSystem;
	
	private final File projectDirectory;
	private final Factory<Serializer> serializer;
	private final Factory<Deserializer> deserializer;
	
	public ProjectManager(SceneManager sceneManager, File projectDirectory) {
		this.sceneManager = sceneManager;
		this.serializationSystem = new SerializationSystem(new Persistable<?>[] {}, new Serializable[] {this.sceneManager});
		this.projectDirectory = projectDirectory;
		this.serializer = () -> new SystemSerializers.FilesystemSerializer(StudioUtil.subFile(projectDirectory.toString(), INFO_FILE));
		this.deserializer = () -> new SystemSerializers.FilesystemDeserializer(StudioUtil.subFile(projectDirectory.toString(), INFO_FILE));
	}
	
	public File getProjectDirectory() {
		return projectDirectory;
	}
	
	public Factory<Serializer> getSerializerFactory() {
		return serializer;
	}
	
	public Factory<Deserializer> getDeserializerFactory() {
		return deserializer;
	}
	
	public void saveProject() {
		saveProject(this.serializer);
	}
	
	public void saveProject(Factory<Serializer> serializerFactory) {
		Serializer serializer = serializerFactory.build();
		this.serializationSystem.serialize(serializer);
		serializer.close();
	}
	
	public SceneManager loadProject() {
		return loadProject(this.deserializer);
	}
	
	public SceneManager loadProject(Factory<Deserializer> deserializerFactory) {
		Deserializer deserializer = deserializerFactory.build();
		SceneManager sceneManager = (SceneManager) this.serializationSystem.deserialize(deserializer)[0];
		sceneManager.setProjectManager(new ProjectManager(sceneManager, deserializer instanceof SystemSerializers.FilesystemDeserializer
				? new File(((SystemSerializers.FilesystemDeserializer) deserializer).getPath()) : this.projectDirectory));
		deserializer.close();
		return sceneManager;
	}
	
	public void createProject() {
		if(projectDirectory.exists())
			throw new RuntimeException("Directory already exists");
		projectDirectory.mkdirs();
		
		try {
			new File(StudioUtil.subFile(projectDirectory.toString(), INFO_FILE)).createNewFile();
		}
		catch(IOException e) {
			throw new RuntimeException("Failed to create resources");
		}
		
		sceneManager.resetStudio();
		saveProject();
	}
	
	public static MenuItem[] toMenuItems(ISetter<SceneManager> sceneManagerSetter) {
		MenuItem newProject = new MenuItem("New Project");
		newProject.setOnAction(event -> {
			File path = MessageHandler.showAndWait(new ProjectSetupPanel(StudioInterface.getInstantiation()),
					new DialogOptionBuilder().withButtonTypes(ButtonType.OK, ButtonType.CANCEL));
			if(path != null) GLRequestSystem.request(() -> {
				ProjectManager manager = new ProjectManager(GLRequestSystem.getSceneManager(), path);
				GLRequestSystem.getSceneManager().setProjectManager(manager);
				manager.createProject();
			});
		});
		SeparatorMenuItem separator = new SeparatorMenuItem();
		MenuItem saveProject = new MenuItem("Save Project");
		saveProject.setOnAction(event -> GLRequestSystem.request(() -> GLRequestSystem.getSceneManager().getProjectManager().saveProject()));
		MenuItem openProject = new MenuItem("Open Project");
		openProject.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Open Project");
			File file = chooser.showDialog(StudioInterface.getInstantiation().getPrimaryScene().getWindow());
			if(file != null)
				GLRequestSystem.request(() -> sceneManagerSetter.set(GLRequestSystem.getSceneManager()
						.getProjectManager().loadProject(() -> new SystemSerializers.FilesystemDeserializer(StudioUtil.subFile(file.toString(), INFO_FILE)))));
		});
		
		return new MenuItem[] {
				newProject, separator, saveProject, openProject
		};
	}
	
}
