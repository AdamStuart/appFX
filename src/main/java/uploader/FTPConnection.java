package uploader;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import dialogs.LoginDialog;
import javafx.animation.Transition;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.util.Pair;
import util.FTPException;
import util.FTPUtil;

public class FTPConnection
{
	ConnectionInfo myInfo;
	ProgressBar progressBar;
	Label status;
	ImageView image;
	static private UploadTask uploadTask;
//	static private ValidationTask validateTask;
	static private ExecutorService uploaderExecutor;
	private FTPClient ftpClient;
	
	public FTPConnection(ConnectionInfo info, FeedbackNodes gauges)
	{
		myInfo = info;
		progressBar = gauges.getProgressBar();
		status = gauges.getLabel();
		image = gauges.getImageView();
		ftpClient = new FTPClient();
		
	}
	boolean verbose = true;
	
	//----------------------------------------------------------------
	public boolean connect() 
	{
		if (verbose) System.out.println("connect");		
        Optional<Pair<String, String>> creds = null;
        if (myInfo.user == null || myInfo.pass == null)
        {
        	creds = LoginDialog.authenticate(myInfo.user, myInfo.pass);
        	if (creds != null)
        	{
        		myInfo.user = creds.get().getKey();
        		myInfo.pass = creds.get().getValue();
      		}        	
        }
//		if (ftpClient != null) return true;
        if (myInfo.user == null || myInfo.pass == null) return false;
		try
		{
	        ftpClient.connect(myInfo.host, myInfo.port);
	        int  replyCode = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(replyCode)) 
	        	throw new FTPException("FTP server refused connection.");
	 
            boolean loggedIn = ftpClient.login(myInfo.user, myInfo.pass);
            if (!loggedIn) {
                ftpClient.disconnect();		 // failed to login
                throw new FTPException("Could not login to the server.");
            }
            ftpClient.enterLocalPassiveMode();
//			status.setText("passiveMode");
		} 
		catch (Exception ex)		
		{	
			ex.printStackTrace();
			System.out.println("connect FAILED: "  + ex.getMessage());  
//			status.setText("connect FAILED: " + ex.getMessage());
			return false;
		}
		return true;
	}
	 //------------------------------------------------------------------------------------------
	
	public void startUpload(FTPConnection ftp, List<File> files)
	{
		BlockingQueue<File> fileQueue = new PriorityBlockingQueue<File>(files);
		uploadTask = new UploadTask(ftp, myInfo.path, fileQueue, files.size());
		uploaderExecutor = createExecutor("Upload");
	    status.textProperty().bind(uploadTask.messageProperty());
		uploaderExecutor.execute(uploadTask);
	    status.textProperty().bind(uploadTask.messageProperty());
	    uploadTask.setOnSucceeded(e -> { complete(true);      });
	    uploadTask.setOnFailed(e -> { complete(false);	});
	}

	private void complete(boolean success)
	{
    	if (status != null)
    	{
    		status.textProperty().unbind();
        	status.setText(success ?  "success" : "failure");
    	}
        new Transition() {
            { setCycleDuration(Duration.millis(450)); }
            protected void interpolate(double frac) 
            {   
            	if (image != null)   	image.setOpacity(1-frac);        
            	if (progressBar != null)   	progressBar.setOpacity(1-frac);        
            }
          }.play();
          image.setOpacity(0);
          progressBar.setOpacity(0);
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
	 //------------------------------------------------------------------------------------------
	class UploadTask extends Task<Void>
	{
		private final int nFiles;
		private final BlockingQueue<File> files;
		FTPConnection ftpConnection;
		String remoteParentDirectory;

		UploadTask(FTPConnection ftp, String remoteDir, BlockingQueue<File> c, final int fileCt)
		{
			files = c;
			ftpConnection = ftp;
			remoteParentDirectory = remoteDir;
			nFiles = fileCt;
			updateProgress(0, nFiles);
		}

		@Override protected Void call() throws Exception
		{
			int i = nFiles;
			for (File f : files)
				System.out.println(f.getAbsolutePath());
			while (i > 0)
			{
				if (isCancelled()) break;
				// wait(500); // ARBITRARY DELAY HERE TO DEBUG
				File f = files.take();
				updateMessage(f.getName());
				if (f.isDirectory())
				{
					System.out.println("PROCESSING directory: " + f.getAbsolutePath());
					String dirname = f.getAbsolutePath().substring(1 + f.getAbsolutePath().lastIndexOf("/"));
					String remoteDir = remoteParentDirectory + "/" + dirname;
					boolean success = ftpConnection.makeDirectory(remoteDir);
					String rpy = ftpConnection.getReplyString();
					String msg = (success) ? "CREATED the directory: " : "COULD NOT create the directory: ";
					System.out.println(msg + dirname + " " + rpy);
					ftpConnection.uploadDirectory(f.getAbsolutePath(), remoteParentDirectory, f.getName());
				} else
				{
					ftpConnection.uploadSingleFile(f.getAbsolutePath(), remoteParentDirectory + "/" + f.getName());
				}
				// nFiles.put(createChart(getXName(i), getYName(i)));

				// status.setText("Uploading " + f.getName());
				i--;
				updateProgress(nFiles - i, nFiles);
			}
			return null;
		}
}

  	//----------------------------------------------------------------
	public void uploadFileList(ObservableList<File> files)
	{
		startUpload(this, files);
	}

	public boolean makeDirectory(String remoteDir)
	{
		try
		{
			return ftpClient.makeDirectory(remoteDir);
		}
		catch (Exception e)
		{
			System.out.print("could not make directory: " + remoteDir);
			return false;
			
		}
	}

	public String getReplyString( )	{			return ftpClient.getReplyString();	}

	public void uploadDirectory(String absolutePath, String remoteParentDir, String name)
	{
		try
		{
			FTPUtil.uploadDirectory(ftpClient, absolutePath, remoteParentDir, name);
		}
		catch (Exception e)
		{
			System.out.print("could not upload directory: " + name);
		}
		
	}

	public void uploadSingleFile(String absolutePath, String string)
	{
		try
		{
			FTPUtil.uploadSingleFile(ftpClient, absolutePath, string);
		}
		catch (Exception e)
		{
			System.out.print("could not upload directory: " + string);
		}
	}
	
}
