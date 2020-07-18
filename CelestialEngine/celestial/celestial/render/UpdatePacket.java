package celestial.render;

import celestial.core.CEObject;
import celestial.scene.Scene;

public class UpdatePacket {
	
	private final CEObject camera;
	private final Scene scene;
	private final boolean paused;
	
	public UpdatePacket(CEObject camera, Scene scene, boolean paused) {
		this.camera = camera;
		this.scene = scene;
		this.paused = paused;
	}
	
	public CEObject getCamera() {
		return camera;
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
}
