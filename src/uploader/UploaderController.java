package uploader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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
	static String HOST = "192.168.2.4";
	static String remoteParentDir = "ftp";		// /ftp
	static String remotePath = "";
	static String password = "CLEO";
	static String username = "adam";
	static String ftpPort = "21";
//	static String username = password = null;
	
	
	@FXML Pane droppane;
	
	@FXML TextField host;
	@FXML TextField user;
	@FXML TextField path;
	@FXML TextField port;
	@FXML PasswordField pass;

	@FXML Label status;
//	@FXML ProgressBar progress;
	@FXML ImageView image;
	@FXML VBox progressContainer;
	@FXML VBox config;
	
	File packageFile;
	FTPClient ftpClient = new FTPClient();
	private final Image WORMHOLE = new Image(UploaderController.class.getResourceAsStream("wormhole.gif"));
	
	@Override	public void initialize(URL location, ResourceBundle resources)
	{
		DropUtil.makeFileDropPane(droppane, (e) -> testuploader(e));		// uploader
		status.setText("Idle");
		image.setImage(WORMHOLE);
		image.setOpacity(0.);
		droppane.setVisible(false);
		config.setVisible(true);
		host.setText(HOST);
		user.setText(username);
		pass.setText(password);
		path.setText(remoteParentDir);
	}
	
	//----------------------------------------------------------------
	@FXML private void doConnect()
	{
		System.out.println("doConnect");
		droppane.setVisible(true);
		config.setVisible(false);
		remoteParentDir = path.getText();		// /ftp
		HOST =  host.getText();
		username =  user.getText();
		password = pass.getText();
		remotePath = path.getText();
		ftpPort = port.getText();
	
	}

	private void uploader(DragEvent e)
	{
		e.consume();
		File dir = validate(e);
		if (dir != null)
		{
			File zipped = FileUtil.compress(dir);
			if (zipped != null)
				if (connect())
					doUploadFile(FXCollections.observableArrayList(dir));
			else System.out.println("connection FAILED");
		} 	
		else System.out.println("validation FAILED: no validated files");
	}

	private void testuploader(DragEvent e)
	{
		e.consume();
//		File dir = validate(e);
		files.addAll(e.getDragboard().getFiles());
		if (files != null)
		{
//			File zipped = FileUtil.compress(dir);
//			if (zipped != null)
				if (connect())
					doUploadFile(files);
			else System.out.println("connection FAILED");
		} 	
		else System.out.println("validation FAILED: no validated files");
}

	List<File> files = FXCollections.observableArrayList();
	//----------------------------------------------------------------
	private File validate(DragEvent ev)
	{
		List<File> topfiles = ev.getDragboard().getFiles();
		if (topfiles.size() > 1 ) return null;			// don't overload the FTPClient with multiple connections
		File topFile = topfiles.get(0);
		if (!topFile.isDirectory()) 
		{
			return null;
		}
		try
		{
			makeEDLManifest(topFile);
			makeAcsManifest(topFile);
			status.setText("validated");
		}
		catch(	Exception ex) { return null;	}
		return topFile;
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
		
			List<String> descriptors = new ArrayList<String>();
			traverseFiles(topFile, descriptors);
			for (String s : descriptors)		manifest.append(s);
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
		manifest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		manifest.append("<UploadInformation>\n");
		manifest.append(" xmlns:edl = \"http://www.zkw.com/toc/\"\n");				// TODO 
		manifest.append(" mlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n");// TODO 
		manifest.append(" xmlns:sig = \"http://www.w3.org/2000/09/xmldsig#\"\n");	// TODO 
		manifest.append(" xsi:schemaLocation = \"http://www.zkw/toc.com/\n");		// TODO 
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
	private void traverseFiles(File directory,   List<String> fileUrls)
	{
		if (!directory.isDirectory())	return;		// error
		for (File f : directory.listFiles())
		{
			if (f.getName().startsWith(".")) continue;
			else if (f.isDirectory())  		traverseFiles(f,fileUrls);
			else fileUrls.add(getFileUrl(f));
		}
	}
	private String getFileUrl(File f)
	{
		String relativePath = f.getAbsolutePath();
		return "<toc:file toc:URI = \"file://" + relativePath + "\"</toc:file>	\n";
	}

	private void addSection(StringBuilder manifest, String string, List<String> objects)
	{
		manifest.append("\t<" + string + " >\n");
		for (String s : objects)	manifest.append("\t").append(s);
		manifest.append("\t</" + string + " >\n\n");
	}

	//----------------------------------------------------------------
	// TODO   RELYING ON CONVENTIONS HERE -- MORE ROBUST PATTERN MATCHING REQD
	

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
		return " <EDLObject ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + " </EDLObject>\n";
	}
	private String getEDLMethodDescriptor(File f)
	{
		return " <EDLMethod ID=\"" + StringUtil.chopExtension(f.getName()) + "\">" + fileSpec(f) + " </EDLMethod>\n";
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
	
	static boolean verbose = true;
	//----------------------------------------------------------------
	private boolean connect() 
	{
		if (verbose) System.out.println("connect");
		
		
        Optional<Pair<String, String>> creds = null;
        if (username == null || password == null)
        {
        	creds = LoginDialog.authenticate(username, password);
        	if (creds != null)
        	{
        		username = creds.get().getKey();
            	password = creds.get().getValue();
      		}        	
        }
//		if (ftpClient != null) return true;
        if (username == null || password == null) return false;
		try
		{
	        ftpClient.connect(HOST, 21);
	        int  replyCode = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(replyCode)) 
	        	throw new FTPException("FTP server refused connection.");
	 
            boolean loggedIn = ftpClient.login(username, password);
            if (!loggedIn) {
                ftpClient.disconnect();		 // failed to login
                throw new FTPException("Could not login to the server.");
            }
            ftpClient.enterLocalPassiveMode();
			status.setText("passiveMode");
		} 
		catch (Exception ex)		
		{	
			ex.printStackTrace();
			System.out.println("connect FAILED: "  + ex.getMessage());  
			status.setText("connect FAILED: " + ex.getMessage());
			return false;
		}
		return true;
	}
	
	//----------------------------------------------------------------
	private void testUpload(VBox layout)
	{
//		files.add(e);
		BlockingQueue<File> fileQueue = new PriorityBlockingQueue<File>(files);
		uploadTask = new UploadTask(fileQueue, files.size());
		uploaderExecutor = createExecutor("Upload");
		uploaderExecutor.execute(uploadTask);
		layout.getChildren().add( createProgressPane(uploadTask));		//validateTask
	}

	
	
	static private UploadTask uploadTask;
//	static private ValidationTask validateTask;
	static private ExecutorService uploaderExecutor;
	private void setupUploadExecuter(VBox layout)
	{
		BlockingQueue<File> fileQueue = new PriorityBlockingQueue<File>(files);
		uploadTask = new UploadTask(fileQueue, files.size());
		uploaderExecutor = createExecutor("Upload");
	    status.textProperty().bind(uploadTask.messageProperty());
		uploaderExecutor.execute(uploadTask);
	    status.textProperty().bind(uploadTask.messageProperty());
	    uploadTask.setOnSucceeded(e -> {
	    	status.textProperty().unbind();
	    	status.setText("completed successfully");
	      });
		layout.getChildren().add( createProgressPane(uploadTask));		//validateTask
	}

	private ExecutorService createExecutor(final String name) {       
    ThreadFactory factory = new ThreadFactory() {
      @Override public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name);
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
	  GridPane progressPane;
	  private Pane createProgressPane(UploadTask uploadTask) {		//ValidationTask validateTask, 
	    progressPane = new GridPane();
	    
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
//	  class ValidationTask extends Task<Void> {
//	    private final int nFiles;
//	    private final BlockingQueue<File> files;
//	    
//	    ValidationTask(BlockingQueue<File> c, final int fileCt) {
//	    	files = c;
//	      nFiles = fileCt;
//	      updateProgress(0, nFiles);
//	    }
//	    
//	    @Override protected Void call() throws Exception {
//	      int i = nFiles;
//	      while (i > 0) {
//	        if (isCancelled())      break;
////	        wait(500);			// ARBITRARY DELAY HERE TO DEBUG
////	        nFiles.put(createChart(getXName(i), getYName(i)));
//	        i--;
//	        File f = files.iterator().next();
//	        updateStatus("Validating: " + f.getName());
//	        updateProgress(nFiles - i, nFiles);
//	        new Transition() {
//	            { setCycleDuration(Duration.millis(250)); }
//	            protected void interpolate(double frac) {        	image.setOpacity(frac);        }
//	          }.play();
////	        image.setOpacity(1);
//	      }
//	      return null;
//	    }
//		private void updateStatus(String str)		{		status.setText(str);		}
//	}
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
	      for (File f : files)
	    	  System.out.println( f.getAbsolutePath() );
	      while (i > 0) {
	        if (isCancelled())      break;
//	        wait(500);			// ARBITRARY DELAY HERE TO DEBUG
	        File f = files.take();
	        updateMessage(f.getName());
	        if (f.isDirectory())
	        {
	            System.out.println("PROCESSING directory: " + f.getAbsolutePath());
	    	    String dirname = f.getAbsolutePath().substring(1 + f.getAbsolutePath().lastIndexOf("/"));
	    	    String remoteDir = remoteParentDir + "/" + dirname;
//	    	    System.out.println("Create remote dir: " + remoteDir);
	    //
	            boolean success = ftpClient.makeDirectory(remoteDir);
	            String rpy = ftpClient.getReplyString();
	            String msg = (success) ? "CREATED the directory: " : "COULD NOT create the directory: ";
	            System.out.println(msg  + dirname+ " " + rpy);
	            FTPUtil.uploadDirectory(ftpClient, f.getAbsolutePath(), remoteParentDir, f.getName());		
	        }
	        else
	        	FTPUtil.uploadSingleFile(ftpClient, f.getAbsolutePath(), remoteParentDir + "/" + f.getName());
//	        nFiles.put(createChart(getXName(i), getYName(i)));
	        
//	        status.setText("Uploading " + f.getName());
	        i--;
	        updateProgress(nFiles - i, nFiles);
	      }
	        new Transition() {
	            { setCycleDuration(Duration.millis(250)); }
	            protected void interpolate(double frac) {      	image.setOpacity(1-frac);  progressPane.setOpacity(1-frac);      }
	          }.play();
	      return null;
	    }
	}

		 //------------------------------------------------------------------------------------------
//		private void doUploadTask(DragEvent ev)
//		{
//			image.setOpacity(1.);
//			setupUploadExecuter(progressContainer);
//			try
//			{
//				List<File> topfiles = ev.getDragboard().getFiles();
//				for (File f : topfiles)
//				{
//					System.out.println("upload");
//					status.setText("uploading " + f.getName());
//					FTPUtil.uploadDirectory(ftpClient, f.getName(), f.getAbsolutePath(), remoteParentDir);
//				}
//			} catch (Exception ex)
//			{
//				System.out.println("upload FAILED: " + ex.getMessage());
//				status.setText("upload FAILED: " + ex.getMessage());
//			}
//		}

		private void doUploadFile(List<File> files)
		{
			int nFiles = files.size();
			for (int i =0; i< nFiles; i++)
			{
				File f= files.get(i);
//				if (f.isDirectory())	files.addAll(FXCollections.observableArrayList(f.listFiles()));
//				else			
					files.add(f);
			}
			image.setOpacity(1.);		// SHOW THE WORMHOLE
			setupUploadExecuter(progressContainer);
//			try
//			{
//				System.out.println("upload");
//				status.setText("uploading " + f.getName());
////				boolean ok = FTPUtil.uploadSingleFile(ftpClient, f.getAbsolutePath(), remoteParentDir);
//			} catch (Exception ex)
//			{
//				System.out.println("upload FAILED: " + ex.getMessage());
//				status.setText("upload FAILED: " + ex.getMessage());
//			}
//			image.setOpacity(0.);
			files.clear();			//  does this need some synchronization flag??
		}

}
