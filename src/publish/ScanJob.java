package publish;

import java.io.File;
import java.io.FileInputStream;

import javafx.scene.image.Image;
import util.FileUtil;

class ScanJob
{
	String id;
	File imageFile;
	Image image;
	
	ScanJob(String inID, File inFile)
	{
		id = inID;
		imageFile = inFile;
		if ((inFile == null || !FileUtil.isImageFile(inFile)))
			image =  null;
		else
			try{
				String path = inFile.getCanonicalPath();
				image =  new Image(new FileInputStream(path));

			}
		catch (Exception e) { System.out.println(inFile.getAbsolutePath());   e.printStackTrace(); }
	}
	public String toString()		{	return id;		}
	public Image getImage()		{	return image;		}
}
