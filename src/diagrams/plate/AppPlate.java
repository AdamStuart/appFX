package diagrams.plate;

import java.net.URL;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import table.codeOrganizer.TreeTableModel;
import util.NodeUtil;

public class AppPlate extends Application
{

	public static void main(String[] args)	{		launch(args);	}
	
//------------------------------------------------------------
//@formatter:off
//	private PlateController controller;
//	public void setController(PlateController c)	{		controller = c;	}
	private static AppPlate instance;
	private Stage fStage;
	public Stage getStage()							{ 		return fStage;	}
	public static AppPlate getInstance()			{		return instance;	}
//	static TreeItem<String> populationTree;
//	public static TreeItem<String> getTreeRoot() 	{		return populationTree; }
//@formatter:on
//------------------------------------------------------------
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		instance = this;
		fStage = primaryStage;
		fStage.setX(20);
		fStage.setWidth(1100);
		fStage.setHeight(650);
		fStage.setTitle("Free Flow - Plato");

		initComponents();
		fStage.show();
	}
	//------------------------------------------------------------

	//------------------------------------------------------------
	private void initComponents()
	{
		URL loc = getClass().getResource("PlateDesigner.fxml");
		Scene scene = null;
		try
		{
			if (loc == null)
				throw new Exception("failed: resource not found");
			Parent root = FXMLLoader.load(loc);
			scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
//			SimpleBooleanProperty boolProp = new SimpleBooleanProperty();
			// System.out.println("[Seed " + seed + "]");

			fStage.setScene(scene);

			NodeUtil.showKids(scene.getRoot(), "");
		} catch (Exception e)
		{
			System.out.println("failed: " + e.getMessage());
		}
	}


}
