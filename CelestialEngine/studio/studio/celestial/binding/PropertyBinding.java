package studio.celestial.binding;

import celestial.util.IGetter;
import celestial.util.ISetter;

public interface PropertyBinding<T> {
	
	public static final String CONTROL_THREAD_ID		= "JavaFX Application Thread";
	public static final String GL_THREAD_ID				= "CEStudio Application Thread";
	
	public IGetter<T> controlEndpointGetter();
	
	public ISetter<T> controlEndpointSetter();
	
	public IGetter<T> glEndpointGetter();
	
	public ISetter<T> glEndpointSetter();
	
	public void set(T value);
	
	public void update();
	
	public PropertyBinding<T> withControlThread(String controlThread);
	
	public PropertyBinding<T> withGLThread(String glThread);
	
	public void sync();
	
	public static <T> boolean equals(T a, T b) {
		if(a == null) return b == null;
		if(b == null) return a == null;
		return a.equals(b);
	}
	
}
