package bookclub;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

// from example by jewelsea on github


public class BirdCell extends ListCell<String> {
 
    private static final String PREFIX = "http://icons.iconarchive.com/icons/jozef89/origami-birds/72/bird";
    private static final String SUFFIX = "-icon.png";
	private static final ObservableList<String> birds = FXCollections.observableArrayList("-black", "-blue", "-red", "-red-2", "-yellow", "s-green", "s-green-2", "-blue", "-red" );
    private static final ObservableList<Image> birdImages = FXCollections.observableArrayList();
    public static ObservableList<Image> getBirdImages()	{ return birdImages;	}

	public static ImageView findBird(String id)
	{
		int idx = birds.indexOf(id);
		if (idx >= 0)
		{
			ImageView newBird = makeDraggableBird(birdImages.get(idx));
			newBird.setId("bird-" + idx);
			return newBird;
		}
		return null;
	}

	//---------------------------------------------------------------------
    public static void initListView(ListView<String> birdList)
	{
		birds.forEach(bird -> birdImages.add(new Image(PREFIX + bird + SUFFIX)));
        birdList.setItems(birds);
        birdList.setCellFactory(p -> new BirdCell());
        birdList.setPrefWidth(180);
        birdList.setMinWidth(180);
	}
	//---------------------------------------------------------------------
	private static ImageView makeDraggableBird(Image i)
	{
		ImageView view = new ImageView(i);
		view.setOnDragDetected( a -> { birdDragStart(a); });
		view.setOnMouseDragged( a -> { birdDrag(a); });
		return view;

	}

	//-----------------------------------------------------
	static double xStart, yStart;
	static private void birdDragStart(MouseEvent ev)
	{
		xStart = ev.getX();
		yStart = ev.getY();
	}

	static private void birdDrag(MouseEvent ev)
	{
		Object t = ev.getTarget();
		if (t instanceof ImageView)
		{
			ImageView iv = (ImageView) t;
			double cx, cy;
			cx = ev.getX();
			cy = ev.getY();
			iv.setTranslateX(iv.getTranslateX() + cx - xStart);
			iv.setTranslateY(iv.getTranslateY() + cy - yStart);
		}
	}
	
	private final ImageView imageView = new ImageView();

    public BirdCell() 
    {
        ListCell<String> thisCell = this;

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setAlignment(Pos.CENTER);

        setOnDragDetected(event -> {
            if (getItem() == null)        return;
            ObservableList<String> items = getListView().getItems();

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(getItem());
            dragboard.setDragView(birdImages.get( items.indexOf( getItem()  ) )   );
            dragboard.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) 
                event.acceptTransferModes(TransferMode.MOVE);
              event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != thisCell &&  event.getDragboard().hasString()) 
                setOpacity(0.3);
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != thisCell &&  event.getDragboard().hasString()) 
                setOpacity(1);
        });

        setOnDragDropped(event -> 
        {      
        	if (getItem() == null)      return; 

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                ObservableList<String> items = getListView().getItems();
                int draggedIdx = items.indexOf(db.getString());
                int thisIdx = items.indexOf(getItem());

                Image temp = birdImages.get(draggedIdx);
                birdImages.set(draggedIdx, birdImages.get(thisIdx));
                birdImages.set(thisIdx, temp);

                items.set(draggedIdx, getItem());
                items.set(thisIdx, db.getString());

                List<String> itemscopy = new ArrayList<>(getListView().getItems());
                getListView().getItems().setAll(itemscopy);

                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        setOnDragDone(DragEvent::consume);
    }

    @Override protected void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);

        if (empty || item == null)        setGraphic(null);
         else 
         {
            imageView.setImage( birdImages.get( getListView().getItems().indexOf(item) ) );
            setGraphic(imageView);
        }
    }

}
