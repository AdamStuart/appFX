package publish;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;

import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import util.FileUtil;
/*
 * Document takes care of reading the state from a file, or writing it to one.
 */
public class PublishDocument
{
	
	PublishController controller;
	File file = null;
	
	public PublishDocument(PublishController inDC)
	{
		controller = inDC;
	}
	// **-------------------------------------------------------------------------------
	public void open()		
	{ 	
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Drawing");
		file = chooser.showOpenDialog(PublishController.getStage());
		if (file == null)			return;			// open was canceled
		Document doc = FileUtil.openXML(file);
		controller.install(doc);					//  parse XML to sceneGraph
	}
	
	// **-------------------------------------------------------------------------------
	public File getSaveDestination()
	{
		if (file == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Save Experiment");
			file = chooser.showSaveDialog(PublishController.getStage());
			if (file == null) 
				PublishController.getStage().setTitle(file.getName());
		}
		return file;
	}
	
	// **-------------------------------------------------------------------------------
	boolean fileDirty = false;
	
	public void close()		{ 			file = null;	}
	public void reset()	{		file = null;		}
	// **-------------------------------------------------------------------------------
	// TODO:  only prints first page, without scaling it.
	
	public void print()
	{
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) return;
		boolean success = false;
		if (success)
			job.endJob();
	}
	// **-------------------------------------------------------------------------------

}
