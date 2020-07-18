package celestial.ui;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.ui.event.ActionEvent;
import celestial.ui.event.EventConsumer;
import celestial.ui.event.KeyEvent;
import celestial.ui.event.MouseEvent;
import celestial.ui.geometry.Alignment;
import celestial.ui.geometry.Margin;
import celestial.util.KVEntry;
import celestial.vecmath.Vector2f;

public abstract class Component {
	
	private final Property<String> identifier;
	private final Property<Graphic> graphic;
	private final Property<Margin> margin;
	private final Property<Vector2f> layoutOffset;
	private final Property<Boolean> focusTraversable;
	private final Property<Boolean> focused;
	
	private final Property<Boolean> requestPosition;
	private final Property<Vector2f> prefPosition;
	private final Property<Boolean> requestScale;
	private final Property<Vector2f> prefScale;
	
	private final Property<EventConsumer<ActionEvent>> onAction;
	
	private final Property<EventConsumer<MouseEvent>> onMousePressed;
	private final Property<EventConsumer<MouseEvent>> onMouseReleased;
	private final Property<EventConsumer<MouseEvent>> onMouseClicked;
	private final Property<EventConsumer<MouseEvent>> onMouseDragged;
	private final Property<EventConsumer<MouseEvent>> onMouseMoved;
	private final Property<EventConsumer<MouseEvent>> onMouseEntered;
	private final Property<EventConsumer<MouseEvent>> onMouseExited;
	
	private final Property<EventConsumer<KeyEvent>> onKeyPressed;
	private final Property<EventConsumer<KeyEvent>> onKeyReleased;
	private final Property<EventConsumer<KeyEvent>> onKeyTyped;
	
	private CompoundComponent registeredContainer;
	private Vector2f constrainedPosition = new Vector2f();
	private Vector2f constrainedScale = new Vector2f();
	
	@SuppressWarnings("unchecked")
	protected Component(String identifier, Graphic graphic) {
		this.identifier = Properties.createStringProperty(identifier);
		this.graphic = Properties.createProperty(Graphic.class, graphic);
		this.margin = Properties.createProperty(Margin.class, new Margin());
		this.layoutOffset = Properties.createVec2Property();
		this.focusTraversable = Properties.createBooleanProperty(true);
		this.focused = Properties.createBooleanProperty();
		
		this.requestPosition = Properties.createBooleanProperty();
		this.prefPosition = Properties.createVec2Property();
		this.requestScale = Properties.createBooleanProperty();
		this.prefScale = Properties.createVec2Property();
		
		this.onAction = Properties.createProperty((Class<EventConsumer<ActionEvent>>) (Class<?>) EventConsumer.class);
		
		this.onMousePressed = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		this.onMouseReleased = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		this.onMouseClicked = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		this.onMouseDragged = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		this.onMouseMoved = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		this.onMouseEntered = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		this.onMouseExited = Properties.createProperty((Class<EventConsumer<MouseEvent>>) (Class<?>) EventConsumer.class);
		
		this.onKeyPressed = Properties.createProperty((Class<EventConsumer<KeyEvent>>) (Class<?>) EventConsumer.class);
		this.onKeyReleased = Properties.createProperty((Class<EventConsumer<KeyEvent>>) (Class<?>) EventConsumer.class);
		this.onKeyTyped = Properties.createProperty((Class<EventConsumer<KeyEvent>>) (Class<?>) EventConsumer.class);
	}
	
	public void registerComponent(CompoundComponent cont) {
		if(registeredContainer == null)
			registeredContainer = cont;
		else if(registeredContainer != cont)
			throw new IllegalStateException("Duplicate component registration");
	}
	
	void setConstrainedCoords(Vector2f constrainedPosition, Vector2f constrainedScale) {
		this.constrainedPosition = constrainedPosition;
		this.constrainedScale = constrainedScale;
	}
	
	public Vector2f getConstrainedPosition() {
		return new Vector2f(constrainedPosition);
	}
	
	public Vector2f getConstrainedScale() {
		return new Vector2f(constrainedScale);
	}
	
	public KVEntry<Vector2f, Vector2f> constrainTo(int boundLeft, int boundRight, int boundTop, int boundBottom, Alignment alignment) {
		Vector2f scale = requestScale.get() ? new Vector2f(prefScale.get()) : new Vector2f(boundRight - boundLeft, boundBottom - boundTop);
		Vector2f position;
		
		if(alignment != null && alignment != Alignment.NONE) {
			position = new Vector2f();
			if(scale.x > boundRight - boundLeft) scale.x = boundRight - boundLeft;
			if(scale.y > boundBottom - boundTop) scale.y = boundBottom - boundTop;
			
			switch(alignment.toHoriz()) {
			case LEFT:
				position.x = boundLeft;
				break;
			case CENTER:
				position.x = (boundLeft + boundRight) / 2 - scale.x / 2;
				break;
			case RIGHT:
				position.x = boundRight - scale.x;
				break;
			default:
			}
			
			switch(alignment.toVertical()) {
			case TOP:
				position.y = boundTop;
				break;
			case CENTER:
				position.y = (boundBottom + boundTop) / 2 - scale.y / 2;
				break;
			case BOTTOM:
				position.y = boundBottom - scale.y;
				break;
			default:
			}
		}
		else if(requestPosition.get()) {
			position = new Vector2f(prefPosition.get());
			if(position.x < boundLeft) position.x = boundLeft;
			if(position.x + scale.x > boundRight) position.x = Math.max(boundLeft, boundRight - scale.x);
			
			if(position.y < boundTop) position.y = boundTop;
			if(position.y + scale.y > boundBottom) position.y = Math.max(boundTop, boundBottom - scale.y);
			
			if(position.x + scale.x > boundRight) scale.x = boundRight - boundLeft;
			if(position.y + scale.y > boundBottom) scale.y = boundBottom - boundTop;
		}
		else {
			position = new Vector2f(boundLeft, boundTop);
			if(position.x + scale.x > boundRight) scale.x = boundRight - boundLeft;
			if(position.y + scale.y > boundBottom) scale.y = boundBottom - boundTop;
		}
		
		return new KVEntry<>(position, scale);
	}
	
	private void clearRegistration() {
		this.registeredContainer = null;
	}
	
	public boolean containsData(GLData data) {
		return graphic.get() != null && graphic.get().containsData(data);
	}
	
	public String getIdentifier() {
		return identifier.get();
	}
	
	public void setIdentifier(String identifier) {
		this.identifier.set(identifier);
	}
	
	public Property<String> identifierProperty() {
		return identifier;
	}
	
	public Graphic getGraphic() {
		return graphic.get();
	}
	
	public void setGraphic(Graphic graphic) {
		this.graphic.set(graphic);
	}
	
	public Property<Graphic> graphicProperty() {
		return graphic;
	}
	
	public Margin getMargin() {
		return margin.get();
	}
	
	public void setMargin(Margin margin) {
		this.margin.set(margin);
	}
	
	public Property<Margin> marginProperty() {
		return margin;
	}
	
	public Vector2f getLayoutOffset() {
		return layoutOffset.get();
	}
	
	public void setLayoutOffset(Vector2f layoutOffset) {
		this.layoutOffset.set(layoutOffset);
	}
	
	public Property<Vector2f> layoutOffsetProperty() {
		return layoutOffset;
	}
	
	public boolean isFocusTraversable() {
		return focusTraversable.get();
	}
	
	public void setFocusTraversable(boolean focusTraversable) {
		this.focusTraversable.set(focusTraversable);
	}
	
	public Property<Boolean> focusTraversableProperty() {
		return focusTraversable;
	}
	
	public boolean isFocused() {
		return focused.get();
	}
	
	public void setFocused(boolean focused) {
		this.focused.set(focused);
	}
	
	public Property<Boolean> focusedProperty() {
		return focused;
	}
	
	public boolean isRequestPosition() {
		return requestPosition.get();
	}
	
	public void setRequestPosition(boolean requestPosition) {
		this.requestPosition.set(requestPosition);
	}
	
	public Property<Boolean> requestPositionProperty() {
		return requestPosition;
	}
	
	public Vector2f getPrefPosition() {
		return prefPosition.get();
	}
	
	public void setPrefPosition(Vector2f prefPosition) {
		if(prefPosition != null)
			requestPosition.set(true);
		this.prefPosition.set(prefPosition);
	}
	
	public Property<Vector2f> prefPositionProperty() {
		return prefPosition;
	}
	
	public boolean isRequestScale() {
		return requestScale.get();
	}
	
	public void setRequestScale(boolean requestScale) {
		this.requestScale.set(requestScale);
	}
	
	public Property<Boolean> requestScaleProperty() {
		return requestScale;
	}
	
	public Vector2f getPrefScale() {
		return prefScale.get();
	}
	
	public void setPrefScale(Vector2f prefScale) {
		if(prefScale != null)
			requestScale.set(true);
		this.prefScale.set(prefScale);
	}
	
	public Property<Vector2f> prefScaleProperty() {
		return prefScale;
	}
	
	public EventConsumer<ActionEvent> getOnAction() {
		return onAction.get();
	}
	
	public void setOnAction(EventConsumer<ActionEvent> onAction) {
		this.onAction.set(onAction);
	}
	
	public Property<EventConsumer<ActionEvent>> onActionProperty() {
		return onAction;
	}
	
	public EventConsumer<MouseEvent> getOnMousePressed() {
		return onMousePressed.get();
	}
	
	public void setOnMousePressed(EventConsumer<MouseEvent> onMousePressed) {
		this.onMousePressed.set(onMousePressed);
	}
	
	public Property<EventConsumer<MouseEvent>> onMousePressedProperty() {
		return onMousePressed;
	}
	
	public EventConsumer<MouseEvent> getOnMouseReleased() {
		return onMouseReleased.get();
	}
	
	public void setOnMouseReleased(EventConsumer<MouseEvent> onMouseReleased) {
		this.onMouseReleased.set(onMouseReleased);
	}
	
	public Property<EventConsumer<MouseEvent>> onMouseReleasedProperty() {
		return onMouseReleased;
	}
	
	public EventConsumer<MouseEvent> getOnMouseClicked() {
		return onMouseClicked.get();
	}
	
	public void setOnMouseClicked(EventConsumer<MouseEvent> onMouseClicked) {
		this.onMouseClicked.set(onMouseClicked);
	}
	
	public Property<EventConsumer<MouseEvent>> onMouseClickedProperty() {
		return onMouseClicked;
	}
	
	public EventConsumer<MouseEvent> getOnMouseDragged() {
		return onMouseDragged.get();
	}
	
	public void setOnMouseDragged(EventConsumer<MouseEvent> onMouseDragged) {
		this.onMouseDragged.set(onMouseDragged);
	}
	
	public Property<EventConsumer<MouseEvent>> onMouseDraggedProperty() {
		return onMouseDragged;
	}
	
	public EventConsumer<MouseEvent> getOnMouseMoved() {
		return onMouseMoved.get();
	}
	
	public void setOnMouseMoved(EventConsumer<MouseEvent> onMouseMoved) {
		this.onMouseMoved.set(onMouseMoved);
	}
	
	public Property<EventConsumer<MouseEvent>> onMouseMovedProperty() {
		return onMouseMoved;
	}
	
	public EventConsumer<MouseEvent> getOnMouseEntered() {
		return onMouseEntered.get();
	}
	
	public void setOnMouseEntered(EventConsumer<MouseEvent> onMouseEntered) {
		this.onMouseEntered.set(onMouseEntered);
	}
	
	public Property<EventConsumer<MouseEvent>> onMouseEnteredProperty() {
		return onMouseEntered;
	}
	
	public EventConsumer<MouseEvent> getOnMouseExited() {
		return onMouseExited.get();
	}
	
	public void setOnMouseExited(EventConsumer<MouseEvent> onMouseExited) {
		this.onMouseExited.set(onMouseExited);
	}
	
	public Property<EventConsumer<MouseEvent>> onMouseExitedProperty() {
		return onMouseExited;
	}
	
	public EventConsumer<KeyEvent> getOnKeyPressed() {
		return onKeyPressed.get();
	}
	
	public void setOnKeyPressed(EventConsumer<KeyEvent> onKeyPressed) {
		this.onKeyPressed.set(onKeyPressed);
	}
	
	public Property<EventConsumer<KeyEvent>> onKeyPressedProperty() {
		return onKeyPressed;
	}
	
	public EventConsumer<KeyEvent> getOnKeyReleased() {
		return onKeyReleased.get();
	}
	
	public void setOnKeyReleased(EventConsumer<KeyEvent> onKeyReleased) {
		this.onKeyReleased.set(onKeyReleased);
	}
	
	public Property<EventConsumer<KeyEvent>> onKeyReleasedProperty() {
		return onKeyReleased;
	}
	
	public EventConsumer<KeyEvent> getOnKeyTyped() {
		return onKeyTyped.get();
	}
	
	public void setOnKeyTyped(EventConsumer<KeyEvent> onKeyTyped) {
		this.onKeyTyped.set(onKeyTyped);
	}
	
	public Property<EventConsumer<KeyEvent>> onKeyTypedProperty() {
		return onKeyTyped;
	}
	
	void internalUpdate0() {
		update0();
	}
	
	void internalUpdate1() {
		clearRegistration();
		update1();
		identifier.update();
		graphic.update();
		margin.update();
		layoutOffset.update();
		requestPosition.update();
		prefPosition.update();
		requestScale.update();
		prefScale.update();
		if(margin.get() != null)
			margin.get().update();
		if(graphic.get() != null)
			graphic.get().update();
		
		
	}
	
	public abstract void update0();
	
	public abstract void update1();
	
}
