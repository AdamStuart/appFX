/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package table.enemyList;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 *
 * @author Narayan G. Maharjan
 */
public class GUIFX implements Initializable
{

	// @FXML
	// private TextField filterName,filterPerson;
	@FXML
	private Text heading, personTxt, nameTxt;
	@FXML
	private VBox namePane, personPane;
	@FXML
	private Rectangle rect;
	@FXML
	private ListView<Person> nameList;
	@FXML
	private ListView<Person> personList;
	@FXML
	private GridPane grid;

	// Adding some fashion styles for some text and rectangle guys
	public void setStyles()
	{
		personTxt.getStyleClass().add("sub-heading");
		nameTxt.getStyleClass().add("sub-heading");
		heading.getStyleClass().add("heading");
		rect.getStyleClass().add("rect");
	}

	final ObservableList<Person> obj = FXCollections.observableArrayList();
	final String[] str = new String[] { "Linda", "Michael", "Mike", "Olic", "Narayan" };

	private void setupModel()
	{
		for (int i = 0; i < str.length; i++)
		{
			String png = GUIFX.class.getResource("/com/ngopal/images/" + (i + 1) + ".png").toExternalForm();
			obj.add(new Person(str[i], png));
		}
	}

	/* This is the main initializer of the JavaFX Application */
	@Override
	public void initialize(URL url, ResourceBundle rb)
	{
		setStyles();
		setupModel();

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
			{
				ImageView node = new ImageView();
				// node.setStyle("-fx-background: yellow");
				URL res = GUIFX.class.getResource("/com/ngopal/demo/noImage.png");
				String png = res == null ? null : res.toExternalForm();
				if (png != null)
					node.setImage(new Image(png));
				// TODO SCALE THE IMAGE TO THE GRID CELL SIZE
				node.setOnDragEntered(e ->
				{
					System.out.println("dragEntered ");
					e.consume();
				});
				node.setOnDragDropped(e ->
				{
					System.out.println("dragDropped " + e.getX() + ", " + e.getY());
					Dragboard db = e.getDragboard();
					Set<DataFormat> o = db.getContentTypes();
					o.forEach(a -> System.out.println("getContentTypes " + a.toString()));
					if (o.size() > 0)
					{
						DataFormat f = o.iterator().next();
						if (f != null)
						{
							System.out.println("format = " + f.toString());
							Object p = db.getContent(f);
							String urlStr = p.toString().substring(2 + p.toString().indexOf('|'));
							node.setImage(new Image(urlStr));
							System.out.println("the content is " + p == null ? "empty" : urlStr);
						}
					}
					e.consume();

				});
				this.grid.add(node, i, j);
			}

		nameList.itemsProperty().set(obj);
		personList.itemsProperty().set(obj);
		setupCellFactories();

		this.grid.setOnDragOver(e ->
		{
			e.acceptTransferModes(TransferMode.ANY);
			e.consume();
		});
		this.grid.setOnDragDropped(e ->
		{
			// Dragboard db = e.getDragboard();
			// this.contextCheckers.forEach(checker -> checker.check(db));
			// setDataFormatListView(db);
				e.setDropCompleted(true);

				// figure out what pane we're in, if it has no imageView, make
				// and insert one, then set its image
				System.out.println("Drop " + e.getX() + ", " + e.getY());
				// int col = e.getX() / grid.get
				e.consume();
			});

		// This is just for layouting all the gui components in main application
		StackPane.setAlignment(personTxt, Pos.CENTER_LEFT);
		StackPane.setAlignment(nameTxt, Pos.CENTER_LEFT);
		// StackPane.setAlignment(filterPerson, Pos.CENTER_RIGHT);
		// StackPane.setAlignment(filterName, Pos.CENTER_RIGHT);

		// Somekind of flexible and adaptable behaviour
		HBox.setHgrow(namePane, Priority.NEVER);
		HBox.setHgrow(personPane, Priority.NEVER);

	}

	//
	// EventHandler<? super DragEvent> handleDrop = new EventHandler<?>
	// {
	//
	// };

	private void setupCellFactories()
	{

		nameList.setCellFactory(new Callback<ListView<Person>, ListCell<Person>>()
		{
			@Override
			public ListCellX<Person> call(ListView<Person> arg0)
			{
				ListCellX<Person> cell = new ListCellX<Person>()
				{
					@Override
					public void updateItem(Person item, boolean empty)
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

			}
		});

		personList.setCellFactory(new Callback<ListView<Person>, ListCell<Person>>()
		{
			@Override
			public ListCell<Person> call(ListView<Person> arg0)
			{
				ListCellX<Person> cell = new ListCellX<Person>()
				{
					@Override
					public void updateItem(Person item, boolean empty)
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
			}
		});

	}

}
