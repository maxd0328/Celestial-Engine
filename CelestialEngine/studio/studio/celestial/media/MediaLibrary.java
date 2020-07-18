package studio.celestial.media;

import java.io.Serializable;

public final class MediaLibrary implements Serializable {
	
	private static final long serialVersionUID = -598483452426518118L;
	
	private final MediaFolder root;
	
	public MediaLibrary() {
		this.root = new MediaFolder("dummy-root");
	}
	
	public MediaFolder getRoot() {
		return root;
	}
	
	public void update() {
		root.update();
	}
	
}
