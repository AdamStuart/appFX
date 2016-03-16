package uploader;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import util.FileUtil;

public class Uploader
{
	private FTPConnection ftp;
	public Uploader(File dir, ConnectionInfo info, FeedbackNodes gauges)
	{
		ImageView wormhole = gauges.getImageView();
		Label status = gauges.getLabel();
		if (wormhole != null)	wormhole.setOpacity(1);
		if (status != null) 	status.setText("uploading...");
		File zipped = FileUtil.compress(dir);
		if (zipped == null) 
		{ 
			if (wormhole != null)	wormhole.setOpacity(0);  
			if (status != null) 	status.setText("File Already Exists");
		} 
		else 
		{
			ftp = new FTPConnection(info, gauges);
			if (ftp.connect())
				ftp.uploadFileList(FXCollections.observableArrayList(zipped));
		}
	}

}
