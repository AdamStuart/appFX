package dialogs;

import java.net.URL;
import java.util.Optional;

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
import javafx.stage.Modality;
import javafx.util.Pair;

//Optional<Pair<String, String>> result = (new LoginDialog()).showAndWait();
//
//result.ifPresent(usernamePassword -> {
//	System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
//});

public class LoginDialog extends Dialog
{
	public LoginDialog()
	{
	super();
    initModality(Modality.WINDOW_MODAL);

		// Create the custom dialog.
//			Dialog<Pair<String, String>> dialog = new Dialog<>();
		setTitle("Login Dialog");
		setHeaderText("We need your password to continue");
	    DialogPane dialogPane = getDialogPane();
//		    URL css = getClass().getResource("styledDialog.css");
//			dialogPane.getStylesheets().add(  css.toExternalForm());
//		    dialogPane.getStyleClass().add("myDialog");
	    

		// Set the icon (must be included in the project).
		URL res =  getClass().getResource("login.png");
		if (res != null)
			setGraphic(new ImageView(res.toString()));

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Sign In", ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField username = new TextField();
		username.setPromptText("Username");
		PasswordField password = new PasswordField();
		password.setPromptText("Password");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);

		// Enable login button depending on whether a username was entered.
		Node loginButton = dialogPane.lookupButton(loginButtonType);
		loginButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		username.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialogPane.setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> username.requestFocus());

		// Convert the result to a username-password-pair when the login button
		// is clicked.
		setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType)
				return new Pair<>(username.getText(), password.getText());
			return null;
		});

	}

	
}
