package table;


import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * @web http://java-buddy.blogspot.com/
 */
public class TestTableView extends Application {

	public static class Record {

		private final SimpleIntegerProperty id;
		private final SimpleStringProperty name;
		private final SimpleStringProperty lastName;
		private final SimpleStringProperty email;

		private Record(int id, String name, String lastName, String email) {
			this.id = new SimpleIntegerProperty(id);
			this.name = new SimpleStringProperty(name);
			this.lastName = new SimpleStringProperty(lastName);
			this.email = new SimpleStringProperty(email);
		}

		public int getId() {
			return this.id.get();
		}

		public void setId(int id) {
			this.id.set(id);
		}

		public String getName() {
			return this.name.get();
		}

		public void setName(String name) {
			this.name.set(name);
		}

		public String getLastName() {
			return this.lastName.get();
		}

		public void setLastName(String lastName) {
			this.lastName.set(lastName);
		}

		public String getEmail() {
			return this.email.get();
		}

		public void setEmail(String email) {
			this.email.set(email);
		}
	}

	private TableView<Record> tableView = new TableView<>();
	private final ObservableList<Record> recordList = FXCollections.observableArrayList();

	private void prepareRecordList() {
		recordList.add(new Record(12, "William", "Austin", "xxx@xxx.xxx"));
		recordList.add(new Record(15, "Chris", "redfield", "yyy@yyy.yyy"));
		recordList.add(new Record(1, "Java", "Buddy", "javabuddy@abc.yyy"));
		recordList.add(new Record(2, "Eric", "Buddy", "ericbuddy@abc.yyy"));
		recordList.add(new Record(3, "Peter", "handsome", "peter@abc.yyy"));
	}

	@Override
	public void start(Stage primaryStage) {
		Scene scene = new Scene(new Group());
		primaryStage.setTitle("http://java-buddy.blogspot.com/");
		primaryStage.setWidth(400);
		primaryStage.setHeight(400);

		prepareRecordList();

		tableView.setEditable(false);

		Callback<TableColumn, TableCell> integerCellFactory = new Callback<TableColumn, TableCell>() {
			@Override
			public TableCell call(TableColumn p) {
				MyIntegerTableCell cell = new MyIntegerTableCell();
				cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
				return cell;
			}
		};

		Callback<TableColumn, TableCell> stringCellFactory = new Callback<TableColumn, TableCell>() {
			@Override
			public TableCell call(TableColumn p) {
				MyStringTableCell cell = new MyStringTableCell();
				cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
				return cell;
			}
		};

		TableColumn colId = new TableColumn("ID");
		colId.setCellValueFactory(new PropertyValueFactory<Record, String>("id"));
		colId.setCellFactory(integerCellFactory);

		TableColumn colName = new TableColumn("Name");
		colName.setCellValueFactory(new PropertyValueFactory<Record, String>("name"));
		colName.setCellFactory(stringCellFactory);

		TableColumn colLastName = new TableColumn("Last Name");
		colLastName.setCellValueFactory(new PropertyValueFactory<Record, String>("lastName"));
		colLastName.setCellFactory(stringCellFactory);

		TableColumn colEmail = new TableColumn("Email");
		colEmail.setCellValueFactory(new PropertyValueFactory<Record, String>("email"));
		colEmail.setCellFactory(stringCellFactory);

		tableView.setItems(recordList);
		tableView.getColumns().addAll(colId, colName, colLastName, colEmail);

		tableView.getStylesheets().add("top/aa.css");

		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().add(tableView);

		((Group) scene.getRoot()).getChildren().addAll(vbox);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	class MyIntegerTableCell extends TableCell<Record, Integer> {

		@Override
		public void updateItem(Integer item, boolean empty) {
			super.updateItem(item, empty);
			setText(empty ? null : getString());
			setGraphic(null);
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	class MyStringTableCell extends TableCell<Record, String> {

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			setText(empty ? null : getString());
			setGraphic(null);
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	class MyEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent t) {
			TableCell c = (TableCell) t.getSource();
			int index = c.getIndex();

			try {
				Record item = recordList.get(index);
				System.out.println("id = " + item.getId());
				System.out.println("name = " + item.getName());
				System.out.println("lastName = " + item.getLastName());
				System.out.println("email = " + item.getEmail());
			} catch (IndexOutOfBoundsException exception) {
				// ...
			}

		}
	}
}
