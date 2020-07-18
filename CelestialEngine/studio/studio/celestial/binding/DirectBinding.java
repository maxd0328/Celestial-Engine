package studio.celestial.binding;

import java.util.ArrayList;

import celestial.util.Event;
import celestial.util.IGetter;
import celestial.util.ISetter;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioInterface;

public final class DirectBinding<T> implements PropertyBinding<T> {
	
	private static final int REFUSAL_THRESHOLD = 10;
	
	private final IGetter<T> controlEndpointGetter, glEndpointGetter;
	private final ISetter<T> controlEndpointSetter, glEndpointSetter;
	private String controlThread = PropertyBinding.CONTROL_THREAD_ID, glThread = PropertyBinding.GL_THREAD_ID;
	
	private T controlEndpoint, glEndpoint;
	private boolean controlChange = false, glChange = false;
	private boolean lockedControl = false, lockedGL = false;
	private ArrayList<RefusalMonitor<T>> refusals = new ArrayList<RefusalMonitor<T>>();
	private ArrayList<T> controlIgnores = new ArrayList<T>(), glIgnores = new ArrayList<T>();
	
	private boolean initSynced;
	
	public DirectBinding(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter, IGetter<T> glEndpointGetter, ISetter<T> glEndpointSetter) {
		this.controlEndpointGetter = controlEndpointGetter;
		this.controlEndpointSetter = controlEndpointSetter;
		this.glEndpointGetter = glEndpointGetter;
		this.glEndpointSetter = glEndpointSetter;
		
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
		controlEndpointSetter.set(value);
		glEndpointSetter.set(value);
		controlEndpoint = value;
		glEndpoint = value;
	}
	
	@Override
	public PropertyBinding<T> withControlThread(String controlThread) {
		this.controlThread = controlThread;
		return this;
	}
	
	@Override
	public PropertyBinding<T> withGLThread(String glThread) {
		this.glThread = glThread;
		return this;
	}
	
	public DirectBinding<T> withLockedControl() {
		this.lockedControl = true;
		return this;
	}
	
	public DirectBinding<T> withLockedGL() {
		this.lockedGL = true;
		return this;
	}
	
	@Override
	public void sync() {
		glEndpoint = glEndpointGetter.get();
		controlEndpoint = controlEndpointGetter.get();
		if(!PropertyBinding.<T>equals(controlEndpoint, glEndpoint)) {
			if(Thread.currentThread().getName().equals(controlThread)) controlEndpointSetter.set(glEndpoint);
			else StudioInterface.getInstantiation().request(s -> controlEndpointSetter.set(glEndpoint));
		}
	}
	
	@Override
	public void update() {
		if(!initSynced && (initSynced = true)) sync();
		T newControlEndpoint = controlEndpointGetter.get(), newGlEndpoint = glEndpointGetter.get();
		
		SynchronizationBlock:
		do {
			if(!lockedControl && (!PropertyBinding.<T>equals(newGlEndpoint, glEndpoint) || glChange)) {
				glChange = false;
				for(T ignore : controlIgnores) if(PropertyBinding.<T>equals(newGlEndpoint, ignore)) break SynchronizationBlock;
				controlIgnores.clear();
				if(Thread.currentThread().getName().equals(controlThread)) controlEndpointSetter.set(newGlEndpoint);
				else StudioInterface.getInstantiation().request(s -> controlEndpointSetter.set(newGlEndpoint));
				glIgnores.add(newGlEndpoint);
				refusals.add(new RefusalMonitor<T>(newGlEndpoint, controlEndpointGetter, o -> controlChange = true));
			}
			else if(!lockedGL && (!PropertyBinding.<T>equals(newControlEndpoint, controlEndpoint) || controlChange)) {
				controlChange = false;
				for(T ignore : glIgnores) if(PropertyBinding.<T>equals(newControlEndpoint, ignore)) break SynchronizationBlock;
				glIgnores.clear();
				if(Thread.currentThread().getName().equals(glThread)) glEndpointSetter.set(newControlEndpoint);
				else GLRequestSystem.request(() -> glEndpointSetter.set(newControlEndpoint));
				controlIgnores.add(newControlEndpoint);
				refusals.add(new RefusalMonitor<T>(newControlEndpoint, glEndpointGetter, o -> glChange = true));
			}
			else if(!PropertyBinding.<T>equals(newControlEndpoint, newGlEndpoint)) {
				if(!lockedGL) {
					if(Thread.currentThread().getName().equals(glThread)) glEndpointSetter.set(newControlEndpoint);
					else GLRequestSystem.request(() -> glEndpointSetter.set(newControlEndpoint));
					controlIgnores.add(newControlEndpoint);
					refusals.add(new RefusalMonitor<T>(newControlEndpoint, glEndpointGetter, o -> glChange = true));
				}
				else if(!lockedControl) {
					if(Thread.currentThread().getName().equals(controlThread)) controlEndpointSetter.set(newGlEndpoint);
					else StudioInterface.getInstantiation().request(s -> controlEndpointSetter.set(newGlEndpoint));
					glIgnores.add(newGlEndpoint);
					refusals.add(new RefusalMonitor<T>(newGlEndpoint, controlEndpointGetter, o -> controlChange = true));
				}
			}
		}
		while(false);
		
		this.controlEndpoint = newControlEndpoint;
		this.glEndpoint = newGlEndpoint;
		
		for(RefusalMonitor<T> refusal : new ArrayList<RefusalMonitor<T>>(refusals)) {
			if(refusal.elapsedCycles++ > REFUSAL_THRESHOLD) {
				if(!PropertyBinding.<T>equals(refusal.requestedValue, refusal.getter.get())) refusal.response.perform(refusal);
				refusals.remove(refusal);
			}
			else if(PropertyBinding.<T>equals(refusal.requestedValue, refusal.getter.get())) refusals.remove(refusal);
		}
	}
	
	public static <T> PropertyBindingFactory<T> factory() {
		return new PropertyBindingFactory<T>() {
			
			@Override
			public PropertyBinding<T> build(IGetter<T> controlEndpointGetter, ISetter<T> controlEndpointSetter, IGetter<T> glEndpointGetter, ISetter<T> glEndpointSetter) {
				return new DirectBinding<T>(controlEndpointGetter, controlEndpointSetter, glEndpointGetter, glEndpointSetter);
			}
			
		};
	}
	
	private static final class RefusalMonitor<T> {
		
		private final T requestedValue;
		private final IGetter<T> getter;
		private final Event response;
		private int elapsedCycles = 0;
		
		public RefusalMonitor(T requestedValue, IGetter<T> getter, Event response) {
			this.requestedValue = requestedValue;
			this.getter = getter;
			this.response = response;
		}
		
	}
	
}
