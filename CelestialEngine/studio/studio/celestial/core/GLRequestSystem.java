package studio.celestial.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import celestial.core.CEObject;
import celestial.data.DataManager;
import celestial.render.Renderer;
import celestial.scene.Layer;
import celestial.scene.Scene;
import celestial.shader.UnifiedShader;

public final class GLRequestSystem {
	
	private static volatile Object lock0 = new Object();
	private static volatile Object lock1 = new Object();
	
	private static volatile DataManager dataManager = null;
	private static volatile Renderer renderer = null;
	private static volatile UnifiedShader shader = null;
	private static volatile SceneManager sceneManager = null;
	
	private static volatile ArrayList<IGLRequest> requests = new ArrayList<IGLRequest>();
	private static volatile LinkedHashMap<String, IGLRequest> singletonRequests = new LinkedHashMap<String, IGLRequest>();
	
	public static DataManager getDataManager() {
		while(dataManager == null);
		synchronized(lock0) {
			return dataManager;
		}
	}
	
	public static Renderer getRenderer() {
		while(renderer == null);
		synchronized(lock0) {
			return renderer;
		}
	}
	
	public static UnifiedShader getShader() {
		while(shader == null);
		synchronized(lock0) {
			return shader;
		}
	}
	
	public static SceneManager getSceneManager() {
		while(sceneManager == null);
		synchronized(lock0) {
			return sceneManager;
		}
	}
	
	public static Scene currentScene() {
		return getSceneManager().getCurrentScene();
	}
	
	public static Layer currentLayer() {
		return getSceneManager().getCurrentLayer();
	}
	
	public static ArrayList<CEObject> selectedObjects() {
		return getSceneManager().getSelectedObjects();
	}
	
	public static synchronized void put(DataManager dataManager, Renderer renderer, UnifiedShader shader, SceneManager sceneManager) {
		synchronized(lock0) {
			GLRequestSystem.dataManager = dataManager;
			GLRequestSystem.renderer = renderer;
			GLRequestSystem.shader = shader;
			GLRequestSystem.sceneManager = sceneManager;
		}
	}
	
	public static ArrayList<IGLRequest> pollRequests() {
		synchronized(lock1) {
			ArrayList<IGLRequest> requests = new ArrayList<IGLRequest>(GLRequestSystem.requests);
			requests.addAll(GLRequestSystem.singletonRequests.values());
			GLRequestSystem.requests.clear();
			GLRequestSystem.singletonRequests.clear();
			return requests;
		}
	}
	
	public static void request(IGLRequest request) {
		synchronized(lock1) {
			requests.add(request);
		}
	}
	
	public static void request(String updateID, IGLRequest request) {
		synchronized(lock1) {
			if(singletonRequests.containsKey(updateID)) return;
			singletonRequests.put(updateID, request);
		}
	}
	
	public static void putRequests(IGLRequest... requests) {
		synchronized(lock1) {
			for(IGLRequest request : requests) GLRequestSystem.requests.add(request);
		}
	}
	
	public static interface IGLRequest {
		
		public void perform();
		
	}
	
}
