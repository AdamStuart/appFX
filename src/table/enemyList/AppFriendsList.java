/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package table.enemyList;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Narayan G. Maharjan
 */
public class AppFriendsList extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		System.out.println(System.getProperty("javafx.version"));

		BorderPane pane = null;
		try {
			pane = (BorderPane) FXMLLoader.load(AppFriendsList.class.getResource("GUIFX.fxml"));
			pane.getStyleClass().add("main");
		} catch (IOException ex) {

			Logger.getLogger(AppFriendsList.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(-1);
		}
		primaryStage.setScene(new Scene(pane, 700, 550));
		primaryStage.setTitle("Enemies List");
		primaryStage.getScene().getStylesheets()
				.add(AppFriendsList.class.getResource("/com/ngopal/css/gui.css").toExternalForm());
		primaryStage.show();
	}
}
