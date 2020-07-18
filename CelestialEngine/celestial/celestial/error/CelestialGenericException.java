package celestial.error;

import org.lwjgl.LWJGLException;

public class CelestialGenericException extends RuntimeException {
	private static final long serialVersionUID = 5201477028404796252L;
	
	public CelestialGenericException() {
		super();
	}
	
	public CelestialGenericException(String message) {
		super(message);
	}
	
	public CelestialGenericException(Throwable cause) {
		super(cause);
	}
	
	public CelestialGenericException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CelestialGenericException(LWJGLException ex) {
		this("Fatal LWJGL error: " + ex.getMessage());
	}
	
}
