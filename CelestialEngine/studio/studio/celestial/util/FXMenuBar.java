package studio.celestial.util;

import java.util.ArrayList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public final class FXMenuBar {
	
	private final ArrayList<Menu> menus;
	
	public FXMenuBar(Menu... menus) {
		this.menus = new ArrayList<Menu>();
		for(Menu menu : menus) this.menus.add(menu);
	}
	
	public void addMenu(Menu menu) {
		this.menus.add(menu);
	}
	
	public void removeMenu(Menu menu) {
		this.menus.remove(menu);
	}
	
	public ArrayList<Menu> getMenus() {
		return new ArrayList<Menu>(menus);
	}
	
	public MenuBar toMenuBar() {
		MenuBar menubar = new MenuBar();
		for(Menu menu : menus) {
			menubar.getMenus().add(menu);
		}
		return menubar;
	}
	
	public Menu getMenu(String name) {
		for(Menu menu : menus) if(menu.getText().equals(name)) return menu;
		return null;
	}
	
}
