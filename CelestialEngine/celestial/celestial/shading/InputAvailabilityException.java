package celestial.shading;

public final class InputAvailabilityException extends RuntimeException {
	
	private static final long serialVersionUID = 2991310946082944487L;
	
	public InputAvailabilityException() {
		super();
	}
	
	public InputAvailabilityException(String message) {
		super(message);
	}
	
	public InputAvailabilityException(Throwable cause) {
		super(cause);
	}
	
	public InputAvailabilityException(String message, Throwable cause) {
		super(message, cause);
	}

}
