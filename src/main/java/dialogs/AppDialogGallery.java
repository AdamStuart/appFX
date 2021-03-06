package dialogs;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class AppDialogGallery extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }
    private String FXML = "";
    @Override public void start(Stage stage) throws Exception 
    {
    	me = this;
	    FXMLLoader fxmlLoader = new FXMLLoader();
	    URL url = getClass().getResource(FXML + "/dialogs/DialogGallery.fxml");
//	    URL url = getClass().getClassLoader().getResource("DialogGallery.fxml");
	   
	    
	    if (url != null)
	    {
	    	try
	    	{
	    		fxmlLoader.setLocation(url);
	    	    AnchorPane appPane = fxmlLoader.load();
	    	    Scene scene = new Scene(appPane, 1000, 800);
	    	    stage.setScene(scene);
	    	    stage.show();
	    	}
	    	catch (SecurityException ex)	{
	    		System.out.println("SecurityException");
	    	}
	    }
	    else System.exit(1);
  }

 static public AppDialogGallery getInstance()	{ return me;	}
 static private AppDialogGallery me;
 static private Stage theStage;
 
 public Stage getStage() { return theStage;  }
     

}
