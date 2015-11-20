package chart.treemap;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class AppTreeMap extends Application 
{
	 public static void main(final String[] args) {     Application.launch(args); }

    @Override public  void start(Stage stage) throws Exception 
    {
    	me = this;
	    VBox vbox = new VBox(new Treemap(BudgetItem.makeBudget()));
	    Scene scene = new Scene(vbox, 1000, 800);
	    stage.setScene(scene);
	    stage.show();
	  }

 static public AppTreeMap getInstance()	{ return me;	}
 static private AppTreeMap me;
 static private Stage theStage;
 public Stage getStage() { return theStage;  }
 
 
}