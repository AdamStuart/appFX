package table.networkTable;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import table.slingshot.AppSlingshot;

public class AppNetworkTable extends Application
{
    public static void main(String[] args) {        launch(args); }

    private NetworkTableController controller;

	private static final String VIEW_DEF = "networkTable.fxml";
	private static final String STYLE_DEF = "application.css";

	@Override
	public void start(Stage primaryStage) {
		try {
	        Pane rootPane = null;
	        try {
	        	URL resURL = getClass().getResource(VIEW_DEF);
	            if (resURL == null)  
	            	System.out.println("getResource failed");
	            FXMLLoader loader = new FXMLLoader();
	            loader.setLocation(resURL);
	            loader.setBuilderFactory(new JavaFXBuilderFactory());
	            rootPane = loader.load(resURL.openStream());
	            controller = loader.getController();
	            controller.setStage(primaryStage);
	        } catch (IOException ex) {
	            rootPane = new BorderPane();
	            Label l = new Label("Error on FXML loading:" + ex.getMessage());
	            rootPane.getChildren().add(l);
	            Logger.getLogger(AppSlingshot.class.getName()).log(Level.SEVERE, null, ex);
	        }
			Scene scene = new Scene(rootPane,900,400);

			scene.getStylesheets().add(getClass().getResource(STYLE_DEF).toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

}
