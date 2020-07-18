package mod.celestial.misc;

import celestial.core.Modifier;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;

public final class EyeSpaceModifier extends Modifier {
	
	private static final long serialVersionUID = -7607614641652263870L;
	
	public static final Factory<EyeSpaceModifier> FACTORY = () -> new EyeSpaceModifier(new Vector3f());
	
	private final Property<Vector3f> offset;
	
	public EyeSpaceModifier(Vector3f offset) {
		super(false, false, false);
		this.offset = Properties.createVec3Property(offset);
	}
	
	private EyeSpaceModifier(EyeSpaceModifier src) {
		super(false, false, false);
		this.offset = src.offset.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {
		obj.setPosition(packet.getCamera().getPosition().clone().translate(offset.get()));
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		offset.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Offset", offset);
		return ctrl;
	}
	
	public Vector3f getOffset() {
		return offset.get();
	}
	
	public void setOffset(Vector3f offset) {
		this.offset.set(offset);
	}
	
	public Property<Vector3f> offsetProperty() {
		return offset;
	}
	
	public Modifier duplicate() {
		return new EyeSpaceModifier(this);
	}
	
}
