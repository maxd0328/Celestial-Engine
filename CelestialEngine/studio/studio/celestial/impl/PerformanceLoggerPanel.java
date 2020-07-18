package studio.celestial.impl;

import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioComponentPanel;
import studio.celestial.core.StudioInterface;
import studio.celestial.dialog.ComplexDialog;
import studio.celestial.dialog.IntegerInputDialog;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.util.StudioUtil;

public final class PerformanceLoggerPanel extends StudioComponentPanel implements ComplexDialog<Object> {
	
	private final Map<String, Long> results;
	private final int frameCount;
	
	public PerformanceLoggerPanel(StudioInterface studio, Map<String, Long> results, int frameCount) {
		super(studio, "Performance Logger Results", 5, 5, 7, 5, 7);
		this.results = results;
		this.frameCount = frameCount;
	}
	
	@Override
	protected void initialize() {
		Label lbl = new Label("Frame Dump: " + frameCount + " frame(s)");
		super.getPane().add(lbl, 0, 0, 20, 2);
		
		TableView<PerformanceLoggerTableItem> view = new TableView<PerformanceLoggerTableItem>();
		view.getColumns().add(StudioUtil.<PerformanceLoggerTableItem, String>generateTableColumn("taskName", "Task Name", 140));
		view.getColumns().add(StudioUtil.<PerformanceLoggerTableItem, String>generateTableColumn("length", "Length (nsec)", 97));
		view.getColumns().add(StudioUtil.<PerformanceLoggerTableItem, String>generateTableColumn("timestamp", "Timestamp", 97));
		
		long timestamp = 0;
		for(String identifier : results.keySet()) {
			view.getItems().add(new PerformanceLoggerTableItem(identifier, results.get(identifier), timestamp));
			timestamp += results.get(identifier);
		}
		
		view.setColumnResizePolicy(p -> true);
		super.getPane().add(view, 0, 2, 20, 18);
		
		super.calculateConstraints(StudioInterface.getInstantiation(), 10, 10);
	}
	
	@Override
	protected void update() {
		
	}
	
	@Override
	public GridPane getPane() {
		return super.getPane();
	}
	
	@Override
	public String getTitle() {
		return super.getName();
	}
	
	@Override
	public int getDialogWidth() {
		return 350;
	}
	
	@Override
	public int getDialogHeight() {
		return 400;
	}
	
	@Override
	public Object getResult() {
		return null;
	}
	
	public static Menu toMenuItem() {
		Menu item = new Menu("Performance Logger");
		MenuItem captureFrame = new MenuItem("Capture Frame");
		captureFrame.setOnAction(event -> GLRequestSystem.request(() -> {
			GLRequestSystem.getRenderer().getLogger().captureFrame();
			GLRequestSystem.getSceneManager().getConfigs().resume();
		}));
		MenuItem captureFrames = new MenuItem("Capture Frames");
		captureFrames.setOnAction(event -> {
			Integer[] arr = MessageHandler.showAndWait(new IntegerInputDialog(StudioInterface.getInstantiation(), "Capture Frames", 1, Integer.MAX_VALUE, "Frame Count"));
			if(arr != null)
				GLRequestSystem.request(() -> {
					GLRequestSystem.getRenderer().getLogger().captureFrames(arr[0]);
					GLRequestSystem.getSceneManager().getConfigs().resume();
				});
		});
		
		item.getItems().add(captureFrame);
		item.getItems().add(captureFrames);
		return item;
	}
	
	public static final class PerformanceLoggerTableItem {
		
		private String taskName;
		private long length;
		private long timestamp;
		
		private PerformanceLoggerTableItem(String taskName, long length, long timestamp) {
			this.taskName = taskName;
			this.length = length;
			this.timestamp = timestamp;
		}
		
		public String getTaskName() {
			return taskName;
		}
		
		public void setTaskName(String taskName) {
			this.taskName = taskName;
		}
		
		public long getLength() {
			return length;
		}
		
		public void setLength(long length) {
			this.length = length;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
		
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		
	}
	
}
