package table.treetable.dirbro;

import java.io.File;
import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import table.codeOrganizer.CodeModule;

public class DirectoryBrowserController {
	@FXML	VBox center;
	@FXML	HBox south;
	@FXML	VBox east;
	@FXML	VBox west;

	HashMap<String, CodeModule> modules = new HashMap<String, CodeModule>();
	ObservableList<Integer> selectedIndexes = FXCollections.observableArrayList();

	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";

	static String missing(String s)	{		return String.format(ctrlStr, s, "CodeOrganizer.fxml");	}

	@FXML	void initialize()
	{
		assert south != null : missing("south");
		assert east != null : missing("east");
		assert west != null : missing("west");
		
		DirectoryBrowserFactory myDir = new DirectoryBrowserFactory();
		DirectoryModel model = new DirectoryModel(new File("/"));
		TreeTableView view = myDir.buildFileBrowserTreeTableView(model);
		center.getChildren().add(view);
		center.setVgrow(view, Priority.ALWAYS);
		//http://stackoverflow.com/questions/23687836/javafx-tableview-resize-to-fit-window
	}
}
