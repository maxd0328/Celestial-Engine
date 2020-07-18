package celestial.ui.event;

public interface EventConsumer<T extends ActionEvent> {
	
	public void accept(T event);
	
}
