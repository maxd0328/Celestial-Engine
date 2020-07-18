package studio.celestial.core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import celestial.core.Modifier;
import celestial.util.Factory;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import studio.celestial.core.StudioView.ScaledPanel;
import studio.celestial.dialog.MessageHandler;
import studio.celestial.impl.ModifierRepositoryPanel.ModifierItem;

public final class StudioInterface extends Application {
	
	public static final Object APPLICATION_HALT = new Object();
	
	/**/
	public static volatile StudioInterfaceInfo staticInfo = null;
	private static volatile StudioInterface instantiation = null;
	/**/
	
	public static void instantiate(StudioInterfaceInfo info, String... args) {
		StudioInterface.staticInfo = info;
		Application.launch(args);
	}
	
	public static StudioInterface getInstantiation() {
		return instantiation;
	}
	
	private StudioInterfaceInfo info;
	private JFrame frame;
	private Canvas canvas;
	private Stage primaryStage;
	private Scene primaryScene;
	private GridPane rootPane;
	private StudioView currentView;
	private LinkedHashMap<ModifierItem, ArrayList<ModifierItem>> categories;
	
	private int canvasX = 0, canvasY = 0, canvasW = 10, canvasH = 10;
	
	private final ArrayList<IFXRequest> requests = new ArrayList<IFXRequest>();
	private final Object lock = new Object();
	
	public StudioInterfaceInfo getInfo() {
		return info;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	public Scene getPrimaryScene() {
		return primaryScene;
	}
	
	public GridPane getRootPane() {
		return rootPane;
	}
	
	public StudioView getCurrentView() {
		return currentView;
	}
	
	public LinkedHashMap<ModifierItem, ArrayList<ModifierItem>> getModifierCategories() {
		return categories;
	}
	
	public ModifierItem getModifier(Class<? extends Modifier> type) {
		for(ModifierItem category : categories.keySet()) {
			for(ModifierItem modifier : categories.get(category)) {
				if(modifier.getType() != null && modifier.getType().equals(type)) return modifier;
			}
		}
		return null;
	}
	
	public void addModifierCategory(String category) {
		this.categories.put(new ModifierItem(category), new ArrayList<>());
	}
	
	public void addModifier(String category, String name, String description, Factory<? extends Modifier> modifier, Class<? extends Modifier> type) {
		categories.get(getCategory(category)).add(new ModifierItem(name, description, modifier, type));
	}
	
	private ModifierItem getCategory(String identifier) {
		for(ModifierItem category : categories.keySet()) if(category.getIdentifier().equals(identifier)) return category;
		return null;
	}
	
	public void show() {
		frame.setVisible(true);
	}
	
	public void hide() {
		frame.setVisible(false);
	}
	
	public void requestFocus() {
		frame.toFront();
		frame.requestFocus();
	}
	
	public void setView(StudioView view) {
		if(currentView != null)
			for(ScaledPanel panel : currentView.getPanels())
				panel.getPanel().close();
		
		this.currentView = view;
		view.initialize();
		this.rootPane.getChildren().clear();
		MenuBar menu = info.getMenuBar().toMenuBar();
		GridPane.setMargin(menu, new Insets(0, -info.getInset(), 0, -info.getInset()));
		rootPane.add(menu, 0, 0, info.getWidth() / info.getHgap(), 1);
		for(ScaledPanel panel : view.getPanels()) {
			GridPane.setMargin(panel.getPanel().getPane(), new Insets(-3));
			this.rootPane.add(panel.getPanel().getPane(), panel.getGridX() * view.getScaleFactor(), panel.getGridY() * view.getScaleFactor() + 2,
					panel.getWidth() * view.getScaleFactor(), panel.getHeight() * view.getScaleFactor());
		}
		this.canvasX = view.getViewX();
		this.canvasY = view.getViewY();
		this.canvasW = view.getViewWidth();
		this.canvasH = view.getViewHeight();
	}
	
	public void request(IFXRequest request) {
		synchronized(lock) {
			this.requests.add(request);
		}
	}
	
	public void start(Stage primaryStage) throws IOException {
		this.info = staticInfo;
		this.primaryStage = primaryStage;
		this.rootPane = new GridPane();
		this.currentView = null;
		this.primaryScene = new Scene(rootPane, info.getWidth(), info.getHeight());
		if(info.getStylesheet() != null) this.primaryScene.getStylesheets().add(info.getStylesheet());
		
		rootPane.setAlignment(Pos.CENTER);
		rootPane.setHgap(info.getHgap());
		rootPane.setVgap(info.getVgap());
		rootPane.setPadding(new Insets(info.getInset(), Math.max(info.getInset() - 2, 0), info.getInset(), Math.max(info.getInset() - 2, 0)));
		rootPane.add(new Label(""), 10000, 10000);
		rootPane.setOnMouseClicked(event -> rootPane.requestFocus());
		
		ObservableList<ColumnConstraints> colConstraints = rootPane.getColumnConstraints();
		ObservableList<RowConstraints> rowConstraints = rootPane.getRowConstraints();
		for(int i = 0 ; i < info.getWidth() / info.getHgap() ; ++i) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth((double) info.getHgap() / (double) info.getWidth() * 100d);
			colConstraints.add(c);
		}
		for(int i = 0 ; i < info.getHeight() / info.getVgap() ; ++i) {
			RowConstraints c = new RowConstraints();
			c.setPercentHeight((double) info.getVgap() / (double) info.getHeight() * 100d);
			rowConstraints.add(c);
		}
		
		final LongProperty prev = new SimpleLongProperty();
		final StudioInterface _this = this;
		
		final JPanel panel = new JPanel();
		final JFXPanel jfxPanel = new JFXPanel();
		
		new AnimationTimer() {
			
			@Override
			public void handle(long now) {
				if(now - prev.get() > info.getUpdateInterval() * 1000000) {
					synchronized(APPLICATION_HALT) {
						synchronized(lock) {
							for(IFXRequest request : new ArrayList<IFXRequest>(requests)) request.perform(_this);
							requests.clear();
						}
						if(currentView != null) currentView.update();
						MessageHandler.update();
						panel.setBounds((int) (canvasX * ((double) frame.getWidth() / info.getWidth())), (int) (canvasY * ((double) frame.getHeight() / info.getHeight())),
								(int) (canvasW * ((double) frame.getWidth() / info.getWidth())), (int) (canvasH * ((double) frame.getHeight() / info.getHeight())));
						canvas.setBounds(0, 0, panel.getWidth(), panel.getHeight());
						
						prev.set(now);
					}
				}
			}
			
		}.start();
		
		jfxPanel.setScene(primaryScene);
		jfxPanel.setLayout(null);
		jfxPanel.setFocusable(true);
		jfxPanel.setRequestFocusEnabled(true);
		
		this.canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setForeground(Color.BLACK);
		panel.setBackground(new Color(32, 32, 32));
		panel.setBounds((int) canvasX, (int) canvasY, (int) canvasW, (int) canvasH);
		panel.add(canvas);
		
		this.frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(info.getWidth(), info.getHeight());
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setResizable(true);
		frame.setTitle(info.getTitle());
		if(info.getIcon() != null) frame.setIconImage(ImageIO.read(new File(info.getIcon())));
		frame.getContentPane().add(jfxPanel);
		jfxPanel.add(panel);
		
		JTextField focusCatalyst = new JTextField();
		focusCatalyst.setBounds(10000, 10000, 0, 0); // some off-screen location
		jfxPanel.add(focusCatalyst);
		
		this.frame.setMinimumSize(new Dimension(info.getWidth(), info.getHeight()));
		this.categories = new LinkedHashMap<ModifierItem, ArrayList<ModifierItem>>();
		
		instantiation = this;
		StudioViewRepository.ping();
	}
	
}
