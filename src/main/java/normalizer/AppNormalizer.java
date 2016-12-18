package normalizer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import javafx.application.Application;
import javafx.stage.Stage;
import model.dao.CSVTableData;
import util.FileUtil;
import util.StringUtil;

public class AppNormalizer extends Application
{
    public static void main(String[] args)    {        launch(args);    }

	@Override public void start(Stage primaryStage) throws Exception
	{
		String path = "/Users/adam/Desktop/microscope";
		Path pp = Paths.get(path);
		Files.walk(pp)
		    .filter(isCSV)
		    .filter(noUnitFileExists)
		    .forEach(p -> { normalize(p); } );
		System.exit(0);
	}

	private void normalize(Path p)
	{
		System.out.println(p.getFileName());	
		CSVTableData data = CSVTableData.readZKWfile(p);
		if (data != null) 
			data.makeUnitFile(p);
	}

	Predicate<Path> isCSV =  path -> FileUtil.isCSV(path.toFile());
	Predicate<Path> noUnitFileExists =  p -> !subPredricate(p);
	
	boolean subPredricate(Path p)
	{
		String s = StringUtil.chopExtension(p.toAbsolutePath().toString()) +".unit";
		File f = new File(s);
		boolean exists = f.exists();
		return exists;
	}	
}
