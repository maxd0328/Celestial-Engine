package studio.celestial.binding;

import java.util.ArrayList;
import celestial.util.IGetter;
import celestial.util.ISetter;

public final class ConditionalBinding<T> implements PropertyBinding<T> {
	
	private final IGetter<T> controlEndpointGetter, glEndpointGetter;
	private final ISetter<T> controlEndpointSetter, glEndpointSetter;
	
	private final ArrayList<BindingState<T>> states = new ArrayList<BindingState<T>>();
	private PropertyBinding<T> finalState;
	
	private PropertyBinding<T> lastState = null;
	
	private boolean initSynced;
	
	public ConditionalBinding(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter,
			IGetter<T> glEndpointGetter, ISetter<T> glEndpointSetter, PropertyBindingFactory<T> initialState) {
		this.controlEndpointGetter = controlEndpointGetter;
		this.controlEndpointSetter = controlEndpointSetter;
		this.glEndpointGetter = glEndpointGetter;
		this.glEndpointSetter = glEndpointSetter;
		this.finalState = initialState.build(controlEndpointGetter, controlEndpointSetter, glEndpointGetter, glEndpointSetter);
		
		this.initSynced = false;
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
		return glEndpointGetter;
	}
	
	@Override
	public ISetter<T> glEndpointSetter() {
		return glEndpointSetter;
	}
	
	@Override
	public void set(T value) {
		for(BindingState<T> state : states) {
			if(state.conditional.condition()) {
				state.binding.set(value);
				return;
			}
		}
		if(finalState != null) finalState.set(value);
	}
	
	@Override
	public PropertyBinding<T> withControlThread(String controlThread) {
		return this;
	}
	
	@Override
	public PropertyBinding<T> withGLThread(String glThread) {
		return this;
	}
	
	@Override
	public void sync() {
		for(BindingState<T> state : states) state.binding.sync();
		if(finalState != null) finalState.sync();
	}
	
	public ConditionalBinding<T> when(IConditional conditional) {
		if(finalState != null) {
			this.states.add(new BindingState<T>(finalState, conditional));
			this.finalState = null;
		}
		return this;
	}
	
	public ConditionalBinding<T> otherwise(PropertyBindingFactory<T> state) {
		if(finalState == null) {
			this.finalState = state.build(controlEndpointGetter, controlEndpointSetter, glEndpointGetter, glEndpointSetter);
		}
		sync();
		return this;
	}
	
	@Override
	public void update() {
		if(!initSynced && (initSynced = true)) sync();
		for(BindingState<T> state : states) {
			if(state.conditional.condition()) {
				if(state.binding != lastState) {
					state.binding.sync();
					lastState = state.binding;
				}
				state.binding.update();
				return;
			}
		}
		if(finalState != null) {
			if(finalState != lastState) {
				finalState.sync();
				lastState = finalState;
			}
			finalState.update();
		}
	}
	
	public static <T> PropertyBindingFactory<T> factory(final ConditionalBinding<T> binding) {
		return new PropertyBindingFactory<T>() {
			
			@Override
			public PropertyBinding<T> build(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter, IGetter<T> glEndpointGetter, ISetter<T> glEndpointSetter) {
				ConditionalBinding<T> genBinding = new ConditionalBinding<T>(controlEndpointGetter, controlEndpointSetter, glEndpointGetter, glEndpointSetter, null);
				genBinding.finalState = binding.finalState;
				genBinding.states.addAll(binding.states);
				return genBinding;
			}
			
		};
	}
	
	private static final class BindingState<T> {
		
		private final PropertyBinding<T> binding;
		private final IConditional conditional;
		
		private BindingState(PropertyBinding<T> binding, IConditional conditional) {
			this.binding = binding;
			this.conditional = conditional;
		}
		
	}
	
	public static interface IConditional {
		
		public boolean condition();
		
	}
	
}
