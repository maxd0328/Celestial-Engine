package studio.celestial.dialog;

import celestial.util.KVEntry;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.util.AdvancedIntegerSpinner;

public final class SSFInputDialog extends StudioComponentPanel implements ComplexDialog<KVEntry<String, KVEntry<String, Integer>>> {
	
	private final String prompt0, prompt1, prompt2;
	private final int limLo, limHi;
	
	public SSFInputDialog(StudioInterface studio, String title, String prompt0, String prompt1, String prompt2, int limLo, int limHi) {
		super(studio, title, 10, 10, 12, 12);
		this.prompt0 = prompt0;
		this.prompt1 = prompt1;
		this.prompt2 = prompt2;
		this.limLo = limLo;
		this.limHi = limHi;
	}
	
	private TextField field0, field1;
	private AdvancedIntegerSpinner spinner;
	
	@Override
	protected void initialize() {
		field0 = new TextField();
		field0.setPromptText(prompt0);
		super.getPane().add(field0, 0, 1, 12, 1);
		
		field1 = new TextField();
		field1.setPromptText(prompt1);
		super.getPane().add(field1, 0, 3, 12, 1);
		
		spinner = new AdvancedIntegerSpinner(limLo, limHi, limLo, 1, prompt2);
		super.getPane().add(spinner.getFXSpinner(), 7, 5, 5, 1);
		super.getPane().add(new Label(" " + prompt2), 0, 5, 7, 1);
		
		super.getPane().add(new Region(), 0, 6, 12, 1);
		
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
	public KVEntry<String, KVEntry<String, Integer>> getResult() {
		return new KVEntry<>(field0.getText(), new KVEntry<>(field1.getText(), spinner.get()));
	}
	
}
