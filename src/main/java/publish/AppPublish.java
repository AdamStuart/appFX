package publish;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppPublish extends Application
{
    public static void main(String[] args)    {        launch(args);    }
//	static final String RESOURCE = "publisher.fxml";
	static final String RESOURCE = "apms.fxml";
    static final String STYLE = "publish.css";

	public AppPublish() 					{	   instance = this;	}
	public static AppPublish getInstance() 	{      return instance;	}
	public static Stage getStage() 			{      return stage;	}
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
        FXMLLoader loader = new FXMLLoader(resource);
        Scene scene = new Scene(loader.load());
        PublishController controller = (PublishController) loader.getController();
		scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());
        stage.setTitle("Affinity Purification Mass Spectometry");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(650);
		controller.start();
		stage.setScene(scene);
		stage.show();
	}
}
