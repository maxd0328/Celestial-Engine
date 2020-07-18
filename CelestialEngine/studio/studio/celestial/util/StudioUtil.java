package studio.celestial.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import studio.celestial.core.GLRequestSystem;

public final class StudioUtil {
	
	public static <S, T> TableColumn<S, T> generateTableColumn(String binding, String header, int width, int minWidth, int maxWidth) {
		TableColumn<S, T> column = new TableColumn<S, T>(header);
		column.setPrefWidth(width);
		column.setMinWidth(minWidth);
		column.setMaxWidth(maxWidth);
		column.setCellValueFactory(new PropertyValueFactory<S, T>(binding));
		return column;
	}
	
	public static <S, T> TableColumn<S, T> generateTableColumn(String binding, String header, int width) {
		return generateTableColumn(binding, header, width, width, width);
	}
	
	public static boolean isValidObjectName(String name) {
		return name.length() > 0 && !name.endsWith(".studio") && GLRequestSystem.getSceneManager().getObject(name) == null;
	}
	
	public static String subFile(String path, String file) {
		if(path.trim().endsWith("\\") || path.trim().endsWith("/"))
			return path + file;
		else if(path.contains("\\"))
			return path + "\\" + file;
		else
			return path + "/" + file;
	}
	
}
