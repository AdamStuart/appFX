package table.binder;


import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppBindings extends Application
{
    static final String RESOURCE = "Bindings.fxml";
    static final String CSS = "application.css";

    @Override public void start(Stage primaryStage) throws Exception
    {
        URL resource = getClass().getResource(RESOURCE);
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource(CSS).toExternalForm());

        primaryStage.setTitle("Binding Sandbox");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)    {    launch(args);    }

}
