package studio.celestial.media;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import celestial.serialization.SerializerImpl;

public final class MediaFolder implements MediaNode, java.io.Serializable {
	
	private static final long serialVersionUID = -7352262615049616348L;
	
	private String name;
	private transient MediaBox mediaBox;
	private final ArrayList<MediaFolder> children;
	private final ArrayList<Resource> resources;
	
	private boolean queuedForDelete = false;
	
	public MediaFolder(String name) {
		this.name = name;
		this.mediaBox = new MediaBox(this);
		this.children = new ArrayList<MediaFolder>();
		this.resources = new ArrayList<Resource>();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
		mediaBox.setIdentifier(name);
	}
	
	@Override
	public MediaBox getMediaBox() {
		return mediaBox;
	}
	
	public ArrayList<MediaFolder> getChildren() {
		return children;
	}
	
	public ArrayList<Resource> getResources() {
		return resources;
	}
	
	public boolean containsMediaBox(MediaBox box) {
		for(MediaFolder child : children) if(child.getMediaBox() == box) return true;
		for(Resource res : resources) if(res.getMediaBox() == box) return true;
		return false;
	}
	
	public void update() {
		resources.removeIf(resource -> resource.queuedForDelete());
		children.removeIf(child -> child.queuedForDelete());
		for(MediaFolder child : children) child.update();
	}
	
	@Override
	public void delete() {
		this.queuedForDelete = true;
	}
	
	@Override
	public boolean queuedForDelete() {
		return queuedForDelete;
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		mediaBox = new MediaBox(this);
	}
	
}
