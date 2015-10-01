package table.enemyList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

/**
 * @author Narayan G. Maharajan
 */
public class ListCellX<T> extends ListCell<T> implements ChangeListener<Number> {
	// This attribute is for making my drag n drop totally private

	public static DataFormat dataFormat = new DataFormat("mycell");
	private static IntegerProperty ind = new SimpleIntegerProperty(-1);			//which item is now currently being dragged
	private static Object temp = null;
	private ObservableList<T> items;
	private boolean draggable = true;

	private static int toBeDeleted = -1;		// When any cell is dragged then I'm being named which index to be deleted
	private String styleclass = "list-cellx";
//	private void setDraggable(boolean b) {	draggable = b;	}

	public boolean isDraggable() {		return draggable;	}

	// Basic mind making I'm adding up to the cell so that
	// those cell can learn how to take place of another cell
	public void init(ObservableList<T> itms) {
		items = itms;

		this.indexProperty().addListener(this);
		this.getStyleClass().add(styleclass);
		// this.setStyle("-fx-background-color : red; ");
	}

	@Override
	public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {

		 
		if (isDraggable() && getIndex() < items.size()) {
			setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {		getListView().scrollTo(getIndex());		}
			});
			setOnDragEntered(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent arg0) {
					// System.out.println("Entered");
					if (arg0.getTransferMode() == TransferMode.MOVE) {
						String cellS = (String) arg0.getDragboard().getContent(dataFormat);

						Object o = arg0.getDragboard().getContent(dataFormat);
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
				}

			});
			ind.addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					if (getIndex() == ind.get()) {
						InnerShadow is = new InnerShadow();
						is.setOffsetX(1.0);
						is.setColor(Color.web("#666666"));
						is.setOffsetY(1.0);
						setEffect(is);
					} else  setEffect(null);
				}

			});
			// Some body just went off dragging from my cell.
			setOnDragExited(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent arg0) {
					// System.out.println("Exited");
					if (arg0.getTransferMode() == TransferMode.MOVE)
					{
						Object o = arg0.getDragboard().getContent(dataFormat);
						if (o != null)
						{
							setEffect(null);
							if (getIndex() < items.size())
								toBeDeleted = getIndex();
						}
					}
				}
			});

			// OMG! That mice pressed me. I need to take some action
			pressedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					InnerShadow is = new InnerShadow();
					is.setOffsetX(1.0);
					is.setColor(Color.web("#666666"));
					is.setOffsetY(1.0);
					if (arg2) {
						// //System.out.println("Pressed " + getIndex() +
						// " "+items.size());

					} else		setEffect(null);
				}
			});

			setOnDragOver(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {		event.acceptTransferModes(TransferMode.MOVE);	}
			});

			setOnDragDetected(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					// System.out.println("Detected");
					Dragboard db = getListView().startDragAndDrop(TransferMode.MOVE);
					temp = items.get(getIndex());
					toBeDeleted = getIndex();
					Object item = items.get(getIndex());
					
					ClipboardContent content = new ClipboardContent();				/* Put a string on a dragboard */
					content.put(dataFormat, (item != null) ? item.toString() + " | " + ((Person)item).getImageUrl() : "XData");
					
					db.setContent(content);
					event.consume();

				}

			});
		}
	}
}
