/*
 * @(#)ValidationDemo.java 5/19/2013
 *
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */

package database.forms;

import java.util.Locale;
import java.util.prefs.Preferences;

import database.model.DBCitation;
import database.model.DBEvent;
import database.model.DBGroup;
import database.model.DBPerson;
import database.model.DBProtocol;
import gui.Borders;
import gui.Forms;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import task.DatabaseAccess;
import task.IDBTable;
import util.DBUtil;

//------------------------------------------------------------------------------------
//  originates from a JideFXDemo 
/*
 * Make a bunch of tabs with structured data types
 * Be able to create or edit records for event, person, citation, experiment, etc.
 * This intends to show how to validate input, either on keystroke or focus-lost events.
 */
public class AppForms extends Application {

    public static void main(String[] args) {       launch(args);    }

// This application has no controller and no FXML file
// We build a bunch of tabs that contain forms and the forms handle 

    @Override
    public void start(Stage stage) {
        String stylesheet = Preferences.userRoot().get("AppForms.UserAgentStylesheet", STYLESHEET_MODENA);
        setUserAgentStylesheet(stylesheet);

        String locale = Preferences.userRoot().get("AppForms.Locale", Locale.getDefault().toLanguageTag());
        Locale.setDefault(Locale.forLanguageTag(locale));
        System.out.println("Setting locale to " + Locale.getDefault().toString());

        Region demoPanel = getTabPanel();
        HBox.setHgrow(demoPanel, Priority.ALWAYS);
        Scene scene = new Scene(demoPanel);

        stage.setTitle("AppForms");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(true);
        stage.show();
// interesting debug method to get the new focus whenever it changes:
//        scene.focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
////            if (isTraceFocus()) {
//                System.out.println(newValue);
////            }
//        });
    }
    //------------------------------------------------------------------------------------
    private TabPane tabPane;
    private DBPerson personDB;
    private DBEvent eventDB;
    private DBProtocol protocolDB;
    private DBCitation citationDB;
    private DBGroup groupDB;
 
    public IDBTable getDBTable(String name)
    {
	   	if ("Events".equals(name))		return eventDB;
	   	if ("Citations".equals(name))	return citationDB;
	   	if ("Protocols".equals(name))	return protocolDB;
	   	if ("Persons".equals(name))		return personDB;
	   	if ("Groups".equals(name))		return groupDB;
	   	return null;
    }
 
    //------------------------------------------------------------------------------------
   public Region getTabPanel() {
        tabPane = new TabPane();
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);

        Tab validator = new Tab("Various Validators",FormsGallery.createValidationForm());
        Tab signup = new Tab("Signup Validation",FormsGallery.createSignUpForm() );
        Tab multi = new Tab("Multiple Instances", Forms.createMultipleInstanceForm("x"));

        personDB = new DBPerson();
        eventDB = new DBEvent();
        protocolDB = new DBProtocol();
        citationDB = new DBCitation();
        groupDB = new DBGroup();
        
        Tab person = new Tab("Persons", personDB.getForm());
        Tab group = new Tab("Groups", groupDB.getForm());
        Tab protocol = new Tab("Protocols", protocolDB.getForm());
        Tab events = new Tab("Events", eventDB.getForm());
        Tab citations = new Tab("Citations", citationDB.getForm());
        
        Tab invoice = new Tab("Invoice", InvoiceForm.createInvoiceForm());
        Tab cyto = new Tab("Cytometry", FormsGallery.createCytometryMLform("CytometryML"));
        Tab experiment = new Tab("Experiment", FormsGallery.createExperimentForm("Experiment"));
       
        tabPane.getTabs().addAll(validator, signup, multi, person, group, events, citations, invoice, protocol, experiment, cyto);
        tabPane.getStylesheets().add(AppForms.class.getResource("Validation.css").toExternalForm());

      	Button button = new Button("CRUD");
        button.setOnAction(event ->  crudThisForm(event) );
        return new VBox(6, new HBox(12, button), tabPane);	
    }

     //------------------------------------------------------------------------------------
    private void crudThisForm(ActionEvent event_ignored)
    {
    	Tab active = tabPane.getSelectionModel().getSelectedItem();
    	String tabName = active.getText();
		IDBTable table = getDBTable(tabName);
		if (table != null)
		{
			DatabaseAccess db = new DatabaseAccess(table,  null);
			if (db != null)
			{
				HBox dbPanel = DBUtil.makeDBControls(db);
				HBox crudbar = DBUtil.makeCrudBar(db);
				TextArea console = new TextArea("console of SQL commands");

				Region form = table.makeForm();
				if (form != null)
				{
					form.setBorder(Borders.etchedBorder);
					Dialog<?> dialog = new Dialog<>();
					dialog.setTitle("Database Connection");
					dialog.getDialogPane().setContent(new VBox(6, dbPanel, crudbar, form, console));
					ButtonType done = ButtonType.FINISH;
					dialog.getDialogPane().getButtonTypes().add(done);
					dialog.show();
				}
			}
		}
	}
 }
