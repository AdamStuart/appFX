package container.publish;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppPublish extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	static final String RESOURCE = "publish.fxml";

	public AppPublish() 					{	   instance = this;	}
	public static AppPublish getInstance() 	{       return instance;	}
	public static Stage getStage() 			{       return stage;	}
	static Stage stage;
	private static AppPublish instance;
	//-----------------------------------------------------------------------------------------
	public void start(Stage primaryStage) throws Exception 
	{
		stage = primaryStage;
		doNew(primaryStage);
	}
	//-----------------------------------------------------------------------------------------
	public void doNew(Stage stage) throws Exception 
	{
        URL resource = getClass().getResource(RESOURCE);
        Scene scene = new Scene(FXMLLoader.load(resource));
        stage.setTitle("A Biological Investigation Editor");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(650);
		stage.setScene(scene);
		stage.show();
	}
}
