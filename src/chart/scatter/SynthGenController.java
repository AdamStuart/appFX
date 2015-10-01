package chart.scatter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
/**
 *  SynthGen - a program to generate list mode files.
 *  In this version only X, Y, id, dataset are written, ie 2D data with identifiers
 *  
 *  Input file:   lines start with * followed by three arguments:  int: number of events, point: center,  dimension: cv
 *  
 * @author adam
 *
 */
public class SynthGenController implements Initializable
{
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		intCols = new TableColumn[]{  idCol, countCol};
		dblCols = new TableColumn[]{ meanXCol, meanYCol, cvXCol, cvXCol, cvYCol};
		
		assert(synthGenTable != null);
		assert(version != null);
		for (TableColumn c : dblCols)	assert(c != null);
		for (TableColumn c : intCols)	assert(c != null);
		
		SynthGenTableSetup.setup(this, synthGenTable);

		version.setText("sg0.1");
		open();
//		logo.setImage(new Image("http://www.slingshotbio.com/wp-content/uploads/2012/08/logo_main10-640x360.png"));

		doPlot();
	}
	// //------------------------------------------------------
	// @FXML
	public TableView<SynthGenRecord> getSynthGenTable()	{ return synthGenTable;	}
//
//	private Stage stage;
//
//	public void setStage(Stage primaryStage)
//	{
//		stage = primaryStage;
//	}

	// TableColumns are injected by SceneBuilder
	@FXML public TableView<SynthGenRecord> synthGenTable;
	@FXML public TableColumn<SynthGenRecord, Integer> idCol;
	@FXML public TableColumn<SynthGenRecord, Integer> countCol;
	@FXML public TableColumn<SynthGenRecord, Double> meanXCol;
	@FXML public TableColumn<SynthGenRecord, Double> meanYCol;
	@FXML public TableColumn<SynthGenRecord, Double> cvXCol;
	@FXML public TableColumn<SynthGenRecord, Double> cvYCol;
	@FXML public Label version;
	public TableColumn<SynthGenRecord, Integer>[] intCols;
	public TableColumn<SynthGenRecord, Double>[] dblCols;

	
	//------------------------------------------------------
//	private void onScatterChart()
//	{
//		Stage stage = new Stage();
//		stage.setTitle("Scatter Chart Sample");
//		final NumberAxis xAxis = new NumberAxis();
//		final NumberAxis yAxis = new NumberAxis();
//		final ScatterChart<Number, Number> sc = new ScatterChart<Number, Number>(xAxis, yAxis);
//		xAxis.setLabel("X value");
//		yAxis.setLabel("Y value");
//		sc.setTitle("A Basic X / Y scatter chart");
//
//		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
//		FileChooser fileChooser = new FileChooser();
//		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Synthetic files", "*.synth"));
//		fileChooser.setTitle("Open Resource File");
//		File f = fileChooser.showOpenDialog(stage);
//		if (f != null)
//		{
//			SynthFileStream input = new SynthFileStream();
//			XYModel rows = input.readFromTextFile(f, 0, 0);
//			rows.fillDataSeries(series1.getData());
//			series1.setName("Events");
//			sc.getData().add(series1);
//		}
//		Scene scene = new Scene(sc, 500, 400);
//		stage.setScene(scene);
//		stage.show();
//	}

	// ------------------------------------------------------
	// ------------------------------------------------------
//	@FXML private void pickAndPlot()
//	{
//		FileChooser fileChooser = new FileChooser();
//		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Synthetic files", "*.fcs"));
//		fileChooser.setTitle("Open Resource File");
//		File f = fileChooser.showOpenDialog(stage);
//		if (f != null)
//		{
//			ScatterChart<Number, Number> chart = plotChart(f);
//			stage.getScene().setRoot(chart);
//		}
//	}
	int deftCt = 100;
	int nextID = 1;
	
	@FXML public void addRow()
	{
		SynthGenRecord row = new SynthGenRecord(nextID, deftCt, 0,0,1,1);
		synthGenTable.getItems().add(row);
		nextID++;
	}
	// ------------------------------------------------------
	public static String summaryFileName = "syntheticDefinitions";
				
	@FXML public void save()
	{
		SynthGenMetaFileStream output = new SynthGenMetaFileStream(this);
		output.writeDefFile(summaryFileName);
	}
	@FXML public void open()
	{
		SynthGenMetaFileStream input = new SynthGenMetaFileStream(this);
		ObservableList<SynthGenRecord> rows = input.readDefFile(summaryFileName);
		synthGenTable.setItems(rows);
	}

	// ------------------------------------------------------
	@FXML public void generate()
	{
		SynthFileStream output = new SynthFileStream();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Synthtetic File");
		File f = fileChooser.showSaveDialog(null);
		if (f == null)			return;
		
		String path = f.getAbsolutePath();
		if (!path.endsWith(".synth"))  path += ".synth";
		output.setOutFilepath(path);
		output.getFormatter().format("%d %f %f\n", 0, 1.f, 3.f);
		output.generate(synthGenTable.getItems());
		output.close();
	}
	// ------------------------------------------------------
	@FXML private void doPlot()
	{
		VBox container = new DrillDownChart(this, synthGenTable.getItems());
		Stage stage = new Stage();
        stage.setTitle("Drill Down Scatter Plot");
        stage.setScene(new Scene(container, 450, 450));
        stage.show();
	}
	// ------------------------------------------------------
//
//	private ScatterChart<Number, Number> plotChart(File synth)
//	{
//		long start = System.currentTimeMillis();
//		long mid = 0;
//		assert (synth != null);
//		final NumberAxis xAxis = new NumberAxis();
//		final NumberAxis yAxis = new NumberAxis();
//		final ScatterChart<Number, Number> scatter = new ScatterChart<Number, Number>(xAxis, yAxis);
//		xAxis.setLabel("Forward Scatter");
//		yAxis.setLabel("Side Scatter");
//		scatter.setTitle(synth.getName().substring(0, synth.getName().lastIndexOf(".")));
//		// scatter.getStylesheets().setAll(style);
//		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
//		series1.setNode(new Circle(2));
//		// String res =
//		// AppSlingshot.getApp().getClass().getResource("application.css").toExternalForm();
//		// scatter.setStyle("-fx-background-color: slateblue; -fx-text-fill: white;");
//		// scatter.getStylesheets().add(res);
//
//		if (synth.getAbsolutePath().endsWith(".synth"))
//		{
//			SynthFileStream input = new SynthFileStream();
//			XYModel model = input.readFromTextFile(synth, 0, 0); // TODO target
//			model.fillDataSeries(series1.getData());
//		} else
//			try
//			{
//				FCSFileReader reader = new FCSFileReader(synth);
//				float[] x = reader.getXData();
//				float[] y = reader.getYData();
//				int z = 1000; // x.length;
//				mid = System.currentTimeMillis();
//				for (int i = 0; i < z; i++)
//				{
//					// System.out.println(i + ": " + x[i] + "," + y[i]);
//					series1.getData().add(new XYChart.Data<Number, Number>(x[i], y[i]));
//				}
//
//			} catch (Exception e)
//			{
//				System.out.println("FCS Reader problem");
//				e.printStackTrace();
//			}
//		series1.setName("Events");
//		scatter.getData().add(series1);
//		long end = System.currentTimeMillis();
//		long a = mid - start;
//		long b = end - mid;
//		long c = end - start;
//		System.out.println("took: " + a + " + " + b + " =  " + c);
//		return scatter;
//	}

}
