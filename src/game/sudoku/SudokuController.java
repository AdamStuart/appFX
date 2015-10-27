package game.sudoku;

import gui.Borders;
import gui.Cursors;
import gui.Effects;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import util.NodeUtil;
import util.SoundUtility;
import util.StringUtil;

public class SudokuController
{
	@FXML private GridPane grid;
	private ToggleGroup numButtons;
	@FXML private HBox numberButnHBox;
	GridPane[] subGrids = new GridPane[9];
	Cell[] allCells = new Cell[81];
	ToggleButton[] numberButtons = new ToggleButton[9];
	Game game;
	public void initialize()
	{
//		numButtons = btn1.getToggleGroup();
		numButtons = new ToggleGroup();
		for (int i=1;i<=9; i++)
		{
			ToggleButton btn = new ToggleButton("" + i);
			numButtons.getToggles().add(btn);
			numberButnHBox.getChildren().add(btn);
			numberButtons[i-1] = btn;
		}	
//		numButtons.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> 
//		  System.out.println("Selected toggle changed from "+oldToggle+" to "+newToggle)
//        );
		

		grid.getStyleClass().add("grid");
		grid.setStyle("-fx-background-color: whitesmoke;");
//		HBox.setMargin(grid, new Insets(20,20,20,20));
		for (int i=0; i<9; i++)
		{
			GridPane subGrid = new GridPane();
			GridPane.setConstraints(subGrid,  i%3, i/3, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS );
			subGrids[i] = subGrid;
			subGrid.setGridLinesVisible(true);
			subGrid.getColumnConstraints().add(new ColumnConstraints(WIDTH));  // TODO DOESN'T WORK
			subGrid.getStylesheets().add("subgrid");
			subGrid.setBorder(Borders.dashedBorder);
			grid.getChildren().add(subGrid);
			
//			subGrid.setOnMouseEntered(ev -> {  ((Node) ev.getTarget()).setEffect(Effects.darkinnershadow); });  
//			subGrid.setOnMouseExited(ev -> mouseExited(ev));
			
			for (int j = 0; j  <9; j++)
			{
				int row =3 * ( i / 3) + j / 3;
				int col = 3 * (i % 3) + j % 3;
				subGrid.getChildren().add(new Cell(row,col));
			}
		}
		doNew();
	}
	
	//------------------------------------------------------
	int WIDTH = 55;
	class Cell extends StackPane
	{
		int row, column;
		Label label;
		
		Cell(int rw, int col)
		{
			super();
			row = rw;
			column = col;
			System.out.println("cell made at: " + row + " , " + column);
			NodeUtil.forceSize(this,WIDTH, WIDTH);
			label = new Label("");
//			NodeUtil.forceSize(label,WIDTH, WIDTH);
			getChildren().add(label);
			allCells[9*row + col] = this;
			GridPane.setConstraints(this,  col%3, row%3, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS );
			label.setStyle("-fx-font-size: 30px;");
			label.getStyleClass().add("cell");
//			label.setBorder(Borders.blueBorder1);
//			setBorder(Borders.cyanBorder);
			setOnMouseEntered(ev -> {	setEffect((game.getNumber(column, row) == 0) ?  Effects.red_innershadow : null); });  
			setOnMouseExited(ev ->  { 	setEffect(null); setCursor(Cursor.DEFAULT);	 });	
			setOnMouseClicked(ev -> {	clickCell(ev);	});		
			setOnMouseMoved(ev ->   {
				ToggleButton cur = (ToggleButton)numButtons.getSelectedToggle();
				if (cur != null)
				{
					String text = cur.getText();
					if (game.getNumber(column, row) == 0)
						setCursor(Cursors.getTextCursor(text, Color.GREEN));
				} });		
		}
		
		Label getLabel()	{ return label;	}
		
		// add props to cell for row and col and segment and position
		// states are selected, guessed
		
		private void clickCell(MouseEvent ev)
		{
			String text = ((ToggleButton)numButtons.getSelectedToggle()).getText();
			if (StringUtil.hasText(text))
			{
				int curVal = game.getNumber(column, row);
				if (curVal == 0)
				{
					int guess = StringUtil.toInteger(text);
					int correctVal = game.getSolution(column,row);
					if (correctVal == guess)
					{
						game.setNumber(column,row, guess);
						setCursor(Cursor.NONE);
						populate();
					}
					else
						reportError();
				}
			}
		}

		private void reportError()
		{
			SoundUtility.playSound("BEEP");
			System.out.println("your guess was wrong~!!!!");			
		}
	}
	//------------------------------------------------------

	@FXML private void doNew()  
	{
		game = new Game();
		for (int i=0; i<9; i++)
			numberButtons[i].setDisable(false);
		populate();
		numButtons.selectToggle(numberButtons[0]);			// start with 1 selected
	}

	@FXML private void doCheck()  
	{
		game.solve();
		populate();
	}

	private void solved()  
	{
		System.out.println("YAHOO!" );	
	}


	//------------------------------------------------------
	
	void populate()
	{
		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
			{
				Label label = getLabel(i,j);   
				if (label == null) 
					System.out.println("populate");
				else
				{
					int number =  game.getNumber(i, j);
					label.setText(number > 0 ? ("" + number ): "");
				}
			}
		game.dump(null);
		int numbersFinished = 0;
		for (int i=0; i<9; i++)
			if (game.allFilled(i+1)) 
			{
				numberButtons[i].setDisable(true);
				numbersFinished++;
			}
		if (numbersFinished == 9)
			solved();
		else 		// make sure something is active
		{
			ToggleButton butn = (ToggleButton) numButtons.getSelectedToggle();
			if (butn == null || butn.isDisable())
				for (int i=0; i<9; i++)
					if (!numberButtons[i].isDisable())
					{	
						numButtons.selectToggle(numberButtons[i]);
						break;
				
					}
		}
	
	
	}

// a hack to work around the order labels are created (by section, not row and column)
	private Label getLabel(int  col, int row)
	{
//		int section = 3 * (row / 3) + (col / 3);		
//		int position = 3 * (row % 3) + (col % 3);
		
		return allCells[9*row+col].getLabel();
	}
}
