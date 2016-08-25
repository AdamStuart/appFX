package table.networkTable;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class HeatmapDialog extends Dialog implements Initializable {
	NetworkTableController controller;
	
	public HeatmapDialog(String title, Group inputGroup, NetworkTableController ctrlr) {
		super();
		controller = ctrlr;
		setTitle(title);
		getDialogPane().setPrefWidth(1200 );
		getDialogPane().setPrefHeight(1125);
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
		getDialogPane().getChildren().add(inputGroup);

	}

	@FXML
	void onOkButtonAction(ActionEvent event) {
		doOK(event);
	}

	public void doOK(ActionEvent ev) {
		System.out.println("doOK");
		controller.reset(this);

	}

	public void doCancel(ActionEvent ev) {
		System.out.println("doCancel");
		close();

	}

	/*
	 * Called when FXML file is load()ed (via FXMLLoader.load()). It will
	 * execute before the form is shown.
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
	
}