package studio.celestial.dialog;

import java.util.ArrayList;
import javafx.scene.control.ButtonBar.ButtonData;

public class DialogOptionBuilder {
	
	private final ArrayList<ButtonType> buttonTypes;
	
	public DialogOptionBuilder(ButtonType... buttonTypes) {
		this.buttonTypes = new ArrayList<ButtonType>();
		for(ButtonType buttonType : buttonTypes)
			this.buttonTypes.add(buttonType);
	}
	
	public DialogOptionBuilder withButtonType(ButtonType buttonType) {
		this.buttonTypes.add(buttonType);
		return this;
	}
	
	public DialogOptionBuilder withButtonTypes(ButtonType... buttonTypes) {
		for(ButtonType buttonType : buttonTypes)
			this.buttonTypes.add(buttonType);
		return this;
	}
	
	public javafx.scene.control.ButtonType[] toButtonTypes() {
		javafx.scene.control.ButtonType[] arr = new javafx.scene.control.ButtonType[buttonTypes.size()];
		for(int i = 0 ; i < arr.length ; ++i)
			arr[i] = buttonTypes.get(i).getFXButtonType();
		return arr;
	}
	
	public static enum ButtonType {
		
		OK(new javafx.scene.control.ButtonType("OK", ButtonData.OK_DONE)),
		
		CANCEL(javafx.scene.control.ButtonType.CANCEL),
		
		EXIT(new javafx.scene.control.ButtonType("Exit"));
		
		private final javafx.scene.control.ButtonType fxButtonType;
		
		private ButtonType(javafx.scene.control.ButtonType fxButtonType) {
			this.fxButtonType = fxButtonType;
		}
		
		public javafx.scene.control.ButtonType getFXButtonType() {
			return fxButtonType;
		}
		
	}
	
}
