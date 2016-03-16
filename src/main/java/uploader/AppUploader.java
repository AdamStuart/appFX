package uploader;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppUploader extends Application
{

	   public static void main(String[] args)    {        launch(args);    }
		private static AppUploader instance;
		static final String RESOURCE = "uploader.fxml";

		public AppUploader() {	   instance = this;	}
		public static AppUploader getInstance() {       return instance;	}
		//-----------------------------------------------------------------------------------------
		public void start(Stage primaryStage) throws Exception 
		{
			doNew(primaryStage);
		}
		
		//-----------------------------------------------------------------------------------------
		public void doNew(Stage stage) throws Exception 
		{
	        URL resource = getClass().getResource(RESOURCE);
	        Scene scene = new Scene(FXMLLoader.load(resource));
	        stage.setTitle("Uploader");
	        stage.setX(800);
			stage.setWidth(400);
			stage.setHeight(350);
			stage.setScene(scene);
			stage.show();
		}


}
