package database.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import task.IDBTable;
import util.DBUtil;
import util.FormsUtil;

public class DBEvent implements IDBTable
{
	static String EVENT_FORM = "DBEvent";
	Region form;
	// ----------------------------------------------------

	@Override public Region getForm()
	{
		if (form == null)
		{
			VBox pane = FormsUtil.makeFormContainer();
			HBox idBox = FormsUtil.formbox( "ID", "id", 50);
			HBox line1 = FormsUtil.makeLabelFieldHBox( "", "Event", "event");
			HBox line2 = FormsUtil.makeTimeDateDurationBox("", "",  true, true, true);
			HBox line3 = FormsUtil.makeURLBox("");
			pane.getChildren().addAll(idBox, line1, line2, line3);
			form = pane;
		}
		return form;
	}

	@Override public String getTableName()	{		return EVENT_FORM;	}

	@Override public String getSchema()
	{
		 return DBUtil.createSchema(EVENT_FORM, 
						 "event _SHORT", 
						 "date _SHORT", 
						 "time _SHORT", 
						 "duration _SHORT", 
						 "url _SHORT");		
	}

	@Override public ObservableList<String> getFieldList()
	{
		ObservableList<String> fields = FXCollections.observableArrayList();
		fields.addAll("id", "event", "date", "time", "duration", "url");
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
