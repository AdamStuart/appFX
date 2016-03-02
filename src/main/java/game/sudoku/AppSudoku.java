package game.sudoku;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//http://www.codeproject.com/Articles/90885/Sudoku-Game-in-Java

public class AppSudoku extends Application
{
	public static void main(String[] args)    {        launch(args);    }
	
	static final String RESOURCE = "sudoku.fxml";
    static final String STYLE = "sudoku.css";
    static AppSudoku instance;
    static public AppSudoku getInstance()	{ return instance;	}
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
		scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());
        primaryStage.setTitle("Sudoku Prototype");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
