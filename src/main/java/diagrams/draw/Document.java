package diagrams.draw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import util.FileUtil;
/*
 * Document takes care of reading the state from a file, or writing it to one.
 */
public class Document
{
	
	Controller drawController;
	File file = null;
	private int verbose = 0;
	
	public Document(Controller inDC)
	{
		drawController = inDC;
	}
	// **-------------------------------------------------------------------------------
	public void open()		
	{ 	
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Drawing");
		file = chooser.showOpenDialog(App.getInstance().getStage());
		if (file == null)			return;			// open was canceled
		org.w3c.dom.Document doc = FileUtil.openXML(file);
		if (doc != null)
			drawController.addState(doc);					//  parse XML to sceneGraph
//		if (verbose  > 0) 	System.out.println(s);
	}
	
	// **-------------------------------------------------------------------------------
	public void save()		
	{ 	
		if (file == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Save Drawing");
			file = chooser.showSaveDialog(App.getInstance().getStage());
			if (file == null) return;
			App.getInstance().getStage().setTitle(file.getName());
		}
		if (verbose > 0) System.out.println("about to do the save traversal");
		String buff =  drawController.getState();
		if (verbose > 0) System.out.println(buff);
		 try (FileOutputStream out = new FileOutputStream(file)) 
		 {
		    out.write( buff.getBytes());
			 out.close();
		 } 
		 catch (IOException e) {     e.printStackTrace();  }
	}
	
	public void saveas()		
	{ 	
		file = null;
		save();	
	}
	// **-------------------------------------------------------------------------------
	boolean fileDirty = false;
	
	public void close()		
	{ 	
//		if (fileDirty)	askToSave();
		file = null;
	}
	// **-------------------------------------------------------------------------------
	// TODO:  only prints first page, without scaling it.
	
	public void print()
	{
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) return;
		boolean success = job.printPage(drawController.getPasteboard().getPane());
		if (success)
			job.endJob();
	}
	// **-------------------------------------------------------------------------------

}
