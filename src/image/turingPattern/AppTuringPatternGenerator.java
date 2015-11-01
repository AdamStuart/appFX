package image.turingPattern;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;

import javafx.animation.Animation.Status;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;

/*
 //Ref:  Jonathan McCabe's multi-scale Turing pattern.: http://www.jonathanmccabe.com/Cyclic_Symmetric_Multi-Scale_Turing_Patterns.pdf
 //Ref: Jason Rampe's blog provides implementation details:   http://softologyblog.wordpress.com/2011/07/05/multi-scale-turing-patterns/

 //
 */
public class AppTuringPatternGenerator extends Application {
	
	public static void main(String[] args) {		launch(args);	}

	private Dimension dim = new Dimension(800, 500);
	public Dimension getCanvasSize() {		return dim;	}

	TPController controller;
	private Stage fStage;

	private TuringPatternList patterns;
	private PixelGrid theGridModel;
	private ColorPool pool;

	Timeline timeline;
	AnimationTimer timer;
	Circle node;
	ListView<TuringPattern> patternList;
	Canvas canvas;
	Canvas histogram;

	private static AppTuringPatternGenerator instance;

	public AppTuringPatternGenerator() {	           instance = this;	}
	// static method to get instance of view
	public static AppTuringPatternGenerator getInstance() {       return instance;	}
	//-----------------------------------------------------------------------------------------
	public void start(Stage primaryStage) throws Exception 
	{
		fStage = primaryStage;
		fStage.setX(20);
		fStage.setWidth(1100);
		fStage.setHeight(650);
		
		initModel();
		initComponents();
		createAnimation();
		
		fStage.show();
		timer.start();
//		timeline.play();
//		timeline.pause();
	}

	static public String relativeSeed = "cain.png";
	private String shortClassname(String class1) 			{		return class1.substring(1 + class1.lastIndexOf('.'));	}
	public void setController(TPController tpController) 	{		controller = tpController;	}
	public void setHistogramCanvas(Canvas hc) 				{		histogram = hc;	}
	public void setPatternCanvas(Canvas c) 					{		canvas = c;	}
	public Window getStage() 								{		return fStage;	}
	public ColorPool getColorPool() 						{		return pool;	}
	public TuringPatternList getPatternList() 				{		return patterns;	}
	//-----------------------------------------------------------------------------------------
	private void initModel() 
	{	
		pool = new ColorPool();
		pool.setRange(0,1);
		theGridModel = new PixelGrid(dim);
		theGridModel.init(pool, null);
		patterns = new TuringPatternList(theGridModel);
		patterns.add(new TuringPattern(dim, new Point(30, 40), new Point(16, 20), 1, 0.0125));
		patterns.add(new TuringPattern(dim, new Point(3, 1), new Point(4, 1), 1, 0.345));
		patterns.add(new TuringPattern(dim, new Point(8, 40), new Point(2, 40), 1, 0.05));
		patterns.add(new TuringPattern(dim, new Point(3, 14), new Point(6, 15), 1, 0.05));
		patterns.add(new TuringPattern(dim, new Point(20, 14), new Point(23, 20), 1, 0.1625));
//		patterns.add(new TuringPattern(dim, new Point(12, 12), new Point(15, 15), 2, 0.1));

// a nice config
//		patterns.add(new TuringPattern(dim, new Point(8, 40), new Point(2, 40), 1, 0.05));
//		patterns.add(new TuringPattern(dim, new Point(6, 14), new Point(25, 20), 1, 0.34625));
	}
//-----------------------------------------------------------------------------------------
	private void initComponents() 
	{
		URL loc = getClass().getResource("TuringPatterns.fxml");
		Scene scene = null;
		try 
		{
			if (loc == null)   	throw new Exception("failed: resource not found");	
			Parent root = FXMLLoader.load(loc);
			scene = new Scene(root, 500 + dim.width, 100 + dim.height);
//			System.out.println("[Seed " + seed + "]");
			fStage.setScene(scene);
		} catch (Exception e) 
		{		System.err.println("Reading the FXML file failed: " + e.getMessage());		}
	}
	//-----------------------------------------------------------------------------------------
	private void createAnimation() {
		
		DoubleProperty x = new SimpleDoubleProperty();
		DoubleProperty y = new SimpleDoubleProperty();

//		timeline = new Timeline(new KeyFrame(Duration.seconds(0), new KeyValue(x, 0), new KeyValue(y, 0)),			// simplify !  no args are needed
//				new KeyFrame(Duration.seconds(1), new KeyValue(x, 50), new KeyValue(y, 150)));
//		timeline.setAutoReverse(true);
//		timeline.setCycleCount(Timeline.INDEFINITE);
//		timeline.setRate(1);

		timer = new AnimationTimer() {
			@Override public void handle(long now) {
				if ((timeline.getStatus() == Status.RUNNING || now < 0) && (canvas != null))
				{
//				System.out.println("handle");
				patterns.step();			// expensive method - should be threaded
				
				drawMyPixels();
				drawColorTableHistogram();
				controller.incCounter();
//				counterLabel.setText(counter + "");
				}
			}
		};
	        }

	double res = 1.0;
	int[] counts;
	//-----------------------------------------------------------------------------------------
	WritableImage pixels =null;
	
	private void drawMyPixels() 
	{
		boolean grays = false;
		GraphicsContext gc = canvas.getGraphicsContext2D();
		int w = theGridModel.getWidth();
		int h = theGridModel.getHeight();
		int poolsize = pool.size();
		counts = new int[poolsize];
		
		if (pixels == null)
			pixels = new WritableImage(dim.width, dim.height);
		
		final PixelWriter pixelWriter = pixels.getPixelWriter();
		for (int i= 0; i < w; i++)
			for (int j= 0; j < h; j++)
			{
				double val = theGridModel.get(i, j);
				double r =  0.5 + val / 2;
				int bin = (int)(r * poolsize);
				if (bin < 0) bin = 0; if (bin >= poolsize) bin = poolsize-1;
				counts[bin]++;		// populate the histogram
				Color c = Color.RED;	
				if (r <= 1 && r >= 0)	
				{	
				 if (grays)    c = new Color(r,r,r,1.0);  
				else 			c = pool.fromValue(r);
				}
				pixelWriter.setColor(i,j,c);
			}
		gc.drawImage(pixels, 0, 0);
	}
	//-----------------------------------------------------------------------------------------
	private void drawColorTableHistogram() 
	{
		GraphicsContext gc = histogram.getGraphicsContext2D();
		int max = 0;
		int baseX = 10; 
		double w = histogram.getWidth();
		double h = histogram.getHeight();
		double hRes = (w-20) / pool.size();
		gc.setFill(Color.GOLDENROD);
		gc.fillRect(0, 0, w, h);
		gc.setStroke(Color.GRAY);
		gc.strokeRect(10,  10, w-20, h-20);

		gc.setStroke(Color.GRAY);
		gc.strokeRect(10, h/2 - 10, w-20 ,0.5 );

		int poolsize = pool.size();
		double baseY = h-10; // (int)(canvasH -20);
		int totalCt = 0;
		for (int n=0; n< poolsize; n++) 
		{
			int ct = counts[n];
			max = Math.max(max,ct);
			totalCt += ct;
		}
		double meanCt = totalCt / poolsize;
		double histoscale = 100 * h / (Math.log(max));
		if (max != 0) {
			for (int n = 0; n < poolsize; n++) {
				double scaledVal = 8 * Math.log(histoscale * counts[n]);
				double y = baseY - scaledVal;
				boolean grays = false;
				gc.setFill(grays ? (new Color(n / poolsize, n / poolsize, n / poolsize, 1)) : pool.get(n));
				gc.fillRect(baseX + n * hRes, y, hRes + 1, scaledVal);
			}
		}
	}
	//-----------------------------------------------------------------------------------------
	boolean verbose = true;
	public AnimationTimer getTimer()		{ return timer;	}
	public Timeline getTimeline()		{ return timeline;	}
	public void reset() {		theGridModel.init(pool, null);	}
	// -----------------------------------------------------------------------------------------
}