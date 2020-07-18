package studio.celestial.core;

import java.util.ArrayList;

public final class StudioView {
	
	private final String identifier;
	private final int viewX;
	private final int viewY;
	private final int viewWidth;
	private final int viewHeight;
	private final int scaleFactor;
	private final ArrayList<ScaledPanel> panels;
	
	public StudioView(String identifier, int viewX, int viewY, int viewWidth, int viewHeight, int scaleFactor, ScaledPanel... panels) {
		this.identifier = identifier;
		this.viewX = viewX;
		this.viewY = viewY;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.scaleFactor = scaleFactor;
		this.panels = new ArrayList<ScaledPanel>();
		for(ScaledPanel p : panels) this.panels.add(p);
		validateBounds();
	}
	
	public void initialize() {
		for(ScaledPanel panel : panels) {
			panel.getPanel().componentInitialize();
			panel.getPanel().calculateConstraints(StudioInterface.getInstantiation(), panel.getWidth(), panel.getHeight());
		}
	}
	
	public void update() {
		for(ScaledPanel panel : panels) panel.getPanel().componentUpdate();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public int getViewX() {
		return viewX;
	}
	
	public int getViewY() {
		return viewY;
	}
	
	public int getViewWidth() {
		return viewWidth;
	}
	
	public int getViewHeight() {
		return viewHeight;
	}
	
	public int getScaleFactor() {
		return scaleFactor;
	}
	
	public ArrayList<ScaledPanel> getPanels() {
		return new ArrayList<ScaledPanel>(panels);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends StudioComponentPanel> T getPanel(Class<T> type) {
		for(ScaledPanel panel : panels) if(type.isInstance(panel.getPanel())) return (T) panel.getPanel();
		return null;
	}
	
	public void addPanels(ScaledPanel... panels) {
		for(ScaledPanel panel : panels) {
			this.panels.add(panel);
			validateBounds();
		}
	}
	
	public void addPanel(ScaledPanel panel) {
		this.panels.add(panel);
		validateBounds();
	}
	
	public void removePanel(ScaledPanel panel) {
		this.panels.remove(panel);
	}
	
	public ScaledPanel getOccupyingPanel(int gridX, int gridY) {
		for(ScaledPanel panel : panels) {
			if(gridX >= panel.getGridX() && gridX <= panel.getGridX() + panel.getWidth() - 1
					&& gridY >= panel.getGridY() && gridY <= panel.getGridY() + panel.getHeight() - 1)
				return panel;
		}
		return null;
	}
	
	private void validateBounds() {
		for(ScaledPanel panel : new ArrayList<ScaledPanel>(panels)) {
			OuterLoop:
			for(int x = panel.getGridX() ; x <= panel.getGridX() + panel.getWidth() - 1 ; ++x) {
				for(int y = panel.getGridY() ; y <= panel.getGridY() + panel.getHeight() - 1 ; ++y) {
					ScaledPanel occupying = getOccupyingPanel(x, y);
					if(occupying != null && occupying != panel) {
						panels.remove(panel);
						break OuterLoop;
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return identifier;
	}
	
	public static final class ScaledPanel {
		
		private final StudioComponentPanel panel;
		private final int gridX, gridY;
		private final int width, height;
		
		public ScaledPanel(StudioComponentPanel panel, int gridX, int gridY, int width, int height) {
			this.panel = panel;
			this.gridX = gridX;
			this.gridY = gridY;
			this.width = width;
			this.height = height;
		}
		
		public StudioComponentPanel getPanel() {
			return panel;
		}
		
		public int getGridX() {
			return gridX;
		}
		
		public int getGridY() {
			return gridY;
		}
		
		public int getWidth() {
			return width;
		}
		
		public int getHeight() {
			return height;
		}
		
	}
	
}
