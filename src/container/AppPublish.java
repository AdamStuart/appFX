package container;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppPublish extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	private static AppPublish instance;
	static final String RESOURCE = "publish.fxml";

	public AppPublish() {	           instance = this;	}
	public static AppPublish getInstance() {       return instance;	}
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
        stage.setTitle("A Biology Article Editor");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(650);
		stage.setScene(scene);
		stage.show();
	}
}
