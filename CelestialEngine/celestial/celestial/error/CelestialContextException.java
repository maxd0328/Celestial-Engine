package celestial.error;

public class CelestialContextException extends RuntimeException {
	private static final long serialVersionUID = -43871299177970082L;
	
	public CelestialContextException() {
		super();
	}
	
	public CelestialContextException(String message) {
		super(message);
	}
	
	public CelestialContextException(Throwable cause) {
		super(cause);
	}
	
	public CelestialContextException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
