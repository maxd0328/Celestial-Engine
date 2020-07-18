package studio.celestial.media;

public interface MediaNode {
	
	public String getName();
	
	public void setName(String name);
	
	public MediaBox getMediaBox();
	
	public void delete();
	
	public boolean queuedForDelete();
	
}
