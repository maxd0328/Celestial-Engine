package celestial.core;

import java.util.HashMap;
import celestial.ctrl.PropertyController;
import celestial.data.GLData;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.ISceneSystem;

public abstract class Modifier implements java.io.Serializable {
	
	private static final long serialVersionUID = 8751055378330459190L;
	
	private static int IDCounter = 0;
	private static final HashMap<Class<? extends Modifier>, Integer> ID_MAP = new HashMap<Class<? extends Modifier>, Integer>();
	
	private final boolean postShader;
	private final boolean allowMultiple;
	private final boolean hasShader;
	private final boolean colorOnly;
	
	protected Modifier(boolean postShader, boolean allowMultiple, boolean colorOnly) {
		this.postShader = postShader;
		this.colorOnly = colorOnly;
		ShaderModule module = getShaderModule();
		this.hasShader = module != null;
		this.allowMultiple = allowMultiple && !hasShader;
	}
	
	/*
	 * Protected methods
	 */
	
	protected boolean isPostShader() {
		return postShader;
	}
	
	protected boolean isAllowMultiple() {
		return allowMultiple;
	}
	
	protected boolean hasShader() {
		return hasShader;
	}
	
	/*
	 * Public methods
	 */
	
	public boolean isColorOnly() {
		return colorOnly;
	}
	
	public final int getID() {
		if(ID_MAP.containsKey(this.getClass())) return ID_MAP.get(this.getClass());
		else {
			ID_MAP.put(this.getClass(), IDCounter++);
			return IDCounter - 1;
		}
	}
	
	/*
	 * Abstract methods
	 */
	
	/**
	 * Returns a shader module if applicable. For no shader module, return null.
	 */
	protected abstract ShaderModule getShaderModule();
	
	/**
	 * Called at times that vary by renderer. This method loads shader variables
	 * and binds VAOs, VBOs, and textures prior to a render.
	 */
	protected abstract void preRender(RenderPacket packet, CEObject obj);
	
	/**
	 * Called once per every instance of the given object. This method loads
	 * instance-dependent variables into the shader and performs object render.
	 */
	protected abstract void render(RenderPacket packet, CEObject obj);
	
	/**
	 * Called at times that vary by renderer. This method unbinds VAOs, VBOs,
	 * and textures after a render, and performs any render after-procedures.
	 */
	protected abstract void postRender(RenderPacket packet, CEObject obj);
	
	/**
	 * Called once per every instance of the given object. This object performs
	 * updates that must happen prior to render and that are required for
	 * every instance of the object.
	 */
	protected abstract void update0(UpdatePacket packet, CEObject obj);
	
	/**
	 * Called only once for the root object. This performs tasks such as
	 * driver updates and things that should only happen once per frame.
	 * This method is called after rendering.
	 */
	protected abstract void update1(UpdatePacket packet, CEObject obj);
	
	/**
	 * Returns a duplicate of the modifier.
	 */
	public abstract Modifier duplicate();
	
	/**
	 * Returns a property controller for all modifiable properties of the
	 * modifier, including drivers.
	 */
	public abstract PropertyController getPropertyController(ISceneSystem system);
	
	/*
	 * Override-recommended methods
	 */
	
	/**
	 * Override recommended for data-containing objects
	 * 
	 * @param data GL oriented data to check
	 * @return
	 */
	public boolean containsData(GLData data) {
		return false;
	}
	
	/**
	 * Functions as an alternative to update1, in which it is called for every
	 * reference of the object as well as the root.
	 * 
	 * @param packet	The update packet
	 * @param obj		The current reference/root being updated
	 */
	protected void update1All(UpdatePacket packet, CEObject obj) {
	}
	
	/*
	 * Static methods
	 */
	
	public static ShaderModule accessShaderModule(Modifier mod) {
		return mod.getShaderModule();
	}
	
	public static int uniqueID() {
		return IDCounter++;
	}
	
}
