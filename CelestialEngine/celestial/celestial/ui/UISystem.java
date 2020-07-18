package celestial.ui;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import celestial.collections.ObservableArrayList;
import celestial.collections.ObservableList;
import celestial.core.EngineRuntime;
import celestial.data.GLData;
import celestial.render.GraphicUtil;
import celestial.render.RenderOutput;
import celestial.util.KVEntry;
import celestial.vecmath.Vector2f;

public final class UISystem {
	
	private final ObservableList<UILayer> layers;
	
	public UISystem(UILayer... layers) {
		this.layers = new ObservableArrayList<UILayer>(layers);
	}
	
	public ObservableList<UILayer> getLayers() {
		return layers;
	}
	
	public boolean containsData(GLData data) {
		for(UILayer layer : layers)
			if(layer.getRootContainer() != null && layer.getRootContainer().containsData(data))
				return true;
		return false;
	}
	
	public void renderUI(RenderOutput output) {
		GraphicUtil.disable(GraphicUtil.GL_DEPTH_TESTING);
		GraphicUtil.disable(GraphicUtil.GL_CULLING);
		GraphicUtil.enable(GraphicUtil.GL_ALPHA_BLENDING);
		
		for(UILayer layer : layers) {
			if(layer.getPostProcessingSystem() != null) {
				output.unbind();
				EngineRuntime.getPostProcessingBuffer().fboBind();
			}
			Map<Component, CompoundComponent> components = new LinkedHashMap<>();
			
			addComponent(layer.getRootContainer(), null, components);
			
			Graphic prev = null;
			Iterator<Component> itr = components.keySet().iterator();
			while(itr.hasNext()) {
				Component comp = itr.next();
				
				comp.getGraphic().bind(prev);
				comp.getGraphic().render(comp.getConstrainedPosition(), comp.getConstrainedScale());
				comp.getGraphic().unbind(itr.hasNext());
				
				prev = comp.getGraphic();
			}
			
			if(layer.getPostProcessingSystem() != null) {
				EngineRuntime.getPostProcessingBuffer().fboUnbind();
				output.bind();
				layer.getPostProcessingSystem().renderResult();
			}
			
			updateComponent(layer.getRootContainer());
		}
		
		GraphicUtil.disable(GraphicUtil.GL_BLENDING);
		GraphicUtil.enable(GraphicUtil.GL_DEPTH_TESTING);
		
		layers.update();
	}
	
	private void addComponent(Component comp, CompoundComponent container, Map<Component, CompoundComponent> components) {
		if(comp == null)
			return;
		
		comp.internalUpdate0();
		if(comp.getGraphic() != null)
			components.put(comp, container);
		
		KVEntry<Vector2f, Vector2f> transform;
		if(container == null)
			transform = new KVEntry<>(new Vector2f(), new Vector2f(EngineRuntime.dispGetDisplayMode().getWidth(), EngineRuntime.dispGetDisplayMode().getHeight()));
		else transform = container.getLayout().restrainComponent(comp, container);
		if(comp.getMargin() != null) {
			transform.getValue().translate(-comp.getMargin().getInsetLeft() - comp.getMargin().getInsetRight(), -comp.getMargin().getInsetTop() - comp.getMargin().getInsetBottom());
			transform.getKey().translate(comp.getMargin().getInsetLeft(), comp.getMargin().getInsetTop());
		}
		if(comp.getLayoutOffset() != null) transform.getKey().translate(comp.getLayoutOffset());
		comp.setConstrainedCoords(transform.getKey(), transform.getValue());
		
		if(comp instanceof CompoundComponent)
			for(Component sub : ((CompoundComponent) comp).subComponents())
				addComponent(sub, (CompoundComponent) comp, components);
	}
	
	private void updateComponent(Component comp) {
		if(comp == null)
			return;
		
		comp.internalUpdate1();
		
		if(comp instanceof CompoundComponent)
			for(Component sub : ((CompoundComponent) comp).subComponents())
				updateComponent(sub);
	}
	
}
