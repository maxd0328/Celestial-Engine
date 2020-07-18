package studio.celestial.dialog;

public final class MessageDialog {
	
	private final MessageType type;
	private final String message;
	
	public MessageDialog(MessageType type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public MessageType getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}
	
	public static enum MessageType {
		
		MESSAGE_TYPE_INFORMATION,
		
		MESSAGE_TYPE_ERROR,
		
		MESSAGE_TYPE_WARNING,
		
		MESSAGE_TYPE_CONFIRMATION;
		
	}
	
}
