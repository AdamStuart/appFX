package dialogs;

import java.net.URL;
import java.sql.ResultSet;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

//Optional<Pair<String, String>> result = (new ConnectDialog()).showAndWait();
//
//result.ifPresent(usernamePassword -> {
//	System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
//});

public class ConnectDialog extends Dialog
{
	TextField driverName, urlField, username;
	
	public ConnectDialog()
	{
	super();
		// Create the custom dialog.
//			Dialog<Pair<String, String>> dialog = new Dialog<>();
		setTitle("Connect");
		setHeaderText("Enter server information to connect");
	    DialogPane dialogPane = getDialogPane();
//		    URL css = getClass().getResource("styledDialog.css");
//			dialogPane.getStylesheets().add(  css.toExternalForm());
//		    dialogPane.getStyleClass().add("myDialog");
	    

		// Set the icon (must be included in the project).
		URL res =  getClass().getResource("connect.png");
		if (res != null)
			setGraphic(new ImageView(res.toString()));

		// Set the button types.
		ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		driverName = new TextField();
		driverName.setPromptText("Driver");
		urlField = new TextField();
		urlField.setPromptText("URL");
		username = new TextField();
		username.setPromptText("Username");
		
		PasswordField password = new PasswordField();
		password.setPromptText("Password");

		grid.add(new Label("Driver:"), 0, 0);		grid.add(driverName, 1, 0);
		grid.add(new Label("URL:"), 0, 1);			grid.add(urlField, 1, 1);

		grid.add(new Label("Username:"), 0, 2);		grid.add(username, 1, 2);
		grid.add(new Label("Password:"), 0, 3);		grid.add(password, 1, 3);

		// Enable login button depending on whether a username was entered.
		Node connectButton = dialogPane.lookupButton(connectButtonType);
		connectButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			connectButton.setDisable(newValue.trim().isEmpty());
		});

		dialogPane.setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> username.requestFocus());

		// Convert the result to a username-password-pair when the login button
		// is clicked.
		setResultConverter(dialogButton -> {
			if (dialogButton == connectButton)
			{
				Pair<String, String> result = new Pair<>(username.getText(), password.getText());
			}
			return null;
		});

	}
	public void setDefaults(String driver, String url, String name)
	{
		if (driver != null) driverName.setText(driver);
		if (url != null) urlField.setText(url);
		if (name != null) username.setText(name);
	}
		
}
