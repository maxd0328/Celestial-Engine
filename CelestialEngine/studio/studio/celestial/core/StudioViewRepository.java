package studio.celestial.core;

import javafx.scene.image.Image;
import studio.celestial.core.StudioView.ScaledPanel;
import studio.celestial.impl.*;

public class StudioViewRepository {
	
	public static final Image IMAGE_DRIVER = new Image("file:C:\\Celestial\\resources\\drive.png");
	public static final Image IMAGE_DELETE = new Image("file:C:\\Celestial\\resources\\delete.png");
	public static final Image IMAGE_INFO = new Image("file:C:\\Celestial\\resources\\info.png");
	public static final Image IMAGE_RENAME = new Image("file:C:\\Celestial\\resources\\rename.png");
	public static final Image IMAGE_VISIBLE = new Image("file:C:\\Celestial\\resources\\visible.png");
	public static final Image IMAGE_GRID = new Image("file:C:\\Celestial\\resources\\grid-icon.png");
	public static final Image IMAGE_PLACE = new Image("file:C:\\Celestial\\resources\\place-grid.png");
	public static final Image IMAGE_OVERLAY = new Image("file:C:\\Celestial\\resources\\overlay.png");
	public static final Image IMAGE_ORTHO = new Image("file:C:\\Celestial\\resources\\ortho.png");
	public static final Image IMAGE_ADD = new Image("file:C:\\Celestial\\resources\\add.png");
	public static final Image IMAGE_SUBTRACT = new Image("file:C:\\Celestial\\resources\\subtract.png");
	public static final Image IMAGE_VISIBLE_SIMPLE = new Image("file:C:\\Celestial\\resources\\visible-simple.png");
	public static final Image IMAGE_SELECT = new Image("file:C:\\Celestial\\resources\\select.png");
	public static final Image IMAGE_UP = new Image("file:C:\\Celestial\\resources\\up.png");
	public static final Image IMAGE_DOWN = new Image("file:C:\\Celestial\\resources\\down.png");
	public static final Image IMAGE_PLAY = new Image("file:C:\\Celestial\\resources\\play.png");
	public static final Image IMAGE_PAUSE = new Image("file:C:\\Celestial\\resources\\pause.png");
	public static final Image IMAGE_FOLDER = new Image("file:C:\\Celestial\\resources\\folder.png");
	public static final Image IMAGE_LOGO = new Image("file:C:\\Celestial\\resources\\logo.png");
	public static final Image IMAGE_LOGO_SMALL = new Image("file:C:\\Celestial\\resources\\logosmall.png");
	
	public static final StudioView STUDIO_VIEW_OBJECT_MODE;
	public static final StudioView STUDIO_VIEW_EDIT_MODE;
	public static final StudioView STUDIO_VIEW_VIEW_MODE;
	
	public static void ping() { // For class-loader
	}
	
	static {
		
		STUDIO_VIEW_OBJECT_MODE = new StudioView("Object Mode", 322, 23, 1261, 800, 4);
		
		STUDIO_VIEW_EDIT_MODE = new StudioView("Edit Mode", 322, 24, 944, 555, 4);
		
		STUDIO_VIEW_VIEW_MODE = new StudioView("View Mode", 5, 24, 1222, 798, 4);
		
		StudioComponentPanel __modifierRepository = new ModifierRepositoryPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __mediaLibrary = new MediaLibraryPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __sceneView = new SceneViewPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __objectEditor = new ObjectEditorPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __placementSettings = new PlacementSettingsPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __driverSettings = new DriverSettingsPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __hierarchyView = new HierarchyViewPanel(StudioInterface.getInstantiation());
		StudioComponentPanel __selectionSettings = new SelectionSettingsPanel(StudioInterface.getInstantiation());
		
		TabPaneComponentPanel __modifiersAndMedia = new TabPaneComponentPanel(StudioInterface.getInstantiation(), "Modifiers and Media", __modifierRepository, __mediaLibrary);
		TabPaneComponentPanel __modifiersMediaObject = new TabPaneComponentPanel(StudioInterface.getInstantiation(),
				"Modifiers/Media/Object", __modifierRepository, __mediaLibrary, __objectEditor);
		TabPaneComponentPanel __driverAndHierarchy = new TabPaneComponentPanel(StudioInterface.getInstantiation(), "Drivesrs and Hierarchy", __driverSettings, __hierarchyView);
		
		STUDIO_VIEW_OBJECT_MODE.addPanels(
				new ScaledPanel(__sceneView, 8, 0, 32, 26),
				new ScaledPanel(__placementSettings, 0, 8, 8, 18),
				new ScaledPanel(__hierarchyView, 0, 0, 8, 8)
		);
		
		STUDIO_VIEW_EDIT_MODE.addPanels(
				new ScaledPanel(__modifiersAndMedia, 0, 0, 8, 17),
				new ScaledPanel(__sceneView, 8, 0, 24, 17),
				new ScaledPanel(__objectEditor, 32, 0, 8, 17),
				new ScaledPanel(__selectionSettings, 0, 17, 18, 7),
				new ScaledPanel(__driverSettings, 18, 17, 12, 7),
				new ScaledPanel(__hierarchyView, 30, 17, 10, 7)
		);
		
		STUDIO_VIEW_VIEW_MODE.addPanels(
				new ScaledPanel(__sceneView, 0, 0, 31, 24),
				new ScaledPanel(__modifiersMediaObject, 31, 0, 9, 17),
				new ScaledPanel(__driverAndHierarchy, 31, 17, 9, 7)
		);
		
	}
	
	public static <T extends StudioComponentPanel> T getPanel(Class<T> type) {
		for(StudioView view : new StudioView[] {STUDIO_VIEW_OBJECT_MODE, STUDIO_VIEW_EDIT_MODE, STUDIO_VIEW_VIEW_MODE}){
			T panel = view.getPanel(type);
			if(panel != null) return panel;
		}
		return null;
	}
	
}
