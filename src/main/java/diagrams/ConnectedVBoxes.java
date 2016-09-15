package diagrams;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class ConnectedVBoxes extends Application {

    @Override
    public void start(Stage primaryStage) {

        Group root = new Group();
        Scene scene = new Scene(root);

        Label  label1  = new Label("Info \n 1");
        Label  label2  = new Label("Info 2");

        VBox vbox1 = new VBox(5);
        vbox1.setMaxWidth(80);
        vbox1.getChildren().add(label1);
        vbox1.getChildren().add(label2);

        Label  label3  = new Label("Info \n 3");
        Label  label4  = new Label("Info 4");
//        label4.setStyle("-fx-border-color: black;-fx-padding: 10px;");
//        label4.setWrapText(true);

        VBox vbox2 = new VBox(5);
        vbox2.setMaxWidth(50);
        vbox2.getChildren().add(label3);
        vbox2.getChildren().add(label4);

        HBox hbox = new HBox(100);
        hbox.getChildren().addAll(vbox1, vbox2);


        Line line = new Line();   
        line.setStrokeWidth(5); 
        line.setStroke(Color.BLACK); 

        Pane stack = new Pane();
        stack.getChildren().addAll(hbox, line);

        ObjectBinding<Bounds> label1InStack = Bindings.createObjectBinding(() -> {
            Bounds label1InScene = label1.localToScene(label1.getBoundsInLocal());
            return stack.sceneToLocal(label1InScene);
        }, label1.boundsInLocalProperty(), label1.localToSceneTransformProperty(), stack.localToSceneTransformProperty());

        ObjectBinding<Bounds> label3InStack = Bindings.createObjectBinding(() -> {
            Bounds label3InScene = label3.localToScene(label3.getBoundsInLocal());
            return stack.sceneToLocal(label3InScene);
        }, label3.boundsInLocalProperty(), label3.localToSceneTransformProperty(), stack.localToSceneTransformProperty());

    //doublePropertyOne.bind(Bindings.createDoubleBinding(() -> Math.exp(doublePropertyTwo.get()), doublePropertyTwo));    
        
    //    DoubleBinding minXBinding = Bindings.selectDouble(node.boundsInParentProperty(), "minX");
    
        DoubleBinding startX = Bindings.createDoubleBinding(() -> label1InStack.get().getMaxX(), label1InStack);
        DoubleBinding startY = Bindings.createDoubleBinding(() -> {
            Bounds b = label1InStack.get();
            return b.getMinY() + b.getHeight() / 2 ;
        }, label1InStack);

        DoubleBinding endX = Bindings.createDoubleBinding(() -> label3InStack.get().getMinX(), label3InStack);
        DoubleBinding endY = Bindings.createDoubleBinding(() -> {
            Bounds b = label3InStack.get();
            return b.getMinY() + b.getHeight() / 2 ;
        }, label3InStack);

        line.startXProperty().bind(startX);
        line.startYProperty().bind(startY);
        line.endXProperty().bind(endX);
        line.endYProperty().bind(endY);


        root.getChildren().addAll(stack);
        primaryStage.setScene(scene);
        primaryStage.show();    
    }

    public static void main(String[] args) {
        launch(args);
    }
}