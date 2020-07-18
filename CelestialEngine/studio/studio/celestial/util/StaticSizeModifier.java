package studio.celestial.util;

import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;
import mod.celestial.misc.CameraModifier;
import mod.celestial.misc.CameraModifier.ProjectionState;

public final class StaticSizeModifier extends Modifier {
	
	private static final long serialVersionUID = 7071998716619463437L;
	
	public static final Factory<StaticSizeModifier> FACTORY = () -> new StaticSizeModifier(new Vector3f(1f));
	
	private final Property<Vector3f> scale;
	
	public StaticSizeModifier(Vector3f scale) {
		super(false, false, false);
		this.scale = Properties.createVec3Property(scale);
	}
	
	private StaticSizeModifier(StaticSizeModifier src) {
		super(false, false, false);
		this.scale = src.scale.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(getCamera(packet).getProjectionState() == ProjectionState.ORTHOGRAPHIC)
			obj.setScale(scale.get().clone().scale((getCamera(packet).getOrthoWidth() + getCamera(packet).getOrthoHeight()) / 2f * 1.3f));
		else obj.setScale(scale.get().clone().scale(Vector3f.sub(packet.getCamera().getPosition(), obj.getPosition()).length()));
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
	public Vector3f getScale() {
		return scale.get();
	}
	
	public void setScale(Vector3f scale) {
		this.scale.set(scale);
	}
	
	public Property<Vector3f> scaleProperty() {
		return scale;
	}
	
	public Modifier duplicate() {
		return new StaticSizeModifier(this);
	}
	
	private CameraModifier getCamera(UpdatePacket packet) {
		return packet.getCamera().getModifier(CameraModifier.class);
	}
	
}
