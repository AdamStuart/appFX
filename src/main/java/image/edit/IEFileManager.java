package image.edit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

/**
 * This class allows for the opening of image files by providing
 * a file chooser and open and save dialogs. The reason for the 
 * creation of this class is because the file chooser doesn't support
 * file extensions when saving a file, so this class provides a dialog
 * with file extensions. File extensions can be passed in via the 
 * constructor.
 * @author Kyle O'Connor
 * @version 1.0
 */
public class IEFileManager {

	private FileChooser fileChooser;
	private ObservableList<String> extensionStrings;

	public IEFileManager(ObservableList<String> extensionStrings)
	{
		this.extensionStrings = extensionStrings;
		fileChooser = new FileChooser();
		initExtensionFilters();
	}

	/**
	 * Opens a file chooser open dialog. Returns the
	 * selected file or null if no file was selected.
	 * @param stage The parent stage of the file chooser.
	 * @return The chosen file. Null if no file was selected.
	 */
	public File chooseFile(Stage stage)
	{
		return fileChooser.showOpenDialog(stage);
	}

	/**
	 * Opens a file chooser save dialog. Returns the selected
	 * file or null if no file was selected.
	 * @param stage The parent stage of the file chooser.
	 * @return The chosen file. Null if no file was selected.
	 */
	public File saveFile(Stage stage)
	{
		return fileChooser.showSaveDialog(stage);
	}

	/**
	 * Loads an image from a given file and returns the image.
	 * @param imageFile The file of the image to be loaded.
	 * @return An image opened from the given file.
	 * @throws Exception
	 */
	public Image loadImage(File imageFile) throws Exception
	{
		if(imageFile != null)
		{
			BufferedImage bufferedImage = ImageIO.read(imageFile);
			Image image = SwingFXUtils.toFXImage(bufferedImage, null);
			return image;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Opens a dialog to save an image. It allows the user to choose
	 * a location for the image and a file extension for the file.
	 * This was created because the file chooser doesn't allow for
	 * file extensions when saving a file.
	 * @param stage The parent stage of the save image stage.
	 * @param image The image to be saved.
	 * @throws IOException
	 */
	public void saveImageDialog(final Stage stage, final WritableImage image)
	{
		final Stage saveImageStage = new Stage();
		saveImageStage.initModality(Modality.WINDOW_MODAL);
		saveImageStage.initOwner(stage);
		saveImageStage.setTitle("Save Image");
		saveImageStage.setResizable(false);

		final GridPane gp = new GridPane();
		final VBox     vb = new VBox();
		vb.setSpacing(10);
		vb.setAlignment(Pos.CENTER);
		gp.setAlignment(Pos.CENTER);
		gp.setHgap(10);

		final Label     	    fileNameLabel    = new Label();
		final ComboBox<String>  saveExtensions   = new ComboBox<String>(extensionStrings);
		final Button 	        chooseFileButton = new Button("Choose Location");
		final Button    	    saveButton       = new Button("Save");
		saveButton.setMaxWidth(80);
		saveButton.setAlignment(Pos.CENTER);
		fileNameLabel.setMinWidth(80);
		fileNameLabel.setMaxWidth(Double.MAX_VALUE);

		fileNameLabel.setStyle("-fx-background-color: white;-fx-border-radius: 1;-fx-border-color: black");

		chooseFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				File chosenFile = saveFile(stage);
				if(chosenFile != null)
				{
					fileNameLabel.setText(chosenFile.getAbsolutePath());
				}
			}
		});

		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try
				{
					if(fileNameLabel.getText().equals(""))
						throw new IllegalArgumentException();
					String extension    = saveExtensions.getValue();
					File   saveLocation = new File(fileNameLabel.getText() + extension);
					ImageIO.write(SwingFXUtils.fromFXImage(image, null), extension.replace(".", ""), saveLocation);
					((Node)(e.getSource())).getScene().getWindow().hide();
				}
				catch(IllegalArgumentException|NullPointerException exception)
				{
//					MessageBox.show(saveImageStage, "Please make sure all fields are filled out correctly.", "Error", MessageBox.ICON_ERROR);
				}
				catch(IOException exception)
				{
//					MessageBox.show(saveImageStage, "There was an error saving this image.", "Error", MessageBox.ICON_ERROR);	
				}
			}
		});

		gp.add(chooseFileButton, 0, 0);
		gp.add(fileNameLabel, 1, 0);
		gp.add(saveExtensions, 2, 0);
		vb.getChildren().addAll(gp, saveButton);

		saveImageStage.setScene(new Scene(vb, 500, 100));
		saveImageStage.show();
	}

	/**
	 * Initializes the extensions filters used by the file chooser
	 * and adds the filters to the file chooser.
	 */
	private void initExtensionFilters()
	{
		FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
		FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
		FileChooser.ExtensionFilter extFilterGIF = new FileChooser.ExtensionFilter("GIF files (*.gif)", "*.GIF");
		fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG, extFilterGIF);
	}
}
