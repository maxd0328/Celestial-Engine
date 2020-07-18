package celestial.serialization;

public class UnserializableException extends RuntimeException {
	private static final long serialVersionUID = -3459032182134895825L;
	
	public UnserializableException() {
		super();
	}
	
	public UnserializableException(String message) {
		super(message);
	}
	
	public UnserializableException(Throwable cause) {
		super(cause);
	}
	
	public UnserializableException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
