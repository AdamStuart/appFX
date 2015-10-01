package table.slingshot;
	
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class AppSlingshot extends Application {
		
	SlingshotController controller = null;
	@Override
	public void start(Stage primaryStage) {
		try {
	        Pane rootPane = null;
	        try {
	        	URL resURL = getClass().getResource("Slingshot.fxml");
	            if (resURL == null)  
	            	System.out.println("getResource failed");
	            FXMLLoader loader = new FXMLLoader();
	            loader.setLocation(resURL);
	            loader.setBuilderFactory(new JavaFXBuilderFactory());
	            rootPane = loader.load(resURL.openStream());
	            controller = loader.getController();
	            controller.setStage(primaryStage);
	        } catch (IOException ex) {
	            rootPane = new BorderPane();
	            Label l = new Label("Error on FXML loading:" + ex.getMessage());
	            rootPane.getChildren().add(l);
	            Logger.getLogger(AppSlingshot.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        instance = this;
			Scene scene = new Scene(rootPane,900,400);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override public void stop()
	{
		controller.save();
	}
	static AppSlingshot instance;
	public static AppSlingshot getApp()	{ return instance;	}
	public static void main(String[] args) {
		launch(args);
	}
}
