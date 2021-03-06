package image.turingPattern;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import util.SoundUtility;



public class TPController  {

	@FXML private SimpleIntegerProperty counterVal;
	@FXML private DoubleProperty valA;			// these are the properties of the active pattern (2 radiuses and a stepSize)
	@FXML private DoubleProperty valB;
	@FXML private DoubleProperty valC;
	@FXML private DoubleProperty valD;
	@FXML private DoubleProperty valE;
	@FXML private StringProperty patternStringProperty;
	@FXML private TextField seedRef;
	@FXML private Button capture;
	@FXML private Button run;
	@FXML private Button pause;
	@FXML private Button step;
	@FXML private Button search;
	@FXML private Button reset;
	@FXML private Button add;
	@FXML private Canvas canvas;
	@FXML private VBox colorTableWrapper;
	@FXML private Canvas histogram;
	@FXML private Label counterLabel;
	@FXML private Label patternString;
	@FXML private ListView<TuringPattern> patternList;
	
	// OLD SCHOOL
	DoubleProperty[] vals;		// keep both enumerated properties, and an array of them for easier iteration
	Slider sliders[];

	@FXML private TableView<Color> colorTable;
	
	private AppTuringPatternGenerator getApp()	{ return AppTuringPatternGenerator.getInstance(); }

	
	public TPController()
	{
        counterVal = new SimpleIntegerProperty(1);	
		valA = new SimpleDoubleProperty(3);	
		valB = new SimpleDoubleProperty(3);
		valC = new SimpleDoubleProperty(3);
		valD = new SimpleDoubleProperty(3);
		valE = new SimpleDoubleProperty(0.3);
		patternStringProperty = new SimpleStringProperty("Active Pattern Shown Here");
		
		vals = new DoubleProperty[] { valA, valB, valC, valD, valE};
		for (DoubleProperty v : vals)		// all the sliders listen and refresh the pattern string 
			v.addListener((obs, old, val) ->	{	recalcPatternString(); 	});
		counterVal.addListener((obs, old, val) ->	{	if (counterLabel != null) counterLabel.setText(val.toString());		});
		patternStringProperty.addListener((obs, old, val) -> {	if (patternString != null) patternString.setText(val.toString()); });
		
		getApp().setController(this);
	}
	//-----------------------------------------------------------------------------------------
	
	@FXML private Slider sliderA;
	@FXML private Slider sliderB;
	@FXML private Slider sliderC;
	@FXML private Slider sliderD;
	@FXML private Slider sliderE;
	//-----------------------------------------------------------------------------------------

	private TuringPattern activePattern = null;
	public TuringPattern getActivePattern() {		return activePattern;	}
	public void setActivePattern(TuringPattern newPattern) {
		if (newPattern == null)
		{
			for (Slider s : sliders) 
				s.setValue(s.getMin());
			return;
		}
		activePattern = newPattern;
		sliders[0].setValue(activePattern.getActivatorX());
		sliders[1].setValue(activePattern.getActivatorY());
		sliders[2].setValue(activePattern.getInhibitorX());
		sliders[3].setValue(activePattern.getInhibitorY());
		sliders[4].setValue(activePattern.getStepSize());
		patternStringProperty.set(activePattern.toString());
	
	}
	//-----------------------------------------------------------------------------------------
	private void recalcPatternString() 
	{
		setActivePattern( (int) valA.get(),  (int) valB.get(), (int) valC.get(),  (int) valD.get(), valE.get());
	}
	
	private void setActivePattern(int a, int b, int c, int d,double e)
	{
		TuringPattern activePattern = getActivePattern();
		if (activePattern != null)
		{
			activePattern.set(a,b,c,d,e);
			patternStringProperty.set(activePattern.toString());
		}
	}
	//-----------------------------------------------------------------------------------------
	@FXML private void initialize() 
	{		
	   try 		// make sure all the items were injected from the FXML file
	   {
	       assert run != null : "run not found";
	       assert pause != null : "pause not found";
	       assert step != null : "step not found";
	       assert search != null : "search not found";
	       assert reset != null : "reset not found";
	       assert canvas != null : "canvas not found";
	       assert colorTableWrapper != null : "colorTableWrapper not found";
	       assert histogram != null : "histogram not found";
	       assert counterLabel != null : "counterLabel not found";
	       assert patternList != null : "patternList not found";
	       assert capture != null : "capture not found";
	   }
	   catch (AssertionError err)	 { 	 System.err.println("Assertion Failed: " + err.getMessage());	} 		
	  
	   try 
	   {
		colorTableWrapper.getChildren().add(new ColorChooser(getApp().getColorPool()));
		sliders = new Slider[] { sliderA, sliderB, sliderC, sliderD, sliderE };
	    assert sliders != null : "sliders not found";
	    AppTuringPatternGenerator app = getApp();
	    assert app != null : "Application not found";
	    app.setHistogramCanvas(histogram);
	    app.setPatternCanvas(canvas);

		TuringPatternList patterns = app.getPatternList();
		if (patternList != null)
		{
			patternList.setCellFactory(c-> new CheckBoxListCell(createSelectionCallback())); 
			patternList.setEditable(true);
			patternList.getSelectionModel().selectedItemProperty().addListener(
				(obs, old, val) ->	{  setActivePattern((TuringPattern)val);  });
			patternList.setEditable(true);
			patternList.getItems().addAll(patterns.getList());
		}
	   }
	   catch (AssertionError err)	{  System.err.println("Assertion Failed: Step 2 " + err.getMessage());	} 		
	}
	//-----------------------------------------------------------------------------------------
	private Callback<TuringPattern, ObservableValue> createSelectionCallback() 
	{
		SimpleBooleanProperty bool = new SimpleBooleanProperty(true);
		return (param) -> 
		{
			bool.addListener(e-> 	{ param.activate(bool.get());	});
			return bool;
		};
	}
	//-----------------------------------------------------------------------------------------

	public void incCounter()		{		counterVal.setValue(1+counterVal.getValue());	}
	@FXML public void addPattern()	{		System.out.println("addPattern");	}
	//-----------------------------------------------------------------------------------------
	@FXML public void resetPattern()	{		AppTuringPatternGenerator.getInstance().reset();	}
	//-----------------------------------------------------------------------------------------
	@FXML public void run()
	{
		if (getApp().getTimer() != null)
			getApp().getTimer().start();
	}

	@FXML public void pause()
	{
		if (getApp().getTimer() != null)
			getApp().getTimer().stop();
	}
	
	@FXML public void step()
	{
		AnimationTimer t = AppTuringPatternGenerator.getInstance().getTimer();
//		Duration duration = t.getCurrentTime();
//		t.jumpTo(duration.add(new Duration(500)));

	}
	
	//-----------------------------------------------------------------------------------------
	@FXML public void capture(ActionEvent ev) throws InterruptedException
	{
		try {
			SoundUtility.playSound("shutter");
			Dimension outSize = getApp().getCanvasSize();
			double mult =  2.4;
			 SnapshotParameters parameters = new SnapshotParameters();
			 Transform transform = new Scale(mult, mult);
			 parameters.setTransform(transform);
			WritableImage wi = new WritableImage((int) (mult * outSize.width),(int)( mult * outSize.height));
			WritableImage snapshot = canvas.snapshot(parameters, wi);
			File output = new File("snapshot" + new Date().getTime() + ".png");
			ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
			System.out.println(output.getAbsolutePath());
		} catch (IOException ex) 
		{
			Logger.getLogger(AppTuringPatternGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//-----------------------------------------------------------------------------------------
	@FXML public void seedBrowse()
	{
		System.out.println("seedBrowse");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		File f = fileChooser.showOpenDialog(getApp().getStage());
		if (f != null) setSeedRef(f);
	}
	
	private void setSeedRef(File f) {		seedRef.setText(f.getName());		}
	//-----------------------------------------------------------------------------------------
	@FXML public void nextColors()
	{
		System.out.println("nextColors");
		getApp().getColorPool().nextColorSet();
	}
	//-----------------------------------------------------------------------------------------
	@FXML public void mouseDragged(MouseEvent event)
	{
		if (event.getSource() instanceof Slider)
		{
			Slider slid = ((Slider) event.getSource());
			double val = slid.getValue();
			int idx = slid.getId().charAt(6)-'A';
			vals[idx].set(val);
		}
	}
}
