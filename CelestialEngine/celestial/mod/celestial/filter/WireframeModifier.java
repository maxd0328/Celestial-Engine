package mod.celestial.filter;

import org.lwjgl.opengl.GL11;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;

public final class WireframeModifier extends Modifier {
	
	private static final long serialVersionUID = -2393059930712257080L;
	
	public static final Factory<WireframeModifier> FACTORY = () -> new WireframeModifier(true);
	
	private final Property<Boolean> wireframe;
	
	public WireframeModifier(boolean wireframe) {
		super(false, false, false);
		this.wireframe = Properties.createBooleanProperty(wireframe);
	}
	
	private WireframeModifier(WireframeModifier src) {
		super(false, false, false);
		this.wireframe = src.wireframe.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(wireframe.get()) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		wireframe.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Wireframe", wireframe);
		return ctrl;
	}
	
	public boolean isWireframe() {
		return wireframe.get();
	}
	
	public void setWireframe(boolean wireframe) {
		this.wireframe.set(wireframe);
	}
	
	public Property<Boolean> wireframeProperty() {
		return wireframe;
	}
	
	public Modifier duplicate() {
		return new WireframeModifier(this);
	}
	
}
