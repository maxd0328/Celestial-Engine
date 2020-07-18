package celestial.ui;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.shading.ShadingSystem;

public class UILayer {
	
	private final Property<String> identifier;
	private final Property<Container> rootContainer;
	private final Property<ShadingSystem> postProcessingSystem;
	
	public UILayer(String identifier, Container rootContainer) {
		this.identifier = Properties.createStringProperty(identifier);
		this.rootContainer = Properties.createProperty(Container.class, rootContainer);
		this.postProcessingSystem = Properties.createProperty(ShadingSystem.class);
	}
	
	public String getIdentifier() {
		return identifier.get();
	}
	
	public void setIdentifier(String identifier) {
		this.identifier.set(identifier);
	}
	
	public Property<String> identifierProperty() {
		return identifier;
	}
	
	public Container getRootContainer() {
		return rootContainer.get();
	}
	
	public void setRootContainer(Container rootContainer) {
		this.rootContainer.set(rootContainer);
	}
	
	public Property<Container> rootContainerProperty() {
		return rootContainer;
	}
	
	public ShadingSystem getPostProcessingSystem() {
		return postProcessingSystem.get();
	}
	
	public void setPostProcessingSystem(ShadingSystem postProcessingSystem) {
		this.postProcessingSystem.set(postProcessingSystem);
	}
	
	public Property<ShadingSystem> postProcessingSystemProperty() {
		return postProcessingSystem;
	}
	
}
