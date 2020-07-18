package studio.celestial.dialog;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.util.AdvancedIntegerSpinner;

public final class IntegerInputDialog extends StudioComponentPanel implements ComplexDialog<Integer[]> {
	
	private final int limLo, limHi;
	private final String[] prompts;
	
	public IntegerInputDialog(StudioInterface studio, String title, int limLo, int limHi, String... prompts) {
		super(studio, title, 10, 7, 12, 12);
		this.limLo = limLo;
		this.limHi = limHi;
		this.prompts = prompts;
	}
	
	private AdvancedIntegerSpinner[] spinners;
	
	@Override
	protected void initialize() {
		this.spinners = new AdvancedIntegerSpinner[prompts.length];
		for(int i = 0 ; i < spinners.length ; ++i) {
			spinners[i] = new AdvancedIntegerSpinner(limLo, limHi, limLo, 1, prompts[i]);
			super.getPane().add(spinners[i].getFXSpinner(), 7, 2 * i, 5, 1);
			super.getPane().add(new Label(" " + prompts[i]), 0, 2 * i, 7, 1);
		}
		
		super.calculateConstraints(StudioInterface.getInstantiation(), 12, 12);
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
		return 100 + 30 * prompts.length;
	}
	
	@Override
	public Integer[] getResult() {
		Integer[] arr = new Integer[spinners.length];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = spinners[i].get();
		return arr;
	}
	
}
