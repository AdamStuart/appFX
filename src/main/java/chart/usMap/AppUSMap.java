package chart.usMap;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class AppUSMap extends Application 
{
    

    public static void main(final String[] args) {
        Application.launch(args);
    }
    static private String FXML = "";

    @Override public 
  void start(Stage stage) throws Exception {
    FXMLLoader fxmlLoader = new FXMLLoader();
    URL url = getClass().getResource(FXML + "us-map.fxml");
    fxmlLoader.setLocation(url);
    AnchorPane appPane = fxmlLoader.load();
    
    Scene scene = new Scene(appPane, 1000, 800);
    stage.setScene(scene);
    stage.show();
  }

 static public AppUSMap getInstance()	{ return me;	}
 static private AppUSMap me;
 static private Stage theStage;
 
 public Stage getStage() { return theStage;  }
     
}