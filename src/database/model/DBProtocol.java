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

public class DBProtocol implements IDBTable
{
// ----------------------------------------------------
	public static final String Protocol_FORM = "Protocol";
	Region form;

	@Override public Region makeForm()
	{
		VBox pane = FormsUtil.makeFormContainer();
		HBox idBox = FormsUtil.formbox( "ID", "id", 50);
		HBox proto = FormsUtil.makeLabelFieldHBox( "Protocol", "description");
		HBox application = FormsUtil.makeLabelFieldHBox( "Application", "id");
		HBox name = FormsUtil.makeNameHBox();
		HBox emailLabel = FormsUtil.makeEmailBox();
		HBox url = FormsUtil.makeURLBox();
		pane.getChildren().addAll(idBox, proto, application, name, emailLabel, url);
		return pane;
	}	
	
	@Override public Region getForm()
	
	{
		if (form == null)
			form = makeForm();
		return form;
	}

	@Override public String getTableName()	{		return Protocol_FORM;	}

	@Override public ObservableList<String> getFieldList()
	{
		ObservableList<String> fields = FXCollections.observableArrayList();
		fields.addAll("id", "description", "firstname", "lastname", "email", "url");
		return fields;
	}


	@Override public String getSchema()
	{
		return DBUtil.createSchema(getTableName(), "description _STR", "firstname _SHORT", "lastname _SHORT", "email _SHORT", "url _STR");
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
