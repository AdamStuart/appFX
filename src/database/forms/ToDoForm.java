package database.forms;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ToDoForm extends VBox
{
	Region root;
	public Region getRegion()	{		return root;	}

	
	public ToDoForm()
	{
		super(8);
		root = createToDoForm();
		getChildren().add(root);
	}

	Region createToDoForm()
	{
		VBox pane = new VBox();
    	pane.setSpacing(12);
    	pane.setPadding(new Insets(8));
    	pane.setPrefHeight(200);
    	pane.setPrefWidth(300);
        Label taskLabel = new Label("Task");
        taskLabel.setId("taskLabel");
        TextField taskField = new TextField();
        taskField.setId("taskField");

        Label descLabel = new Label("Description");
        descLabel.setId("descLabel");
        TextField descField = new TextField();
        descField.setId("descField");
        pane.getChildren().addAll(new HBox(6, taskLabel, taskField), new HBox(6, descLabel, descField));
		return pane;
	}
}