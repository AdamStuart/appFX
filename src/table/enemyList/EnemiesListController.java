/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package table.enemyList;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import gui.Effects;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * based on FriendsList @author Narayan G. Maharjan
 */

//-----------------------------------------------------------------
public class EnemiesListController extends Application implements Initializable
{
	public static void main(String[] args) {	launch(args);	}
	
	// @FXML these fields are pre-constructed and injected here
	@FXML private Text heading, personTxt, nameTxt;
	@FXML private VBox namePane, personPane;
	@FXML private Rectangle rect;
	@FXML private ListView<Person> nameList;
	@FXML private ListView<Person> personList;
	@FXML private GridPane grid;

	// this is normally kept in a separate file, but is easier to understand here
	static String CSS = 
			".main				{  	-fx-background-color : linear-gradient(to bottom, #ecfaff 0%,#b8f5ff 100%);}" +
			".list-cell:empty	{   -fx-background-color : white;}" +
			".rect				{   -fx-fill : linear-gradient(to bottom, #14a1c0 0%,#0d7d96 100%);   }"+
			".heading			{   -fx-font: 22 Arial;    -fx-fill: yellow;}"+
			".sub-heading		{   -fx-font: 15 Arial;    -fx-fill: black;}";
	
	///-------------------------------------------------------------------------------------------
	@Override public void start(Stage primaryStage) {

		System.out.println(System.getProperty("javafx.version"));

		BorderPane pane = null;
		try {
			pane = (BorderPane) FXMLLoader.load(getClass().getResource("enemies.fxml"));
			pane.getStyleClass().add("main");
		} catch (IOException ex) {

			Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
			System.exit(-1);
		}
		primaryStage.setScene(new Scene(pane, 700, 550));
		primaryStage.setTitle("Enemies List");
		
		//CSS = getClass().getResource("enemies.css").toExternalForm();
		primaryStage.getScene().getStylesheets().add(CSS);
		primaryStage.show();
	}

	// Adding some fashion styles for some text and rectangle guys
	///-------------------------------------------------------------------------------------------
	public void setStyles()
	{
		personTxt.getStyleClass().add("sub-heading");
		nameTxt.getStyleClass().add("sub-heading");
		heading.getStyleClass().add("heading");
		rect.getStyleClass().add("rect");
	}


	private void setupModel()
	{
		for (int i = 0; i < str.length; i++)
		{
			URL url = EnemiesListController.class.getResource("/images/" + (i + 1) + ".png");
			if (url != null)
				obj.add(new Person(str[i], url.toExternalForm()));
		}
	}

	///-------------------------------------------------------------------------------------------
	/* This is the main initializer of the JavaFX Application */
	@Override public void initialize(URL url, ResourceBundle rb)
	{
		setStyles();
		setupModel();

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
			{
				ImageView node = new ImageView();
				// node.setStyle("-fx-background: yellow");
				URL res = EnemiesListController.class.getResource("noImage.png");
				String png = res == null ? null : res.toExternalForm();
				if (png != null)
					node.setImage(new Image(png));
				// TODO SCALE THE IMAGE TO THE GRID CELL SIZE
				node.setOnDragEntered(e ->		{	if (verbose) System.out.println("dragEntered ");				e.consume();				});
				node.setOnDragDropped(e ->
				{
					if (verbose) System.out.println("dragDropped " + e.getX() + ", " + e.getY());
					Dragboard db = e.getDragboard();
					
					Set<DataFormat> o = db.getContentTypes();
					if (verbose) 	o.forEach(a -> System.out.println("getContentTypes " + a.toString()));
					if (o.size() > 0)
					{
						DataFormat f = o.iterator().next();
						if (f != null)
						{
							if (verbose) 	System.out.println("format = " + f.toString());
							Object p = db.getContent(f);
							String urlStr = p.toString().substring(2 + p.toString().indexOf('|'));
							node.setImage(new Image(urlStr));
							if (verbose) 	System.out.println("the content is " + p == null ? "empty" : urlStr);
						}
					}
					e.consume();
				});
				this.grid.add(node, i, j);
			}

		nameList.itemsProperty().set(obj);
		personList.itemsProperty().set(obj);
		setupCellFactories();

		grid.setOnDragOver(e ->	{		e.acceptTransferModes(TransferMode.ANY);	e.consume();	});
		grid.setOnDragDropped(e ->	{ 	e.setDropCompleted(true);		e.consume();
				if (verbose) System.out.println("Drop " + e.getX() + ", " + e.getY());
			});

		// This is just for layouting all the gui components in main application
		StackPane.setAlignment(personTxt, Pos.CENTER_LEFT);
		StackPane.setAlignment(nameTxt, Pos.CENTER_LEFT);
		
		// Some kind of flexible and adaptable behaviour
		HBox.setHgrow(namePane, Priority.NEVER);
		HBox.setHgrow(personPane, Priority.NEVER);
	}
	///-------------------------------------------------------------------------------------------

	private void setupCellFactories()
	{
		nameList.setCellFactory(arg -> {
			ListCellX<Person> cell = new ListCellX<Person>()
			{
				@Override public void updateItem(Person item, boolean empty)
				{
					if (item != null)
					{
						super.updateItem(item, empty);
						setText(item.getName());
					}
				}
			};
			cell.init(obj);
			return cell;
		});

		personList.setCellFactory(arg -> {
			ListCellX<Person> cell = new ListCellX<Person>()
			{
				@Override public void updateItem(Person item, boolean empty)
				{
					super.updateItem(item, empty);
					if (item != null)
					{
						HBox box = new HBox();
						StackPane pane = new StackPane();
						ImageView r = new ImageView();
						Image im = new Image(item.getImageUrl());
						r.setImage(im);
						r.setFitHeight(50);
						r.setPreserveRatio(true);
						r.setFitWidth(50);

						Rectangle rect = new Rectangle();
						rect.setWidth(50);
						rect.setHeight(50);
						pane.getChildren().addAll(rect, r);

						Text t = new Text(item.getName());
						box.getChildren().addAll(pane, t);
						box.setSpacing(10);
						box.setPadding(new Insets(5, 5, 5, 5));
						setGraphic(box);
					}
				}
			};
			cell.init(obj);
			return cell;
		});
	}
	
	static DataFormat dataFormat = new DataFormat("mycell");
	private static IntegerProperty ind = new SimpleIntegerProperty(-1);			//which item is now currently being dragged
	private static Object temp = null;

///-------------------------------------------------------------------------------------------
 class ListCellX<T> extends ListCell<T> implements ChangeListener<Number> {

	private ObservableList<T> items;
	private boolean draggable = true;

	private int toBeDeleted = -1;		// When any cell is dragged then I'm being named which index to be deleted
	private String styleclass = "list-cellx";

	public boolean isDraggable() {		return draggable;	}

	// Basic mind making I'm adding up to the cell so that
	// those cell can learn how to take place of another cell
	public void init(ObservableList<T> itms) {
		items = itms;
		indexProperty().addListener(this);
		getStyleClass().add(styleclass);
		// this.setStyle("-fx-background-color : red; ");
	}

	@Override public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
		if (isDraggable() && getIndex() < items.size()) {
			setOnMouseClicked(e -> {		getListView().scrollTo(getIndex());	});
			setOnDragEntered(ev -> {
					// System.out.println("Entered");
					if (ev.getTransferMode() == TransferMode.MOVE) {
						String cellS = (String) ev.getDragboard().getContent(dataFormat);

						Object o = ev.getDragboard().getContent(dataFormat);
						if (toBeDeleted == getIndex()) 			return;
						if (toBeDeleted != -1) 
						{
							items.remove(toBeDeleted);
							toBeDeleted = -1;
						}
						if (o != null && temp != null) {
							if (getIndex() < items.size())
								items.add(getIndex(), (T) temp);
							else if (getIndex() == items.size())
								items.add((T) temp);
						}
						ind.set(getIndex());
					}
			});
			ind.addListener(observable -> { setEffect((getIndex() == ind.get()) ? Effects.innershadow : null);  });
			// Some body just went off dragging from my cell.
			setOnDragExited(ev-> {
				if (ev.getTransferMode() == TransferMode.MOVE)
				{
					Object o = ev.getDragboard().getContent(dataFormat);
					if (o != null)
					{
						setEffect(null);
						if (getIndex() < items.size())
							toBeDeleted = getIndex();
					}
				}
			});

			pressedProperty().addListener((obs, old, nVal) -> {	setEffect (nVal ? Effects.innershadow : null); 	});

			setOnDragOver(event -> {	event.acceptTransferModes(TransferMode.MOVE);	});
			setOnDragDetected(event -> {
				Dragboard db = getListView().startDragAndDrop(TransferMode.MOVE);
				temp = items.get(getIndex());
				toBeDeleted = getIndex();
				Object item = items.get(getIndex());
				
				ClipboardContent content = new ClipboardContent();				/* Put a string on a dragboard */
				content.put(dataFormat, (item != null) ? item.toString() + " | " + ((Person)item).getImageUrl() : "XData");
				
				db.setContent(content);
				event.consume();

			});
		}
	}
}
///-------------------------------------------------------------------------------------------
	final ObservableList<Person> obj = FXCollections.observableArrayList();
	final String[] str = new String[] { "Hillary", "Ted", "Jeb", "Donald", "Bernie" };
	boolean verbose = false;

	class Person {
	    private String name;
	    private String imageUrl;
	    
	    public Person(String n,String u) 	{        name = n;    imageUrl = u;    }
	    public String getImageUrl() 		{        return imageUrl;    }
	    public void setImageUrl(String u) 	{        imageUrl = u;    }
	    public String getName() 			{        return name;    }
	    public void setName(String n) 		{        name = n;    }
	    @Override public String toString()  {        return name;    }
	}
}
