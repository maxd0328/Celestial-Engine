package celestial.util;

public enum Priority {
	
	HIGHEST,
	
	HIGH,
	
	MEDIUM,
	
	LOW,
	
	LOWEST,
	
	NONE;
	
	public boolean hasPriorityOver(Priority other) {
		if(this == NONE)
			return false;
		
		return this.asInteger() > asInteger(other);
	}
	
	public boolean isEqualPriority(Priority other) {
		if(this == NONE)
			return false;
		
		return this.asInteger() == asInteger(other);
	}
	
	public int asInteger() {
		switch(this) {
		case HIGHEST:
			return 5;
		case HIGH:
			return 4;
		case MEDIUM:
			return 3;
		case LOW:
			return 2;
		case LOWEST:
			return 1;
		case NONE:
		default:
			return 0;
		}
	}
	
	public static int asInteger(Priority priority) {
		return priority == null ? 0 : priority.asInteger();
	}
	
	public static int getRange() {
		return HIGHEST.asInteger();
	}
	
}
