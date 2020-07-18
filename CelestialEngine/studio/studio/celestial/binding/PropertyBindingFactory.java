package studio.celestial.binding;

import celestial.util.IGetter;
import celestial.util.ISetter;

public interface PropertyBindingFactory<T> {
	
	public PropertyBinding<T> build(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter, IGetter<T> glEndpointGetter, ISetter<T> glEndpointSetter);
	
}
