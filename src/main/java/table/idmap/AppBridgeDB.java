package table.idmap;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import publish.PublishController;

public class AppBridgeDB extends Application
{
    public static void main(String[] args)    {        launch(args);    }
	static final String RESOURCE = "bridgeDBmap.fxml";
    static final String STYLE = "idmapping.css";

	public AppBridgeDB() 					{	   instance = this;	}
	public static AppBridgeDB getInstance() 	{      return instance;	}
	public static Stage getStage() 			{      return stage;	}
	static Stage stage;
	private static AppBridgeDB instance;
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
        BridgeDbController controller = (BridgeDbController) loader.getController();
		scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());
        stage.setTitle("ID Mapping with BridgeDB");
        stage.setX(20);
		stage.setWidth(800);
		stage.setHeight(650);
		controller.start();
		stage.setScene(scene);
		stage.show();
	}
}
