package celestial.error;

public class CelestialGLException extends RuntimeException {
	private static final long serialVersionUID = -995911372755119259L;
	
	public CelestialGLException() {
		super();
	}
	
	public CelestialGLException(String message) {
		super(message);
	}
	
	public CelestialGLException(Throwable cause) {
		super(cause);
	}
	
	public CelestialGLException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
