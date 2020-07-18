package celestial.util;

import java.util.Collection;

import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.scene.Layer;
import celestial.scene.Scene;

public interface ISceneSystem {
	
	public Collection<Scene> getScenes();
	
	public Scene getCurrentScene();
	
	public void setCurrentScene(Scene scene);
	
	public Layer getCurrentLayer();
	
	public void setCurrentLayer(String layerId);
	
	public Scene getScene(String identifier, Scene... exclusions);
	
	public Layer getLayer(String identifier, Layer... exclusions);
	
	public Layer getLayer(String identifier, Scene scene, Layer... exclusions);
	
	public CEObject getObject(String identifier, CEObject... exclusions);
	
	public void addScene(Scene scene);
	
	public void removeScene(Scene scene);
	
	public void addObject(CEObject obj);
	
	public void removeObject(CEObject obj);
	
	public int indexOf(Layer layer, Scene scene);
	
	public int indexOf(Layer layer);
	
	public void reinstantiateModifier(Modifier _old, Modifier _new);
	
}
