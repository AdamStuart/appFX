package chart.heatmap.draggable;

import chart.usMap.ColorUtil;
import chart.waterloo.markers.Square;
import gui.Backgrounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.stat.Range;

public class RowGroup extends Group {
	Square[] squares;
	Label label;
	SquareMap parentMap;
	int length; 
	int index; 		// what row am I
	public int getIndex() 		{	return index;	}
	public void setIndex(int i) {	index = i;	}
	// String name;  -- saved in label.getText
	public String getName()	{ return label.getText();	}
	public void setText(String string) {		label.setText(string);		}

	public void dump(String name) {   System.out.println(name + ", " + toString());	}
	
	public String toString() { 	
		return  getName() + "@" + getIndex() + " , " + (isVisible() ? "V" : "X") + 
			" " + (int) getTranslateY() + " " + (int) getLayoutY();
	}

	
	public RowGroup(int len, int row, String name, SquareMap map)
	{
		super();
		parentMap = map;
		length = len;
		index = row;
		if (squares == null)			squares = new Square[len];
		label = makeHeaderLabel(name, row);
		setUserData(name);
		for (int j=0; j<len; j++) 
		{
			squares[j] = new Square(SquareMap.XOFFSET + SquareMap.halfWidth + SquareMap.CELL_WIDTH * j, SquareMap.halfWidth, SquareMap.halfWidth);
			getChildren().add(squares[j]);
		}
		setTranslateY(SquareMap.MARGIN + SquareMap.CELL_WIDTH * row);
		setTranslateX(SquareMap.MARGIN);
		setOnDragDetected(ev -> rowDragStart(ev, index));
		setOnMouseDragged(ev -> rowDrag(ev));
		setOnMouseReleased(ev -> rowDragReleased(ev));

	}
	
	
	private Label makeHeaderLabel(String name, int i) {
		Label label = new Label(name);
		label.setFont(Font.font(8));
		label.setPrefWidth(SquareMap.XOFFSET);
		label.setPrefHeight(SquareMap.CELL_WIDTH-6);
		label.setMaxHeight(SquareMap.CELL_WIDTH-6);
		label.setBackground(Backgrounds.colored(Color.AZURE));
//		label.setBorder(Borders.thinEtchedBorder);
		getChildren().add(label);
		return label;
	}
	
	public void fillRow(int idx, double[] input, Range r)
	{
		index = idx;
		if (squares == null) return;
		setSquares(input, r);
		setText(parentMap.getName(index));
		setTranslateX(SquareMap.MARGIN);
		setTranslateY(SquareMap.MARGIN + SquareMap.CELL_WIDTH * index);
		setLayoutY(0);
	}

	public void setSquares(double[] input, Range r) {
		for(int col=0; col<length; col++)
			squares[col].setFill(valToColor(input[col], r));	
	}
	static boolean GRAYSCALE = false;

	Color valToColor(double v, Range r) {
		double x = Math.min(1, (v - r.min()) / (r.width()));
		return (GRAYSCALE) ? ColorUtil.gray(x) : ColorUtil.blueYellow(x);
	}
	//-----------------------------------------------------
	// DRAGGING
	//-----------------------------------------------------
	
	private static double yStart;	// where the mouse went down
	private static double yRef;		// the coordinates we watch for the next swap
	private static double yBase;		// top left of hit row		
	private static double yMin, yMax;		// top and bottom allowable y values	
	private static double yOffset = -1;	// difference between mouse location and nodes's top-left
//	private static int dragIndex;
//	private static Group dragGroup;

	private ImageView imgView = null;

	private void rowDragStart(MouseEvent ev, int dragIndex)
	{
		yOffset = ev.getY();
		yRef = yStart = ev.getSceneY(); //  - getTranslateY();
//		String name = "" + getUserData();
		yBase = SquareMap.MARGIN + (dragIndex * SquareMap.CELL_WIDTH);
		yMin = SquareMap.MARGIN + yOffset - getTranslateY(); 		//yStart - (dragIndex * SquareMap.CELL_WIDTH)
		yOffset = yRef - yBase;
		yMax = (parentMap.getMapBottom() - SquareMap.CELL_WIDTH + yOffset);
//		System.out.println("-----------------\nDragging: " + name + " " + dragIndex );
//		System.out.println("Y Coords: " + (int) yRef + " / " + (int) yBase + " / "  + (int)yOffset + " / "  + (int) yStart );
//		System.out.println("Y Range: " + (int) yMin + " - " + (int) yMax  );

		int w = (int) parentMap.getTotalRowWidth();
		int h = (int) parentMap.getRowHeight();
		WritableImage img = new WritableImage(w, h);
		snapshot(null, img);
		imgView = new ImageView(img);	
		imgView.setX(getTranslateX());
		double ystart = yBase; //  getTranslateY();
		imgView.setY(ystart);
		parentMap.addDragImage(imgView);
		setVisible(false);
	ev.consume();
	System.out.println("rowDragStart" );
	}


	private void rowDrag(MouseEvent ev)		//, String name
	{
		System.out.println("rowDrag" );
		if (yOffset < 0) return;
		double dy = ev.getSceneY() - yStart;
		if (imgView != null && ev.getSceneY() > yMin  && ev.getSceneY() < yMax)  //  )
			imgView.setTranslateY(dy);

		dy = ev.getSceneY() - yRef;
		double magnitude = Math.abs(dy);
		if (magnitude > SquareMap.CELL_WIDTH)
		{
			dy = ev.getSceneY() - yRef;
			magnitude = Math.abs(dy);
//			System.out.println("Swap:  sceneY: " + (int) ev.getSceneY() + 
//					", yRef: " + (int)  yRef +  ", magn: " + (int) magnitude	);
			boolean swapUp = dy < 0;
			int idx = getIndex();
			if ((idx <= length-(swapUp ? 1 : 2))  && idx >= (swapUp ? 1 : 0))
			{
				parentMap.swap(idx, swapUp);
//				int index = getIndex();
//				System.out.println("idx: " + (int) index + (swapUp ? " up" : " down"));
			}
			updateYRef(dy < 0);
		}
		ev.consume();
	}
	
	private void updateYRef(boolean swapUp) {
		yRef += SquareMap.CELL_WIDTH * (swapUp ? -1 : 1);
	}
	
	private void rowDragReleased(MouseEvent ev)		//, String name
	{
		yOffset = -1;
		setVisible(true);
		if (imgView != null)
		{
			imgView.setVisible(false);
			parentMap.killDragImage();
		}
		parentMap.finishDrag(ev);
	}
	
}
