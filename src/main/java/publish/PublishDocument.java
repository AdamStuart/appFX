package publish;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.FileUtil;
import xml.XMLFactory;
import xml.XMLTools;
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
	static public Document open()		
	{ 	
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Document");
		File file = chooser.showOpenDialog(PublishController.getStage());
		if (file == null)			return null;			// open was canceled		
		
		Document doc = null;
		if (FileUtil.isXML(file))
		{
			try {
				doc = FileUtil.openXML(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return doc;
		}
		if (FileUtil.isCSV(file))
		{
//			controller.openCSVfile(file);
		}
		return doc;
	}
	
	// **-------------------------------------------------------------------------------
	public File getSaveDestination()
	{
		if (file == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Save Experiment");
			file = chooser.showSaveDialog(PublishController.getStage());
			if (file != null) 
				PublishController.getStage().setTitle(file.getName());
		}
		return file;
	}
	
	// **-------------------------------------------------------------------------------
	boolean fileDirty = false;
	
	public void close()	{ 	file = null;	}
	public void reset()	{	file = null;	}
	// **-------------------------------------------------------------------------------
	// TODO:  only a shell of a print job
	
	public void print()
	{
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) return;
		boolean success = false;
		if (success)
			job.endJob();
	}
	// **-------------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------------------------
	// TODO -- persistence is not finished.  Questions about enclosing data vs. referring to it.
	
	private List<XMLEvent> steps;
	private XMLEventFactory  werk = 	XMLEventFactory.newInstance();

	public void saveas()		
	{ 	
		reset();
		save();	
	}
	
	public void save()
	{
		File f = getSaveDestination();
		if (f != null)
		{
			steps = new ArrayList<XMLEvent>();
			steps.add(werk.createStartElement( "", "", "Publication"));
			extractState(); 
			extractHypothesis();				// TODO these should be bound, not extracted
			extractResearch();
			extractMethods();
			extractResults();
			extractAnalysis();
			extractDiscussion();
			steps.add(werk.createEndElement( "", "", "Publication"));
			XMLFactory.writeEvents(steps, f.getAbsolutePath());
		}
	}
	private void extractState()
	{
		// window positions, active tab, selections, etc
		steps.add(werk.createStartElement( "", "", "State"));
		steps.add(werk.createAttribute( "active", controller.getActiveTab()));
		steps.add(werk.createAttribute( "x", "" + controller.getScene().getX()));
		steps.add(werk.createAttribute( "y", "" + controller.getScene().getWindow().getY()));
		steps.add(werk.createEndElement( "", "", "State"));
	}
	
	private void extractHypothesis()
	{
		steps.add(werk.createStartElement( "", "", "Hypothesis"));
		steps.add(werk.createAttribute("species", controller.getSelectedSpecies()));
		steps.add(werk.createAttribute("celltype", controller.getSelectedCellType()));
		steps.add(werk.createAttribute("technology", controller.getSelectedTechnology()));
		steps.add(werk.createStartElement( "", "", "Keywords"));
		steps.add(werk.createCData(controller.getKeywords()));
		steps.add(werk.createEndElement( "", "", "Keywords"));
		steps.add(werk.createStartElement( "", "", "Content"));
		steps.add(werk.createCData(controller.getHTML()));
		steps.add(werk.createEndElement( "", "", "Content"));
		steps.add(werk.createEndElement( "", "", "Hypothesis"));
	}
	
	private void extractResearch()
	{
		steps.add(werk.createStartElement( "", "", "Research"));
		controller.getQuerier().setXML(werk, steps);
		steps.add(werk.createEndElement( "", "", "Research"));
	}
	
	private void extractMethods()
	{
		steps.add(werk.createStartElement( "", "", "Methods"));
		if (methodsPath != null) 
		{
			steps.add(werk.createStartElement( "", "", "File"));
			steps.add(werk.createAttribute("path", getMethodsFilePath()));
			steps.add(werk.createEndElement( "", "", "File"));
		}
		steps.add(werk.createEndElement( "", "", "Methods"));
	}
	
	String methodsPath = null;
	private String getMethodsFilePath()			{		return methodsPath;	}
	public void setMethodsFilePath(String inS)	{		methodsPath = inS;	}
	
	private void extractResults()
	{
		steps.add(werk.createStartElement( "", "", "Results"));
		steps.add(werk.createEndElement( "", "", "Results"));
	}
	
	private void extractAnalysis()
	{
		steps.add(werk.createStartElement( "", "", "Analysis"));
		steps.add(werk.createEndElement( "", "", "Analysis"));
	}
	
	private void extractDiscussion()
	{
		steps.add(werk.createStartElement( "", "", "Discussion"));
		steps.add(werk.createCData(controller.getDiscussionHTML()));
		steps.add(werk.createEndElement( "", "", "Discussion"));
	}
	
	//-------------------------------------------------------------------------------------------

	public void install(Document doc)
	{
		if (doc == null) return;
		Element experiment = doc.getDocumentElement();
		Map<String, org.w3c.dom.Node> partMap = XMLFactory.readElements(experiment);
		setState(partMap.get("State"));
		setHypothesis(partMap.get("Hypothesis"));
		setResearch(partMap.get("Research"));
		setMethods(partMap.get("Methods"));
		setResults(partMap.get("Results"));
		setAnalysis(partMap.get("Analysis"));
		setDiscussion(partMap.get("Discussion"));
	}
	
	private void setState(org.w3c.dom.Node elem)
	{
		// window positions, active tab, selections, etc
		if (elem != null)
		{
			String active = XMLTools.getChildAttribute(elem, "active");
			controller.setActiveTab(active);
			double x = XMLTools.getDoubleAttribute(elem, "x");
			double y = XMLTools.getDoubleAttribute(elem, "y");
			Stage stage = controller.getStage();
			stage.setX(x);
			stage.setY(x);
		}
	}
	private void setHypothesis(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			String Species = XMLTools.getChildAttribute(elem, "species");
			String Celltype = XMLTools.getChildAttribute(elem, "celltype");
			String Technology = XMLTools.getChildAttribute(elem, "technology");
			controller.setSelectedSpecies(Species) ;
			controller.setSelectedCellType(Celltype);
			controller.setSelectedTechnology(Technology);

			org.w3c.dom.Node child = XMLTools.getChildByName(elem, "Keywords");
			if (child != null)  			controller.setKeywords(child.getTextContent());
			child = XMLTools.getChildByName(elem, "Content");
			if (child != null) 				controller.setHTML(child.getTextContent()); 
		}
	}
	
	private void setResearch(org.w3c.dom.Node elem)
	{
		if (elem != null)
			controller.getQuerier().setXML(elem);
	}
	
	private void setMethods(org.w3c.dom.Node elem)
	{
		if (elem != null)
		{
			org.w3c.dom.Node child = XMLTools.getChildByName(elem, "File");
			if (child != null)
			{
				String path = XMLTools.getChildAttribute(child, "path");
				if (path != null)
				{
					File f = new File(path);
					EDLParsingHelper.setEDLDirectory(f, controller);
				}
			}			
		}
	}
	
	private void setResults(org.w3c.dom.Node elem)
	{
		if (elem != null)		{ }
	}
		
	private void setAnalysis(org.w3c.dom.Node elem)
	{
		if (elem != null)		{ }
	}
	
	private void setDiscussion(org.w3c.dom.Node elem)
	{
		if (elem != null)
			if (elem instanceof Element)
				controller.setHTML(((Element)elem).getTextContent());
	}

	//-------------------------------------------------------------------------------------------
	
}
