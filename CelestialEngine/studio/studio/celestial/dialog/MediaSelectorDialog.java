package studio.celestial.dialog;

import celestial.error.CelestialGenericException;
import celestial.util.KVEntry;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.media.Media.MediaType;

public final class MediaSelectorDialog extends StudioComponentPanel implements ComplexDialog<MediaType> {
	
	private final KVEntry<String, MediaType>[] types;
	
	@SafeVarargs
	public MediaSelectorDialog(StudioInterface studio, KVEntry<String, MediaType>... types) {
		super(studio, "Select Media Type", 10, (int) (10 * (1f / (types.length / 6f))), 12, 12);
		this.types = types;
		if(types.length == 0) throw new CelestialGenericException("Must have at least one media type");
	}
	
	private RadioButton[] buttons;
	
	@Override
	protected void initialize() {
		this.buttons = new RadioButton[types.length];
		ToggleGroup grp = new ToggleGroup();
		
		int i = 0;
		for(KVEntry<String, MediaType> type : types) {
			RadioButton btn = new RadioButton(type.getKey());
			btn.setToggleGroup(grp);
			btn.setUserData(type.getValue());
			super.getPane().add(btn, 0, i, 12, 1);
			buttons[i++] = btn;
		}
		
		super.getPane().add(new Region(), 0, i, 12, 1);
		buttons[0].setSelected(true);
		
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
		return 200;
	}
	
	@Override
	public int getDialogHeight() {
		return 15 + 50 * types.length;
	}
	
	@Override
	public MediaType getResult() {
		for(RadioButton btn : buttons)
			if(btn.isSelected()) return (MediaType) btn.getUserData();
		return null;
	}
	
}
