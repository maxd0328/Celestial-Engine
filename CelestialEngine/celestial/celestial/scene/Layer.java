package celestial.scene;

import java.util.ArrayList;
import java.util.Arrays;
import celestial.core.CEObject;
import celestial.util.Event;

public final class Layer implements java.io.Serializable {
	
	private static final long serialVersionUID = -4556898194259325855L;
	
	private final ArrayList<CEObject> objects;
	private boolean enabled = true;
	private String identifier;
	private Event event = null;
	
	public Layer(String identifier, CEObject...objects) {
		this.objects = new ArrayList<CEObject>(Arrays.asList(objects));
		this.identifier = identifier;
	}
	
	public synchronized CEObject getObject(String identifier) {
		for(CEObject object : objects) if(object.getIdentifier().equals(identifier)) return object;
		return null;
	}
	
	public synchronized ArrayList<CEObject> getObjects() {
		return objects;
	}
	
	public synchronized void addObject(CEObject object) {
		this.objects.add(object);
	}
	
	public synchronized void removeObject(CEObject object) {
		this.objects.remove(object);
	}
	
	public synchronized void removeObject(String identifier) {
		for(CEObject obj : new ArrayList<CEObject>(objects)) if(obj.getIdentifier().equals(identifier)) objects.remove(obj);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public void disable() {
		this.enabled = false;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public void setEvent(Event event) {
		this.event = event;
	}
	
	@Override
	public String toString() {
		return "CELayer:\"" + identifier + "\"";
	}
	
}
