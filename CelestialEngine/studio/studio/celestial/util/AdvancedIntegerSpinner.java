package studio.celestial.util;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.robot.Robot;
import javafx.util.StringConverter;

public final class AdvancedIntegerSpinner {
	
	private final int minValue;
	private final int maxValue;
	private final int initialValue;
	private final int step;
	private final String tooltip;
	
	private Spinner<Integer> spinner;
	private int prev;
	
	public AdvancedIntegerSpinner(int minValue, int maxValue, int initialValue, int step, String tooltip) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initialValue = initialValue;
		this.step = step;
		this.tooltip = tooltip;
		
		this.lastValue = initialValue;
		this.prev = initialValue;
		createFXSpinner();
	}
	
	public int getMinValue() {
		return minValue;
	}
	
	public int getMaxValue() {
		return maxValue;
	}
	
	public int getInitialValue() {
		return initialValue;
	}
	
	public int getStep() {
		return step;
	}
	
	public String getTooltip() {
		return tooltip;
	}
	
	public Spinner<Integer> getFXSpinner() {
		return spinner;
	}
	
	public int get() {
		return spinner.getValue();
	}
	
	public void set(int value) {
		this.spinner.getValueFactory().setValue(value);
	}
	
	public int getDelta() {
		int value = get() - prev;
		prev = get();
		return value;
	}
	
	private int lastValue;
	private double mouseX, mouseY;
	private boolean dragged = false, spin = true;
	private double value;
	private void createFXSpinner() {
		spinner = new Spinner<Integer>();
		spinner.setEditable(true);
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner.setTooltip(new Tooltip(tooltip));
		spinner.setMinWidth(Double.MIN_VALUE);
		spinner.setMaxWidth(Double.MAX_VALUE);
		
		SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initialValue, step);
		valueFactory.setConverter(new StringConverter<Integer>() {
			@Override
			public Integer fromString(String s) {
				int value;
				try {
					value = Integer.parseInt(s);
					lastValue = value;
				}
				catch(NumberFormatException ex) {
					value = lastValue;
				}
				return value;
			}
			
			@Override
			public String toString(Integer d) {
				return String.valueOf(d);
			}
		});
		spinner.setValueFactory(valueFactory);
		
		spinner.getEditor().setOnMousePressed((event) -> {
			value = spinner.getValue();
			mouseX = event.getScreenX();
			mouseY = event.getScreenY();
			dragged = false;
		});
		spinner.getEditor().setOnMouseReleased((event) -> {
			if(!dragged && spin) {
				spinner.getEditor().setCursor(Cursor.TEXT);
				spinner.getEditor().selectAll();
				spin = false;
			}
		});
		spinner.getEditor().setOnMouseDragged((event) -> {
			if(!spin) return;
			double newMouseX = event.getScreenX();
			if(newMouseX < 0) newMouseX--;
			value += (newMouseX - mouseX) * step / 10;
			value = Math.min(Math.max(value, minValue), maxValue);
			spinner.getValueFactory().setValue((int) value);
			spinner.getEditor().deselect();
			new Robot().mouseMove(new Point2D(mouseX, mouseY));
			dragged = true;
		});
		spinner.getEditor().focusedProperty().addListener((obs, oldV, newV) -> {
			if(!newV) {
				spin = true;
				spinner.getEditor().setCursor(Cursor.H_RESIZE);
			}
		});
		spinner.getEditor().setCursor(Cursor.H_RESIZE);
	}
	
}
