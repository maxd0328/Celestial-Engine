package studio.celestial.dialog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import studio.celestial.core.StudioInterface;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public final class MessageHandler {
	
	private static int messageCount = 0;
	private static final Object LOCK = new Object();
	
	public static void show(MessageDialog message) {
		if(messageCount > 0) return;
		++messageCount;
		Alert alert = createAlert(message);
		alert.setOnHidden((event) -> --messageCount);
		if(alert != null) alert.show();
	}
	
	private static boolean tmpResult = false;
	public static synchronized boolean showAndWait(MessageDialog message) {
		if(messageCount > 0) return false;
		++messageCount;
		tmpResult = false;
		Alert alert = createAlert(message);
		alert.setOnHidden((event) -> --messageCount);
		if(alert != null) alert.showAndWait().ifPresent((response) -> tmpResult = response == ButtonType.YES);
		return tmpResult;
	}
	
	public static String showAndWait(String title, String prompt, String defaultText) {
		if(messageCount > 0) return null;
		++messageCount;
		TextInputDialog dialog = new TextInputDialog(defaultText);
		dialog.setOnHidden((event) -> --messageCount);
		dialog.setTitle(title);
		dialog.setHeaderText(prompt);
		dialog.getDialogPane().setPrefWidth(300);
		((Stage) dialog.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
		if(StudioInterface.staticInfo.getStylesheet() != null) dialog.getDialogPane().getStylesheets().add(StudioInterface.staticInfo.getStylesheet());
		
		Optional<String> result = dialog.showAndWait();
		if(result.isPresent()) return result.get();
		else return null;
	}
	
	public static <T> T showAndWait(ComplexDialog<T> complexDialog) {
		return MessageHandler.<T>showAndWait(complexDialog, new DialogOptionBuilder().withButtonTypes(DialogOptionBuilder.ButtonType.OK, DialogOptionBuilder.ButtonType.CANCEL), false);
	}
	
	public static <T> T showAndWait(ComplexDialog<T> complexDialog, DialogOptionBuilder builder) {
		return MessageHandler.<T>showAndWait(complexDialog, builder, false);
	}
	
	public static <T> void show(ComplexDialog<T> complexDialog) {
		MessageHandler.<T>showAndWait(complexDialog, new DialogOptionBuilder().withButtonTypes(DialogOptionBuilder.ButtonType.OK, DialogOptionBuilder.ButtonType.CANCEL), true);
	}
	
	public static <T> void show(ComplexDialog<T> complexDialog, DialogOptionBuilder builder) {
		MessageHandler.<T>showAndWait(complexDialog, builder, true);
	}
	
	private static final LinkedHashMap<ComplexDialog<?>, Dialog<?>> DIALOGS = new LinkedHashMap<ComplexDialog<?>, Dialog<?>>();
	private static <T> T showAndWait(ComplexDialog<T> complexDialog, DialogOptionBuilder builder, boolean noWait) {
		Dialog<T> dialog = new Dialog<>();
		synchronized(LOCK) {
			DIALOGS.put(complexDialog, dialog);
		}
		((Stage) dialog.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
		dialog.setTitle(complexDialog.getTitle());
		dialog.setOnHidden(event -> {
			synchronized(LOCK) {
				DIALOGS.remove(complexDialog);
			}
		});
		dialog.getDialogPane().getButtonTypes().addAll(builder.toButtonTypes());
		
		complexDialog.componentInitialize();
		dialog.getDialogPane().setContent(complexDialog.getPane());
		dialog.getDialogPane().setMinSize(complexDialog.getDialogWidth(), complexDialog.getDialogHeight());
		dialog.getDialogPane().setMaxSize(complexDialog.getDialogWidth(), complexDialog.getDialogHeight());
		dialog.getDialogPane().setPrefSize(complexDialog.getDialogWidth(), complexDialog.getDialogHeight());
		if(complexDialog.getPane().getStyle() != null) {
			dialog.getDialogPane().setStyle(complexDialog.getPane().getStyle());
		}
		if(StudioInterface.staticInfo.getStylesheet() != null) {
			dialog.getDialogPane().getStylesheets().add(StudioInterface.staticInfo.getStylesheet());
			dialog.getDialogPane().setId("panel-no-edge");
		}
		if(StudioInterface.staticInfo.getIcon() != null)
			((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:" + StudioInterface.staticInfo.getIcon()));
		dialog.setResultConverter(dialogButton -> {
			if(dialogButton == null)
				return null;
			else if(dialogButton == DialogOptionBuilder.ButtonType.OK.getFXButtonType())
				return complexDialog.getResult();
			else if(dialogButton == DialogOptionBuilder.ButtonType.EXIT.getFXButtonType())
				System.exit(0);
			return null;
		});
		
		if(noWait) {
			dialog.show();
			return null;
		}
		
		Optional<T> result = dialog.showAndWait();
		if(result.isPresent()) return result.get();
		else return null;
	}
	
	private static Alert createAlert(MessageDialog message) {
		Alert alert;
		switch(message.getType()) {
		case MESSAGE_TYPE_INFORMATION:
			alert = new Alert(AlertType.INFORMATION, message.getMessage(), ButtonType.OK);
			break;
		case MESSAGE_TYPE_ERROR:
			alert = new Alert(AlertType.ERROR, message.getMessage(), ButtonType.OK);
			break;
		case MESSAGE_TYPE_WARNING:
			alert = new Alert(AlertType.WARNING, message.getMessage(), ButtonType.OK);
			break;
		case MESSAGE_TYPE_CONFIRMATION:
			alert = new Alert(AlertType.CONFIRMATION, message.getMessage(), ButtonType.NO, ButtonType.YES);
			break;
		default: alert = null;
		}
		((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
		if(alert != null && StudioInterface.staticInfo.getStylesheet() != null) alert.getDialogPane().getStylesheets().add(StudioInterface.staticInfo.getStylesheet());
		return alert;
	}
	
	public static void update() {
		synchronized(LOCK) {
			for(ComplexDialog<?> dialog : DIALOGS.keySet())
				dialog.componentUpdate();
		}
	}
	
	public static Map<ComplexDialog<?>, Dialog<?>> getDialogs() {
		return new LinkedHashMap<ComplexDialog<?>, Dialog<?>>(DIALOGS);
	}
	
	public static void denyAll() {
		for(ComplexDialog<?> dialog : DIALOGS.keySet())
			DIALOGS.get(dialog).close();
	}
	
	public static void acceptAll() {
		for(ComplexDialog<?> dialog : DIALOGS.keySet()) {
			if(DIALOGS.get(dialog).getDialogPane().lookupButton(DialogOptionBuilder.ButtonType.OK.getFXButtonType()) != null)
				((Button) DIALOGS.get(dialog).getDialogPane().lookupButton(DialogOptionBuilder.ButtonType.OK.getFXButtonType())).fire();
			else {
				DIALOGS.get(dialog).getDialogPane().getButtonTypes().add(DialogOptionBuilder.ButtonType.OK.getFXButtonType());
				((Button) DIALOGS.get(dialog).getDialogPane().lookupButton(DialogOptionBuilder.ButtonType.OK.getFXButtonType())).fire();
			}
		}
	}
	
}
