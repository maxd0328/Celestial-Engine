package celestial.shading;

import celestial.data.GLData;
import celestial.data.Sampler;
import celestial.util.Factory;

public final class FactoryOutput extends Output {
	
	private final Factory<Sampler> factory;
	
	public FactoryOutput(ConnectorType type, Factory<Sampler> factory) {
		super(type, null);
		this.factory = factory;
	}
	
	@Override
	public Sampler obtainOutput(ShadingSystem system) {
		return factory.build();
	}
	
	@Override
	public boolean containsData(GLData data, ShadingSystem system) {
		return factory.build() == data;
	}
	
}
