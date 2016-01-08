package uploader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import dialogs.LoginDialog;
import gui.DropUtil;
import javafx.animation.Transition;
import javafx.beans.binding.NumberExpression;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;
import services.MD5;
import util.FTPException;
import util.FTPUtil;
import util.FileUtil;
import util.StringUtil;
public class UploaderController implements Initializable
{
	static String HOST = "ftp.treister.com";
	static String remoteParentDir = "/doc/zkw/";
	static String remotePath = "";
	static String password = null;
	static String username = "user";
	
	
	@FXML Pane droppane;
	@FXML Label status;
//	@FXML ProgressBar progress;
	@FXML ImageView image;
	@FXML VBox progressContainer;
	
	File packageFile;
	FTPClient ftpClient = new FTPClient();
	private final Image WORMHOLE = new Image(UploaderController.class.getResourceAsStream("wormhole.gif"));
	
	@Override	public void initialize(URL location, ResourceBundle resources)
	{
		DropUtil.makeFileDropPane(droppane, (e) -> uploader(e));
		status.setText("Idle");
		image.setImage(WORMHOLE);
		image.setOpacity(0.);
	}
	
	//----------------------------------------------------------------
	private void uploader(DragEvent e)
	{
		if (validate(e))
		{
			if (connect()) 
				doUploadTask(e);
			else System.out.println("connection FAILED");
		} 	
		else System.out.println("validation FAILED");
}
	List<File> files = FXCollections.observableArrayList();
	//----------------------------------------------------------------
	private boolean validate(DragEvent ev)
	{
		List<File> topfiles = ev.getDragboard().getFiles();
		if (topfiles.size() > 1 ) return false;			// don't overload the FTPClient with multiple connections
		File topFile = topfiles.get(0);
		if (!topFile.isDirectory()) return false;
		try
		{
			makeEDLManifest(topFile);
			makeAcsManifest(topFile);
			status.setText("validated");
		}
		catch(	Exception ex) { return false;	}
		return true;
	}
	
	
	private boolean makeAcsManifest(File topFile)
	{
		try
		{
			StringBuilder manifest = new StringBuilder();
		
			manifest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			manifest.append("<toc:TOC \n");
			manifest.append(" xmlns:toc = \"http://www.isac-net.org/std/ACS/1.0/toc/\"\n");
			manifest.append(" mlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n");
			manifest.append(" xmlns:sig = \"http://www.w3.org/2000/09/xmldsig#\"\n");
			manifest.append(" xsi:schemaLocation = \"http://www.isac-net.org/std/ACS/1.0/toc/");
			manifest.append(" http://flowcyt.sf.net/acs/toc/TOC.v1.0.xsd\"\n>\n");
		
			List<String> objects = new ArrayList<String>();
			mine(topFile, objects);
			for (String s : objects)		manifest.append(s);
			manifest.append("</toc:TOC>");
			FileUtil.writeTextFile(topFile, "TOC1.xml", manifest.toString());
		}
		catch(	Exception ex) { return false;	}
		return true;
	}
	
	private boolean makeEDLManifest(File topFile)
	{
		List<String> objects = new ArrayList<String>();
		List<String> methods = new ArrayList<String>();
		List<String> images = new ArrayList<String>();
		List<String> other = new ArrayList<String>();
		mine(topFile, objects, methods, images, other);	
	
		StringBuilder manifest = new StringBuilder();
		manifest.append("<UploadInformation>\n");
		manifest.append("\t<MetaInfo ID=" + topFile.getName() + "/>\n");
		
		manifest.append("\t<EDLEntities >\n");
		addSection(manifest, "EDLObjects", objects);
		addSection(manifest, "EDLMethods", methods);
		manifest.append("\t</EDLEntities >\n\n");
		
		addSection(manifest, "ImageFiles", images);
		addSection(manifest, "TableFiles", other);
									
		manifest.append("</UploadInformation >\n");
		FileUtil.writeTextFile(topFile, "manifest", manifest.toString());
		return true;

	}

	//----------------------------------------------------------------
	// recurse thru the file system sorting files into four sets
	private void mine(File directory, List<String> objects, List<String> methods, List<String> images, List<String> other)
	{
		if (!directory.isDirectory())	return;		// error
		for (File f : directory.listFiles())
		{
			if (f.getName().startsWith(".")) continue;
			else if (isEDLObjectFile(f)) 	objects.add(getEDLObjectDescriptor(f));
			else if (isEDLMethodFile(f)) 	methods.add(getEDLMethodDescriptor(f));
			else if (isEDLImageFile(f)) 	images.add(getEDLImageDescriptor(f));
			else if (f.isDirectory())  		mine(f, objects, methods, images, other);
			else other.add(getEDLTableDescriptor(f));
		}
	}
	//----------------------------------------------------------------
	// recurse thru the file system sorting files into four sets
	private void mine(File directory,   List<String> fileUrls)
	{
		if (!directory.isDirectory())	return;		// error
		for (File f : directory.listFiles())
		{
			if (f.getName().startsWith(".")) continue;
			else if (f.isDirectory())  		mine(f,fileUrls);
			else fileUrls.add(getFileUrl(f));
		}
	}
	private String getFileUrl(File f)
	{
		return "<toc:file toc:URI = \"file://" + f.getAbsolutePath() + "</toc:file>	\n";
	}

	private void addSection(StringBuilder manifest, String string, List<String> objects)
	{
		manifest.append("\t<" + string + " >\n");
		for (String s : objects)	manifest.append("\t").append(s);
		manifest.append("\t</" + string + " >\n\n");
	}

	//----------------------------------------------------------------
	// RELYING ON CONVENTIONS HERE -- MORE ROBUST PATTERN MATCHING REQD
	

	private boolean isEDLMethodFile(File f)
	{
		String name = f.getName();
		return (name.startsWith("E") && isEDLname(name));
	}
	private boolean isEDLObjectFile(File f)
	{
		String name = f.getName();
		return (!name.startsWith("E") && isEDLname(name));
	}
	private boolean isEDLImageFile(File f)
	{
		return FileUtil.isImageFile(f);
	}
	private boolean isEDLname(String s)
	{
		char c = s.charAt(0);
		boolean startsGood = (c=='E' || c=='D' || c == 'M');
		String tail = s.substring(1);
		boolean rightExt = (tail.endsWith(".XML"));
		String id = StringUtil.chopExtension(tail);
		boolean numberTail = StringUtil.isNumber(id);
		return (startsGood && numberTail && rightExt);
	}
	
	
	private String getEDLObjectDescriptor(File f)
	{
		return " <EDLObject ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + "</EDLObject>\n";
	}
	private String getEDLMethodDescriptor(File f)
	{
		return " <EDLMethod ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + "</EDLMethod>\n";
	}
	private String getEDLImageDescriptor(File f)
	{
		File parent = f.getParentFile();		if (parent == null) return "";
		File gp = parent.getParentFile();		if (gp == null) return "";
		File ggp = gp.getParentFile();			if (ggp == null) return "";
		String id = ggp.getName() + "/" + gp.getName();
		
		return " <ImageFile ID=\"" + id + "\">" + fileSpecWithPath(f) + " </ImageFile>\n";
	}
	private String getEDLTableDescriptor(File f)
	{
		return " <TableFile ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + " </TableFile>\n";
	}

		//----------------------------------------------------------------

//	private void buildManifest(File f, StringBuilder manifest)
//	{
//		manifest.append(fileSpec(f));
//		if (f.isDirectory())			
//			for (File sub : f.listFiles())
//				if (sub.getName().startsWith(".")) continue;
//				else	buildManifest(sub,  manifest);
//	}
//	
	//----------------------------------------------------------------
	private String fileSpec(File f)
	{
		return "<File filename=\"" + f.getName() + "\" length=\""+ f.length() + "\" md5=\"" + MD5.forFile(f) + "\" /> ";
	}
	//----------------------------------------------------------------
	
	String getNAncestors(File f, int n)
	{
		String path = "";
		File file = f.getParentFile();
		while (n-- > 0 &&  file != null)
		{
			path =  file.getName() + "/" + path;
			file = file.getParentFile();
		}
		return path;
	}
	//----------------------------------------------------------------
	private String fileSpecWithPath(File f)
	{
		String path = getNAncestors(f, 4); 
		return "<File filename=\"" + f.getName() + "\" path=\"" + path + "\" length=\""+ f.length() + "\" md5=\"" + MD5.forFile(f) + "\" /> ";
	}
	
	//----------------------------------------------------------------
	private boolean connect() 
	{
		System.out.println("connect");
		if (status != null)
		{
			status.setText("no connect attempted");  
			return false;
		}
		// ---------------SKIPPING THIS FOR NOW----------------
		
        Pair<String, String> creds = null;
        if (username == null || password == null)
        	creds = LoginDialog.authenticate(username, password);
//		if (ftpClient != null) return true;
		try
		{
	        ftpClient.connect(HOST, 21);
	        int  replyCode = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(replyCode)) 
	        	throw new FTPException("FTP server refused connection.");
	 
            boolean logged = ftpClient.login(creds.getKey(), creds.getValue());
            if (!logged) {
                ftpClient.disconnect();		 // failed to login
                throw new FTPException("Could not login to the server.");
            }
            ftpClient.enterLocalPassiveMode();
			status.setText("passiveMode");
		} 
		catch (Exception ex)		
		{	
			System.out.println("connect FAILED: " + ex.getMessage());  
			status.setText("connect FAILED: " + ex.getMessage());
			return false;
		}
		return true;
	}
	
	//----------------------------------------------------------------

	static private UploadTask uploadTask;
	static private ValidationTask validateTask;
	static private ExecutorService uploaderExecutor;
	private void setupUploadExecuter(VBox layout)
	{
		BlockingQueue<File> fileQueue = new PriorityBlockingQueue<File>(files);
		uploadTask = new UploadTask(fileQueue, files.size());
		uploaderExecutor = createExecutor("Upload");
		uploaderExecutor.execute(uploadTask);
		layout.getChildren().add( createProgressPane(validateTask, uploadTask));
	}

	private ExecutorService createExecutor(final String name) {       
    ThreadFactory factory = new ThreadFactory() {
      @Override public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name);
        t.setDaemon(true);
        return t;
      }
    };
    
    return Executors.newSingleThreadExecutor(factory);
  }  
  
	  //----------------------------------------------------------------------------------------
	  private ProgressBar createBoundProgressBar(NumberExpression progressProperty) {
	    ProgressBar progressBar = new ProgressBar();
	    progressBar.setMaxWidth(148);
	    progressBar.progressProperty().bind(progressProperty);
	    GridPane.setHgrow(progressBar, Priority.NEVER);			// was ALWAYS, but that looks bad after completion
	    return progressBar;
	  }
	 	 //----------------------------------------------------------------------------------------
	 private Pane createProgressPane(ValidationTask validateTask, UploadTask uploadTask) {
	    GridPane progressPane = new GridPane();
	    
	    progressPane.setHgap(5);
	    progressPane.setVgap(5);
//	    progressPane.addRow(0, new Label("Validate:"),  createBoundProgressBar(validateTask.progressProperty()));
	    progressPane.addRow(0, new Label("Upload:"));
	    progressPane.addRow(1, createBoundProgressBar(uploadTask.progressProperty()));
	    return progressPane;
	  }
	   
//	    ReadOnlyDoubleProperty validationProgressProperty() {     return validateTask.progressProperty();    }
//	    ReadOnlyDoubleProperty uploadProgressProperty() 	{     return uploadTask.progressProperty();    }
	 //------------------------------------------------------------------------------------------
	  class ValidationTask extends Task<Void> {
	    private final int nFiles;
	    private final BlockingQueue<File> files;
	    
	    ValidationTask(BlockingQueue<File> c, final int fileCt) {
	    	files = c;
	      nFiles = fileCt;
	      updateProgress(0, nFiles);
	    }
	    
	    @Override protected Void call() throws Exception {
	      int i = nFiles;
	      while (i > 0) {
	        if (isCancelled())      break;
	        wait(500);			// ARBITRARY DELAY HERE TO DEBUG
//	        nFiles.put(createChart(getXName(i), getYName(i)));
	        i--;
	        File f = files.iterator().next();
	        updateStatus("Validating: " + f.getName());
	        updateProgress(nFiles - i, nFiles);
	        new Transition() {
	            { setCycleDuration(Duration.millis(250)); }
	            protected void interpolate(double frac) {        	image.setOpacity(frac);        }
	          }.play();
//	        image.setOpacity(1);
	      }
	      return null;
	    }
		private void updateStatus(String str)		{		status.setText(str);		}
	}
		 //------------------------------------------------------------------------------------------
	  class UploadTask extends Task<Void> {
	    private final int nFiles;
	    private final BlockingQueue<File> files;
	    
	    UploadTask(BlockingQueue<File> c, final int fileCt) {
	    	files = c;
	      nFiles = fileCt;
	      updateProgress(0, nFiles);
	    }
	    
	    @Override protected Void call() throws Exception {
	      int i = nFiles;
	      while (i > 0) {
	        if (isCancelled())      break;
	        wait(500);			// ARBITRARY DELAY HERE TO DEBUG
	        File f = files.take();
			FTPUtil.uploadDirectory(ftpClient, f.getName(), f.getAbsolutePath(), remoteParentDir);
//	        nFiles.put(createChart(getXName(i), getYName(i)));
	        i--;
	        updateProgress(nFiles - i, nFiles);
	      }
	        new Transition() {
	            { setCycleDuration(Duration.millis(250)); }
	            protected void interpolate(double frac) {        	image.setOpacity(1-frac);        }
	          }.play();
//			image.setOpacity(0.);
	      return null;
	    }
	}

		 //------------------------------------------------------------------------------------------
	private void doUploadTask(DragEvent ev)
	{
		image.setOpacity(1.);
		setupUploadExecuter(progressContainer);
		try
		{
			List<File> topfiles = ev.getDragboard().getFiles();
			for (File f : topfiles)
			{
				System.out.println("upload");
				status.setText("uploading " + f.getName());
				FTPUtil.uploadDirectory(ftpClient, f.getName(), f.getAbsolutePath(), remoteParentDir);
			}
		} catch (Exception ex)
		{
			System.out.println("upload FAILED: " + ex.getMessage());
			status.setText("upload FAILED: " + ex.getMessage());
		}
	}

}
