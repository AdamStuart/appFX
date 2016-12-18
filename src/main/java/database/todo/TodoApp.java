package database.todo;

import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

//http://www.javacodegeeks.com/2015/01/javafx-list-example.html
public class TodoApp extends Application
{

	private ListView<Todo> listView;
	private ObservableList<Todo> data;
	private TextField nametxt;
	private TextArea desctxt;
	private Text actionstatus;

	private TodoDataAccess dbaccess;

	public static void main(String[] args)
	{

		Application.launch(args);
	}

	@Override public void init()
	{

		try
		{
			dbaccess = new TodoDataAccess();
		} catch (Exception e)
		{
			displayException(e);
		}
	}

	@Override public void stop()
	{

		try
		{
			dbaccess.closeDb();
		} catch (Exception e)
		{
			displayException(e);
		}
	}

	@Override public void start(Stage primaryStage)
	{

		primaryStage.setTitle("Todo App - version 3 (final)");

		// gridPane layout

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(15);
		grid.setVgap(20);
		grid.setPadding(new Insets(25, 25, 25, 25));

		// list view, listener and list data

		listView = new ListView<>();
		listView.getSelectionModel().selectedIndexProperty().addListener(new ListSelectChangeListener());
		data = getDbData();
		listView.setItems(data);
		grid.add(listView, 1, 1); // col = 1, row = 1

		// todo name label and text fld - in a hbox

		Label namelbl = new Label("Todo Name:");
		nametxt = new TextField();
		nametxt.setMinHeight(30.0);
		nametxt.setPromptText("Enter todo name (required).");
		nametxt.setPrefColumnCount(20);
		nametxt.setTooltip(new Tooltip("Todo name (5 to 50 chars length)"));
		HBox hbox = new HBox();
		hbox.setSpacing(10);
		hbox.getChildren().addAll(namelbl, nametxt);

		// todo desc text area in a scrollpane

		desctxt = new TextArea();
		desctxt.setPromptText("Enter description (optional).");
		desctxt.setWrapText(true);
		ScrollPane sp = new ScrollPane();
		sp.setContent(desctxt);
		sp.setFitToWidth(true);
		sp.setFitToHeight(true);
		sp.setPrefHeight(300);
		sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		// todo hbox (label + text fld), scrollpane - in a vbox

		VBox vbox = new VBox();
		vbox.setSpacing(10);
		vbox.getChildren().addAll(hbox, sp);

		grid.add(vbox, 2, 1); // col = 2, row = 1

		// new and delete buttons

		Button newbtn = new Button("New");
		newbtn.setOnAction(new NewButtonListener());
		Button delbtn = new Button("Delete");
		delbtn.setOnAction(new DeleteButtonListener());
		HBox hbox2 = new HBox(10);
		hbox2.getChildren().addAll(newbtn, delbtn);
		grid.add(hbox2, 1, 2); // col = 1, row = 2

		// save button to the right anchor pane and grid

		Button savebtn = new Button("Save");
		savebtn.setOnAction(new SaveButtonListener());
		AnchorPane anchor = new AnchorPane();
		AnchorPane.setRightAnchor(savebtn, 0.0);
		anchor.getChildren().add(savebtn);
		grid.add(anchor, 2, 2); // col = 2, row = 2

		// action message (status) text

		actionstatus = new Text();
		actionstatus.setFill(Color.FIREBRICK);
		actionstatus.setText("");
		grid.add(actionstatus, 1, 3); // col = 1, row = 3

		// scene

		Scene scene = new Scene(grid, 750, 400); // width = 750, height = 400
		primaryStage.setScene(scene);
		primaryStage.show();

		// initial selection
		listView.getSelectionModel().selectFirst(); // does nothing if no data

	} // start()

	private class ListSelectChangeListener implements ChangeListener<Number>
	{

		@Override public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
		{

			if ((new_val.intValue() < 0) || (new_val.intValue() >= data.size()))
			{

				return; // invalid data
			}

			// set name and desc fields for the selected todo
			Todo todo = data.get(new_val.intValue());
			nametxt.setText(todo.getName());
			desctxt.setText(todo.getDesc());
			actionstatus.setText(todo.getName() + " - selected");
		}
	}

	private ObservableList<Todo> getDbData()
	{

		List<Todo> list = null;

		try
		{
			list = dbaccess.getAllRows();
		} catch (Exception e)
		{

			displayException(e);
		}

		ObservableList<Todo> dbData = FXCollections.observableList(list);
		return dbData;
	}

	private class NewButtonListener implements EventHandler<ActionEvent>
	{

		@Override public void handle(ActionEvent e)
		{

			actionstatus.setText("New");

			// creates a todo at first row with name NEW todo and selects it
			Todo todo = new Todo(0, "NEW Todo", ""); // 0 = dummy id
			int ix = 0;
			data.add(ix, todo);
			listView.getSelectionModel().clearAndSelect(ix);
			nametxt.clear();
			desctxt.clear();
			nametxt.setText("NEW Todo");
			nametxt.requestFocus();
		}
	}

	private class SaveButtonListener implements EventHandler<ActionEvent>
	{

		@Override public void handle(ActionEvent ae)
		{

			int ix = listView.getSelectionModel().getSelectedIndex();

			if (ix < 0)
			{ // no data selected or no data

				return;
			}

			String s1 = nametxt.getText();
			String s2 = desctxt.getText();

			// validate name

			if ((s1.length() < 5) || (s1.length() > 50))
			{

				actionstatus.setText("Name must be 5 to 50 characters in length");
				nametxt.requestFocus();
				nametxt.selectAll();
				return;
			}

			// check if name is unique

			Todo todo = data.get(ix);
			todo.setName(s1);
			todo.setDesc(s2);

			if (isNameAlreadyInDb(todo))
			{

				actionstatus.setText("Name must be unique!");
				nametxt.requestFocus();
				return;
			}

			if (todo.getId() == 0)
			{ // insert in db (new todo)

				int id = 0;

				try
				{
					id = dbaccess.insertRow(todo);
				} catch (Exception e)
				{

					displayException(e);
				}

				todo.setId(id);
				data.set(ix, todo);
				actionstatus.setText("Saved (inserted)");
			} else
			{ // db update (existing todo)

				try
				{
					dbaccess.updateRow(todo);
				} catch (Exception e)
				{

					displayException(e);
				}

				actionstatus.setText("Saved (updated)");

			} // end-if, insert or update in db

			// update list view with todo name, and select it
			data.set(ix, null); // required for refresh
			data.set(ix, todo);
			listView.getSelectionModel().clearAndSelect(ix);
			listView.requestFocus();
		}
	}

	private boolean isNameAlreadyInDb(Todo todo)
	{

		boolean bool = false;

		try
		{
			bool = dbaccess.nameExists(todo);
		} catch (Exception e)
		{

			displayException(e);
		}

		return bool;
	}

	private class DeleteButtonListener implements EventHandler<ActionEvent>
	{

		@Override public void handle(ActionEvent ae)
		{

			int ix = listView.getSelectionModel().getSelectedIndex();

			if (ix < 0)
			{ // no data or none selected

				return;
			}

			Todo todo = data.remove(ix);

			try
			{
				dbaccess.deleteRow(todo);
			} catch (Exception e)
			{

				displayException(e);
			}

			actionstatus.setText("Deleted");

			// set next todo item after delete

			if (data.size() == 0)
			{

				nametxt.clear();
				desctxt.clear();
				return; // no selection
			}

			ix = ix - 1;

			if (ix < 0)
			{

				ix = 0;
			}

			listView.getSelectionModel().clearAndSelect(ix);

			// selected ix data (not set by list listener);
			// requires this is set
			Todo itemSelected = data.get(ix);
			nametxt.setText(itemSelected.getName());
			desctxt.setText(itemSelected.getDesc());
			listView.requestFocus();
		}
	}

	private void displayException(Exception e)
	{

		System.out.println("###### Exception ######");
		e.printStackTrace();
		System.exit(0);
	}
}
