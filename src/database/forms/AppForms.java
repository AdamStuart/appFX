/*
 * @(#)ValidationDemo.java 5/19/2013
 *
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */

package database.forms;

import gui.Borders;

import java.util.Locale;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import task.DatabaseAccess;
import task.IDBTable;
import util.DBUtil;
import database.model.DBCitation;
import database.model.DBEvent;
import database.model.DBGroup;
import database.model.DBPerson;
import database.model.DBProtocol;

public class AppForms extends Application {



    @Override
    public void start(Stage stage) {
        String stylesheet = Preferences.userRoot().get("JideFXDemo.UserAgentStylesheet", STYLESHEET_MODENA);
        setUserAgentStylesheet(stylesheet);

        String locale = Preferences.userRoot().get("JideFXDemo.Locale", Locale.getDefault().toLanguageTag());
        Locale.setDefault(Locale.forLanguageTag(locale));
        System.out.println("Setting locale to " + Locale.getDefault().toString());

        Scene scene = new Scene(createDemo());

        stage.setTitle("AppForms");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(true);
        stage.show();

        scene.focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
//            if (isTraceFocus()) {
                System.out.println(newValue);
//            }
        });
    }

    public Pane createDemo() {
        Region demoPanel = getDemoPanel();

//        demoPanel.setPadding(createInsets());
//        Region optionsPanel = createOptionsPanel(demo, demoPanel);
//        optionsPanel.setPadding(createInsets());

        HBox.setHgrow(demoPanel, Priority.ALWAYS);
        return new HBox(10, demoPanel);		// optionsPanel,
    } 
    public static void main(String[] args) {       launch(args);    }

    TabPane tabPane;
    
    DBPerson personDB;
    DBEvent eventDB;
    DBProtocol protocolDB;
    DBCitation citationDB;
    DBGroup groupDB;
  
   public Region getDemoPanel() {
        tabPane = new TabPane();
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);

        
//        installValidatorsForPersonForm(pane);


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
        
        VBox box = new VBox();
        SignUpForm form = new SignUpForm();
        form.setBorder(Borders.dashedBorder);

//        form.setBorder(Borders.redBorder);
       Tab signup = new Tab("Signup Validation",form );
        Tab validator = new Tab("Various Validators", new ValidatorsForm());
        Tab invoice = new Tab("Invoice", InvoiceForm.createInvoiceForm());
        Tab cyto = new Tab("Cytometry", FormsGallery.createCytometryMLform("CytometryML"));
        Tab experiment = new Tab("Experiment", FormsGallery.createExperimentForm("Experiment"));
        Tab multi = new Tab("Multiple Instances", FormsGallery.createMultipleInstanceForm("x"));
       
        tabPane.getTabs().addAll(multi, experiment, cyto, signup, validator, person, group, protocol, events, citations, invoice);

//        CheckBox checkBox = new CheckBox("Using Validation.css");
//        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                if (newValue) {
//                    // use a validation css for demo purpose
//                    tabPane.getStylesheets().add(AppForms.class.getResource("Validation.css").toExternalForm());
//                } else {
//                    tabPane.getStylesheets().remove(AppForms.class.getResource("Validation.css").toExternalForm());
//                }
//            }
//        });
        Button button = new Button("CRUD");
        button.setOnAction(event ->  crudThisForm(event) );
        return new VBox(6, new HBox(12, button), tabPane);			//checkBox, 
    }

   IDBTable getDBTable(String name)
    {
	   	if ("Events".equals(name))	return eventDB;
	   	if ("Citations".equals(name))	return citationDB;
	   	if ("Protocols".equals(name))	return protocolDB;
	   	if ("Persons".equals(name))	return personDB;
	   	if ("Groups".equals(name))	return groupDB;
	return null;
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

				Region form = table.getForm();
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

//
//
//  
//    String PROTOCOL_FORM = "Protocol";
    
    //---------------------------------------------------

 }
