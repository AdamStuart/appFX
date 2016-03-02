package dialogs;

import java.net.URL;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

//new FindDialog()).showAndWait();
// TODO  currently there is no information returned !!


public class FindDialog extends Dialog
{
	public FindDialog(boolean showReplace)
	{
		super();

		// Create the custom dialog.
//			Dialog<Pair<String, String>> dialog = new Dialog<>();
		setTitle("Find / Replace");
		setHeaderText("Search and replace functions ");
	    DialogPane dialogPane = getDialogPane();
		// Set the icon (must be included in the project).
		URL res =  getClass().getResource("login.png");
		if (res != null)
			setGraphic(new ImageView(res.toString()));

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Find", ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		
		
		TextField findFld = new TextField();
		findFld.setPromptText("Target");
		TextField replaceFld = new TextField();
		replaceFld.setPromptText("New Text");

		grid.add(new Label("Find:"), 0, 0);			grid.add(findFld, 1, 0);
		grid.add(new Label("Replace with:"), 0, 1);	grid.add(replaceFld, 1, 1);

		// Enable login button depending on whether a username was entered.
		Node loginButton = dialogPane.lookupButton(loginButtonType);
		loginButton.setDisable(true);

		CheckBox caseSens = new CheckBox("Case sensitive");
		CheckBox whole = new CheckBox("Whole word");
		CheckBox wrap = new CheckBox("Wrap search");
		// Do some validation (using the Java 8 lambda syntax).

		grid.add(caseSens, 0, 3);	
		grid.add(whole, 0, 4);	
		grid.add(wrap, 0, 5);	
	
		Button findButn = new Button("Find");
		Button replaceFindButn = new Button("Replace/Find");
		Button replaceButn = new Button("Replace");
		Button replaceAllButn = new Button("Replace All");
		
		grid.add(findButn, 0, 6);	
		grid.add(replaceFindButn, 1, 6);	
		grid.add(replaceButn, 2, 6);	
		grid.add(replaceAllButn, 3, 6);	
	
//		
//		username.textProperty().addListener((observable, oldValue, newValue) -> {
//			loginButton.setDisable(newValue.trim().isEmpty());
//		});

		dialogPane.setContent(grid);

		// Request focus on the findFld field by default.
		Platform.runLater(() -> findFld.requestFocus());

		// Convert the result to a username-password-pair when the login button
		// is clicked.
		setResultConverter(dialogButton -> {
//			if (dialogButton == loginButtonType)
//				return new Pair<>(username.getText(), password.getText());
//			ResultSet out = new ResultSet<>();
			return null;
		});

	}

	
}
