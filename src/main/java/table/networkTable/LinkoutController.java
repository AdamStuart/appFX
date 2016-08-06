package table.networkTable;

import java.net.URL;
import java.util.ResourceBundle;

import game.bookclub.AppBookClub;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class LinkoutController implements Initializable {
	
	@FXML private WebView ncbiWeb;
	@FXML private WebView ensemblWeb;
	@FXML private ScrollPane ncbiScroll;
	@FXML private ScrollPane ensemblScroll;
	
	
	@Override public void initialize(URL location, ResourceBundle resources)
	{
//    	System.err.println("LinkoutController.initialize");
    	WebView browser = new WebView();
    	WebEngine webEngine = browser.getEngine();
    	webEngine.load( "http://www.ncbi.nlm.nih.gov/gene/672");
		ncbiScroll.setContent(browser);
//	    webEngine = ensemblWeb.getEngine();
//	    webEngine.load(dummy);
	    
//		if (ncbiWeb != null)
//		{
//		    final WebEngine awebEngine = ncbiWeb.getEngine();
//
//		    ScrollPane scrollPane = new ScrollPane();
//		    scrollPane.setContent(ncbiWeb);
//		    webEngine.getLoadWorker().stateProperty().addListener((x,y,newState) -> {
//		            if (newState == Worker.State.SUCCEEDED) 
//		            {
//		            	System.err.println("SUCCEEDED");
//
//		            }//    AppBookClub.getInstance().getStage().setTitle(awebEngine.getLocation());
//		          }  );
//		}

		    
	    	WebView browser2 = new WebView();
	    	WebEngine webEngine2 = browser2.getEngine();
	    	webEngine2.load("http://uswest.ensembl.org/Homo_sapiens/Gene/Summary?g=ENSG00000012048;r=17:43044295-43170245");
	    	ensemblScroll.setContent(browser2);

	}


}
