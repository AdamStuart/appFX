package table.treetable.fitlered;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

@SuppressWarnings("all")
public class TreeTableViewFilterDemo extends Application {

    private TreeItem<Map<String, Object>> root;
    private TreeTableView<Map<String, Object>> tree;


    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox outer = new VBox();

        TextField filter = new TextField();
        filter.textProperty().addListener((observable, oldValue, newValue) -> filterChanged(newValue));

        root = new TreeItem<>();
        tree = new TreeTableView<>(root);
        addColumn("Region", "region");
        addColumn("Type", "type");
        addColumn("Pop.", "population");
        setup();
        tree.setShowRoot(false);

        outer.getChildren().addAll(filter, tree);
        Scene scene = new Scene(outer, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void filterChanged(String filter) {
        if (filter.isEmpty()) {
            tree.setRoot(root);         
        }
        else {
            TreeItem<Map<String, Object>> filteredRoot = new TreeItem<>();
            filter(root, filter, filteredRoot);
            tree.setRoot(filteredRoot);
        }
    }


    private void filter(TreeItem<Map<String, Object>> root, String filter, TreeItem<Map<String, Object>> filteredRoot) {
        for (TreeItem<Map<String, Object>> child : root.getChildren()) {            
            TreeItem<Map<String, Object>> filteredChild = new TreeItem<>();
            filteredChild.setValue(child.getValue());
            filteredChild.setExpanded(true);
            filter(child, filter, filteredChild );
            if (!filteredChild.getChildren().isEmpty() || isMatch(filteredChild.getValue(), filter)) {
                System.out.println(filteredChild.getValue() + " matches.");
                filteredRoot.getChildren().add(filteredChild);
            }
        }
    }

    private boolean isMatch(Map<String, Object> value, String filter) {
        return value.values().stream().anyMatch((Object o) -> o.toString().contains(filter));
    }

    private void setup() {
        TreeItem<Map<String, Object>> europe = createItem(root, "Europe", "continent", 742500000L);
        createItem(europe, "Germany", "country", 80620000L);
        TreeItem<Map<String, Object>> austria = createItem(europe, "Austria", "country", 847400L);
        createItem(austria, "Tyrol", "state", 728537L);     
        TreeItem<Map<String, Object>> america = createItem(root, "America", "continent", 953700000L);
        createItem(america, "USA", "country", 318900000L);
        createItem(america, "Mexico", "country", 122300000L);       
    }

    private TreeItem<Map<String, Object>> createItem(TreeItem<Map<String, Object>> parent, String region, String type, long population) {
        TreeItem<Map<String, Object>> item = new TreeItem<>();
        Map<String, Object> value = new HashMap<>();
        value.put("region",  region);
        value.put("type", type);
        value.put("population", population);
        item.setValue(value);
        parent.getChildren().add(item);
        item.setExpanded(true);
        return item;
    }

    protected void addColumn(String label, String dataIndex) {
        TreeTableColumn<Map<String, Object>, String> column = new TreeTableColumn<>(label);
        column.setPrefWidth(150);
        column.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<Map<String, Object>, String> param) -> {
                ObservableValue<String> result = new ReadOnlyStringWrapper("");
                if (param.getValue().getValue() != null) {
                    result = new ReadOnlyStringWrapper("" + param.getValue().getValue().get(dataIndex));
                }
                return result;
            }
        );      
        tree.getColumns().add(column);
    }


    public static void main(String[] args) {
        launch(args);
    }

}