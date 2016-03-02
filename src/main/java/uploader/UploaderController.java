package uploader;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import gui.DropUtil;
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
import javafx.scene.layout.VBox;
import util.StringUtil;
public class UploaderController implements Initializable
{
	static String HOST = "192.168.2.4";				// just to save myself repeated entry!
	static String remoteParentDir = "ftp";		
	static String remotePath = "";
	static String password = "CLEO";
	static String username = "adam";
	static String ftpPort = "21";
	private ConnectionInfo connectInfo;
	
	@FXML Pane droppane;
	@FXML TextField host;
	@FXML TextField user;
	@FXML TextField path;
	@FXML TextField port;
	@FXML PasswordField pass;
	@FXML Label status;
	@FXML ImageView image;
	@FXML VBox progressContainer;
	@FXML VBox config;
	
	private final Image WORMHOLE = new Image(UploaderController.class.getResourceAsStream("wormhole.gif"));
	
	@Override	public void initialize(URL location, ResourceBundle resources)
	{
		DropUtil.makeFileDropPane(droppane, (e) -> uploader(e));		// testuploader
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
		droppane.setVisible(true);
		config.setVisible(false);
		connectInfo = new ConnectionInfo();
		connectInfo.host = host.getText();
		connectInfo.user = user.getText();
		connectInfo.pass = pass.getText();
		connectInfo.path = path.getText();
		connectInfo.port = StringUtil.toInteger(port.getText(), 21);
	}
	//----------------------------------------------------------------
	private void uploader(DragEvent e)
	{
		e.setDropCompleted(false);
		e.consume();
		File dir = validate(e);
		if (dir != null)
		{
			createProgressPane();
			FeedbackNodes nodes = new FeedbackNodes(status, progressBar, image, null);
			new Uploader(dir, connectInfo, nodes);
		}
	}
	//----------------------------------------------------------------
	private File validate(DragEvent ev)
	{
		List<File> topfiles = ev.getDragboard().getFiles();
		if (topfiles.size() > 1 ) return null;			// don't overload the FTPClient with multiple connections
		File topFile = topfiles.get(0);
		if (!topFile.isDirectory()) 			return null;		// we require: one directory is added.
		File[] files = topFile.listFiles( );
		int inVis = 0;
		for (File f : files) if (f.getName().startsWith(".")) inVis++;
		if (files.length - inVis == 0)
		{
			setWatchFolder(topFile);
			return null;
		}
		try
		{
			ManifestBuilder manifest = new ManifestBuilder();
			manifest.makeEDLManifest(topFile);		// this computes MD5, which takes a long time on big directories
			manifest.makeAcsManifest(topFile);
			status.setText("validated");
		}
		catch(	Exception ex) { image.setOpacity(0);;	}
		return topFile;
	}
	
	private void setWatchFolder(File topFile)
	{
		status.setText("Watch folder should be set to " + topFile.getName());
		// TODO -- actually watch it!!
		
	}

	//----------------------------------------------------------------------------------------
	  GridPane progressPane;
	  ProgressBar progressBar;
	  
	  private void createProgressPane() {		//ValidationTask validateTask, 
	    progressPane = new GridPane();
	    
	    progressPane.setHgap(5);
	    progressPane.setVgap(5);
//	    progressPane.addRow(0, new Label("Validate:"),  createBoundProgressBar(validateTask.progressProperty()));
	    progressPane.addRow(0, new Label("Upload:"));
	    progressBar = new ProgressBar();
	    progressBar.setMaxWidth(148);
	    progressPane.addRow(1, progressBar);
	  }
	   

}
