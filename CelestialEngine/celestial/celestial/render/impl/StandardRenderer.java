package celestial.render.impl;

import java.util.ArrayList;

import celestial.core.CEObject;
import celestial.core.Modifier;
import celestial.render.RenderAbortException;
import celestial.render.RenderConstraints;
import celestial.render.RenderPacket;
import celestial.render.Renderer;
import celestial.shader.Shader;
import celestial.shader.UnifiedShader;
import celestial.util.Event;
import celestial.render.RenderReiterationException;
import celestial.vecmath.Vector3f;

public final class StandardRenderer extends Renderer {
	
	public StandardRenderer(UnifiedShader shader, Vector3f screen) {
		super(shader, screen);
	}
	
	protected void implRender(RenderPacket packet, RenderConstraints constraints) {
		for(CEObject obj : packet.getScene()) {
			boolean reiterate = true;
			while(reiterate) {
				reiterate = false;
				
				Shader shader = super.getShader().activateShader(obj.toShaderTemplate(e -> constraints.validate(e)));
				RenderPacket currentPacket = new RenderPacket(packet, shader);
				
				try {
					int i;
					ArrayList<Modifier> mods = obj.getModifiers();
					for(i = 0 ; i < mods.size() ; ++i) if(constraints.validate(mods.get(i))) obj.preRender(currentPacket, i);
					for(Event event : obj.getPreRenderEvents()) event.perform(obj, currentPacket);
					for(i = 0 ; i < mods.size() ; ++i) if(constraints.validate(mods.get(i))) obj.render(currentPacket, i);
					for(Event event : obj.getPostRenderEvents()) event.perform(obj, currentPacket);
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
	
}
