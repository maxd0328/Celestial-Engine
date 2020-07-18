package celestial.render;

public class RenderAbortException extends RuntimeException {
	
	private static final long serialVersionUID = -6947982240928857430L;
	
	public RenderAbortException() {
		super();
	}
	
	public RenderAbortException(String message) {
		super(message);
	}
	
	public RenderAbortException(Throwable cause) {
		super(cause);
	}
	
	public RenderAbortException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
