package studio.celestial.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class SplashScreen {
	
	private final int width;
	private final int height;
	private final String imageURL;
	private final String copyrightText;
	private final String loadingText;
	
	private final Stage stage;
	private final BooleanProperty showLoadingText = new SimpleBooleanProperty();
	
	public SplashScreen(int width, int height, String imageURL, String copyrightText, String loadingText) {
		this.width = width;
		this.height = height;
		this.imageURL = imageURL;
		this.copyrightText = copyrightText;
		this.loadingText = loadingText;
		
		if(Thread.currentThread().getName() != null && Thread.currentThread().getName().equals("JavaFX Application Thread"))
			this.stage = createSplash();
		else {
			FutureTask<Stage> query = new FutureTask<>(() -> createSplash());
			Platform.runLater(query);
			try {
				this.stage = query.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getImageURL() {
		return imageURL;
	}
	
	public String getCopyrightText() {
		return copyrightText;
	}
	
	public String getLoadingText() {
		return loadingText;
	}
	
	public boolean isShowLoadingText() {
		return showLoadingText.get();
	}
	
	public void setShowLoadingText(boolean showLoadingText) {
		this.showLoadingText.set(showLoadingText);
	}
	
	public BooleanProperty showLoadingTextProperty() {
		return showLoadingText;
	}
	
	public void show() {
		if(Thread.currentThread().getName() != null && Thread.currentThread().getName().equals("JavaFX Application Thread"))
			this.stage.show();
		else
			Platform.runLater(() -> this.stage.show());
	}
	
	public void hide() {
		if(Thread.currentThread().getName() != null && Thread.currentThread().getName().equals("JavaFX Application Thread"))
			this.stage.hide();
		else
			Platform.runLater(() -> this.stage.hide());
	}
	
	private Stage createSplash() {
		Stage stage = new Stage();
		if(StudioInterface.getInstantiation().getInfo().getIcon() != null)
			stage.getIcons().add(new Image("file:" + StudioInterface.getInstantiation().getInfo().getIcon()));
		
		Text copyright = new Text(copyrightText);
		copyright.setFill(Color.WHITE);
		StackPane.setAlignment(copyright, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(copyright, new Insets(7));
		
		Text loading = new Text(loadingText);
		loading.textProperty().bind(Bindings.when(showLoadingText).then(loadingText).otherwise(""));
		loading.setFill(Color.WHITE);
		StackPane.setAlignment(loading, Pos.BOTTOM_CENTER);
		StackPane.setMargin(loading, new Insets(7));
		
		Rectangle rect = new Rectangle(0, 0, width, height);
		rect.setArcWidth(30.0);
		rect.setArcHeight(30.0);
		rect.setFill(new ImagePattern(new Image(imageURL)));
		
		Pane pane = new StackPane();
		pane.getChildren().addAll(rect, copyright, loading);
		pane.setBackground(null);
		Scene scene = new Scene(pane);
		scene.setFill(Color.TRANSPARENT);
		stage.initStyle(StageStyle.TRANSPARENT);
		Rectangle2D bounds = Screen.getPrimary().getBounds();
		stage.setScene(scene);
		stage.setX(bounds.getMinX() + bounds.getWidth() / 2 - width / 2);
		stage.setY(bounds.getMinY() + bounds.getHeight() / 2 - height / 2);
		stage.setWidth(width);
		stage.setHeight(height);
		
		return stage;
	}
	
}
