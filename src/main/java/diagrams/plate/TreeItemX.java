package diagrams.plate;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;


public class TreeItemX<T> extends TreeItem<T> implements ChangeListener<Number> {

	public static DataFormat dataFormat = new DataFormat("mycell");
	private static IntegerProperty ind = new SimpleIntegerProperty(-1);			//which item is now currently being dragged
	private static Object temp = null;
	private ObservableList<T> items;
	private boolean draggable = true;

	private static int toBeDeleted = -1;		// When any cell is dragged then I'm being named which index to be deleted
	private String styleclass = "tree-itemx";

	public void setDraggable(boolean b) {	draggable = b;	}
	public boolean isDraggable() {		return draggable;	}
	Pane subPane;

	public void init(ObservableList<T> itms) {
		items = itms;
		subPane = new Pane();
//		this.indexProperty().addListener(this);
//		this.getStyleClass().add(styleclass);
		// this.setStyle("-fx-background-color : red; ");
	}
	
	private final class TextFieldTreeCellImpl extends TreeCell<String> {
		 
        private TextField textField;
        private ContextMenu rightClickMenu = new ContextMenu();
        
        public TextFieldTreeCellImpl() 
        {
        	 MenuItem addMenuItem = new MenuItem("Add");			// i18n
             rightClickMenu.getItems().add(addMenuItem);
             addMenuItem.setOnAction(new EventHandler() {      public void handle(Event t) {  }        });
          	 MenuItem copyMenuItem = new MenuItem("Copy");
             rightClickMenu.getItems().add(copyMenuItem);
             copyMenuItem.setOnAction(new EventHandler() {      public void handle(Event t) {  }        });
          	 MenuItem delMenuItem = new MenuItem("Delete");
             rightClickMenu.getItems().add(delMenuItem);
             delMenuItem.setOnAction(new EventHandler() {      public void handle(Event t) {  }        });
        }
 
        @Override
        public void startEdit() 
        {
            super.startEdit();
 
            if (textField == null) 
                createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
 
        @Override
        public void cancelEdit() 
        {
            super.cancelEdit();
            setText((String) getItem());
            setGraphic(getTreeItem().getGraphic());
        }
 
        @Override
        public void updateItem(String item, boolean empty) 
        {
            super.updateItem(item, empty);
 
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    if (!getTreeItem().isLeaf()&&getTreeItem().getParent()!= null )
                           setContextMenu(rightClickMenu);
                }
            }
        }
 
        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
 
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }
 
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

	@Override
	public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {

		 
		if (isDraggable() && getParent() != null) 
		{
			subPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {		}			//	getTreeView().scrollTo(getIndex());	
			});
			subPane.setOnDragEntered(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent arg0) {
					// System.out.println("Entered");
					if (arg0.getTransferMode() == TransferMode.MOVE) {
						String cellS = (String) arg0.getDragboard().getContent(dataFormat);
		int index = 0;
						Object o = arg0.getDragboard().getContent(dataFormat);
						if (toBeDeleted == -1) 			return;
						if (toBeDeleted != -1) 
						{
							items.remove(toBeDeleted);
							toBeDeleted = -1;
						}
						if (o != null && temp != null) {
							if (index < items.size())
								items.add(index, (T) temp);
							else if (index == items.size())
								items.add((T) temp);
						}
						ind.set(index);
					}
				}

			});
			ind.addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					int index = 0;
					if (index == ind.get()) {
//						InnerShadow is = new InnerShadow();
//						is.setOffsetX(1.0);
//						is.setColor(Color.web("#666666"));
//						is.setOffsetY(1.0);
//						setEffect(is);
//					} else  setEffect(null);
				 }
				}

			});
			// Some body just went off dragging from my cell.
			subPane.setOnDragExited(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent arg0) {
					// System.out.println("Exited");
					int index = 0;
					if (arg0.getTransferMode() == TransferMode.MOVE)
					{
						Object o = arg0.getDragboard().getContent(dataFormat);
						if (o != null)
						{
//							setEffect(null);
							if (index < items.size())
								toBeDeleted = index;
						}
					}
				}
			});

			// OMG! That mice pressed me. I need to take some action
			subPane.pressedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					InnerShadow is = new InnerShadow();
					is.setOffsetX(1.0);
					is.setColor(Color.web("#666666"));
					is.setOffsetY(1.0);
					if (arg2) {
						// //System.out.println("Pressed " + getIndex() +
						// " "+items.size());

					} //else		setEffect(null);
				}
			});

			subPane.setOnDragOver(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {		event.acceptTransferModes(TransferMode.MOVE);	}
			});

			subPane.setOnDragDetected(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					// System.out.println("Detected");
					int index = 0;
					if (event.getSource() instanceof TreeView)
					{
					TreeView view = (TreeView) event.getSource();
					Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
					temp = items.get(index);
					toBeDeleted = index;
					Object item = items.get(index);
					
					ClipboardContent content = new ClipboardContent();				/* Put a string on a dragboard */
//					if (item instanceof Person)
//						content.put(dataFormat, (item != null) ? item.toString() + " | " + ((Person)item).getImageUrl() : "XData");
//					else
						content.put(DataFormat.PLAIN_TEXT, (item != null) ? item.toString() : "NoData??");
					db.setContent(content);
//					event.consume();
					}
				}

			});
		}
	}
}
