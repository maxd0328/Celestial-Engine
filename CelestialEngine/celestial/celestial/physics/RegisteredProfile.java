package celestial.physics;

public abstract class RegisteredProfile {
	
	private boolean active = true;
	
	protected RegisteredProfile() {}
	
	protected void register() {
		this.active = true;
	}
	
	protected void await() {
		this.active = false;
	}
	
	public boolean isActive() {
		return active;
	}
	
}
