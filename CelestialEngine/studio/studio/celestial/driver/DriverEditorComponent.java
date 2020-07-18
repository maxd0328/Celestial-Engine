package studio.celestial.driver;

import java.util.ArrayList;
import java.util.Collection;

import celestial.beans.property.Property;
import celestial.util.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import studio.celestial.binding.DirectBinding;
import studio.celestial.binding.PropertyBinding;
import studio.celestial.util.AdvancedIntegerSpinner;
import studio.celestial.util.AdvancedSpinner;

public abstract class DriverEditorComponent {
	
	private final String title;
	private final boolean grow;
	private final Node node;
	
	private final ArrayList<PropertyBinding<?>> bindings;
	
	protected DriverEditorComponent(String title, boolean grow, Node node) {
		this.title = title;
		this.grow = grow;
		this.node = node;
		this.bindings = new ArrayList<PropertyBinding<?>>();
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isGrow() {
		return grow;
	}
	
	public Node getNode() {
		return node;
	}
	
	public ArrayList<PropertyBinding<?>> getBindings() {
		return new ArrayList<PropertyBinding<?>>(bindings);
	}
	
	protected void addBinding(PropertyBinding<?> binding) {
		this.bindings.add(binding);
	}
	
	protected void removeBinding(PropertyBinding<?> binding) {
		this.bindings.remove(binding);
	}
	
	public void update() {
		for(PropertyBinding<?> binding : bindings)
			binding.update();
	}
	
	public static final class FloatEditorComponent extends DriverEditorComponent {
		
		@SuppressWarnings("unchecked")
		public FloatEditorComponent(String title, boolean grow, float min, float max, Property<Float> src) {
			super(title, grow ,new AdvancedSpinner(min, max, min, 0.1f, title == null ? "" : title).getFXSpinner());
			((Spinner<Double>) super.getNode()).getValueFactory().setValue((double) src.get());
			super.addBinding(new DirectBinding<Float>(() -> (float) (double) ((Spinner<Double>) super.getNode()).getValueFactory().getValue(),
					s -> ((Spinner<Double>) super.getNode()).getValueFactory().setValue((double) s), () -> src.get(), s -> src.set(s)));
		}
		
	}
	
	public static final class IntegerEditorComponent extends DriverEditorComponent {
		
		@SuppressWarnings("unchecked")
		public IntegerEditorComponent(String title, boolean grow, int min, int max, Property<Integer> src) {
			super(title, grow, new AdvancedIntegerSpinner(min, max, min, 1, title == null ? "" : title).getFXSpinner());
			((Spinner<Integer>) super.getNode()).getValueFactory().setValue(src.get());
			super.addBinding(new DirectBinding<Integer>(() -> ((Spinner<Integer>) super.getNode()).getValueFactory().getValue(),
					s -> ((Spinner<Integer>) super.getNode()).getValueFactory().setValue(s), () -> src.get(), s -> src.set(s)));
		}
		
	}
	
	public static final class StringEditorComponent extends DriverEditorComponent {
		
		private String stringValue;
		
		public StringEditorComponent(String title, boolean grow, Property<String> src, int width, IStringConditional textValidator) {
			super(title, grow, new TextField());
			final TextField field = (TextField) super.getNode();
			field.setText(src.get());
			field.setOnAction((event) -> { if(textValidator.condition(field.getText())) stringValue = field.getText(); else field.setText(stringValue); });
			field.focusedProperty().addListener((obs, oldV, newV) ->
					{ if(!newV && textValidator.condition(field.getText())) stringValue = field.getText(); else field.setText(stringValue); });
			if(width > 0) {
				field.setMinWidth(width);
				field.setMaxWidth(width);
			}
			stringValue = src.get();
			super.addBinding(new DirectBinding<String>(() -> stringValue, s -> { field.setText(s); stringValue = s; }, () -> src.get(), s -> src.set(s)));
		}
		
	}
	
	public static final class HorizRegionComponent extends DriverEditorComponent {
		
		public HorizRegionComponent(int width) {
			super(null, false, new Region());
			final Region region = (Region) super.getNode();
			region.setMinWidth(width);
			region.setMaxWidth(width);
		}
		
	}
	
	public static final class EmptyRegionComponent extends DriverEditorComponent {
		
		public EmptyRegionComponent(String title) {
			super(title, false, null);
		}
		
	}
	
	public static final class ListSelectorComponent<T> extends DriverEditorComponent {
		
		private final ComboBox<T> selector;
		
		@SuppressWarnings("unchecked")
		public ListSelectorComponent(String title, boolean grow, Property<Collection<T>> property) {
			super(title, grow, new ComboBox<T>());
			this.selector = (ComboBox<T>) super.getNode();
			this.selector.setTooltip(new Tooltip(title));
			this.selector.setMinWidth(150);
			
			super.addBinding(new DirectBinding<Collection<T>>(() -> new ArrayList<>(selector.getItems()), s -> { selector.getItems().clear();
					selector.getItems().addAll(s); selector.getSelectionModel().select(selector.getItems().size() - 1); }, () -> property.get(), s -> property.set(s)));
		}
		
		public ComboBox<T> getSelector() {
			return selector;
		}
		
	}
	
	public static final class ButtonComponent extends DriverEditorComponent {
		
		public ButtonComponent(String title, boolean grow, String text, Event event) {
			super(title, grow, new Button(text));
			Button btn = (Button) super.getNode();
			btn.setOnAction(e -> event.perform(btn));
		}
		
	}
	
	public static interface IStringConditional {
		
		public boolean condition(String s);
		
	}
	
}
