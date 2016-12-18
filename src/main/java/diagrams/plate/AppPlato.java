package diagrams.plate;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppPlato extends Application
{

	public static void main(String[] args)	{		launch(args);	}
	
//------------------------------------------------------------
//@formatter:off
	private static AppPlato instance;
	private Stage fStage;
	public Stage getStage()					{ 		return fStage;		}
	public static AppPlato getInstance()	{		return instance;	}
//@formatter:on
//------------------------------------------------------------
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		instance = this;
		fStage = primaryStage;
		fStage.setX(20);
		fStage.setWidth(850);
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
//			Parent root = new VBox(new Label("Override"));
			scene = new Scene(root);
			fStage.setScene(scene);

//			NodeUtil.showKids(scene.getRoot(), "");
		} catch (Exception e)
		{
			System.out.println("failed: " + e.getMessage());
		}
	}


}
