package studio.celestial.driver;

import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public final class DriverEditor {
	
	private final ArrayList<DriverEditorRow> rows;
	
	public DriverEditor(DriverEditorRow... rows) {
		this.rows = new ArrayList<DriverEditorRow>();
		for(DriverEditorRow row : rows) this.rows.add(row);
	}
	
	public ArrayList<DriverEditorRow> getRows() {
		return rows;
	}
	
	public void update() {
		for(DriverEditorRow row : rows)
			row.update();
	}
	
	public VBox toFXComponent() {
		VBox box = new VBox();
		
		box.setSpacing(5);
		box.setAlignment(Pos.TOP_LEFT);
		box.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(1))));
		box.setPadding(new Insets(5));
		
		for(DriverEditorRow row : rows)
			box.getChildren().add(row.toFXComponent());
		
		return box;
	}
	
}
