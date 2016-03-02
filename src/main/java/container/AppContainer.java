package container;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppContainer extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	private static AppContainer instance;
	static final String RESOURCE = "container.fxml";

	public AppContainer() {	           instance = this;	}
	public static AppContainer getInstance() {       return instance;	}
	//-----------------------------------------------------------------------------------------
	public void start(Stage primaryStage) throws Exception 
	{
		doNew(primaryStage);
	}
	
	//-----------------------------------------------------------------------------------------
	public void doNew(Stage stage) throws Exception 
	{
        SecurityManager sm = System.getSecurityManager();
        URL resource = getClass().getResource(RESOURCE);
        Scene scene = new Scene(FXMLLoader.load(resource));
//       Scene scene = new Scene(FXMLLoader.load(resource));
        stage.setTitle("A Container of Files");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(650);
		stage.setScene(scene);
		stage.show();
	}

}
