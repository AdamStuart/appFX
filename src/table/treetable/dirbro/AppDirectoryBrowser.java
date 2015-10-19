package table.treetable.dirbro;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppDirectoryBrowser extends Application
{
	public static void main(String[] args)	{		launch(args);	}
	private DirectoryBrowserController controller;

	public void setController(DirectoryBrowserController c)
	{
		controller = c;
	}
	private static AppDirectoryBrowser instance;
	private Stage fStage;

	public static AppDirectoryBrowser getInstance()
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
		URL loc = getClass().getResource("DirectoryBrowser.fxml");
		Scene scene = null;
		try
		{
			if (loc == null)
				throw new Exception("failed: resource not found");
			Parent root = FXMLLoader.load(loc);
			scene = new Scene(root, 800, 600);
			fStage.setScene(scene);
		} catch (Exception e)
		{
			System.out.println("failed: " + e.getMessage());
		}
	}
}
