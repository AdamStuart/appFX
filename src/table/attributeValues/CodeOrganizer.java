package table.attributeValues;

import java.net.URL;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CodeOrganizer extends Application
{
	public static void main(String[] args)	{		launch(args);	}
	private CodeOrganizerController controller;

	public void setController(CodeOrganizerController c)
	{
		controller = c;
	}
	private static CodeOrganizer instance;
	private Stage fStage;

	public static CodeOrganizer getInstance()
	{
		return instance;
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		instance = this;
		fStage = primaryStage;
		fStage.setX(20);
		fStage.setWidth(1100);
		fStage.setHeight(650);

		initModel();
		initComponents();
		fStage.show();
	}

	// ------------------------------------------------------------
	private void initModel()
	{
	}

	private void initComponents()
	{
		URL loc = getClass().getResource("CodeOrganizer.fxml");
		Scene scene = null;
		try
		{
			if (loc == null)
				throw new Exception("failed: resource not found");
			Parent root = FXMLLoader.load(loc);
			scene = new Scene(root, 800, 600);
//			SimpleBooleanProperty boolProp = new SimpleBooleanProperty();

			fStage.setScene(scene);
		} catch (Exception e)
		{
			System.out.println("failed: " + e.getMessage());
		}
	}
}
