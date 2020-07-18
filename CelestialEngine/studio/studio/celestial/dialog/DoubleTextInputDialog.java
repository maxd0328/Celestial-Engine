package studio.celestial.dialog;

import celestial.util.KVEntry;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;

public final class DoubleTextInputDialog extends StudioComponentPanel implements ComplexDialog<KVEntry<String, String>> {
	
	private final String prompt0, prompt1;
	
	public DoubleTextInputDialog(StudioInterface studio, String title, String prompt0, String prompt1) {
		super(studio, title, 10, 10, 12, 12);
		this.prompt0 = prompt0;
		this.prompt1 = prompt1;
	}
	
	private TextField field0, field1;
	
	@Override
	protected void initialize() {
		field0 = new TextField();
		field0.setPromptText(prompt0);
		super.getPane().add(field0, 0, 1, 12, 1);
		
		field1 = new TextField();
		field1.setPromptText(prompt1);
		super.getPane().add(field1, 0, 3, 12, 1);
		
		super.getPane().add(new Region(), 0, 4, 12, 2);
		
		super.calculateConstraints(StudioInterface.getInstantiation(), 12, 7);
	}
	
	@Override
	protected void update() {
	}
	
	@Override
	public String getTitle() {
		return super.getName();
	}
	
	@Override
	public GridPane getPane() {
		return super.getPane();
	}
	
	@Override
	public int getDialogWidth() {
		return 350;
	}
	
	@Override
	public int getDialogHeight() {
		return 180;
	}
	
	@Override
	public KVEntry<String, String> getResult() {
		return new KVEntry<>(field0.getText(), field1.getText());
	}
	
}
