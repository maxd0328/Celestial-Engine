package celestial.vecmath;

public class VecmathException extends RuntimeException {
	private static final long serialVersionUID = -7288790610334706642L;
	
	public VecmathException() {
		super();
	}
	
	public VecmathException(String message) {
		super(message);
	}
	
	public VecmathException(Throwable cause) {
		super(cause);
	}
	
	public VecmathException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
