package celestial.shading;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.data.Sampler;

public class Input {
	
	private final String attribName;
	private final ConnectorType type;
	private final Property<Output> link;
	
	public Input(String attribName, ConnectorType type) {
		this.attribName = attribName;
		this.type = type;
		this.link = Properties.createProperty(Output.class, null);
	}
	
	public String getAttribName() {
		return attribName;
	}
	
	public ConnectorType getType() {
		return type;
	}
	
	public Output getLink() {
		return link.get();
	}
	
	public void setLink(Output link) {
		this.link.set(link);
	}
	
	public Property<Output> linkProperty() {
		return link;
	}
	
	public Sampler obtainInput(ShadingSystem system) {
		if(link.get() == null)
			throw new InputAvailabilityException();
		
		if(!type.isCompatibleWith(link.get().getType()))
			throw new IllegalStateException("Incompatible link between nodes");
		
		return link.get().obtainOutput(system);
	}
	
	public boolean containsData(GLData data) {
		return false;
	}
	
}
