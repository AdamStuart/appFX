package table;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Main class to start the application.
 * 
 * @author Marco Jakob
 */
public class AppPersonTable extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Table Cell Rendering");
		
		try {
			FXMLLoader loader = new FXMLLoader(AppPersonTable.class.getResource("PersonTable.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
