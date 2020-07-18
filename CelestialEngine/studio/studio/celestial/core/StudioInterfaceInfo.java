package studio.celestial.core;

import studio.celestial.util.FXMenuBar;

public final class StudioInterfaceInfo {
	
	private final String title;
	private final String icon;
	private final String stylesheet;
	private final int width;
	private final int height;
	private final int hgap;
	private final int vgap;
	private final int inset;
	private final int updateInterval;
	private final FXMenuBar menuBar;
	
	public StudioInterfaceInfo(String title, String icon, String stylesheet, int width, int height,
			int hgap, int vgap, int inset, int updateInterval, FXMenuBar menuBar) {
		this.title = title;
		this.icon = icon;
		this.stylesheet = stylesheet;
		this.width = width;
		this.height = height;
		this.hgap = hgap;
		this.vgap = vgap;
		this.inset = inset;
		this.updateInterval = updateInterval;
		this.menuBar = menuBar;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public String getStylesheet() {
		return stylesheet;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getHgap() {
		return hgap;
	}
	
	public int getVgap() {
		return vgap;
	}
	
	public int getInset() {
		return inset;
	}
	
	public int getUpdateInterval() {
		return updateInterval;
	}
	
	public FXMenuBar getMenuBar() {
		return menuBar;
	}
	
}
