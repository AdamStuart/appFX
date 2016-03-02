package diagrams.plate;

import java.util.List;

import model.AttributeValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;


public class PlateModel 
{
	int nWells = 96; // TODO 96
	int nCols = 12; // TODO 96
	ObservableList<Well> wellData;
	final PlateController controller;
	
	public PlateController getController()		{return controller; }

	public PlateModel(PlateController c, GridPane plate)
	{
		controller = c;
		wellData = FXCollections.observableArrayList();
		for (int i = 0; i < nWells; i++)
			wellData.add(new Well(this, i / nCols, i % nCols));		// make the data model for each well
			
		int nRows = getNRows();  								
		for (int x = 0; x < nCols; x++)
			for (int y = 0; y < nRows; y++)
				plate.add(getWell(x,y).makeWellPane(), x+1,y+1);	// make a stack pane for each well
		
		for (int x = 0; x < nCols; x++)								// make col & row headers
			plate.add(makeColumnHead(x), x+1,0);
		for (int y = 0; y < nRows; y++)
			plate.add(makeRowHead(y),0, y+1);
		plate.add(makeColumnHead(-1), 0,0);

	}
//----------------------------------------------------------------------------------
	private void clearFirst(MouseEvent ev)
	{
		boolean modified =  (ev.isControlDown() || ev.isShiftDown());
		if (!modified)
			wellData.forEach(w -> { w.setSelected(false);} );
	}
	
	public void clearSelection()
	{
		wellData.forEach(w -> { w.setSelected(false);} );
	}
		
	private void hiliteAll(boolean on)
	{
		for (int col=0; col < getNCols(); col++)
			for (int row=0; row < getNRows(); row++)
				getWell(col, row).setHilited(on);
	}
	private void selectAll(boolean on)
	{
		for (int col=0; col < getNCols(); col++)
			for (int row=0; row < getNRows(); row++)
				getWell(col, row).setSelected(on);
	}
	//----------------------------------------------------------------------------------

	private StackPane makeColumnHead(int x)
	{
		StackPane stack = new StackPane();
		String text = "";
		if (x >= 0) text += (x + 1);
		Label label =  new Label(text);
		
		GridPane.setHalignment(label, HPos.CENTER);	
		stack.getChildren().addAll(label);
		stack.setOnDragEntered(ev -> { hiliteColumn(x+1, true);   ev.consume();} );
		stack.setOnDragExited(ev -> { hiliteColumn(x+1, false);   ev.consume();} );
//		stack.setOnDragDropped(ev -> { processColumnDrop(ev, x+1);   ev.consume();} );
		stack.setOnMouseClicked(ev -> { clearFirst(ev);  selectColumn(x+1, true);   ev.consume();} );
		stack.getStyleClass().add("colheader");
		stack.setOnDragOver(event ->
		{
			if (event.getGestureSource() != stack)
				event.acceptTransferModes(TransferMode.COPY);
			event.consume();
		});
		stack.setOnDragDropped(event ->
		{ 
			processDrop(event, (x < 0) ? getAllWells() : getColumnWells(x+1));
			event.consume();
			
		});
		
		return stack;
	}
	private void processColumnDrop(DragEvent ev, int col)
	{
		System.err.println("processColumnDrop " + col);
		
	}

	private void hiliteColumn(int col, boolean on)
	{
		if (col <= 0)
		{
			System.err.println("hiliteAll ");
			hiliteAll(on);
			return;
		}
		System.err.println("hiliteColumn " + col);
		for (int row=1; row <= getNRows(); row++)
			getWell(col-1, row-1).setHilited(on);
	}

	private void selectColumn(int col, boolean on)
	{
		System.err.println("selectColumn " + col);
		if (col < 1) selectAll(on);
		else
		for (int row=1; row <= getNRows(); row++)
			getWell(col-1, row-1).setSelected(on);
	}
	
	//----------------------------------------------------------------------------------
	private StackPane makeRowHead(int y)
	{
		StackPane stack = new StackPane();
		Label label =  new Label("" + (char) ('A' + y));
		stack.getChildren().addAll(label);
		GridPane.setHalignment(label, HPos.CENTER);		
		stack.setOnDragEntered(ev -> { hiliteRow(y+1, true);   ev.consume();} );
		stack.setOnDragExited(ev -> { hiliteRow(y+1, false);   ev.consume();} );
		stack.setOnDragOver(event ->
		{
			if (event.getGestureSource() != stack)
				event.acceptTransferModes(TransferMode.COPY);
			event.consume();
		});
		stack.setOnDragDropped(ev -> {  processDrop(ev, getRowWells(y+1));   ev.consume();} );
		stack.setOnMouseClicked(ev -> { clearFirst(ev); selectRow(y+1, true);   ev.consume();} );
		stack.getStyleClass().add("rowheader");
		return stack;
	}

	private void processDrop(DragEvent ev, 	List<Well> theWells)
	{
		Dragboard db = ev.getDragboard();
		boolean success = false;
		if (db.hasContent(PlateController.avListDataFormat))
		{
			Object raw = db.getContent(PlateController.avListDataFormat);
			ObservableList<AttributeValue> avs = AttributeValue.parseList(raw.toString());	
			if (avs != null)
				for (Well w : theWells)
					w.addAttributes(avs);
			success = true;
		} 
		else if (db.hasContent(PlateController.avDataFormat))
		{
			String raw = "" + db.getContent(PlateController.avDataFormat);
			AttributeValue av =  new AttributeValue(raw);		// split at :
			if (av != null)
				for (Well w : theWells)
					w.addAttribute(av);
			success = true;
		}
		else  if (db.hasString()) 
		{
			System.out.println(db.getString());
			success = true;
		 }
	}

	private void hiliteRow(int row, boolean on)
	{
		System.err.println("hiliteRow " + row);
		for (int col=1; col <= getNCols(); col++)
			getWell(col-1, row-1).setHilited(on);
	}
	
	private void selectRow(int row, boolean on)
	{
		System.err.println("selectRow " + row);
		for (int col=1; col <= getNCols(); col++)
			getWell(col-1, row-1).setSelected(on);
	}
	//----------------------------------------------------------------------------------
	public int getNRows()	{		return nWells / nCols;	}			// generally two thirds of nCols
	public int getNCols()	{		return nCols;	}
	public int getNWells()	{		return nWells;	}

	public Well getWell(int x, int y)	{		return wellData.get(y * nCols + x);	}

	//----------------------------------------------------------------------------------
	
	public List<Well> getRowWells(int row)
	{
		List<Well> wells = FXCollections.observableArrayList();
		for (int col=0; col < getNCols(); col++)
			wells.add(getWell(col, row-1));
		return wells;
	}
	
	public List<Well> getColumnWells(int col)
	{
		List<Well> wells = FXCollections.observableArrayList();
		for (int row=0; row < getNRows(); row++)
			wells.add(getWell(col-1, row));
		return wells;
	}

	public List<Well> getAllWells()
	{
		List<Well> wells = FXCollections.observableArrayList();
		for (int i=0; i < wellData.size(); i++)
			wells.add(wellData.get(i));
		return wells;
	}

	
	public List<Well> getSelectedWells()
	{
		List<Well> wells = FXCollections.observableArrayList();
		wellData.forEach(w -> { if (w.getSelected()) wells.add(w);	} );
		return wells;
	}
	
	public String getSelectedWellStr()
	{
		StringBuilder buff = new StringBuilder();
		wellData.forEach(w -> { if (w.getSelected()) buff.append(w.getDescriptor() + ", ");	} );
		return buff.toString();
	}
	public void addAttributesToSelection(ObservableList<AttributeValue> avs)
	{
		for (Well w : getSelectedWells())
			w.addAttributes(avs);
	}

	public void addAttributeToSelection(AttributeValue av)
	{
		for (Well w : getSelectedWells())
			w.addAttribute(av);
	}

	public void resetData()
	{
		for (Well w : getAllWells())
			w.resetData();
	}

	public void setData()
	{
		for (Well w : getAllWells())
			w.setData(20 + 200 * Math.random());
	}

}
