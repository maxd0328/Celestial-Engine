package studio;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.lwjgl.input.Keyboard;
import celestial.core.EngineRuntime;
import celestial.data.DataManager;
import celestial.data.DataManager.ClusterDataManager;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.render.RenderConstraints;
import celestial.render.Renderer;
import celestial.render.impl.InstancedRenderer;
import celestial.shader.UnifiedShader;
import celestial.util.KVEntry;
import celestial.vecmath.Vector3f;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.ProjectManager;
import studio.celestial.core.StudioInterface;
import studio.celestial.core.StudioInterfaceInfo;
import studio.celestial.core.StudioViewRepository;
import studio.celestial.core.GLRequestSystem.IGLRequest;
import studio.celestial.dialog.DialogOptionBuilder;
import studio.celestial.dialog.DialogOptionBuilder.ButtonType;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.impl.ObjectEditorPanel;
import studio.celestial.impl.PerformanceLoggerPanel;
import studio.celestial.impl.ProjectConfigurationPanel;
import studio.celestial.impl.StudioSetupPanel;
import studio.celestial.core.SceneManager;
import studio.celestial.core.SplashScreen;
import studio.celestial.util.FXMenuBar;

public class CelestialGameStudio {
	
	private static final int WIDTH = 1600;
	private static final int HEIGHT = 900;
	
	private static final int HGAP = 10;
	private static final int VGAP = 10;
	private static final int INSET = 10;
	
	private static final int UPDATE_INTERVAL = 10;
	
	private static SceneManager sceneManager;
	
	public static void main(String... args) throws InterruptedException {
		
		Thread.currentThread().setName("CEStudio Application Thread");
		FXMenuBar menuBar = new FXMenuBar(
				new Menu("File"),
				new Menu("Edit"),
				new Menu("View"),
				new Menu("Tools"),
				new Menu("Options"),
				new Menu("Project"),
				new Menu("Help")
		);
		
		StudioInterfaceInfo info = new StudioInterfaceInfo("Celestial Game Studio",
				"C:\\Celestial\\icon0.png",
				CelestialGameStudio.class.getResource("style.css").toExternalForm(),
				WIDTH, HEIGHT, HGAP, VGAP, INSET, UPDATE_INTERVAL, menuBar);
		new Thread(() -> StudioInterface.instantiate(info, args)).start();
		
		while(StudioInterface.getInstantiation() == null);
		
		menuBar.getMenu("File").getItems().addAll(ProjectManager.toMenuItems(s -> sceneManager = s));
		menuBar.getMenu("Tools").getItems().add(PerformanceLoggerPanel.toMenuItem());
		menuBar.getMenu("Project").getItems().add(ProjectConfigurationPanel.toMenuItem());
		
		SplashScreen splashScreen = new SplashScreen(500, 281, "file:C:\\Celestial\\resources\\splashscreen.png", "\u00A9 Max Derbenwick 2020", "Loading...");
		splashScreen.show();
		Thread.sleep(5000);
		for(int i = 0 ; i < 5 ; ++i) {
			splashScreen.setShowLoadingText(i % 2 == 0);
			Thread.sleep(2000);
		}
		
		ModifierLoader.loadModifiers();
		splashScreen.hide();
		
		File projectDirectory;
		boolean projectExists;
		
		FutureTask<KVEntry<File, Boolean>> query = new FutureTask<>(() -> MessageHandler.showAndWait(new StudioSetupPanel
				(StudioInterface.getInstantiation()), new DialogOptionBuilder().withButtonTypes(ButtonType.EXIT)));
		Platform.runLater(query);
		try {
			KVEntry<File, Boolean> projectPath = query.get();
			if(projectPath == null) System.exit(0);
			projectDirectory = projectPath.getKey();
			projectExists = projectPath.getValue();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		
		StudioInterface.getInstantiation().request((studio) -> studio.setView(StudioViewRepository.STUDIO_VIEW_OBJECT_MODE));
		StudioInterface.getInstantiation().show();
		StudioInterface.getInstantiation().requestFocus();
		
		DataManager dataManager = new ClusterDataManager();
		EngineRuntime.dispSetParent(StudioInterface.getInstantiation().getCanvas());
		EngineRuntime.create(new GLDisplayMode(1920, 1080, false), new GLViewport(0, 0, 1, 1), 3, 2, true, true, 4, 24, dataManager);
		
		UnifiedShader shader = new UnifiedShader(450).withDefaults();
		Renderer renderer = new InstancedRenderer(shader, new Vector3f(0.25f, 0.25f, 0.25f));
		GLRequestSystem.put(dataManager, renderer, shader, null);
		renderer.setPaused(true);
		sceneManager = new SceneManager(projectDirectory, SceneManager.createStudioScene("Default Scene"));
		
		if(!projectExists)
			sceneManager.getProjectManager().createProject();
		else 
			sceneManager = sceneManager.getProjectManager().loadProject();
		
		while(!EngineRuntime.isCloseRequested() && StudioInterface.getInstantiation().getFrame().isShowing()) {
			
			GLDisplayMode dm = new GLDisplayMode(StudioInterface.getInstantiation().getCanvas().getWidth(), StudioInterface.getInstantiation().getCanvas().getHeight(), false);
			if(!dm.equals(EngineRuntime.dispGetDisplayMode())) EngineRuntime.dispResize(dm);
			StudioViewRepository.STUDIO_VIEW_EDIT_MODE.getPanel(ObjectEditorPanel.class).validateBounds();
			
			if(renderer.getLogger().isResultAvailable()) {
				sceneManager.getConfigs().pause();
				StudioInterface.getInstantiation().request(studio -> MessageHandler.show(new PerformanceLoggerPanel(studio,
						renderer.getLogger().getResults(), renderer.getLogger().getInitialFrameCount()), new DialogOptionBuilder().withButtonType(ButtonType.OK)));
			}
			
			renderer.render(sceneManager.getCurrentScene(), "camera.studio", new RenderConstraints());
			EngineRuntime.update(60);
			
			sceneManager.update();
			GLRequestSystem.put(dataManager, renderer, shader, sceneManager);
			for(IGLRequest request : GLRequestSystem.pollRequests()) request.perform();
			Keyboard.isKeyDown(0); // Ping keyboard
			
		}
		
		EngineRuntime.destroy();
		System.exit(0);
		
	}
	
}
