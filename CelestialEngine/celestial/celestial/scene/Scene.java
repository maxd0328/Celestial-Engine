package celestial.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import celestial.core.CEObject;
import celestial.shading.ShadingSystem;
import celestial.ui.UISystem;

public final class Scene implements java.io.Serializable {
	
	private static final long serialVersionUID = 9149538754125284155L;
	
	private final ArrayList<Layer> layers;
	private final transient UISystem guiSystem;
	private final transient List<ShadingSystem> shadingSystems;
	private transient ShadingSystem postProcessingSystem;
	private String identifier;
	
	public Scene(String identifier, Layer...layers) {
		this.layers = new ArrayList<Layer>(Arrays.asList(layers));
		if(layers.length == 0) this.layers.add(new Layer("Base Layer"));
		this.guiSystem = new UISystem();
		this.shadingSystems = new ArrayList<>();
		this.postProcessingSystem = null;
		this.identifier = identifier;
	}
	
	public synchronized Layer getBaseLayer() {
		return layers.size() == 0 ? null : layers.get(0);
	}
	
	public synchronized Layer getLayer(int index) {
		return index < 0 || index >= layers.size() ? null : layers.get(index);
	}
	
	public synchronized Layer getLayer(String identifier) {
		for(Layer layer : layers) if(layer.getIdentifier().equals(identifier)) return layer;
		return null;
	}
	
	public synchronized ArrayList<Layer> getLayers() {
		return layers;
	}
	
	public synchronized void addLayer(Layer layer) {
		this.layers.add(layer);
	}
	
	public synchronized void removeLayer(Layer layer) {
		this.layers.remove(layer);
	}
	
	public synchronized Collection<CEObject> getObjects() {
		Collection<CEObject> list = new ArrayList<>();
		for(Layer layer : layers)
			list.addAll(layer.getObjects());
		return list;
	}
	
	public UISystem getGuiSystem() {
		return guiSystem;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public List<ShadingSystem> getShadingSystems() {
		return shadingSystems;
	}
	
	public ShadingSystem getPostProcessingSystem() {
		return postProcessingSystem;
	}
	
	public void setPostProcessingSystem(ShadingSystem postProcessingSystem) {
		this.postProcessingSystem = postProcessingSystem;
	}
	
	@Override
	public String toString() {
		return "CEScene:\"" + identifier + "\"";
	}
	
}
