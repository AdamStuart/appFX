package table.personTable;
	
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Main class to start the PersonTable application.
 * 
 * @author Marco Jakob
 */
public class AppPersonTable extends Application {
	
	@Override	public void start(Stage primaryStage) {
		primaryStage.setTitle("Table Cell Rendering");
		
		try {
			URL res = AppPersonTable.class.getResource("PersonTable.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(res);
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            AnchorPane page = (AnchorPane) loader.load(res.openStream());
//			AnchorPane page = (AnchorPane) loader.load();
			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {		launch(args);	}
}
