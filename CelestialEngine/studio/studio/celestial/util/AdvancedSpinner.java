package studio.celestial.util;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.robot.Robot;
import javafx.util.StringConverter;

public final class AdvancedSpinner {
	
	private final float minValue;
	private final float maxValue;
	private final float initialValue;
	private final float step;
	private final String tooltip;
	
	private Spinner<Double> spinner;
	
	public AdvancedSpinner(float minValue, float maxValue, float initialValue, float step, String tooltip) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initialValue = initialValue;
		this.step = step;
		this.tooltip = tooltip;
		
		this.lastValue = initialValue;
		createFXSpinner();
	}
	
	public float getMinValue() {
		return minValue;
	}
	
	public float getMaxValue() {
		return maxValue;
	}
	
	public float getInitialValue() {
		return initialValue;
	}
	
	public float getStep() {
		return step;
	}
	
	public String getTooltip() {
		return tooltip;
	}
	
	public Spinner<Double> getFXSpinner() {
		return spinner;
	}
	
	public float get() {
		return (float) (double) spinner.getValue();
	}
	
	public void set(float value) {
		this.spinner.getValueFactory().setValue((double) value);
	}
	
	private double lastValue;
	private double mouseX, mouseY;
	private boolean dragged = false, spin = true;
	private void createFXSpinner() {
		spinner = new Spinner<Double>();
		spinner.setEditable(true);
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner.setTooltip(new Tooltip(tooltip));
		spinner.setMinWidth(Double.MIN_VALUE);
		spinner.setMaxWidth(Double.MAX_VALUE);
		
		SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, initialValue, step);
		valueFactory.setConverter(new StringConverter<Double>() {
			@Override
			public Double fromString(String s) {
				double value;
				try {
					value = Double.parseDouble(s);
					lastValue = value;
				}
				catch(NumberFormatException ex) {
					value = lastValue;
				}
				return value;
			}
			
			@Override
			public String toString(Double d) {
				return String.valueOf(d);
			}
		});
		spinner.setValueFactory(valueFactory);
		
		spinner.valueProperty().addListener((obs, oldV, newV) -> spinner.getValueFactory().setValue(round(newV, 3)));
		spinner.getEditor().setOnMousePressed((event) -> {
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
			spinner.getValueFactory().setValue(spinner.getValueFactory().getValue() + (newMouseX - mouseX) * step / 10);
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
		spinner.getValueFactory().setValue(round(spinner.getValue(), 3));
	}
	
	private static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	
}
