package game.sudoku;

import game.bookclub.BirdCell;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
//just a shell of a sudoku game at this point!!!


public class SudokuGridFactory
{

	public SudokuGridFactory()
	{
		
	}
	
	Game matrix81 = new Game();
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
				label.setId("" + val);
//				label.setScaleX(0.75);
//				label.setScaleY(0.75);
//				label.setOnMouseEntered(null);
//				label.setOnMouseExited(null);

//				Image image = birdImages.get(val);
//				label.setGraphic(new ImageView(image));
////				label.setText("");
//				label.setOnDragDetected(event -> {
//
//		            Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
//		            ClipboardContent content = new ClipboardContent();
//		            content.putString(label.getId());
//		            dragboard.setDragView(((ImageView)(label.getGraphic())).getImage() );
//		            dragboard.setContent(content);
//		            event.consume();
//		        });
//
//				label.setOnDragOver(event -> {
//		            if (event.getGestureSource() != label && event.getDragboard().hasString()) 
//		                event.acceptTransferModes(TransferMode.MOVE);
//		              event.consume();
//		        });
//
//		        label.setOnDragEntered(event -> {
//		            if (event.getGestureSource() != label &&  event.getDragboard().hasString()) 
//		            	label.setOpacity(0.3);
//		        });
//
//		        label.setOnDragExited(event -> {
//		            if (event.getGestureSource() != label &&  event.getDragboard().hasString()) 
//		            	label.setOpacity(1);
//		        });
//
//		        label.setOnDragDropped(event -> 
//		        {      
//		            Dragboard db = event.getDragboard();
//		            boolean success = false;
//
//		            if (db.hasString()) {
//		                success = true;
//		                String str = db.getString();
//		                int idx = Integer.parseInt(str);
//		                Image img = birdImages.get(idx);
//		                ImageView iv = (ImageView)(label.getGraphic());
//		                iv.setImage(img);
//		                label.setId("" + idx);
//		            }
//		            event.setDropCompleted(success);
//		            event.consume();
//		        });
//
//		        label.setOnDragDone(DragEvent::consume);
				GridPane.setConstraints(label, i, j);
				GridPane.setHalignment(label, HPos.CENTER);
				theGrid.getChildren().addAll( label);			 // don't forget to add children to gridpane
			}	
		}
				
	 
	}
	}