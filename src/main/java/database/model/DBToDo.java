package database.model;

import gui.Forms;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import task.IDBTable;
import util.DBUtil;

public class DBToDo implements IDBTable
{
	static String TODO_FORM = "ToDo";
	Region form;
	// ----------------------------------------------------
	@Override public Region makeForm()
	{
		VBox pane = Forms.makeFormContainer();
		HBox line1 = Forms.makeLabelFieldHBox( "Task", "task");
		HBox line2 = Forms.makeLabelFieldHBox( "Description", "description");
		pane.getChildren().addAll(line1, line2);
		return pane;
	}
	
	@Override public Region getForm()
	{
		if (form == null)
			form = makeForm();
		return form;
	}

	@Override public String getTableName()	{		return TODO_FORM;	}

	@Override public String getSchema()
	{
		 return DBUtil.createSchema(TODO_FORM, "task _SHORT",  "description _SHORT");			//NAME_SQL, ADDRESS_SQL, 
	}

	@Override public ObservableList<String> getFieldList()
	{
		ObservableList<String> fields = FXCollections.observableArrayList();
		fields.addAll("id", "task", "description");
		return fields;
	}

	@Override public void install(ObservableMap<String, String> fields)
	{
		DBUtil.install(form, fields);
	
	}

	@Override public void extract(ObservableMap<String, String> fields)
	{
		DBUtil.extract(form, fields);
	}

}
