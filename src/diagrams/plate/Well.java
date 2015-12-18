package diagrams.plate;

import gui.Backgrounds;
import gui.Borders;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.AttributeValue;

public class Well 
{
	private int row;
	private int column;
	private DoubleProperty height;
	private final PlateModel parent;
	private StackPane stack = null;
	private ObservableList<AttributeValue> attributes;

	//------------------------------------------------------------------------------------
	public Well(final PlateModel model, int r, int c)
	{
		row = r;
		column = c;
		parent = model;
		attributes = FXCollections.observableArrayList();
		height = new SimpleDoubleProperty(5 + 100 * Math.random());  //1.5 * (8-r) * 1.2 * (12-c));
	}
	
	public DoubleProperty heightProperty()	{		return height;	}
	public void resetData() { setData(0);	}
	public void setData(double d) 			{ 		height.set(d);	}
	//------------------------------------------------------------------------------------
	private BooleanProperty selected = new SimpleBooleanProperty();
	public BooleanProperty selectedProperty()			{		return selected;	}
	public boolean getSelected()		{		return selected.get();	}
	public void setSelected(boolean b)	
	{		
		selected.set(b);	
		updateStackBackground();
	}
	
	private BooleanProperty hilited = new SimpleBooleanProperty();
	public BooleanProperty hilitedProperty()			{		return hilited;	}
	public boolean getHilited()			{		return hilited.get();	}
	public void setHilited(boolean b)	
	{	
		hilited.set(b);	
		updateStackBackground();
	}

private void updateStackBackground()
{
	boolean isHilited = getHilited();
	boolean isSelected = getSelected();
	Color c = isHilited ? hilitedColor : (isSelected ? selectedColor : unselectedColor);
	stack.setBackground(Backgrounds.colored(c)) ;

}
	//------------------------------------------------------------------------------------

	public StackPane makeWellPane()
	{
		Rectangle box = new Rectangle();
		box.setWidth(50);
		box.setHeight(50);
		box.setFill(unselectedColor.darker());
//		box.getStyleClass().add("well.box");
		hilited.addListener((observable, oldValue, newValue) ->	{	updateStackBackground(); });

		
		Circle cir = new Circle(14);
//		cir.getStyleClass().add("well");
		Text text = new Text(getDescriptor());
//		text.getStyleClass().add("wellText");
		stack = new StackPane(box, cir, text);
		box.widthProperty().bind(stack.widthProperty().divide(1.6));
		box.heightProperty().bind(stack.heightProperty().divide(1.6));
		GridPane.setHgrow(stack, Priority.ALWAYS);
		GridPane.setVgrow(stack, Priority.ALWAYS);
		text.setMouseTransparent(true);
		stack.setBorder(Borders.lineBorder);

		stack.setOnDragEntered(ev -> { dragEntered(ev, stack);	  ev.consume(); });
		stack.setOnDragExited(ev -> { 	dragExited(ev, stack);	 ev.consume(); 	});	
		stack.setOnMouseEntered(ev -> { getController().setAttributeText(getAttributeText());	  ev.consume(); });
		stack.setOnMouseExited(ev -> { 	getController().setAttributeText(getAttributeText()); ev.consume(); 	});	
//		cir.setOnMouseClicked(ev -> { doCircleClick(ev);	});
//		cir.setOnMouseEntered(ev -> { getController().setAttributeText(getAttributeText());		});

		stack.setOnDragOver(event ->
		{
			if (event.getGestureSource() != stack)
				event.acceptTransferModes(TransferMode.COPY);
			event.consume();
		});

		stack.setOnDragDropped(event ->
		{
			Dragboard db = event.getDragboard();
			boolean success = false;
			
			if (db.hasContent(PlateController.avListDataFormat))
			{
				Object raw = db.getContent(PlateController.avListDataFormat);
				ObservableList<AttributeValue> avs = AttributeValue.parseList(raw.toString());	
				if (avs != null)
					if (getSelected())		parent.addAttributesToSelection(avs);
					else	addAttributes(avs);
				success = true;
			} 
			else if (db.hasContent(PlateController.avDataFormat))
			{
				String raw = "" + db.getContent(PlateController.avDataFormat);
				AttributeValue av =  new AttributeValue(raw);		// split at :
				if (getSelected())		parent.addAttributeToSelection(av);
				else	addAttribute(av);
				addAttribute(av);
				System.out.println(getDescriptor() + ": " + av.getAttribute() + " = " + av.getValue());
				success = true;
			}
			else  if (db.hasString()) 
			{
				System.out.println(db.getString());
				success = true;
			 }
			/*
			 * let the source know whether the string was successfully transferred and used
			 */
			event.setDropCompleted(success);
			// stack.getStyleClass().remove( "selected");
			event.consume();

		});

		return stack;
	}

	//------------------------------------------------------------------------------------
	static Color selectedColor = Color.RED;
	static Color unselectedColor = Color.CORNSILK;
	static Color unhilitedColor = Color.BEIGE;
	static Color hilitedColor = Color.GREEN;
	
	private void dragEntered(DragEvent ev, StackPane stack)
	{
		setHilited(true);  
		if (ev.getGestureSource() != stack) 
			updateStackBackground();
	 }
	
	private void dragExited(DragEvent ev, StackPane stack)
	{
		setHilited(false);  //stack.setBackground(Backgrounds.colored(unselectedColor)) ;	
		updateStackBackground();
	 }

	//------------------------------------------------------------------------------------
	public void doCircleClick(MouseEvent ev)
	{
		clearFirst(ev);
		setSelected(true);
		// beep
		System.out.println("click: " + getDescriptor());
		attributes.forEach(av -> System.out.println(av.makeString()));
		getController().setAttributeText(getAttributeText());
	}
	//------------------------------------------------------------------------------------
//	private void doCircleActive(MouseEvent ev)		{		getController().setAttributeText(getAttributeText());	}
//	private void doCircleInActive(MouseEvent ev)		{		getController().setAttributeText("");	}
	//------------------------------------------------------------------------------------
	private PlateController getController()		{		return parent.getController();	}
	private String getAttributeText()
	{
		StringBuilder buf = new StringBuilder(getDescriptor()+"\n");
		for (AttributeValue av : attributes)
			buf.append(av.toString()).append("\n");
		return buf.toString();
	}
	//------------------------------------------------------------------------------------
	private void clearFirst(MouseEvent ev)
	{
		boolean modified =  (ev.isControlDown() || ev.isShiftDown());
		if (!modified)
			parent.clearSelection();
	}
	
	//------------------------------------------------------------------------------------
	public void draw()
	{
		System.out.println("draw: " + getDescriptor());
	}
	
	public void addAttributes(ObservableList<AttributeValue> avs)	{		attributes.addAll(avs);	}
	public void addAttribute(AttributeValue av)	{		attributes.addAll(av);	}

	// ---------------------------------------------------------------------------------------------------
// TODO -- 96 this doesn't work for 1684 plates where AA - AF are needed
	public String getDescriptor()
	{
		char c = (char) ('A' + row);
		String colStr = ("0" + (1 + column));
		int len = colStr.length();
		if (len > 2)
			colStr = colStr.substring(1, 3);
		return c + colStr;
	}

	// ---------------------------------------------------------------------------------------------------
	   final public static String wellNotation96(int i, int j)
	   {
		   return wellNotation96(i * 12 + j);	
	   }
	   
	final static public String wellNotation96(int i)
	{
		return padTo3("" + (char) ('A' + (i / 12)) + (int) (1 + (i % 12)));
	}

	final static public int wellToInt96(String s)
	{
		return (int) (s.charAt(0) - 'A') * 12 + Integer.parseInt(s.substring(1));
	}

	final static private String padTo3(String s)
	{
		return (s.length() == 3 ? s : "" + s.charAt(0) + '0' + s.charAt(1));
	}
	   
	
	
}
