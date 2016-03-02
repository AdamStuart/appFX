//Copyright 2011 Yuichi Sakuraba
//       http://www.apache.org/licenses/LICENSE-2.0

    	   
    	   package image.svg;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SVGLoaderSample extends Application {

    @Override
    public void start(Stage stage) {
        SVGContent content = SVGLoader.load(getClass().getResource("duke.svg").toString());
        
        Scene scene = new Scene(content, 1024, 768);
        
        stage.setScene(scene);
        stage.setTitle("SVGLoader Sample");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
