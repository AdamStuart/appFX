package game.bookclub;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import game.sudoku.SudokuGame;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Main controller class initializes all the scenes
 * and creates all the tabs with their demos
 *  
 * @author adam
 *
 */
public class BookClubController
{
	@FXML private BorderPane container;			// root of fxml
	@FXML private Button leftSideBarButton;
	@FXML private Button toolbarVisible;
	@FXML private ListView<String> listToDo;
	@FXML private ListView<String> listRead;
	@FXML private ListView<String> listWrite;
	@FXML private ListView<String> listCreate;
	@FXML private ListView<String> listEvent;
	@FXML private ListView<String> birdList;
	@FXML private Group sceneGroup;
	@FXML private Group contestGroup;
	@FXML private Pane gameboard;
	@FXML private Label prompt;
	@FXML private HTMLEditor editor;
//	@FXML private MediaView mediaView;
	@FXML private AnchorPane mediaContainer;
	@FXML private ScrollPane galleryScroll;
	@FXML private WebView webview;
	@FXML private StackPane flashcardStack;
	@FXML private VBox surveyVbox;
	@FXML private GridPane theGrid;
	//---------------------------------------------------------------------

	static String USER =  "{  'user': 1  'firstname': 'Katerina' " +
					" 'todo': [ 'wake up'  'brush teeth' ]" + 
					" 'read': [ 'NY Times'  'War and Peace' ]" + 
					" 'write': [ 'Journal'  'Todays Prompt'  'Letter to Mom'  'Extension Request' ] " + 
					" 'create': [ 'Order' ] " + 
					" 'events': [ 'Meeting' 'Dentist'  'Dinner' ] " + 
					" 'prompt': 'Explain the mood in the room:'   }";

 	//---------------------------------------------------------------------
    String path = "bookclub/";
    
	public void initialize()
	{
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 140);
		buildBirdList(birdList);
		buildScene();
		new Flashcard(flashcardStack);
		
		buildSurvey();
		buildGallery(path);
		if (webview != null)
		{
		    final WebEngine webEngine = webview.getEngine();

		    ScrollPane scrollPane = new ScrollPane();
		    scrollPane.setContent(webview);

		    webEngine.getLoadWorker().stateProperty().addListener((x,y,newState) -> {
		            if (newState == Worker.State.SUCCEEDED) 
		              AppBookClub.getInstance().getStage().setTitle(webEngine.getLocation());
		          }  );
		    webEngine.load( "http://thesecretbookclub.org");
		}
	}
	
	/*
	 *   A list of interspersed prompts and text fields 
	 *   that grow to their content
	 */

	private void buildSurvey()
	{
		assert(surveyVbox != null);
		surveyVbox.setSpacing(12);
		String[] prompts = new String[] { 
						"What does <CHAR> want?", 
						"What have you learned?", 
						"Who are your friends?", 
						"What physical activity do you like to do?", 
						"What have you created?", 
						"What problem have you solved?", 
						"How has your world changed?"
		};
	
		
		String[] journey = new String[] { 
						"Introduce the hero in his ORDINARY WORLD:", 
						"Explain why he is RELUCTANT to change:", 
						"How does he CROSS THE FIRST THRESHOLD?", 
						"What TESTS, ALLIES and ENEMIES does he encounter?", 
						"Describe the journey reaching the INNERMOST CAVE:", 
						"Explain how he endures the SUPREME ORDEAL:", 
						"When does he SEIZE THE SWORD or treasure?",
						"Describe the ROAD BACK to his world:",
						"How is he RESURRECTED and transformed?",
						"What ELIXIR does he bring to benefit his world?"
		};

		for (String s : journey)
		{
			s = variableSubstitue(s);
			Label labl = new Label(s);
			labl.setStyle("-fx-font: 18px \"SansSerif\";");

		    final TextArea textArea = new TextArea();
		    textArea.setMinHeight(100);
	        textArea.setPrefSize(500, 100);
	        textArea.setStyle("-fx-font: 18px \"Serif\";");
	        textArea.setWrapText(true);
	        SimpleIntegerProperty count = new SimpleIntegerProperty(100);
	        int rowheight=10;

	        textArea.prefHeightProperty().bind(count);
	        textArea.minHeightProperty().bind(count);
	        textArea.scrollTopProperty().addListener((ov, old, newval)-> {
	           if(newval.intValue()>rowheight)
	                   count.setValue(count.get()+newval.intValue());
	         });
	        surveyVbox.getChildren().add(labl);
			surveyVbox.getChildren().add(textArea);

		}
		surveyVbox.setMaxWidth(500);
	}

	String username = "Jasmin";
	
	boolean chickFlick = true;
private String variableSubstitue(String s)
	{
		String sub = s.replace("<CHAR>", username);
		
		if (chickFlick) 
			sub = sub.replace(" he ", " she ").replace(" his ", " her ");
		return sub;
	}

	//---------------------------------------------------------------------
/*
 * traverse a directory to find all images and put them into tiles of a TilePane
 */
	private void buildGallery(String path)
	{
		if (galleryScroll != null)
		{
	        TilePane tile = new TilePane();
	        tile.setPadding(new Insets(15, 15, 15, 15));
	        tile.setHgap(15);

	        galleryScroll.setStyle("-fx-background-color: DAE6F3;");
	        galleryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal
	        galleryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
	        galleryScroll.setFitToWidth(true);
	        galleryScroll.setContent(tile);

	      path = "./src/bookclub/";				// hack
	        try
	        {
		        File folder = new File(path);
		        File[] listOfFiles = folder.listFiles();
		        if (listOfFiles == null) return;
		        for (final File file : listOfFiles) 
		        	if (isImage(file))
		                tile.getChildren().add(createImageView(file));
		        
        	
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	        }
		}

		
	}
	  private boolean isImage(File file)
		{
			String path = file.getAbsolutePath();
			if (path.endsWith(".png")) return true;
			if (path.endsWith(".jpg")) return true;
			if (path.endsWith(".jpeg")) return true;
			return false;
		}

		private ImageView createImageView(final File imageFile) {
	        // DEFAULT_THUMBNAIL_WIDTH is a constant you need to define
	        // The last two arguments are: preserveRatio, and use smooth (slower)
	        // resizing

	        ImageView imageView = null;
	        try {
	            final Image image = new Image(new FileInputStream(imageFile), 150, 0, true, true);
	            imageView = new ImageView(image);
	            imageView.setFitWidth(150);
	       		Tooltip t = new Tooltip(imageFile.getName());
	       		Tooltip.install(imageView, t);
            
	        	DropShadow glow = new DropShadow();
	        	glow.setOffsetY(5.0);
	        	glow.setOffsetX(5.0);
	        	glow.setColor(Color.BROWN);


	        	imageView.setOnMouseEntered(mouseEvent -> { ((Node)mouseEvent.getTarget()).setEffect(glow); });
	            imageView.setOnMouseExited(mouseEvent -> { ((Node)mouseEvent.getTarget()).setEffect(null); });

	            imageView.setOnMouseClicked(mouseEvent -> {
	                    if(mouseEvent.getButton().equals(MouseButton.PRIMARY))
	                        if(mouseEvent.getClickCount() == 2)
	                          showDetail(image, imageFile);
	            });
	        } catch (FileNotFoundException ex) {
	            ex.printStackTrace();
	        }
	        return imageView;
	    }
	
		private void showDetail(Image image, File imageFile)
		{
        try {
            BorderPane borderPane = new BorderPane();
            ImageView imageView2 = new ImageView();
            Image image2 = new Image(new FileInputStream(imageFile));
            imageView2.setImage(image);
            imageView2.setStyle("-fx-background-color: BLACK");
            imageView2.setFitHeight(galleryScroll.getHeight() - 10);
            imageView2.setPreserveRatio(true);
            imageView2.setSmooth(true);
            imageView2.setCache(true);
            borderPane.setCenter(imageView2);
            borderPane.setStyle("-fx-background-color: BLACK");
            Stage newStage = new Stage();
            newStage.setWidth(galleryScroll.getWidth());
            newStage.setHeight(galleryScroll.getHeight());
            newStage.setTitle(imageFile.getName());
            Scene scene = new Scene(borderPane,Color.BLACK);
            newStage.setScene(scene);
            newStage.show();
        } catch (FileNotFoundException e) {   e.printStackTrace();    }
			
		}

	//---------------------------------------------------------------------
	private void buildScene()
	{	
		load(USER);			// dummy function to parse JSON as a session key
//	    Player.readPlayerInfo(1);		// test functions that hit a heroku server
//	    Story.readStoryList();
		try
		{
			makeMediaView();
	    
			String filename = "bookclub/open_book.png";	// "src/bookclub/open_book.png";

			makeImageView(sceneGroup, filename);
			sceneGroup.setOnDragDropped((DragEvent ev) -> drop(sceneGroup, ev));

			ImageView view = makeImageView(contestGroup, "bookclub/background.jpg");
			contestGroup.setOnDragDropped((DragEvent ev) -> drop(contestGroup, ev));
			view.fitWidthProperty().bind(container.widthProperty().divide(1.2));
			view.fitHeightProperty().bind(container.heightProperty().divide(1.2));
			view.setPreserveRatio(true);
			ImageView cup = makeImageView("bookclub/coffee.png", 770, 90, 0.7);
			ImageView calendar = makeImageView("bookclub/events.png", 60, 30, 0.9);
			ImageView paper = makeImageView("bookclub/news.png", 300, 300, 0.6);
			contestGroup.getChildren().addAll(cup, calendar, paper);

			Color[] palette = new Color[] { Color.BLANCHEDALMOND, Color.DARKOLIVEGREEN, Color.CORAL, Color.BROWN,
					Color.CORNSILK, Color.BLUE };
			int sideDim = 700;
			gameboard.getChildren().add(new Gameboard(sideDim, sideDim, palette));

			buildSudokuGrid(theGrid);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	// ---------------------------------------------------------------------
// Scenes 
	SudokuGame matrix81 = new SudokuGame();
	public void buildSudokuGrid(GridPane theGrid)
	{
		assert(theGrid != null);
		theGrid.setStyle("-fx-font: 9px \"SansSerif\";");
		theGrid.gridLinesVisibleProperty().set(true);
		ObservableList<Image> birdImages = BirdCell.getBirdImages();
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				int val = 0; //  matrix81.get(i,j);
				Label label = new Label("");
				label.setId("" + i);
				label.setScaleX(0.75);
				label.setScaleY(0.75);
				label.setOnMouseEntered(null);
				label.setOnMouseExited(null);
				Image image = birdImages.get(i);
				label.setGraphic(new ImageView(image));
//				label.setText("");
				label.setOnDragDetected(event -> {

		            Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
		            ClipboardContent content = new ClipboardContent();
		            content.putString(label.getId());
		            System.out.println(label.getId());
		            dragboard.setDragView(((ImageView)(label.getGraphic())).getImage() );
		            dragboard.setContent(content);
		            event.consume();
		        });

				label.setOnDragOver(event -> {
		            if (event.getGestureSource() != label && event.getDragboard().hasString()) 
		                event.acceptTransferModes(TransferMode.MOVE);
		              event.consume();
		        });

		        label.setOnDragEntered(event -> {
		            if (event.getGestureSource() != label &&  event.getDragboard().hasString()) 
		            	label.setOpacity(0.3);
		        });

		        label.setOnDragExited(event -> {
		            if (event.getGestureSource() != label &&  event.getDragboard().hasString()) 
		            	label.setOpacity(1);
		        });

		        label.setOnDragDropped(event -> 
		        {      
		            Dragboard db = event.getDragboard();
		            boolean success = false;

		            if (db.hasString()) {
		                success = true;
		                String str = db.getString();
		                int idx = Integer.parseInt(str);
		                Image img = birdImages.get(idx);
		                ImageView iv = (ImageView)(label.getGraphic());
		                iv.setImage(img);
		                label.setId("" + idx);
		            }
		            event.setDropCompleted(success);
		            event.consume();
		        });

		        label.setOnDragDone(DragEvent::consume);
				GridPane.setConstraints(label, i, j);
				GridPane.setHalignment(label, HPos.CENTER);
				theGrid.getChildren().addAll( label);			 // don't forget to add children to gridpane
			}	
		}	 
	}
	
	private ImageView makeImageView(String filename, double xOff, double yOff, double scale)
	{
		Image image = null;
		try
		{
			image = new Image("" + filename);
		} catch (IllegalArgumentException ex)		{	}
		if (image == null)
	    {
	    	System.err.println("image missing: " + filename);
	    	return null;
	    }
	    ImageView view =  new ImageView(image);
	    view.setScaleX(scale);	    view.setScaleY(scale);
	    view.setTranslateX(xOff);  view.setTranslateY(yOff);
		view.setOnMousePressed( a -> { deskDragStart(a); });
		view.setOnMouseDragged( a -> { deskDrag(a); });

	    return view;
	}
	//---------------------------------------------------------------------
	static double xStart, yStart;
	static double xOffset, yOffset;
	static private void deskDragStart(MouseEvent ev)
	{
		xStart = ev.getX();			// TODO offset by position relative to origin
		yStart = ev.getY();		
	}

	static private void deskDrag(MouseEvent ev)
	{
		Object t = ev.getTarget();
		if (t instanceof ImageView)
		{
			ImageView iv = (ImageView) t;

			double cx = ev.getX();
			double cy = ev.getY();
			double dx = cx - xStart;
			double dy = cy - yStart; 

			if ((iv.getTranslateX()  >= 0 || dx > 0) && (iv.getTranslateY() >= 0 || dy > 0))
			{
				iv.setTranslateX(iv.getTranslateX() + dx);
				iv.setTranslateY(iv.getTranslateY() + dy);
			}
		}
	}
	//---------------------------------------------------------------------
	private ImageView makeImageView(Group parent, String filename)
	{
	    Image image = new Image(filename);
	    ImageView view =  new ImageView(image);
	    parent.setOnDragEntered((DragEvent ev) -> dragenter(ev));
	    parent.setOnDragOver((DragEvent ev) -> dragover(ev));
	    parent.getChildren().add(view);
	    return view;
	}
	//---------------------------------------------------------------------
	private void makeMediaView()
	{
		String path = "src/bookclub/video1.mp4";
		// https://youtu.be/EU8rPW2KyAY
		Media media = new Media(new File(path).toURI().toString());

		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(false);
		MediaControl ctrol = new MediaControl(mediaPlayer);
		mediaContainer.getChildren().add(ctrol);
		ctrol.getMediaView().fitWidthProperty().bind(mediaContainer.widthProperty().divide(1.6));
		ctrol.getMediaView().fitHeightProperty().bind(mediaContainer.heightProperty().divide(1.6));

		// DropShadow effect
		DropShadow dropshadow = new DropShadow();
		dropshadow.setOffsetY(5.0);
		dropshadow.setOffsetX(5.0);
		dropshadow.setColor(Color.BROWN);
		mediaContainer.setStyle("-fx-background-color: black;");
		ctrol.setEffect(dropshadow);
	}

	//---------------------------------------------------------------------
	
	private void dragenter(DragEvent ev)	{		ev.acceptTransferModes(TransferMode.MOVE);	}
	private void dragover(DragEvent ev)		{		ev.acceptTransferModes(TransferMode.MOVE);	}

	private void drop(Group group, DragEvent ev)
	{
        Dragboard db = ev.getDragboard();
		if (db.hasString())
		{
			String id = db.getString();
			ImageView newBird = BirdCell.findBird(id);
			if (newBird != null)
			{
				group.getChildren().add(newBird);
				newBird.setTranslateX(ev.getX()-30);
				newBird.setTranslateY(ev.getY()-30);
			}
//			System.err.println(id);
		}
		ev.consume();
	}

	//---------------------------------------------------------------------
	private void buildBirdList( ListView<String> birdList)
	{
        BirdCell.initListView(birdList);
	}
	//---------------------------------------------------------------------
	private void showToolbar(boolean isVis)
	{
	    Node node = editor.lookup(".top-toolbar");
	    if (node != null) 	{	node.setManaged(isVis); 	node.setVisible(isVis);		}
	    node = editor.lookup(".bottom-toolbar");
	    if (node != null)  	{	node.setManaged(isVis); 	node.setVisible(isVis);		}
	}

	private boolean isToolbarVisible()
	{
	    Node node = editor.lookup(".top-toolbar");
	    return node != null && node.isVisible();
	}
	
	@FXML private void toggleToolbar()
	{
	   boolean vis = isToolbarVisible();
	   showToolbar(!vis);
	}
//
//	public class ResourceService {
//	    private ObservableList<String> collections;
//	 
//	    JsonBuilderFactory builderFactory =  Json.createBuilderFactory(Collections.emptyMap());
//	 
//	    public JsonArray collectionToJsonArray() {
//	        return collections.createACollection()
//	                .stream()
//	                .map(myObject -> myObject.id) // field is public
//	                .collect(
//	                        builderFactory::createArrayBuilder,
//	                        (a, s) -> a.add(s),
//	                        (b1, b2) -> b1.add(b2))
//	                .build();
//	    }
//	}
//	//---------------------------------------------------------------------
	public void load(String s)
	{
		try {
			s = s.replace("'", "\"");
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(s);  
			
			JSONArray todo= (JSONArray) jsonObject.get("todo");			if (todo != null) loadList(listToDo, todo);
			JSONArray read= (JSONArray) jsonObject.get("read");			if (read != null) loadList(listRead, read);
			JSONArray write= (JSONArray) jsonObject.get("write");		if (write != null) loadList(listWrite, write);
			JSONArray create= (JSONArray) jsonObject.get("create");		if (create != null) loadList(listCreate, create);
			JSONArray events= (JSONArray) jsonObject.get("events");		if (events != null) loadList(listEvent, events);
			String promptStr = "" + jsonObject.get("prompt");
			if (promptStr.length() > 4)
				prompt.setText(promptStr);
		}
		catch (ParseException ex) {			ex.printStackTrace();		} 
		catch (NullPointerException ex) {			ex.printStackTrace();		}

	}
	//---------------------------------------------------------------------
	@FXML public void save()
	{
		StringBuffer buffer = new StringBuffer("{ \n");
		ObservableList<Node> kids = sceneGroup.getChildren();
		for (Node k : kids)
			if (k instanceof ImageView)
			{
				if (k.getId() == null) continue;
				buffer.append("[ \"id\": " + '"' + k.getId() + '"' + " ");
				buffer.append(" \"x\": " + k.getTranslateX() + " ");
				buffer.append(" \"y\": " + k.getTranslateY() + " ] \n");
			}
		buffer.append(" }");
		System.out.println(buffer.toString());
}

	//---------------------------------------------------------------------
	public void loadList(ListView<String> ctrl, JSONArray jsonArray)
	{
		Iterator<?> i = jsonArray.iterator();
		ObservableList<String> items = FXCollections.observableArrayList();
		
		while (i.hasNext()) {
			String innerObj = (String) i.next();
			items.add(innerObj);
		}
		ctrl.setItems(items);

	}
	//---------------------------------------------------------------------

	
}
