package game.iching;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppIChing extends Application
{
	public static void main(String[] args)    {        launch(args);    }
	
	static final String RESOURCE = "iching.fxml";
//    static final String STYLE = "editor.css";
    static AppIChing instance;
    static public AppIChing getInstance()	{ return instance;	}
    Stage stage;
    public Stage getStage()	{ return stage;	}
    
    @Override
    public void start(Stage primaryStage) throws Exception
    {
    	instance = this;
    	stage = primaryStage;
         URL resource = getClass().getResource(RESOURCE);
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        primaryStage.setTitle("I Ching Prototype");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
