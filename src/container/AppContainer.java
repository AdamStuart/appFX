package container;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppContainer extends Application
{
	private static AppContainer instance;
	   static final String RESOURCE = "container.fxml";

	public AppContainer() {	           instance = this;	}
	// static method to get instance of view
	public static AppContainer getInstance() {       return instance;	}
	//-----------------------------------------------------------------------------------------
	public void start(Stage primaryStage) throws Exception 
	{
        URL resource = getClass().getResource(RESOURCE);
        Scene scene = new Scene(FXMLLoader.load(resource));
        primaryStage.setTitle("A Container of Files");
        primaryStage.setX(20);
		primaryStage.setWidth(1100);
		primaryStage.setHeight(650);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
    public static void main(String[] args)
    {
        launch(args);
    }

}
