package database;

import java.net.URL;

import diagrams.draw.App;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppDatabase extends Application
{

    public static void main(final String[] args) {     Application.launch(args);   }
    
    static private String FXML = "/database/";

@Override public  void start(Stage stage) throws Exception {
    instance = this;
    theStage = stage;			// only used to position global dialogs
    doNew(theStage);
  }

	 static public AppDatabase getInstance()	{ return instance;	}
	 static private AppDatabase instance;
	 private Stage theStage;
	 public Stage getStage() 			{ return theStage;  }

	public void doNew(Stage stage)
	{
		if (stage == null)
			stage = new Stage();
		try 
		{
		    stage.setTitle("A Database Application");
			FXMLLoader fxmlLoader = new FXMLLoader();
		    URL url = getClass().getResource(FXML + "database.fxml");
		    fxmlLoader.setLocation(url);
		    BorderPane appPane =  fxmlLoader.load();
		    stage.setScene(new Scene(appPane, 1000, 800));
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}
 

}
