package studio.celestial.core;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public final class TabPaneComponentPanel extends StudioComponentPanel {
	
	private final StudioComponentPanel[] tabs;
	
	public TabPaneComponentPanel(StudioInterface studio, String name, StudioComponentPanel... tabs) {
		super(studio, name, 10, 10, 0, 0);
		this.tabs = tabs;
	}
	
	private TabPane tabPane;
	private Tab[] fxTabs;
	
	@Override
	protected void initialize() {
		this.tabPane = new TabPane();
		this.fxTabs = new Tab[tabs.length];
		
		for(int i = 0 ; i < fxTabs.length ; ++i) {
			fxTabs[i] = new Tab(tabs[i].getName(), tabs[i].getPane());
			fxTabs[i].setClosable(false);
			tabPane.getTabs().add(fxTabs[i]);
			
			tabs[i].getPane().setBorder(null);
			tabs[i].componentInitialize();
		}
		
		super.getPane().add(tabPane, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	@Override
	protected void update() {
		for(int i = 0 ; i < tabs.length ; ++i) {
			if(tabPane.getSelectionModel().isSelected(i)) tabs[i].componentUpdate();
		}
	}
	
	@Override
	public void calculateConstraints(StudioInterface studio, int width, int height) {
		super.calculateConstraints(studio, width, height);
		for(StudioComponentPanel tab : tabs)
			tab.calculateConstraints(studio, width, height);
		
		for(int i = 0 ; i < tabs.length ; ++i)
			if(fxTabs[i] != null) fxTabs[i].setContent(tabs[i].getPane());
	}
	
	@Override
	public void close() {
		for(Tab tab : fxTabs)
			tab.setContent(null);
	}
	
}
