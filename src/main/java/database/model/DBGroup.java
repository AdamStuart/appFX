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

public class DBGroup implements IDBTable
{
	// ----------------------------------------------------
	static String GROUP_FORM = "DBGroup";
	Region form;

	@Override public Region makeForm()
	{
		VBox pane = Forms.makeFormContainer();
		HBox idBox = Forms.formbox( "ID", "id", 50);
		HBox groupname = Forms.makeLabelFieldHBox("Group", "groupname");
		HBox institution = Forms.makeLabelFieldHBox("Institution", "institution");
		HBox groupid = Forms.makeLabelFieldHBox("Group Id", "groupid");
		HBox url = Forms.makeURLBox();
		HBox email = Forms.makeEmailBox();
		HBox contact = Forms.makeNameHBox();
		pane.getChildren().addAll(idBox, groupname, institution, groupid, url, email, contact);
		return pane;
	}
	@Override public Region getForm()
	{
		if (form == null)
			form = makeForm();
		return form;
	}

	@Override public String getTableName()	{		return GROUP_FORM;	}

	@Override public String getSchema()
	{

		return DBUtil.createSchema(GROUP_FORM, 
						"groupname _STR", 
						"institution _STR", 
						"groupid _SHORT", 
						"email _STR", 
						"url _STR", 
						"first _SHORT", 
						"last _SHORT");
	}

	@Override public ObservableList<String> getFieldList()
	{
		ObservableList<String> fields = FXCollections.observableArrayList();
		fields.addAll("id", "groupname", "institution", "groupid", "email", "url", "first", "last");
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
