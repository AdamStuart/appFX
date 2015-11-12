package table;

import java.util.Random;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class MyTable extends Application {
private BorderPane root;
private HBox hBox;
private TableView<MyData> tableView;    
private Button buttAddColumn, buttDeleteColumn;
private Button buttAddRow, buttDeleteRow;
private int numberOfColumns = 0;
private ObservableList<MyData> dataList = FXCollections.observableArrayList();

@Override
public void init() throws Exception {
    root = new BorderPane();
    hBox = new HBox();
    buttAddColumn = new Button("Add columns");
    buttDeleteColumn = new Button("Delete columns");
    buttAddRow = new Button("Add rows");
    buttDeleteRow = new Button("Delete rows");
    tableView = new TableView<>();

    tableView.setEditable(true);
    tableView.setItems(dataList);
    hBox.getChildren().addAll(buttAddColumn,buttDeleteColumn,buttAddRow,buttDeleteRow);

    root.setTop(hBox);
    root.setCenter(tableView);


    //----------------------------------------------------------------
    buttAddColumn.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            buttAddColumnAction(event);
        }
    });
    buttDeleteColumn.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            buttDeleteColumnAction(event);
        }
    });
    buttAddRow.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            buttAddRowAction(event);
        }
    });
    buttDeleteRow.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            buttDeleteRowAction(event);
        }
    });
}

private void buttAddColumnAction(ActionEvent event){        
    int i = numberOfColumns;// thats the key for lambda expression. Unique number for each column to access its variable;

    if(dataList.size() > 0)//resizing each data object with new variable
        for(MyData x: dataList)
            x.addNew(i);
    TableColumn<MyData, Integer> newColumn = new TableColumn<>("#" + String.valueOf(++numberOfColumns));    
    newColumn.setCellValueFactory(cellData -> cellData.getValue().getCellValue(i).asObject());
    newColumn.setCellFactory(TextFieldTableCell.<MyData, Integer>forTableColumn(new IntegerStringConverter()));

    tableView.getColumns().add(newColumn);
}
private void buttDeleteColumnAction(ActionEvent event){

}
private void buttAddRowAction(ActionEvent event){
    dataList.add(new MyData(numberOfColumns));

}
private void buttDeleteRowAction(ActionEvent event){

}

//*******************************************************************
public class MyData{ //dont forget about public because you wont get acces to properties
    private ObservableList<SimpleIntegerProperty> cellValue = FXCollections.observableArrayList();

    public MyData(int howManyColumns) {
        for(int i=0; i<howManyColumns; ++i)
           this.cellValue.add(new SimpleIntegerProperty(new Random().nextInt(10)));
    }

    public SimpleIntegerProperty getCellValue(int whichOne) {
        return cellValue.get(whichOne);
    }

    public void setCellValue(int cellValue, int whichOne) {
        this.cellValue.set(whichOne, new SimpleIntegerProperty(cellValue));
    }

    public void addNew(int numberOfNewElement){ //ads another variable for another column
        cellValue.add(new SimpleIntegerProperty(new Random().nextInt(10)));
    }
}

//*******************************************************************
@Override
public void start(Stage primaryStage) throws Exception {
    try {
        Scene scene = new Scene(root,400,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    } catch(Exception e) {
        e.printStackTrace();
    }
}
public static void main(String[] args) {
    launch(args);
}
}