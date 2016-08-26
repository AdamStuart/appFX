package chart.heatmap.draggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gui.Backgrounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import model.Range;
import table.networkTable.NodeRecord;

public class SquareMap extends HashMap<String, HashMap<String, Double>> {

	int len = 0;
	ArrayList<String> names = new ArrayList<String>();
	Range range = new Range();
	Group parentGroup = new Group();
	Group foregroundGroup = new Group();
	RowGroup[] rowGroups;
	ImageView imgView;		// dragImage
	
	
	public static double CELL_WIDTH = 10;				// side of one square
	public static double halfWidth = CELL_WIDTH/2;
	public static double XOFFSET = 30;					// room for label on left
	public static int MARGIN = 20;						// room for background around squares

	public double getTotalRowWidth() 	{ 	return names.size() * CELL_WIDTH + XOFFSET;	}
	public double getRowHeight()		{	return CELL_WIDTH;	}
	public double getMapBottom()		{	return MARGIN + (len * CELL_WIDTH);	}
	public Group getParentGroup()		{	return parentGroup;		}
	public void addDragImage(ImageView view) { parentGroup.getChildren().add(imgView = view);  }
	public void killDragImage() 		{ parentGroup.getChildren().remove(imgView); imgView = null; }
	public String getName(int col) {		return names.get(col);	}	
	
	//---------------------------------------------------
	public SquareMap(List<NodeRecord> items) {

		len = items.size();
		double totalSize = len *  SquareMap.CELL_WIDTH + 2 * MARGIN;
		
		Rectangle background = new Rectangle(0, 0, totalSize + SquareMap.XOFFSET, totalSize);
		background.setFill(Backgrounds.sand);
		parentGroup.getChildren().add(background);
		parentGroup.getChildren().add(foregroundGroup);
		
		for (NodeRecord rec : items)
			names.add(rec.getName());
		for (NodeRecord rec : items)
			put(rec.getName(), rec.buildCoexpressionMap(items));	
		range = getTotalRange(items);
//		for (NodeRecord rec : items)
//			rec.normalize(range);

	}
	
	//---------------------------------------------------
	public void makeRows()
	{
		if (rowGroups == null)			rowGroups = new RowGroup[len];
		foregroundGroup.getChildren().clear();
		for (int row=0; row<len; row++) {
			RowGroup rowGroup = new RowGroup(len, row, names.get(row), this);
			rowGroups[row] = rowGroup;
			foregroundGroup.getChildren().add(rowGroup);
		}
	}
	
	public void fillSquares()
	{
		double[][] rawArray = buildArray();
		double[][] inputArray = normalize(rawArray, false);
		for (int row=0; row<len; row++) 
			rowGroups[row].setSquares(inputArray[row], range);
	}

	void swap(int idx, boolean swapUp)
	{
		int neighbor = idx + (swapUp ? -1 : 1);
		if (neighbor < 0 || neighbor >= names.size()) return;
		swapNames(idx, swapUp);
		swapGroups(idx, swapUp);
		
//		Transition t = AnimationUtils.makeSliderY(rowGroups[neighbor], swapUp ? CELL_WIDTH : -CELL_WIDTH);
//		t.setOnFinished(e -> {		
//			System.out.println("pushed " + rowGroups[neighbor].getName() + " " + (swapUp ? "down" : "up") );
// });
//		t.play();
	}

	private void swapNames(int idx, boolean swapUp) {
		String name = names.get(idx);
//		String otherName = names.get(idx + (swapUp ? -1 : 1));
		names.remove(idx);
		names.add(idx+ (swapUp ? -1 : 1)  , name);
//		System.out.println(name + " <-> " + otherName + ":   " + names.toString());
	}

	// swap the groups in their list, then swap back index and translateY
	private void swapGroups(int idx, boolean swapUp) {
		RowGroup temp = rowGroups[idx];
		int otherIdx = idx + (swapUp ? -1 : 1);
		rowGroups[idx] = rowGroups[otherIdx];
		rowGroups[otherIdx] = temp;
		
		double tempY = rowGroups[idx].getTranslateY();
		rowGroups[idx].setTranslateY(rowGroups[otherIdx].getTranslateY());
		rowGroups[otherIdx].setTranslateY(tempY);
		
		int tempIdx = rowGroups[idx].getIndex();
		rowGroups[idx].setIndex(rowGroups[otherIdx].getIndex());
		rowGroups[otherIdx].setIndex(tempIdx);
		rowGroups[otherIdx].setLayoutY(0);
		rowGroups[idx].setLayoutY(0);
		
	}
	
	public void finishDrag(MouseEvent ev) {
//		dump(); 	
		ev.consume();
		makeRows(); 
		fillSquares();
	}
	
//	private void dump()
//	{
//		for (int i=0; i<len; i++)
//			rowGroups[i].dump(names.get(i));
//		System.out.println("");
//	}
	//-----------------------------------------------------------------------------------------
	// model based utilities to get ranges and fill / normalize arrays.
	
	private Range getTotalRange(List<NodeRecord> items)
	{
		Range r = new Range(Double.MAX_VALUE, Double.MIN_VALUE);
		for (NodeRecord rec : items)
			r.union(rec.getRange());
		return r;
	}
//
//	private Range getValueRange() {
//
//		double min = Double.MAX_VALUE;
//		double max = Double.MIN_VALUE;
//		for (String name : names) {
//			HashMap<String, Double> row = get(name);
//			for (String str : names) {
//				double d = row.get(str);
//				if (d < min)	min = d;
//				if (d > max)	max = d;
//			}
//		}
//		return new Range(min, max);
//	}
	
	private Range getValueRange(double [][] values) {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int len = values.length;   // assuming square
		for (int i=0; i<len; i++) {
			for (int j=0; j<len; j++) {
				double d = values[i][j];
				if (d < min)	min = d;
				if (d > max)	max = d;
			}
		}
		return new Range(min, max);
	}

	private double[][] buildArray() {
		int nRows = names.size();
		double[][] matrix = new double[nRows][nRows];
		for (int i = 0; i < nRows; i++) {
			String name = names.get(i);
			HashMap<String, Double> row = get(name);
			for (int j = 0; j < nRows; j++)
				matrix[i][j] = row.get(names.get(j));
		}
		return matrix;
	}
	
	private double [][] normalize(double [][] input, boolean isLog )
	{
		int nRows = input.length;
		Range range = getValueRange(input);
		double [][] output = new double[nRows][nRows];
		int len = input.length;   // assuming square
		for (int i=0; i<len; i++) {
			for (int j=0; j<len; j++) 
				output[i][j] = range.normalize(input[i][j], isLog);
			}
//		Range after = getValueRange(output);		debug to confirm range is 0-1
		return output;
	}
}
