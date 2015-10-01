package dialogs;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.Pair;
import model.DataItem;
import model.DataItemDao.FileFormat;

public class DialogGalleryController implements Initializable
{
	@FXML private Button alert;			// its only necessary to inject buttons so you can disable commands
	@FXML private Button confirm;
	@FXML private Button inform;
	@FXML private Button deny;		// Log in
	@FXML private Button explain;
	@FXML private Button choose;
	@FXML private Button open;
	@FXML private Button save;
	@FXML private Button find;
	@FXML private Button rules;

	@Override public void initialize(URL url, ResourceBundle rb)
	{
		System.out.println("DialogGalleryController.initialize");
		
		
		  Button btn = confirm;
		    btn.setOnAction(event -> {
		        Alert alert = new Alert(AlertType.CONFIRMATION);
		        alert.setTitle("Confirmation Dialog");
		        alert.setHeaderText("Look, a Confirmation Dialog");
		        alert.setContentText("Are you ok with this?");

		        ButtonBar buttonBar=(ButtonBar)alert.getDialogPane().lookup(".button-bar");
		        buttonBar.setDisable(true);
		        alert.initModality(Modality.APPLICATION_MODAL);
		        alert.show();            
		        // now we can retrive alert bounds:
		        double yIni=-alert.getHeight();
		        double yEnd=alert.getY();
		        // and move alert to the top of the screen
		        alert.setY(yIni);

		        buttonBar.getButtons().stream().filter(b->((Button)b).isDefaultButton()).findFirst()
		            .ifPresent(b->((Button)b).addEventFilter(EventType.ROOT, 
		                e->{
		                    if(e.getEventType().equals(ActionEvent.ACTION)){
		                        e.consume();
		                        final DoubleProperty yPropertyOut = new SimpleDoubleProperty(yEnd);
		                        yPropertyOut.addListener((ov,n,n1)->alert.setY(n1.doubleValue()));            
		                        Timeline timeOut = new Timeline();
		                        timeOut.getKeyFrames().add(new KeyFrame(Duration.seconds(1.5), t->alert.close(),
		                                new KeyValue(yPropertyOut, yIni,Interpolator.EASE_BOTH)));
		                        timeOut.play();
		                    }
		                }));

		        final DoubleProperty yProperty = new SimpleDoubleProperty();
		        yProperty.addListener((ob,n,n1)->alert.setY(n1.doubleValue()));
		        Timeline timeIn = new Timeline();
		        timeIn.getKeyFrames().add(new KeyFrame(Duration.seconds(1.5), e->{
		            buttonBar.setDisable(false);
		        },new KeyValue(yProperty, yEnd,Interpolator.EASE_BOTH)));
		        timeIn.play();
		    });

		
		
	}

	@FXML private void doAlert()
	{
		System.out.println("doAlert");
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(null);
		alert.setContentText("DialogGalleryController has many example dialogs implemented");
		alert.showAndWait();
	}

	// --------------------------------------------------------------------------------------
	@FXML private void doConfirm()
	{
		System.out.println("doConfirm");

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("Look, a Confirmation Dialog");
		alert.setContentText("Are you ok with this?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK)
		{
			System.out.println("user chose OK");
		} else
		{
			System.out.println("CANCEL (or user closed the dialog)");
		}
	}

	// --------------------------------------------------------------------------------------
	@FXML private void doStyledDialog()
	{
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.setTitle("Confirmation Dialog");
	    alert.setHeaderText("This is a Custom Confirmation Dialog");
	    alert.setContentText("We override the style classes of the dialog");

	    DialogPane dialogPane = alert.getDialogPane();
	    URL css = getClass().getResource("styledDialog.css");
		dialogPane.getStylesheets().add(  css.toExternalForm());
	    dialogPane.getStyleClass().add("myDialog");
	    
		alert.showAndWait();
}

	// --------------------------------------------------------------------------------------
	@FXML private void doLogin()
	{
		LoginDialog login = new LoginDialog();
		Optional<Pair<String, String>> result = login.showAndWait();

		result.ifPresent(usernamePassword -> {
			System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
		});
	}
	

	// --------------------------------------------------------------------------------------
	@FXML private void doTextInput()
	{
		TextInputDialog dialog = new TextInputDialog("walter");
		dialog.setTitle("Name Entry");
		dialog.setHeaderText("Your name is used throughout the program");
		dialog.setContentText("Please enter your name:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
		{
			System.out.println("Your name: " + result.get());
		}

		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(name -> System.out.println("Your name: " + name));
	}

	// --------------------------------------------------------------------------------------
	@FXML private void doChoose()
	{
		List<String> choices = new ArrayList<>();
		choices.add("Apples");
		choices.add("Oranges");
		choices.add("Pears");

		ChoiceDialog<String> dialog = new ChoiceDialog<>("Pears", choices);
		dialog.setTitle("Choice Dialog");
		dialog.setHeaderText("Favorite Fruit");
		dialog.setContentText("Choose your preferred dessert fruit:");
		Optional<String> result = dialog.showAndWait();
		// The Java 8 way to get the response value (with lambda expression).
		result.ifPresent(letter -> System.out.println("Your choice: " + letter));
	}

	// --------------------------------------------------------------------------------------
	@FXML private void doOpen()
	{
		importFromFile(0, FileFormat.CSV);
	}

	@FXML private void doSave()
	{
		exportToFile(null, FileFormat.CSV);
	}

	@FXML private void doRules()
	{
		ObservableList<String> fields = FXCollections.observableArrayList();
		fields.addAll("Subject", "Verb", "Object", "Date");
		new RulesDialog(fields).showAndWait();
	}

	@FXML private void doConnect()
	{
		ConnectDialog dlog = new ConnectDialog();
		dlog.setDefaults("org.h2.Driver", "jdbc:h2:~/test", "Adam");
		dlog.showAndWait();
	}

	@FXML private void doFind()
	{
		new FindDialog(true).showAndWait();
	}

	private void importFromFile(int index, FileFormat fileFormat)
	{
		FileChooser fileChooser = createFileChooser("Import a " + fileFormat.name() + " file", fileFormat);
		File file = fileChooser.showOpenDialog(null);
		if (file != null)
			System.out.println("Read the file, as per FileUtil");
	}

	private void exportToFile(List<DataItem> dataItems, FileFormat fileFormat)
	{
		FileChooser fileChooser = createFileChooser("Export a " + fileFormat + " file", fileFormat);
		fileChooser.setInitialFileName("export." + fileFormat.getFileExtension());
		File file = fileChooser.showSaveDialog(null);
		if (file != null)
			System.out.println("Write the file, as per FileUtil");
	}

	private static FileChooser createFileChooser(String title, FileFormat fileFormat)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(File.listRoots()[0]);
		fileChooser.setTitle(title);
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter(fileFormat.name() + " files", "*." + fileFormat.getFileExtension()));
		return fileChooser;
	}

}
