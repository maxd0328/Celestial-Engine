package mod.celestial.misc;

import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;

public final class ScriptableModifier extends Modifier {
	
	private static final long serialVersionUID = -7459079624292801078L;
	
	public static final Factory<ScriptableModifier> FACTORY = () -> new ScriptableModifier((ObjectScript) null);
	
	private final ObjectScript script;
	
	public ScriptableModifier(ObjectScript script) {
		super(false, true, false);
		this.script = script;
	}
	
	private ScriptableModifier(ScriptableModifier src) {
		super(false, true, false);
		this.script = src.script;
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {}
	
	@Override
	protected void update1All(UpdatePacket packet, CEObject obj) {
		if(script != null)
			script.resolve(packet, obj);
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
	/* Getters and setters */
	
	public Modifier duplicate() {
		return new ScriptableModifier(this);
	}
	
	@FunctionalInterface
	public static interface ObjectScript {
		
		public void resolve(UpdatePacket pckt, CEObject obj);
		
	}
	
}
