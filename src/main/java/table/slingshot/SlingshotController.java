package table.slingshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SlingshotController implements Initializable
{
	@FXML private TableView<SlingshotDataRow> theTable;

	public TableView<SlingshotDataRow> getTable()
	{
		return theTable;
	}

	@FXML private AnchorPane root;
	@FXML private ImageView logo;
	@FXML private TextField targetXfld;
	@FXML private TextField targetYfld;
	@FXML private Label version;
	// TableColumns are injected by SceneBuilder
	@FXML public TableColumn<SlingshotDataRow, String> nameCol;
	@FXML public TableColumn<SlingshotDataRow, LocalDate> dateCol;
	@FXML public TableColumn<SlingshotDataRow, Integer> idCol;
	@FXML public TableColumn<SlingshotDataRow, Double> varCol;
	@FXML public TableColumn<SlingshotDataRow, Double> varXCol;
	@FXML public TableColumn<SlingshotDataRow, Double> varYCol;
	@FXML public TableColumn<SlingshotDataRow, Double> metricXCol;
	@FXML public TableColumn<SlingshotDataRow, Double> metricYCol;
	@FXML public TableColumn<SlingshotDataRow, Double> medXCol;
	@FXML public TableColumn<SlingshotDataRow, Double> medYCol;
	@FXML public TableColumn<SlingshotDataRow, Double> cvXCol;
	@FXML public TableColumn<SlingshotDataRow, Double> cvYCol;
	@FXML public TableColumn<SlingshotDataRow, Double> targXCol;
	@FXML public TableColumn<SlingshotDataRow, Double> targYCol;
	public TableColumn<SlingshotDataRow, Double>[] varCols;
	public TableColumn<SlingshotDataRow, Double>[] intNumCols;
 
	float targetX = 0, targetY = 0; // TODO properties
	int index;

	public float getTargetX()	{		return targetX;	}

	public float getTargetY()	{		return targetY;	}
	public int getIndex()	{		return index;	}

	InnerShadow shadow;
	
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		varCols = new TableColumn[]{ varCol, varXCol, varYCol, cvXCol, cvYCol, metricXCol, metricYCol};
		intNumCols = new TableColumn[]{  medXCol, medYCol, targXCol, targYCol};
		initializeModel();

		shadow = new InnerShadow();
		shadow.setOffsetX(1.0);
		shadow.setColor(Color.web("#00EEEE"));
		shadow.setOffsetY(1.0);
		version.setText("v0.3");
		logo.setImage(new Image("http://www.slingshotbio.com/wp-content/uploads/2012/08/logo_main10-640x360.png"));
	}

	private Stage stage;
	public void setStage(Stage primaryStage)	{		stage = primaryStage;	}

	// ------------------------------------------------------
	private void initializeModel()
	{
		if (theTable != null)
		{
			theTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); 
			SlingshotTableSetup.setup(this);
			open();
		}
	}

	// ------------------------------------------------------
	public void setParameters(float x, float y, int i)
	{
		targetX = x;
		targetY = y;
		index = i;
		ctr = index;
		targetXfld.setText("" + x);
		targetYfld.setText("" + y);
	}
	// ------------------------------------------------------
	String SUMMARYFILE = "slingshot.data.summary";

	@FXML public void save()
	{
		readTarget();
		SlingshotFileStream output = new SlingshotFileStream(this);
		output.writeToTextFile(theTable.getItems(), SUMMARYFILE);
	}

	@FXML private void open()
	{
		SlingshotFileStream input = new SlingshotFileStream(this);
		ObservableList<SlingshotDataRow> rows = input.readFromTextFile(SUMMARYFILE);
		theTable.setItems(rows);
	}
	// ------------------------------------------------------
	static int ctr = 1;

	private void addFCSFile(File f)
	{
		try
		{
			FCSFileReader reader = new FCSFileReader(f);
			reader.setTarget(targetX, targetY);
			reader.calculate();
			double[] stats11 = reader.getStats11(); // this is where the median
													// & variance calculations occur
			int id = reader.getId();
			String name = reader.getName();
			long date = reader.getDate();
			SlingshotDataRow row = new SlingshotDataRow(id, name, date, stats11);
			theTable.getItems().add(row);

		} 
		catch (FileNotFoundException e)	{	e.printStackTrace();	}
	}

	// ------------------------------------------------------
	@FXML private void onDragEnter(DragEvent e)
	{
		// System.out.println("dragEntered ");
		root.setEffect(shadow);
		e.consume();
	}

	@FXML private void onDragExit(DragEvent e)
	{
		// System.out.println("onDragExit ");
		root.setEffect(null);
		e.consume();
	}

	@FXML private void onDragOver(DragEvent e)
	{
		e.acceptTransferModes(TransferMode.ANY); // this is necessary, or drops
													// don't work!
		e.consume();
	}

	@FXML private void onDragDropped(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		readTarget(); // hack to update changes that may have been made to the
						// target fields
		e.acceptTransferModes(TransferMode.ANY);
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		if (db.hasFiles())
		{
//			if (SynthFileMgr.hasSynthFiles(db))
//				SynthFileMgr.addSynthFiles(db, this);

			if (FCSFileReader.hasFCSFiles(db))
				for (File f : db.getFiles())
					try
					{
						if (FCSFileReader.isFCS(f)) // TODO recurse for
													// directories
							addFCSFile(f);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}

		}
		e.acceptTransferModes(TransferMode.ANY);
		e.consume();
		// System.out.println("onDragDropped!!!");
	}

	// ------------------------------------------------------
	//
	public void readTarget() // this is a hack, because the target isn't in
								// Property as it should be!
	{
		targetX = Float.parseFloat(targetXfld.getText());
		targetY = Float.parseFloat(targetYfld.getText());
	}

}
