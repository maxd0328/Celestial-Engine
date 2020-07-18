package studio.celestial.core;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.lwjgl.input.Keyboard;
import celestial.core.Modifier;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.core.ObjectConstraints;
import celestial.core.SystemInput;
import celestial.core.CEObjectReference;
import celestial.data.ImageSampler;
import celestial.glutil.RotationPattern;
import celestial.render.UpdatePacket;
import celestial.scene.Layer;
import celestial.scene.Scene;
import celestial.serialization.SerializerImpl;
import celestial.util.Event;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import mod.celestial.filter.AlphaModifier;
import mod.celestial.filter.FogEffectModifier;
import mod.celestial.filter.FragFogEffectModifier;
import mod.celestial.filter.StaticColorModifier;
import mod.celestial.filter.TintModifier;
import mod.celestial.mesh.CylinderMeshModifier;
import mod.celestial.mesh.PlaneMeshModifier;
import mod.celestial.mesh.PointMeshModifier;
import mod.celestial.mesh.CrossSectionMeshModifier;
import mod.celestial.misc.CameraModifier;
import mod.celestial.texture.DiffuseMapModifier;
import studio.celestial.core.ProjectConfigurations.ProjectConfigurationBean;
import studio.celestial.driver.DriverEditorFactory;
import studio.celestial.driver.StudioDriverSystem;
import studio.celestial.impl.ObjectEditorPanel;
import studio.celestial.impl.SceneViewPanel;
import studio.celestial.media.MediaLibrary;
import studio.celestial.util.OrbitalTransformModifier;
import studio.celestial.util.StaticSizeModifier;

public final class SceneManager implements ISceneSystem, java.io.Serializable {
	
	private static final long serialVersionUID = -4702414515394887041L;
	
	private static final CEObject OBJECT_ICON;
	private static final CEObject OBJECT_RADIUS;
	
	private static final CEObject OBJECT_ICON_SELECTED;
	private static final CEObject OBJECT_RADIUS_SELECTED;
	
	private static final CEObject RELATIONSHIP_LINE;
	
	public static final int GRID_MIN = -300, GRID_MAX = 300;
	
	static {
		OBJECT_ICON = new CEObject("icon.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		OBJECT_ICON.addModifier(new DiffuseMapModifier(ImageSampler.create("C:\\Celestial\\resources\\object.png"), 0, 1, 1, 1, false, 0, new Vector2f(), false, false));
		OBJECT_ICON.addModifier(new PointMeshModifier(false, false, new Vector3f()));
		OBJECT_ICON.addModifier(new StaticSizeModifier(new Vector3f(0.014f, 0.014f, 1f)));
		OBJECT_ICON.getModifier(PointMeshModifier.class).setBlendMode(PointMeshModifier.BLEND_MODE_ALPHA);
		OBJECT_RADIUS = new CEObject("radius.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		OBJECT_RADIUS.addModifier(new DiffuseMapModifier(ImageSampler.create("C:\\Celestial\\resources\\radius.png"), 0, 1, 1, 1, false, 0, new Vector2f(), false, false));
		OBJECT_RADIUS.addModifier(new PointMeshModifier(false, false, new Vector3f()));
		OBJECT_RADIUS.getModifier(PointMeshModifier.class).setBlendMode(PointMeshModifier.BLEND_MODE_ALPHA);
		
		OBJECT_ICON_SELECTED = new CEObject("Sicon.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		OBJECT_ICON_SELECTED.addModifier(new DiffuseMapModifier(ImageSampler.create("C:\\Celestial\\resources\\object.png"), 0, 1, 1, 1, false, 0, new Vector2f(), false, false));
		OBJECT_ICON_SELECTED.addModifier(new TintModifier(new Vector3f(94.9f / 100f, 75.0f / 100f, 16.1f / 100f), 0.5f));
		OBJECT_ICON_SELECTED.addModifier(new PointMeshModifier(false, false, new Vector3f()));
		OBJECT_ICON_SELECTED.addModifier(new StaticSizeModifier(new Vector3f(0.014f, 0.014f, 1f)));
		OBJECT_ICON_SELECTED.getModifier(PointMeshModifier.class).setBlendMode(PointMeshModifier.BLEND_MODE_ALPHA);
		OBJECT_RADIUS_SELECTED = new CEObject("Sradius.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		OBJECT_RADIUS_SELECTED.addModifier(new DiffuseMapModifier(ImageSampler.create("C:\\Celestial\\resources\\radius.png"), 0, 1, 1, 1, false, 0, new Vector2f(), false, false));
		OBJECT_RADIUS_SELECTED.addModifier(new TintModifier(new Vector3f(94.9f / 100f, 75.0f / 100f, 16.1f / 100f), 0.5f));
		OBJECT_RADIUS_SELECTED.addModifier(new PointMeshModifier(false, false, new Vector3f()));
		OBJECT_RADIUS_SELECTED.getModifier(PointMeshModifier.class).setBlendMode(PointMeshModifier.BLEND_MODE_ALPHA);
		
		RELATIONSHIP_LINE = new CEObject("relationship.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		RELATIONSHIP_LINE.addModifier(new StaticColorModifier(new Vector3f(0.5f, 0.5f, 0.5f)));
		RELATIONSHIP_LINE.addModifier(new CylinderMeshModifier(true, false, new Vector3f()));
		RELATIONSHIP_LINE.getModifier(CylinderMeshModifier.class).setRotationPattern(RotationPattern.ROTATION_PATTERN_ZYX);
	}
	
	private transient ProjectManager projectManager;
	private ProjectConfigurations configs;
	private final MediaLibrary mediaLibrary;
	private transient StudioDriverSystem driverSystem;
	private final ArrayList<Scene> scenes;
	
	private Scene currentScene;
	private int currentLayer;
	
	private final ArrayList<CEObject> selectedObjects;
	private boolean selectionChanged;
	
	public SceneManager(File projectDirectory, Scene currentScene, Scene... scenes) {
		this.projectManager = new ProjectManager(this, projectDirectory);
		this.configs = new ProjectConfigurations(new ProjectConfigurationBean());
		this.mediaLibrary = new MediaLibrary();
		instantiateDriverSystem();
		this.scenes = new ArrayList<Scene>();
		for(Scene scene : scenes) this.scenes.add(scene);
		setCurrentScene(currentScene);
		currentLayer = 0;
		this.selectedObjects = new ArrayList<CEObject>();
	}
	
	public synchronized ProjectManager getProjectManager() {
		return projectManager;
	}
	
	public synchronized void setProjectManager(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	public synchronized ProjectConfigurations getConfigs() {
		return configs;
	}
	
	public synchronized MediaLibrary getMediaLibrary() {
		return mediaLibrary;
	}
	
	public synchronized StudioDriverSystem getDriverSystem() {
		return driverSystem;
	}
	
	public synchronized ArrayList<Scene> getScenes() {
		return new ArrayList<Scene>(scenes);
	}
	
	public synchronized Scene getCurrentScene() {
		return currentScene;
	}
	
	public synchronized Layer getCurrentLayer() {
		if(currentScene.getLayer(currentLayer) == null) {
			if(currentScene.getLayers().size() == 0) currentScene.addLayer(new Layer("Base Layer"));
			currentLayer = 0;
		}
		return currentScene.getLayer(currentLayer);
	}
	
	public synchronized CEObject getObject(String name, CEObject... exclusions) {
		List<CEObject> exc = Arrays.asList(exclusions);
		for(Scene scene : scenes)
			for(Layer layer : scene.getLayers())
				for(CEObject object : layer.getObjects())
					if(object.getIdentifier().equals(name) && !exc.contains(object))
						return object;
		return null;
	}
	
	public synchronized Scene getScene(String name, Scene... exclusions) {
		List<Scene> exc = Arrays.asList(exclusions);
		for(Scene scene : scenes) if(scene.getIdentifier().equals(name) && !exc.contains(scene)) return scene;
		return null;
	}
	
	public synchronized Layer getLayer(String name, Layer... exclusions) {
		return getLayer(name, currentScene, exclusions);
	}
	
	public synchronized Layer getLayer(String name, Scene scene, Layer... exclusions) {
		List<Layer> exc = Arrays.asList(exclusions);
		for(Layer layer : scene.getLayers()) if(layer.getIdentifier().equals(name) && !exc.contains(layer)) return layer;
		return null;
	}
	
	public synchronized void setCurrentLayer(String currentLayer) {
		int index = 0;
		for(Layer layer : currentScene.getLayers()) {
			if(layer.getIdentifier().equals(currentLayer)) {
				this.currentLayer = index;
				return;
			}
			index++;
		}
	}
	
	public synchronized void setCurrentScene(Scene currentScene) {
		if(currentScene == null) return;
		if(!scenes.contains(currentScene)) scenes.add(currentScene);
		this.currentScene = currentScene;
		this.currentLayer = 0;
	}
	
	public synchronized void addScene(Scene scene) {
		this.scenes.add(scene);
	}
	
	public synchronized void removeScene(Scene scene) {
		if(scenes.size() > 1) {
			this.currentScene = scenes.get(scenes.indexOf(scene) == 0 ? 1 : scenes.indexOf(scene) - 1);
			scenes.remove(scene);
		}
	}
	
	public synchronized void addObject(CEObject obj) {
		currentScene.getLayer(currentLayer).addObject(obj);
	}
	
	public synchronized void removeObject(CEObject obj) {
		for(Scene scene : scenes) for(Layer layer : scene.getLayers()) layer.removeObject(obj);
	}
	
	public int indexOf(Layer layer) {
		return indexOf(layer, currentScene);
	}
	
	public synchronized int indexOf(Layer layer, Scene scene) {
		ArrayList<Layer> layers = scene.getLayers();
		for(int i = 0 ; i < layers.size() ; ++i) if(layers.get(i) == layer) return i;
		return -1;
	}
	
	public synchronized void reinstantiateModifier(Modifier _old, Modifier _new) {
		for(Scene scene : scenes) {
			for(Layer layer : scene.getLayers()) {
				for(CEObject obj : layer.getObjects()) {
					for(int i = 0 ; i < obj.getModifiers().size() ; ++i) {
						if(obj.getModifiers().get(i) == _old) {
							obj.removeModifier(i);
							obj.insertModifier(i, _new);
						}
					}
				}
			}
		}
	}
	
	public synchronized ArrayList<CEObject> getSelectedObjects() {
		return new ArrayList<CEObject>(selectedObjects);
	}
	
	public synchronized void clearAndSelect(CEObject object) {
		this.selectedObjects.clear();
		this.selectedObjects.add(object);
		this.selectionChanged = true;
	}
	
	public synchronized void deselectAll() {
		this.selectedObjects.clear();
		this.selectionChanged = true;
	}
	
	public synchronized void addSelection(CEObject object) {
		this.selectedObjects.add(object);
		this.selectionChanged = true;
	}
	
	public synchronized void removeSelection(CEObject object) {
		this.selectedObjects.remove(object);
		this.selectionChanged = true;
	}
	
	public synchronized boolean isSelectionChanged() {
		boolean selectionChanged = this.selectionChanged;
		this.selectionChanged = false;
		return selectionChanged;
	}
	
	public void update() {
		if(currentScene == null) {
			currentScene = new Scene("Current Scene", new Layer("Base Layer"));
			currentLayer = 0;
		}
		if(currentScene.getLayers().size() == 0) {
			currentScene.addLayer(new Layer("Base Layer"));
			currentLayer = 0;
		}
		
		ArrayList<CEObject> fullList = new ArrayList<CEObject>();
		for(Scene scene : scenes) for(Layer layer : scene.getLayers()) fullList.addAll(layer.getObjects());
		for(Scene scene : scenes) for(Layer layer : scene.getLayers()) for(CEObject obj : layer.getObjects())
			if(obj.getParent() != null && !fullList.contains(obj.getParent())) layer.removeObject(obj);
		
		for(CEObject obj : currentScene.getLayer(".studio").getObjects())
			if(obj.getIdentifier().endsWith("-relationship.studio") || obj.getIdentifier().endsWith("-icon.studio") || obj.getIdentifier().endsWith("-radius.studio"))
				currentScene.getLayer(".studio").removeObject(obj);
		
		ArrayList<CEObject> activeSceneList = new ArrayList<CEObject>();
		for(Layer layer : currentScene.getLayers()) if(layer.isEnabled() && !layer.getIdentifier().endsWith(".studio")) activeSceneList.addAll(layer.getObjects());
		
		for(CEObject obj : activeSceneList) {
			boolean selected = isObjectSelected(obj);
			
			if(obj.getParent() != null) {
				Vector3f dir = Vector3f.sub(obj.getParent().getPosition(), obj.getPosition());
				if(dir.length() > 0) dir.normalize();
				float radius = Vector3f.cross(Vector3f.sub(obj.getParent().getPosition(), obj.getPosition()), Vector3f.sub(obj.getPosition(), currentScene.getLayer("X.studio")
						.getObject("camera.studio").getPosition())).length() / Vector3f.sub(obj.getParent().getPosition(), obj.getPosition()).length() * 0.003f;
				// Distance from camera position to line
				
				Vector3f position = Vector3f.add(obj.getPosition(), obj.getParent().getPosition()).scale(0.5f);
				Vector3f rotation = new Vector3f((float) -Math.toDegrees(Math.asin(dir.y)) + 90, (float) -Math.toDegrees(Math.atan2(dir.z, dir.x)) + 90, 0);
				Vector3f scale = new Vector3f(radius, Vector3f.sub(obj.getPosition(), obj.getParent().getPosition()).length() / 2f, radius);
				CEObjectReference relationship = new CEObjectReference(obj.getIdentifier() + "-relationship.studio", position, rotation, scale, RELATIONSHIP_LINE);
				currentScene.getLayer(".studio").addObject(relationship);
			}
			
			CEObjectReference icon = new CEObjectReference(obj.getIdentifier() + "-icon.studio", obj.getPosition(),
					new Vector3f(), new Vector3f(1), selected ? OBJECT_ICON_SELECTED : OBJECT_ICON);
			CEObjectReference radius = new CEObjectReference(obj.getIdentifier() + "-radius.studio", obj.getPosition(),
					new Vector3f(), new Vector3f(obj.getMaxScale() * obj.getConstraints().getFrustumRadius()), selected ? OBJECT_RADIUS_SELECTED : OBJECT_RADIUS);
			currentScene.getLayer(".studio").addObject(icon);
			currentScene.getLayer(".studio").addObject(radius);
		}
		
		for(CEObject obj : new ArrayList<CEObject>(selectedObjects)) if(!activeSceneList.contains(obj)) selectedObjects.remove(obj);
		
		for(Scene scene : scenes) {
			if(getScene(scene.getIdentifier(), scene) != null) scene.setIdentifier(nextSceneIdentifier(scene.getIdentifier()));
			else if(scene.getIdentifier().endsWith(".studio")) scene.setIdentifier(nextSceneIdentifier());
			for(Layer layer : scene.getLayers()) {
				if(!layer.getIdentifier().endsWith(".studio")) {
					if(getLayer(layer.getIdentifier(), scene, layer) != null) layer.setIdentifier(nextLayerIdentifier(layer.getIdentifier()));
					for(CEObject obj : layer.getObjects()) {
						if(getObject(obj.getIdentifier(), obj) != null) obj.setIdentifier(nextObjectIdentifier(obj.getIdentifier()));
						else if(obj.getIdentifier().endsWith(".studio")) obj.setIdentifier(nextObjectIdentifier());
					}
				}
			}
		}
		
		configs.update();
		mediaLibrary.update();
		driverSystem.update();
	}
	
	public synchronized boolean isObjectSelected(CEObject obj) {
		return selectedObjects.contains(obj);
	}
	
	public synchronized boolean isObjectSelected(String identifier) {
		for(CEObject obj : selectedObjects) if(obj.getIdentifier().equals(identifier)) return true;
		return false;
	}
	
	public String nextObjectIdentifier() {
		return nextObjectIdentifier("Object");
	}
	
	public String nextSceneIdentifier() {
		return nextSceneIdentifier("Scene");
	}
	
	public String nextLayerIdentifier() {
		return nextLayerIdentifier("Layer");
	}
	
	public String nextObjectIdentifier(String root) {
		String identifier = root + ".???";
		int current = 0;
		do {
			identifier = identifier.substring(0, root.length() + 1);
			identifier += String.format("%03d", current++);
		}
		while(getObject(identifier) != null);
		return identifier;
	}
	
	public String nextSceneIdentifier(String root) {
		String identifier = root + ".???";
		int current = 0;
		do {
			identifier = identifier.substring(0, 6);
			identifier += String.format("%03d", current++);
		}
		while(getScene(identifier) != null);
		return identifier;
	}
	
	public String nextLayerIdentifier(String root) {
		String identifier = root + ".???";
		int current = 0;
		do {
			identifier = identifier.substring(0, 6);
			identifier += String.format("%03d", current++);
		}
		while(getLayer(identifier) != null);
		return identifier;
	}
	
	@SerializerImpl
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(driverSystem.getDriverSubjectName());
		out.writeObject(driverSystem.getCurrentDriverSubjects());
		out.writeInt(StudioViewRepository.getPanel(SceneViewPanel.class).getMode());
	}
	
	@SuppressWarnings("unchecked")
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		instantiateDriverSystem();
		this.driverSystem.open(in.readUTF(), (ArrayList<Property<?>>) in.readObject());
		StudioViewRepository.getPanel(SceneViewPanel.class).setMode(in.readInt());
		StudioViewRepository.getPanel(ObjectEditorPanel.class).deactivateAll();
	}
	
	public void resetStudio() {
		this.configs = new ProjectConfigurations(new ProjectConfigurationBean());
		this.mediaLibrary.getRoot().getChildren().clear();
		this.mediaLibrary.getRoot().getResources().clear();
		this.driverSystem.close();
		this.scenes.clear();
		this.scenes.add(createStudioScene("Default Scene"));
		this.currentScene = scenes.get(0);
		this.currentLayer = 0;
		this.selectedObjects.clear();
		this.selectionChanged = true;
		StudioViewRepository.getPanel(SceneViewPanel.class).setMode(0);
	}
	
	private void instantiateDriverSystem() {
		this.driverSystem = new StudioDriverSystem(DriverEditorFactory.LINEAR_DRIVER_FACTORY, DriverEditorFactory.SINE_WAVE_DRIVER_FACTORY,
				DriverEditorFactory.KEYFRAME_DRIVER_FACTORY, DriverEditorFactory.EXPRESSION_DRIVER_FACTORY, DriverEditorFactory.SMOOTH_DRIVER_FACTORY);
	}
	
	public static Scene createStudioScene(String identifier) {
		Layer layer = new Layer(".studio");
		Layer xlayer = new Layer("X.studio");
		
		final CEObject camera = new CEObject("camera.studio", new Vector3f(0, 0, 14), new Vector3f(), new Vector3f(1f),
				new ObjectConstraints(1, 0), new CameraModifier(70, 2, 2, 0.1f, 4000, 1f, 1f), new OrbitalTransformModifier(new Vector3f()));
		xlayer.addObject(camera);
		
		final CEObject axisX = new CEObject("axisX.studio", new Vector3f(), new Vector3f(), new Vector3f(1f), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		axisX.addModifier(new StaticColorModifier(new Vector3f(60.4f / 100f, 25.5f / 100f, 31.0f / 100f)));
		axisX.addModifier(new FogEffectModifier(true, new Vector3f(), 0.01f, 1.0f));
		axisX.addModifier(new CrossSectionMeshModifier(false, true, new Vector3f()));
		layer.addObject(axisX);
		
		final CEObject axisY = new CEObject("axisY.studio", new Vector3f(), new Vector3f(0, 0, 90), new Vector3f(1f), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		axisY.addModifier(new StaticColorModifier(new Vector3f(37.1f / 100f, 59.2f / 100f, 16.1f / 100f)));
		axisY.addModifier(new FogEffectModifier(true, new Vector3f(), 0.01f, 1.0f));
		axisY.addModifier(new CrossSectionMeshModifier(false, true, new Vector3f()));
		layer.addObject(axisY);
		
		final CEObject axisZ = new CEObject("axisZ.studio", new Vector3f(), new Vector3f(0, 90, 0), new Vector3f(1f), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		axisZ.addModifier(new StaticColorModifier(new Vector3f(25.5f / 100f, 31.0f / 100f, 60.4f / 100f)));
		axisZ.addModifier(new FogEffectModifier(true, new Vector3f(), 0.01f, 1.0f));
		axisZ.addModifier(new CrossSectionMeshModifier(false, true, new Vector3f()));
		layer.addObject(axisZ);
		
		final CEObject plane = new CEObject("plane.studio", new Vector3f(), new Vector3f(0, 0, 0), new Vector3f(800), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		plane.addModifier(new StaticColorModifier(new Vector3f(0.7f, 0f, 0.7f)));
		plane.addModifier(new AlphaModifier(0.5f, 1f));
		plane.addModifier(new FragFogEffectModifier(true, new Vector3f(), 200f));
		plane.addModifier(new PlaneMeshModifier(false, true, new Vector3f()));
		plane.getModifier(PlaneMeshModifier.class).setBlendMode(PlaneMeshModifier.BLEND_MODE_ALPHA);
		plane.getModifier(PlaneMeshModifier.class).setRotationPattern(RotationPattern.ROTATION_PATTERN_ZYX);
		layer.addObject(plane);
		
		final CEObject grid = new CEObject("grid.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		grid.addModifier(new StaticColorModifier(new Vector3f(0.3f, 0.3f, 0.3f)));
		grid.addModifier(new FogEffectModifier(true, new Vector3f(), 0.024f, 1.0f));
		grid.addModifier(new CrossSectionMeshModifier(false, true, new Vector3f()));
		
		final CEObject gridThick = new CEObject("grid-thick.studio", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(Float.MAX_VALUE, Float.MAX_VALUE));
		gridThick.addModifier(new StaticColorModifier(new Vector3f(0.35f, 0.35f, 0.35f)));
		gridThick.addModifier(new FogEffectModifier(true, new Vector3f(), 0.018f, 1.0f));
		gridThick.addModifier(new CrossSectionMeshModifier(false, true, new Vector3f()));
		
		for(int i = GRID_MIN ; i <= GRID_MAX ; ++i) {
			CEObject gridRef = new CEObjectReference(i + "grid.studio", new Vector3f(0, 0, i), new Vector3f(), new Vector3f(1), i % 10 == 0 ? gridThick : grid);
			layer.addObject(gridRef);
		}
		
		for(int i = GRID_MIN ; i <= GRID_MAX ; ++i) {
			CEObject gridRef = new CEObjectReference(i + "vert-grid.studio", new Vector3f(0, 0, i), new Vector3f(0, 90, 0), new Vector3f(1), i % 10 == 0 ? gridThick : grid);
			layer.addObject(gridRef);
		}
		
		layer.setEvent(new Event() {
			
			private static final long serialVersionUID = 7125371497242888815L;
			
			private ArrayList<CEObject> grids = new ArrayList<CEObject>();
			private ArrayList<CEObject> vertGrids = new ArrayList<CEObject>();
			
			public void perform(Object... o) {
				UpdatePacket packet = (UpdatePacket) o[0];
				if(grids.size() == 0) for(int i = GRID_MIN ; i <= GRID_MAX ; ++i) grids.add(packet.getScene().getLayer(".studio").getObject(i + "grid.studio"));
				if(vertGrids.size() == 0) for(int i = GRID_MIN ; i <= GRID_MAX ; ++i) vertGrids.add(packet.getScene().getLayer(".studio").getObject(i + "vert-grid.studio"));
				
				CEObject camera = packet.getScene().getLayer("X.studio").getObject("camera.studio");
				CEObject axisX = packet.getScene().getLayer(".studio").getObject("axisX.studio");
				CEObject axisY = packet.getScene().getLayer(".studio").getObject("axisY.studio");
				CEObject axisZ = packet.getScene().getLayer(".studio").getObject("axisZ.studio");
				
				float distX = Vector3f.sub(camera.getPosition(), axisX.getPosition()).scale(new Vector3f(0, 1, 1)).length();
				axisX.setScale(new Vector3f(30f * distX, 0.002f * distX, 0.002f * distX));
				axisX.getBasePosition().x = camera.getPosition().x;
				float distY = Vector3f.sub(camera.getPosition(), axisY.getPosition()).scale(new Vector3f(1, 0, 1)).length();
				axisY.setScale(new Vector3f(30f * distY, 0.002f * distY, 0.002f * distY));
				axisY.getBasePosition().y = camera.getPosition().y;
				float distZ = Vector3f.sub(camera.getPosition(), axisZ.getPosition()).scale(new Vector3f(1, 1, 0)).length();
				axisZ.setScale(new Vector3f(30f * distZ, 0.002f * distZ, 0.002f * distZ));
				axisZ.getBasePosition().z = camera.getPosition().z;
				
				for(int i = GRID_MIN ; i <= GRID_MAX ; ++i) {
					CEObject grid = grids.get(i - GRID_MIN);
					float dist = Vector3f.sub(camera.getPosition(), grid.getPosition()).scale(new Vector3f(0, 1, 1)).length();
					grid.setScale(new Vector3f(30f * dist, 0.001f * dist, 0.001f * dist));
					grid.getBasePosition().x = camera.getPosition().x;
					grid.getBasePosition().z = camera.getPosition().z - camera.getPosition().z % 10 + i;
					if(grid.getBasePosition().z == 0 && axisX.isEnabled()) grid.setScale(new Vector3f(0));
				}
				
				for(int i = GRID_MIN ; i <= GRID_MAX ; ++i) {
					CEObject grid = vertGrids.get(i - GRID_MIN);
					float dist = Vector3f.sub(camera.getPosition(), grid.getPosition()).scale(new Vector3f(1, 1, 0)).length();
					grid.setScale(new Vector3f(30f * dist, 0.001f * dist, 0.001f * dist));
					grid.getBasePosition().z = camera.getPosition().z;
					grid.getBasePosition().x = camera.getPosition().x - camera.getPosition().x % 10 + i;
					if(grid.getBasePosition().x == 0 && axisZ.isEnabled()) grid.setScale(new Vector3f(0));
				}
				
				if(StudioInterface.getInstantiation().getCurrentView() != StudioViewRepository.STUDIO_VIEW_OBJECT_MODE && SystemInput.isButtonPressed(0)) {
					TreeMap<Float, CEObject> hits = new TreeMap<Float, CEObject>();
					for(Layer layer : GLRequestSystem.getSceneManager().getCurrentScene().getLayers()) {
						if(layer.isEnabled() && !layer.getIdentifier().endsWith(".studio")) {
							for(CEObject obj : layer.getObjects()) {
								float distance = GLRequestSystem.getSceneManager().getCurrentScene().getLayer("X.studio")
										.getObject("camera.studio").getModifier(CameraModifier.class).getMousePicker().getDistance(obj.getPosition());
								if(distance <= Math.max(1f, Math.abs(obj.getConstraints().getFrustumRadius() * obj.getMaxScale()) * 0.9f)) hits.put(distance, obj);
							}
						}
					}
					if(hits.size() > 0) {
						CEObject hit = hits.firstEntry().getValue();
						if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
							if(GLRequestSystem.getSceneManager().isObjectSelected(hit))
								GLRequestSystem.getSceneManager().removeSelection(hit);
							else GLRequestSystem.getSceneManager().addSelection(hit);
						}
						else GLRequestSystem.getSceneManager().clearAndSelect(hit);
					}
					else if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
						GLRequestSystem.getSceneManager().deselectAll();
				}
			}
			
			@SerializerImpl
			private void writeObject(ObjectOutputStream out) throws IOException {
				out.writeObject(grids);
				out.writeObject(vertGrids);
			}
			
			@SuppressWarnings("unchecked")
			@SerializerImpl
			private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
				this.grids = (ArrayList<CEObject>) in.readObject();
				this.vertGrids = (ArrayList<CEObject>) in.readObject();
			}
		});
		
		Scene scene = new Scene(identifier);
		scene.addLayer(layer);
		scene.addLayer(xlayer);
		return scene;
	}
	
}
