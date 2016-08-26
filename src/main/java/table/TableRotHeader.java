package table;

import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//http://stackoverflow.com/questions/27666087/vertical-column-text-in-tableview

public class TableRotHeader extends Application {

@Override
public void start(Stage stage) throws IOException {
    Group group = new Group();
    Scene scene = new Scene(group);
    scene.getStylesheets().add("com/sun/javafx/scene/control/skin/modena/blackOnWhite.css");

    stage.setTitle("Table with rotated header");
    stage.setWidth(800);
    stage.setHeight(600);

    TableView tableView = new TableView();
    TableColumn colA = new TableColumn("horizontal\ncol header");
    TableColumn colB = new TableColumn("");
    final int minWidth = 50;
    colA.setMinWidth(minWidth);
    colB.setMinWidth(minWidth);

    Label label1 = new Label("col 1");
    Label label2 = new Label("col number 2 with a long text");
    label2.setStyle("-fx-font-weight: normal");

    VBox vbox = new VBox(label1, label2);
    vbox.setRotate(-90);
    vbox.setPadding(new Insets(5, 5, 5, 5));

    Group g = new Group(vbox);
    colB.setGraphic(g);

    tableView.getColumns().addAll(colA, colB);

    group.getChildren().add(tableView);
    tableView.prefWidthProperty().bind(scene.widthProperty());
    tableView.prefHeightProperty().bind(scene.heightProperty());
    stage.setScene(scene);
    stage.show();
}

public static void main(String[] args) {
    launch(args);
}
}