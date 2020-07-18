package celestial.render.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import celestial.core.CEObject;
import celestial.core.CEObjectReference;
import celestial.core.Modifier;
import celestial.render.RenderAbortException;
import celestial.render.RenderConstraints;
import celestial.render.RenderPacket;
import celestial.render.Renderer;
import celestial.shader.Shader;
import celestial.shader.UnifiedShader;
import celestial.render.RenderReiterationException;
import celestial.vecmath.Vector3f;

public final class InstancedRenderer extends Renderer {
	
	public InstancedRenderer(UnifiedShader shader, Vector3f screen) {
		super(shader, screen);
	}
	
	protected void implRender(RenderPacket packet, RenderConstraints constraints) {
		LinkedHashMap<CEObject, ArrayList<CEObject>> map = new LinkedHashMap<CEObject, ArrayList<CEObject>>();
		for(CEObject obj : packet.getScene()) addObject(map, obj);
		
		for(CEObject obj : map.keySet()) {
			boolean reiterate = true;
			while(reiterate) {
				reiterate = false;
				
				Shader shader = super.getShader().activateShader(obj.toShaderTemplate(e -> constraints.validate(e)));
				RenderPacket currentPacket = new RenderPacket(packet, shader);
				
				try {
					int i;
					ArrayList<Modifier> mods = obj.getModifiers();
					for(i = 0 ; i < mods.size() ; ++i) if(constraints.validate(mods.get(i))) obj.preRender(currentPacket, i);
					for(CEObject instance : map.get(obj)) {
						instance.getPreRenderEvents().forEach(e -> e.perform(obj, currentPacket));
						for(i = 0 ; i < mods.size() ; ++i) if(constraints.validate(mods.get(i))) instance.render(currentPacket, i);
						instance.getPostRenderEvents().forEach(e -> e.perform(obj, currentPacket));
					}
					for(i = 0 ; i < mods.size() ; ++i) if(constraints.validate(mods.get(i))) obj.postRender(currentPacket, i);
				}
				catch(RenderReiterationException ex) {
					reiterate = true;
				}
				catch(RenderAbortException ex) {
				}
			}
			
			super.getShader().deactivateShader(obj.toShaderTemplate());
		}
	}
	
	private void addObject(HashMap<CEObject, ArrayList<CEObject>> map, CEObject object) {
		CEObject root = object instanceof CEObjectReference ? getRoot((CEObjectReference) object) : object;
		if(map.containsKey(root)) map.get(root).add(object);
		else {
			ArrayList<CEObject> list = new ArrayList<CEObject>();
			list.add(object);
			map.put(root, list);
		}
	}
	
	private CEObject getRoot(CEObjectReference obj) {
		CEObject root = obj.getRoot();
		if(root instanceof CEObjectReference)
			return getRoot((CEObjectReference) root);
		else return root;
	}
	
}
