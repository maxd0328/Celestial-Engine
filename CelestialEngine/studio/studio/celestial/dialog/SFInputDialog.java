package studio.celestial.dialog;

import celestial.util.KVEntry;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.util.AdvancedIntegerSpinner;

public final class SFInputDialog extends StudioComponentPanel implements ComplexDialog<KVEntry<String, Integer[]>> {
	
	private final String prompt0;
	private final int limLo, limHi;
	private final String[] prompts;
	
	public SFInputDialog(StudioInterface studio, String title, String prompt0, int limLo, int limHi, String... prompts) {
		super(studio, title, 10, 7, 12, 12);
		this.prompt0 = prompt0;
		this.limLo = limLo;
		this.limHi = limHi;
		this.prompts = prompts;
	}
	
	private TextField field0;
	private AdvancedIntegerSpinner[] spinners;
	
	@Override
	protected void initialize() {
		field0 = new TextField();
		field0.setPromptText(prompt0);
		super.getPane().add(field0, 0, 1, 12, 1);
		
		this.spinners = new AdvancedIntegerSpinner[prompts.length];
		int i;
		for(i = 0 ; i < spinners.length ; ++i) {
			spinners[i] = new AdvancedIntegerSpinner(limLo, limHi, limLo, 1, prompts[i]);
			super.getPane().add(spinners[i].getFXSpinner(), 7, 3 + 2 * i, 5, 1);
			super.getPane().add(new Label(" " + prompts[i]), 0, 3 + 2 * i, 7, 1);
		}
		
		super.getPane().add(new Region(), 0, 3 + 2 * i, 12, 1);
		
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
		return 150 + 30 * prompts.length;
	}
	
	@Override
	public KVEntry<String, Integer[]> getResult() {
		Integer[] arr = new Integer[spinners.length];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = spinners[i].get();
		return new KVEntry<>(field0.getText(), arr);
	}
	
}
