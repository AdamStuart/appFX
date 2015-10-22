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

public class DBPerson implements IDBTable
{
	static String PERSON_FORM = "DBPerson";
//	static String NAME_SQL = "first _SHORT\n last _SHORT\n";
	static String FIRSTNAME_SQL = "First _SHORT";
	static String LASTNAME_SQL =  "Last _SHORT";
	static String ADDRESS1_SQL = "Addr1 _STR";
	static String ADDRESS2_SQL = "Addr2 _STR";
	static String ADDRESS_CITY_SQL = "City _SHORT";
	static String ADDRESS_STATE_SQL = "State _SHORT";
	static String ADDRESS_ZIP_SQL = "Zip _SHORT";
	static String ADDRESS_COUNTRY_SQL = "Country _SHORT";
	Region form;
	
	public Region makeForm()
	{
		VBox pane = FormsUtil.makeFormContainer();
		HBox idBox = FormsUtil.formbox( "ID", "id", 50);
		HBox line1 = FormsUtil.makeNameHBox();
		VBox line2 = FormsUtil.makeAddressVBox( 400, true);		// include country
		HBox line3 = FormsUtil.makeEmailBox();
		pane.getChildren().addAll(idBox, line1, line3, line2);
		return pane;
	
	}
	
	@Override public Region getForm()
	{
		if (form == null)
			form = makeForm();
		return form;
	}

	@Override public String getTableName()	{		return PERSON_FORM;	}

	@Override public String getSchema()
	{
		 return DBUtil.createSchema(getTableName(), FIRSTNAME_SQL, LASTNAME_SQL, ADDRESS1_SQL, ADDRESS2_SQL, 
						 ADDRESS_CITY_SQL, ADDRESS_STATE_SQL, ADDRESS_COUNTRY_SQL, ADDRESS_ZIP_SQL, "email _STR");
	}

	@Override public ObservableList<String> getFieldList()
	{
		ObservableList<String> fields = FXCollections.observableArrayList();
		fields.addAll("id", "First", "Last", "Addr1", "Addr2", "City", "State", "Zip", "Country");
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
