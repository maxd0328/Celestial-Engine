package celestial.shading;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.data.Sampler;

public final class Attribute extends Input {
	
	private final Property<Constant> constant;
	
	public Attribute(String attribID, Constant constant) {
		super(attribID, constant.getType());
		this.constant = Properties.createProperty(Constant.class, constant);
	}
	
	public Constant getConstant() {
		return constant.get();
	}
	
	public void setConstant(Constant constant) {
		this.constant.set(constant);
	}
	
	public Property<Constant> constantProperty() {
		return constant;
	}
	
	@Override
	public Sampler obtainInput(ShadingSystem system) {
		try {
			return super.obtainInput(system);
		}
		catch(InputAvailabilityException ex) {
			return constant.get().obtainConstant();
		}
	}
	
	@Override
	public boolean containsData(GLData data) {
		return constant.get().containsData(data);
	}
	
}
