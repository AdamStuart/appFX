package image.edit;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class IEToolPane extends GridPane {
	
	private IEToolManager toolManager;
	
	public IEToolPane(IEToolManager toolManager)
	{
		super();
		this.toolManager = toolManager;
		initToolPane();
	}
	
	/**
	 * Initializes the tool pane and adds the appropriate buttons.
	 */
	private void initToolPane()
	{
		Button b1 = new Button(" ");
		Button b2 = new Button(" ");
		Button b3 = new Button(" ");
		add(b1, 0, 0);
		add(b2, 1, 0);
		add(b3, 0, 1);
	}
	
}
