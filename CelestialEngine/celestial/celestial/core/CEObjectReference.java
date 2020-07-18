package celestial.core;

import java.util.ArrayList;
import celestial.data.GLData;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderTemplate;
import celestial.vecmath.Vector3f;

public final class CEObjectReference extends CEObject {
	
	private static final long serialVersionUID = -2908659755698683221L;
	
	private CEObject root;
	
	public CEObjectReference(String identifier, Vector3f position, Vector3f rotation, Vector3f scale, CEObject root) {
		super(identifier, position, rotation, scale, root.getConstraints());
		for(int i = 0 ; i < configurations.length ; ++i) configurations[i] = root.configurations[i].clone();
		this.root = root;
	}
	
	@Override
	public ObjectConstraints getConstraints() {
		return root.getConstraints();
	}
	
	@Override
	public boolean addModifier(Modifier modifier) {
		return root.addModifier(modifier);
	}
	
	@Override
	public ArrayList<Modifier> getModifiers() {
		return root.getModifiers();
	}
	
	@Override
	public <T extends Modifier> T getModifier(Class<T> type) {
		return root.getModifier(type);
	}
	
	@Override
	public void removeModifier(Modifier modifier) {
		root.removeModifier(modifier);
	}
	
	@Override
	public boolean containsData(GLData data) {
		return root.containsData(data);
	}
	
	@Override
	public void preRender(RenderPacket packet, int index) {
		if(isEnabled()) root.modifiers.get(index).preRender(packet, this);
	}
	
	@Override
	public void render(RenderPacket packet, int index) {
		if(isEnabled()) root.modifiers.get(index).render(packet, this);
	}
	
	@Override
	public void postRender(RenderPacket packet, int index) {
		if(isEnabled()) root.modifiers.get(index).postRender(packet, this);
	}
	
	@Override
	public void update0(UpdatePacket packet) {
		if(!isEnabled()) return;
		if(super.updateToggle) return;
		super.updateToggle = true;
		for(Modifier mod : root.modifiers) mod.update0(packet, this);
		if(!packet.getScene().getObjects().contains(root))
			root.updateToggle = true;
	}
	
	@Override
	public void update1(UpdatePacket packet) {
		if(!isEnabled()) return;
		if(!super.updateToggle) return;
		super.updateToggle = false;
		
		identifier.update(!packet.isPaused());
		position.update(!packet.isPaused());
		rotation.update(!packet.isPaused());
		scale.update(!packet.isPaused());
		enabled.update(!packet.isPaused());
		parent.update(!packet.isPaused());
		translateOnly.update();
		for(int i = 0 ; i < configurations.length ; ++i) configurations[i].update();
		
		for(Modifier mod : root.modifiers) {
			if(root.isEnabled() && root.updateToggle)
				mod.update1(packet, this);
			mod.update1All(packet, this);
		}
		
		if(!root.isEnabled()) return;
		root.updateToggle = false;
	}
	
	@Override
	public ShaderTemplate toShaderTemplate() {
		return root.toShaderTemplate();
	}
	
	public CEObject getRoot() {
		return root;
	}
	
	public void setRoot(CEObject root) {
		this.root = root;
	}
	
}
