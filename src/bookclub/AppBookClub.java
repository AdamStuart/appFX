package bookclub;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppBookClub extends Application
{
    public static void main(String[] args)    {        launch(args);    }
    static final String RESOURCE = "bookclub.fxml";
//    static final String STYLE = "editor.css";
    static AppBookClub instance;
    public Stage getStage()	{ return stage;	}
    Stage stage;
    
    @Override  public void start(Stage primaryStage) throws Exception
    {
    	instance = this;
    	stage = primaryStage;
         URL resource = getClass().getResource(RESOURCE);
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
//		scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());


        primaryStage.setTitle("Book Club Prototype");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    static public AppBookClub getInstance()	{ return instance;	}


}
