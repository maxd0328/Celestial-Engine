package studio.celestial.binding;

import celestial.util.IGetter;
import celestial.util.ISetter;
import studio.celestial.core.GLRequestSystem;

public final class DeltaBinding<T> implements PropertyBinding<T> {
	
	public static final IDifferentiator<Float> FLOAT_DIFFERENTIATOR = new IDifferentiator<Float>() {
		
		@Override
		public Float difference(Float _old, Float _new) {
			return _new - _old;
		}
		
	};
	
	public static final IDifferentiator<Integer> INTEGER_DIFFERENTIATOR = new IDifferentiator<Integer>() {
		
		@Override
		public Integer difference(Integer _old, Integer _new) {
			return _new - _old;
		}
		
	};
	
	private final IGetter<T> controlEndpointGetter;
	private final ISetter<T> controlEndpointSetter, glEndpointSetter;
	private final IDifferentiator<T> differentiator;
	private String glThread = PropertyBinding.GL_THREAD_ID;
	
	private T controlEndpoint;
	
	/**
	 * GL-Endpoint setter should add, not set
	 */
	public DeltaBinding(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter, ISetter<T> glEndpointSetter, IDifferentiator<T> differentiator) {
		this.controlEndpointGetter = controlEndpointGetter;
		this.controlEndpointSetter = controlEndpointSetter;
		this.glEndpointSetter = glEndpointSetter;
		this.differentiator = differentiator;
		
		this.controlEndpoint = controlEndpointGetter.get();
	}
	
	@Override
	public IGetter<T> controlEndpointGetter() {
		return controlEndpointGetter;
	}
	
	@Override
	public ISetter<T> controlEndpointSetter() {
		return controlEndpointSetter;
	}
	
	@Override
	public IGetter<T> glEndpointGetter() {
		return null;
	}
	
	@Override
	public ISetter<T> glEndpointSetter() {
		return glEndpointSetter;
	}
	
	public IDifferentiator<T> getDifferentiator() {
		return differentiator;
	}
	
	@Override
	public void set(T value) {
		controlEndpointSetter.set(value);
		controlEndpoint = value;
	}
	
	@Override
	public PropertyBinding<T> withControlThread(String controlThread) {
		return this;
	}
	
	@Override
	public PropertyBinding<T> withGLThread(String glThread) {
		this.glThread = glThread;
		return this;
	}
	
	@Override
	public void sync() {
		controlEndpoint = controlEndpointGetter.get();
	}
	
	@Override
	public void update() {
		T newControlEndpoint = controlEndpointGetter.get();
		
		if(!PropertyBinding.<T>equals(newControlEndpoint, controlEndpoint)) {
			T difference = differentiator.difference(controlEndpoint, newControlEndpoint);
			
			if(Thread.currentThread().getName().equals(glThread)) glEndpointSetter.set(difference);
			else GLRequestSystem.request(() -> glEndpointSetter.set(difference));
		}
		
		this.controlEndpoint = newControlEndpoint;
	}
	
	public static <T> PropertyBindingFactory<T> factory(final ISetter<T> glSumEndpointSetter, final IDifferentiator<T> differentiator) {
		return new PropertyBindingFactory<T>() {
			
			@Override
			public PropertyBinding<T> build(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter, IGetter<T> glEndpointGetter, ISetter<T> glEndpointSetter) {
				return new DeltaBinding<T>(controlEndpointGetter, controlEndpointSetter, glSumEndpointSetter, differentiator);
			}
			
		};
	}
	
	public static interface IDifferentiator<T> {
		
		public T difference(T _old, T _new);
		
	}
	
}
