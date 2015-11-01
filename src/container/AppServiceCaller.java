package container;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppServiceCaller extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	private static AppServiceCaller instance;
	static final String RESOURCE = "serviceCaller.fxml";

	public AppServiceCaller() 						{	    instance = this;	}
	public static AppServiceCaller getInstance() 	{       return instance;	}
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
        stage.setTitle("A Collection of External APIs");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(650);
		stage.setScene(scene);
		stage.show();
	}

}
