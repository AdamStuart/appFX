package plate;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AppPlato extends Application
{

    public static void main(final String[] args) {     Application.launch(args);   }
    
    static private String FXML = "";

@Override public  void start(Stage stage) throws Exception {
    instance = this;
    theStage = stage;			//  used to position global dialogs
    doNew(theStage);
  }

	 static public AppPlato getInstance()	{ return instance;	}
	 static private AppPlato instance;
	 private Stage theStage;
	 public Stage getStage() 			{ return theStage;  }

	public void doNew(Stage stage)
	{
		if (stage == null)
			stage = new Stage();
		try 
		{
		    stage.setTitle("Plato: A plate based attribute editor");
		    
//		    https://www.partiallyexaminedlife.com/wp-content/uploads/Plato1.jpg
// embed picture		    	
		    	
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = FXML + "PlateDesigner.fxml";
		    URL url = getClass().getResource(fullname);
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    VBox appPane =  fxmlLoader.load();
		    stage.setScene(new Scene(appPane, 1000, 800));
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}
 

}
