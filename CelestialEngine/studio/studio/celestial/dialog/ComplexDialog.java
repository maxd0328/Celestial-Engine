package studio.celestial.dialog;

import javafx.scene.layout.GridPane;

public interface ComplexDialog<E> {
	
	public String getTitle();
	
	public GridPane getPane();
	
	public int getDialogWidth();
	
	public int getDialogHeight();
	
	public void componentInitialize();
	
	public void componentUpdate();
	
	public E getResult();
	
}
