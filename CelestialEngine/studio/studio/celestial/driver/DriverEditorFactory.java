package studio.celestial.driver;

import java.util.ArrayList;
import java.util.Collection;

import celestial.beans.driver.Driver;
import celestial.beans.driver.ExpressionDriver;
import celestial.beans.driver.KeyframeDriver;
import celestial.beans.driver.KeyframeDriver.KeyFrame;
import celestial.beans.driver.LinearDriver;
import celestial.beans.driver.SineWaveDriver;
import celestial.beans.driver.SmoothDriver;
import celestial.beans.property.Properties;
import celestial.util.Factory;
import javafx.geometry.Pos;
import javafx.scene.control.SingleSelectionModel;
import studio.celestial.driver.DriverEditorComponent.*;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.dialog.MessageDialog;
import studio.celestial.dialog.MessageDialog.MessageType;
import studio.celestial.dialog.MessageHandler;

public interface DriverEditorFactory {
	
	public Class<? extends Driver> getType();
	
	public Factory<? extends Driver> getDriverFactory();
	
	public String getName();
	
	public DriverEditor create(Driver... drivers);
	
	public static final DriverEditorFactory LINEAR_DRIVER_FACTORY = new DriverEditorFactory() {
		
		@Override
		public Class<? extends Driver> getType() {
			return LinearDriver.class;
		}
		
		@Override
		public Factory<? extends Driver> getDriverFactory() {
			return LinearDriver.FACTORY;
		}
		
		@Override
		public String getName() {
			return "Linear Driver";
		}
		
		@Override
		public DriverEditor create(Driver... drivers) {
			DriverEditorComponent valueDelta = new FloatEditorComponent("Value Delta", true, -Float.MAX_VALUE, Float.MAX_VALUE,
					Properties.createFloatProperty(() -> ((LinearDriver) drivers[0]).getSlope(), s -> { for(Driver d : drivers) ((LinearDriver) d).setSlope(s); }));
			DriverEditorRow row = new DriverEditorRow(valueDelta);
			DriverEditor editor = new DriverEditor(row);
			return editor;
		}
		
	};
	
	public static final DriverEditorFactory SINE_WAVE_DRIVER_FACTORY = new DriverEditorFactory() {
		
		@Override
		public Class<? extends Driver> getType() {
			return SineWaveDriver.class;
		}
		
		@Override
		public Factory<? extends Driver> getDriverFactory() {
			return SineWaveDriver.FACTORY;
		}
		
		@Override
		public String getName() {
			return "Sine Wave Driver";
		}
		
		@Override
		public DriverEditor create(Driver... drivers) {
			DriverEditorComponent min = new FloatEditorComponent("Minimum", true, -Float.MAX_VALUE, Float.MAX_VALUE, Properties.createFloatProperty
					(() -> ((SineWaveDriver) drivers[0]).getMin(), s -> { for(Driver d : drivers) ((SineWaveDriver) d).setMin(s); }));
			DriverEditorRow row0 = new DriverEditorRow(min);
			DriverEditorComponent max = new FloatEditorComponent("Maximum", true, -Float.MAX_VALUE, Float.MAX_VALUE, Properties.createFloatProperty
					(() -> ((SineWaveDriver) drivers[0]).getMax(), s -> { for(Driver d : drivers) ((SineWaveDriver) d).setMax(s); }));
			DriverEditorRow row1 = new DriverEditorRow(max);
			DriverEditorComponent time = new IntegerEditorComponent("Oscillation Time", true, 1, Integer.MAX_VALUE, Properties.createIntegerProperty
					(() -> ((SineWaveDriver) drivers[0]).getTime(), s -> { for(Driver d : drivers) ((SineWaveDriver) d).setTime(s); }));
			DriverEditorRow row2 = new DriverEditorRow(time);
			DriverEditor editor = new DriverEditor(row0, row1, row2);
			return editor;
		}
		
	};
	
	public static final DriverEditorFactory KEYFRAME_DRIVER_FACTORY = new DriverEditorFactory() {
		
		@Override
		public Class<? extends Driver> getType() {
			return KeyframeDriver.class;
		}
		
		@Override
		public Factory<? extends Driver> getDriverFactory() {
			return KeyframeDriver.FACTORY;
		}
		
		@Override
		public String getName() {
			return "Keyframe Driver";
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public DriverEditor create(Driver... drivers) {
			DriverEditorComponent animLength = new IntegerEditorComponent("Animation Length", true, 1, Integer.MAX_VALUE, Properties.createIntegerProperty(
					() -> ((KeyframeDriver) drivers[0]).getAnimLength(), s -> { for(Driver d : drivers) ((KeyframeDriver) d).setAnimLength(s); }));
			
			HorizRegionComponent region = new HorizRegionComponent(6);
			ListSelectorComponent<KeyFrame> selector = new ListSelectorComponent<KeyFrame>(null, false, Properties.<Collection<KeyFrame>>createProperty(
					(Class<Collection<KeyFrame>>) new ArrayList<KeyFrame>().getClass(), () -> new ArrayList<KeyFrame>(((KeyframeDriver) drivers[0]).getKeyFrames()),
					s -> { KeyframeDriver d = (KeyframeDriver) drivers[0]; d.getKeyFrames().clear(); d.getKeyFrames().addAll(s); }));
			ButtonComponent addKeyframe = new ButtonComponent("Keyframe Selector", true, "+", o -> GLRequestSystem.request(() -> {
				for(Driver d : drivers) ((KeyframeDriver) d).getKeyFrames().add(new KeyFrame(0, 0));
			}));
			ButtonComponent remKeyframe = new ButtonComponent(null, false, "-", o -> GLRequestSystem.request(() -> {
				if(selector.getSelector().getItems().size() > 0)
					for(Driver d : drivers) ((KeyframeDriver) d).getKeyFrames().remove(selector.getSelector().getSelectionModel().getSelectedIndex());
			}));
			
			final SingleSelectionModel<KeyFrame> sel = selector.getSelector().getSelectionModel();
			DriverEditorComponent time = new IntegerEditorComponent("Keyframe Time", true, 0, Integer.MAX_VALUE,
					Properties.createIntegerProperty(() -> ((KeyframeDriver) drivers[0]).getKeyFrames().size() == 0 || sel.getSelectedIndex() < 0 ?
					0 : ((KeyframeDriver) drivers[0]).getKeyFrames().get(sel.getSelectedIndex()).getTime(),
					s -> { for(Driver d : drivers) if(((KeyframeDriver) d).getKeyFrames().size() > 0 && sel.getSelectedIndex() >= 0) ((KeyframeDriver) d).getKeyFrames()
					.get(sel.getSelectedIndex()).setTime(s); }));
			DriverEditorComponent value = new FloatEditorComponent("Keyframe Value", true, -Float.MAX_VALUE, Float.MAX_VALUE,
					Properties.createFloatProperty(() -> ((KeyframeDriver) drivers[0]).getKeyFrames().size() == 0 || sel.getSelectedIndex() < 0 ?
					0f : ((KeyframeDriver) drivers[0]).getKeyFrames().get(selector.getSelector().getSelectionModel().getSelectedIndex()).getValue(),
					s -> { for(Driver d : drivers) if(((KeyframeDriver) d).getKeyFrames().size() > 0 && sel.getSelectedIndex() >= 0) ((KeyframeDriver) d).getKeyFrames()
					.get(sel.getSelectedIndex()).setValue(s); }));
			
			DriverEditorRow row0 = new DriverEditorRow(animLength);
			DriverEditorRow row1 = new DriverEditorRow(addKeyframe, remKeyframe, region, selector);
			DriverEditorRow row2 = new DriverEditorRow(time);
			DriverEditorRow row3 = new DriverEditorRow(value);
			DriverEditor editor = new DriverEditor(row0, row1, row2, row3);
			return editor;
		}
		
	};
	
	public static final DriverEditorFactory EXPRESSION_DRIVER_FACTORY = new DriverEditorFactory() {
		
		@Override
		public Class<? extends Driver> getType() {
			return ExpressionDriver.class;
		}
		
		@Override
		public Factory<? extends Driver> getDriverFactory() {
			return ExpressionDriver.FACTORY;
		}
		
		@Override
		public String getName() {
			return "Expression Driver";
		}
		
		@Override
		public DriverEditor create(Driver... drivers) {
			DriverEditorComponent expression = new StringEditorComponent("Expression", true, Properties.createStringProperty(
					() -> ((ExpressionDriver) drivers[0]).getExpression(), s -> { for(Driver d : drivers) ((ExpressionDriver) d).setExpression(s); }), 300,
					s -> { boolean valid = ((ExpressionDriver) drivers[0]).isValidExpression(s); if(!valid) MessageHandler.show(new MessageDialog
							(MessageType.MESSAGE_TYPE_ERROR, "Invalid expression")); return valid; });
			
			DriverEditorComponent note = new EmptyRegionComponent("NOTE: All expression drivers have full access to each variable in the existing variable table."
					+ " These variables can be acssed simply by using their assigned names.");
			
			DriverEditorRow row0 = new DriverEditorRow(expression);
			DriverEditorRow row1 = new DriverEditorRow(note);
			row1.setHeight(100);
			row1.setAlignment(Pos.TOP_LEFT);
			DriverEditor editor = new DriverEditor(row0, new DriverEditorRow(), row1);
			return editor;
		}
		
	};
	
	public static final DriverEditorFactory SMOOTH_DRIVER_FACTORY = new DriverEditorFactory() {
		
		@Override
		public Class<? extends Driver> getType() {
			return SmoothDriver.class;
		}
		
		@Override
		public Factory<? extends Driver> getDriverFactory() {
			return SmoothDriver.FACTORY;
		}
		
		@Override
		public String getName() {
			return "Smooth Driver";
		}
		
		@Override
		public DriverEditor create(Driver... drivers) {
			DriverEditorComponent agility = new FloatEditorComponent("Agility", true, -Float.MAX_VALUE, Float.MAX_VALUE, Properties.createFloatProperty(
					() -> ((SmoothDriver) drivers[0]).getAgility(), s -> { for(Driver d : drivers) ((SmoothDriver) d).setAgility(s); }));
			DriverEditorRow row = new DriverEditorRow(agility);
			DriverEditor editor = new DriverEditor(row);
			return editor;
		}
		
	};
	
}
