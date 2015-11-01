package game.life;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
/**
   * GameOfLifeView is the View of the project. Installs mouse listeners to 
   * the grid, and to each cell as it is created.
   * 
   * @author Martella Massimiliano
   * 
   * {@link http://www.codeproject.com/Articles/1043443/The-Game-Of-Life-Advanced-Style-of-Programmation }
   *
   * @author JavaFX port by Adam Treister
**/

class GameOfLifeView extends VBox {
	public static int blockSize = 15;
	public static int matrixLen = 40;
	public static int getMatrixSide()	{		return matrixLen;	}
	public static int getPixelSize()	{		return matrixLen * blockSize;	}

  	private AnchorPane gridPanel;
  	private Button startButton = new Button("Start");
  	private Button stopButton = new Button("Stop");
  	private Button resetButton = new Button("Reset");
  	private Label state  = new Label("Ready...");;
  	private HBox statusLine = new HBox(12, new Label("State: "), state);
  	private Label rateLabel = new Label("Rate:");	
  	private Slider sleepSetter = new Slider(20, 500, 200);
  	private HBox controlPanel = new HBox(12, startButton, stopButton, resetButton, rateLabel, sleepSetter);
  	
  	private BooleanProperty stopFlag = new SimpleBooleanProperty(true); 
  	private IntegerProperty sleepTime= new SimpleIntegerProperty();
  	private ArrayList<Rectangle> cells = new ArrayList<Rectangle>(0);
  	public GameOfLifeView() 	
  	{
	    super(12);
		startButton.setOnAction(ev -> {	stopFlag.set(false);  new ComputeThread(this,stopFlag, sleepTime).start(); });
		stopButton.setOnAction(ev -> {	stopFlag.set(true); });
		resetButton.setOnAction(ev -> {	reset(); });
		startButton.disableProperty().bind(Bindings.not(stopFlag));
		resetButton.disableProperty().bind(Bindings.not(stopFlag));
		stopButton.disableProperty().bind(stopFlag);
		sleepTime.bind(sleepSetter.valueProperty());
		sleepSetter.setRotate(180);		// flip it so right is fast and left is slow
		sleepSetter.setPadding(new Insets(10, 10, 10 , 10));
		rateLabel.setPadding(new Insets(10, 10, 10 , 10));
		controlPanel.setMinHeight(40);

		gridPanel = new AnchorPane(this);
		makeGrid();
		reset();
		gridPanel.setOnMousePressed(ev ->  setOneCell(ev));
		gridPanel.setOnMouseDragged(ev ->  setOneCell(ev));
		getChildren().addAll(controlPanel, statusLine, gridPanel);
		setPadding(new Insets(10, 10, 10 , 10));
	}
  	
  	public void setState(String st)  	{ 	Platform.runLater(() -> state.setText(st) );	 }
  	public int getBlockSize()			{ 	return blockSize;	}
	public void update()				{	Platform.runLater(() ->	{ updateMatrix();	});	}
  	public ArrayList<Rectangle> getLiveCells() {  		return cells;  	}
  	public void setNextGen(String state, ArrayList<Rectangle> r) 
  	{ 
		Platform.runLater(() -> {
			cells = r;  	
	 		setState(state);
	  		updateMatrix();
		});
  	}
  	
  	private void setOneCell(MouseEvent ev)
  	{
  		Platform.runLater(() -> 
  		{
  			addPoint(ev);		
  			setState("new cell"); 
  			updateMatrix();	
  		});
  	}
	//-------------------------------------------------------------------
  	private Group grid = new Group();
  	private void makeGrid() {
		
  		for (int i = 0; i <= matrixLen; i++) {
			
  			int I = i * blockSize;
			Line hGrid = new Line(I, 0, I,  matrixLen * blockSize);
			Line vGrid = new Line(0, I, blockSize * matrixLen, I);

			hGrid.setStroke(Color.BLACK);		hGrid.setStrokeWidth(i % 10 == 0 ? 0.6 : 0.3);		// TODO CSS
			vGrid.setStroke(Color.BLACK);		vGrid.setStrokeWidth(i % 10 == 0 ? 0.6 : 0.3);
			grid.getChildren().addAll(hGrid, vGrid);
  		}
	}
	//-------------------------------------------------------------------
  	/**
  	 * Update the cells  -- the array of LiveCells has changed.  Clear
  	 * the ones in the scene graph, reset the grid, and add in the new live cells
  	 */
	public void updateMatrix() 
  	{  
		gridPanel.getChildren().clear(); 	
		gridPanel.getChildren().addAll(getLiveCells());  	
		gridPanel.getChildren().add(grid);
  	}
	  	//-------------------------------------------------------------------
  	boolean inRange(double x, double min, double max){ return x >= min && x < max;	}
  	
  	public void addPoint(MouseEvent ev) {
  		double x = ev.getX() / blockSize;
  		double y = ev.getY() / blockSize;
  		if (inRange(x, 0, matrixLen) && inRange(y,  0, matrixLen))
  				addPoint((int) x, (int) y);
  	}

  	public Rectangle makeCell(int x, int y) {
  		Rectangle r = new Rectangle(x*blockSize, y*blockSize, blockSize, blockSize);
  		r.setFill(Color.ORANGE);
  		r.setOnMousePressed(ev ->  {	setState("removing");	cells.remove(r); 
  										ev.consume();    updateMatrix();}	);
  		return r;
  	}

  	public void addPoint(int x, int y) {  		cells.add(makeCell(x, y));  	}
  	//-------------------------------------------------------------------
  	/**
  	 * Add in the Gosper Glider Gun as an initial state
  	 * {@link https://en.wikipedia.org/wiki/Gun_(cellular_automaton)}
  	 */
  	public void reset() {
  		cells.clear();
  		gridPanel.getChildren().clear(); 	
		gridPanel.getChildren().add(grid);

  		addPoint(2, 14);  		addPoint(2, 15);  		addPoint(3, 14);  		addPoint(3, 15);
  		//
  		addPoint(12, 14);  		addPoint(12, 15);  		addPoint(12, 16);  		addPoint(13, 13);
  		addPoint(13, 17);  		addPoint(14, 12);  		addPoint(15, 12);  		addPoint(14, 18);
  		addPoint(15, 18);  		addPoint(16, 15);  		addPoint(17, 13);  		addPoint(17, 17);
  		addPoint(18, 14);  		addPoint(18, 15);  		addPoint(18, 16);  		addPoint(19, 15);
  		//
  		addPoint(22, 14);  		addPoint(22, 13);  		addPoint(22, 12);  		addPoint(23, 14);
  		addPoint(23, 13);  		addPoint(23, 12);  		addPoint(24, 11);  		addPoint(24, 15);
  		addPoint(26, 11);  		addPoint(26, 10);  		addPoint(26, 15);  		addPoint(26, 16);
  		//
  		addPoint(36, 12);  		addPoint(36, 13);  		addPoint(37, 12);  		addPoint(37, 13);
  		updateMatrix();
  	}
  }
