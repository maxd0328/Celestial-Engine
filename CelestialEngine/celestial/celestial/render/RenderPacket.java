package celestial.render;

import java.util.ArrayList;
import celestial.core.CEObject;
import celestial.shader.Shader;

public final class RenderPacket {
	
	private final Renderer renderer;
	private final CEObject camera;
	private final Shader shader;
	private final boolean fboRender;
	private final ArrayList<CEObject> unalteredScene;
	private final CEObject[] scene;
	
	protected RenderPacket(Renderer renderer, CEObject camera, Shader shader, boolean fboRender, ArrayList<CEObject> unalteredScene, CEObject...scene) {
		this.renderer = renderer;
		this.camera = camera;
		this.shader = shader;
		this.fboRender = fboRender;
		this.unalteredScene = unalteredScene;
		this.scene = scene;
	}
	
	public RenderPacket(RenderPacket src, Shader shader) {
		this.renderer = src.renderer;
		this.camera = src.camera;
		this.shader = shader;
		this.fboRender = src.fboRender;
		this.unalteredScene = src.unalteredScene;
		this.scene = src.scene;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	public CEObject getCamera() {
		return camera;
	}
	
	public Shader getShader() {
		return shader;
	}
	
	public boolean isFboRender() {
		return fboRender;
	}
	
	public ArrayList<CEObject> getUnalteredScene() {
		return unalteredScene;
	}
	
	public CEObject[] getScene() {
		return scene;
	}
	
}
