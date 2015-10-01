package chart.flexiPie;
	
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane root;
			  String name = "chart/flexiPie/FlexiPie.fxml";
			  URL res = getClass().getClassLoader().getResource(name);
			  root = FXMLLoader.load(res );
			 
			Scene scene = new Scene(root,900,600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setX(50);
			primaryStage.setY(10);
			primaryStage.show();
		} 
		catch(Exception e) {			e.printStackTrace();	}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
