package uploader;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

public class FeedbackNodes
{
	private Label label;
	private ProgressBar progress;
	private Logger logger;
	private ImageView image;
	
	public FeedbackNodes(Label l, ProgressBar p, ImageView i, Logger logr)
	{
		label = l;
		progress = p;
		logger = logr;
		image = i;
	}
	public Label getLabel() 			{ return label; 	}
	public ProgressBar getProgressBar() { return progress; 	}
	public ImageView getImageView() 	{ return image; 	}
	public void log(String msg) 		
	{ 
		if (logger != null) logger = Logger.getGlobal();
		if (logger != null) logger.log(Level.INFO,msg); 	
	}
}
