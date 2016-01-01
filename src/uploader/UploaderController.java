package uploader;

import java.io.File;
import java.net.URL;
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
import javafx.util.Pair;
import services.MD5;
import util.FTPException;
import util.FTPUtil;
public class UploaderController implements Initializable
{
	static String HOST = "ftp.treister.com";
	static String remoteParentDir = "/doc/zkw/";
	static String remotePath = "";
	static String password = "pw";
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
		for (File f : topfiles)
			if (!f.isDirectory()) return false;
		try
		{
			files.clear();
			files.addAll(topfiles);
			StringBuilder manifest = new StringBuilder();
			for (File f : files)
			{
				buildManifest(f, manifest);
				status.setText("validated :\n" + f.getName()	);
			}
		
			System.out.println("validated :\n" + manifest	);
			status.setText("validated :\n" + manifest	);
		}
		catch(	Exception ex) { return false;	}
		return true;
	}
	//----------------------------------------------------------------

	private void buildManifest(File f, StringBuilder manifest)
	{
		manifest.append(fileSpec(f));
		if (f.isDirectory())			
			for (File sub : f.listFiles())
				buildManifest(sub,  manifest);
	}
	
	//----------------------------------------------------------------
	private String fileSpec(File f)
	{
		return f.getName() + "\t"+ f.length() + "\t" + MD5.forFile(f) + "\n";
	}
	//----------------------------------------------------------------
	
	private boolean connect() 
	{
		System.out.println("connect");
		status.setText("connect");
		if (ftpClient != null) return true;
		try
		{
	        ftpClient.connect(HOST, 21);
	        int  replyCode = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(replyCode)) 
	        	throw new FTPException("FTP server refused connection.");
	 
	        Pair<String, String> creds = null;
	        if (username == null || password == null)
	        	creds = LoginDialog.authenticate(username, password);
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
	        image.setOpacity(1);
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
			image.setOpacity(0.);
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
