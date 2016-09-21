package diagrams.draw;

import java.awt.Dimension;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class Action
{
	public Action(ActionType t, String unusedText)
	{
		type = t;
		debugText = unusedText;
		state = ActionState.New;
		creation = System.currentTimeMillis();
	}
	
	public Action(ActionType t)
	{
		this(t, ""); 
	}
	
	public void doIt()			{ state = ActionState.Done;  }
	public void undo()			{ state = ActionState.Undone;  }	
	public boolean isDone()		{ return state == ActionState.Done; }
	public boolean isUndone()	{ return state == ActionState.Undone; }
	
	ActionType type = ActionType.Undefined;
	protected Controller controller;
	public void setController(Controller c)	{		controller =c;	}
	public Controller getController()		{		return controller;	}
	
	protected ActionState state;
	final long creation;
	protected String debugText;
	public String getText()			{	return debugText;	}
	public void setText(String s)			{	debugText = s;	}

	protected String preState;
	public void saveState()		{	preState = controller.getState();	}
	public String getState()	{	return preState;	}
	
	private Dimension amountMoved = null;				// unused but reserved for animated undo 	
	private Dimension amountResized = null;	
	private double amountRotated = 0;
	
	
	@Override
	public String toString()	
	{ 
		String selectionString;
		int siz = theSelection.size();
		if (siz == 0)			selectionString = "";
		else if (siz == 1)		selectionString = " " + theSelection.get(0).getId();
		else selectionString = "  [" + siz + "]"; 
		
		return (state == ActionState.Undone ? "undone: " : "") 
				+ type.toString()  + selectionString + getText();
	}
	
	
	protected ObservableList<Node> theSelection;
	public void setSelection( ObservableList<Node> s) 	{	theSelection = FXCollections.observableArrayList(s);	} 
	public ObservableList<Node> getSelection() 			{		return theSelection;	} 

	public enum ActionState {
		New,
		Done,
		Undone;
	}
	
	public enum ActionType {
		
		Undefined,
		Test,
		Add,			// Files
		Align,
		Cut,
		Delete,
		Group,
		Move,
		New,
		Paste,
		Property,
		Resize,
		Reorder,
		Rotate,
		Select,
		Ungroup,
		Zoom,
		Duplicate,
		Connect,
		;
	
	}
}
