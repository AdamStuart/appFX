package game.iching;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class IChingController
{
	@FXML private WebView iching;
	@FXML private TextField question;
	@FXML private Pane hexagram;

	public void initialize()
	{
	}
	
	@FXML private void throwCoins()  
	{
		System.out.println("throwCoins");
		AnimationTimer  timer = new AnimationTimer() {
	          int i;
	          private long lastUpdate = 0 ;
	          long TIMER50MILLI = 50000000;
          @Override public void handle(long now) {
              
        	  if (now - lastUpdate >= TIMER50MILLI) 
              {
            	  	hexagram.getChildren().clear();
	    			int idx = (int) (Math.random() * 64);
	    			VBox hex = makeHexagramBox(idx);
	                hexagram.getChildren().add(hex);
	                lastUpdate = now ;
	                if (++i > 0 ) 
	                {
	                    readFortune(idx);
	                    stop();
	                }
             }
	        };
		};
		timer.start();
	}
	//------------------------------------------------------------------------
	//  looking at the table at the top of the web page, this maps a
	// binary 3 digit number into the position in the I Ching table
	//  solid bar = 1, broken = 0, top is most significant bit.
	int map(int in)
	{
		switch (in)
		{
			case 0:	 return 4;	//K'UN  THE RECEPTIVE, EARTH
			case 1:	 return 1;	//CHêN  THE AROUSING, THUNDER
			case 2:	 return 2;	//K'AN  THE ABYSMAL, WATER
			case 3:	 return 7;	// TUI  THE JOYOUS, LAKE	
			case 4:	 return 3;	// KêN  KEEPING STILL, MOUNTAIN
			case 5:	 return 6;	// LI   THE CLINGING, FLAME
			case 6:	 return 5;	// SUN  THE GENTLE, WIND
			case 7:	 return 0;	// CH'IEN  THE CREATIVE, HEAVEN
		}
		return 0;
		
	}
	
	String[] trigrams = new String[] { 
			"CH'IEN - The Creative Heaven",
			"CHêN - The Arousing / Thunder",
			"K'AN - The Absymal / Water",
			"KêN  Keeping Still / Mountain",
			"K'UN  The Receptive / Earth",
			"SUN  The Gentle / Wind",
			"LI   The Clinging / Flame", 
			"TUI  The Joyous / Lake"
		};
	
	//"http://www.akirarabelais.com/i/i.html
	// this chart maps from (column, row) -> page number
	int[] pages = new int[] 
	{ 		1, 34, 5, 26, 11, 9, 14, 43, 
			25, 51, 3, 27, 24, 42, 21, 17, 
			6, 40, 29, 4, 7, 59, 64, 47, 
			33, 62, 39, 52, 15, 53, 56, 31, 
			12, 16, 8, 23, 2, 20, 35, 45, 
			44, 32, 48, 18, 46, 57, 50, 28,
			13, 55, 63, 22, 36, 37, 30, 49, 
			10, 54, 60, 41, 19, 61, 38, 58
	};

	private VBox makeHexagramBox(int idx)
	{
		System.out.println(idx  + ": is idx");
		VBox mom = new VBox(32);
		int top = map(idx % 8);
		int btm = map(idx / 8);
		System.out.println(idx % 8 + ": top: -> " + top + " " + trigrams[top]);
		System.out.println(idx / 8 + ": btm: ->" + btm + " " + trigrams[btm]);

		VBox topTri = makeTrigram(idx % 8);
		VBox btmTri = makeTrigram(idx / 8);
		mom.getChildren().addAll(topTri, btmTri);
		mom.setPadding(new Insets(20,20,20,20));
		return mom;
	}

	private VBox makeTrigram(int idx)
	{
		System.out.println(idx  + ": makeTrigram");
		 Label bar1 = (idx > 4) ? makeSolid() : makeBroken() ;
		 Label bar2 = bit2set(idx) ? makeSolid() : makeBroken() ;
		 Label bar3 = (idx % 2 == 1) ? makeSolid() : makeBroken() ;
		 return new VBox(8, bar1, bar2, bar3);
	}
	boolean bit2set(int i) { return i == 2 || i ==3 || i ==6 || i == 7; }

	static int WIDTH = 200;
	static int BARHEIGHT = 35;
	private Label makeSolid()	{return new Label("", new Rectangle(WIDTH, BARHEIGHT));	}
	private Label makeBroken()	{	return new Label("", 
					new Group(	new Rectangle(0,0,WIDTH/3, BARHEIGHT),
								new Rectangle(2*WIDTH/3,0,WIDTH/3, BARHEIGHT)));
	}

	//------------------------------------------------------------------------
	private void readFortune(int idx)  
	{
		int top = map(idx / 8);
		int btm = map(idx % 8);
//		System.out.println(idx  + ": is readFortune");
//		System.out.println(idx / 8 + ": " + top + " " + trigrams[top]);
//		System.out.println(idx % 8 + ": " + btm + " " + trigrams[btm]);
//
		int pageIdx = 8 * top + btm;
		int anchorNumber = pages[pageIdx]; // mapIndexToAnchor(idx);

//		System.out.println("readFortune: " + top + " / " + btm + " = " + anchorNumber); 
//		System.out.println("====\n");
			
	    final WebEngine webEngine = iching.getEngine();
	    ScrollPane scrollPane = new ScrollPane();
	    scrollPane.setContent(iching);

//	    webEngine.getLoadWorker().stateProperty().addListener((x,y,newState) -> {
//	            if (newState == Worker.State.SUCCEEDED) 
//	              AppIChing.getInstance().getStage().setTitle(webEngine.getLocation());
//	          }  );
	    webEngine.load("http://www.akirarabelais.com/i/i.html#" + anchorNumber);
	};

}
