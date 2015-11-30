package bookclub;

import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class Flashcard extends GeneralTransition
{
	//---------------------------------------------------------------------

	public Flashcard(StackPane flashStack)
	{
		  super(Transition.FLIP_VERTICAL, 1, 1);
		  frontImage         = new Image(getClass().getResource("profile.png").toExternalForm());
		  backImage          = new Image(getClass().getResource("todolist.png").toExternalForm());
		  stepSizeX          = frontImage.getWidth() / noOfTilesX;
		  stepSizeY          = frontImage.getHeight() / noOfTilesY;
		
		 assert(flashStack != null);

		  // init the lists
		  int count = 0;
		  for (int y = 0 ; y < noOfTilesY ; y++) 
		  {
		      for (int x = 0 ; x < noOfTilesX ; x++) 
		      {
		          imageViewsFront.add(new ImageView());
		          imageViewsBack.add(new ImageView());

		          timelines.add(new Timeline());
		          tiles.add(new StackPane(imageViewsBack.get(count), imageViewsFront.get(count)));
		          count++;
		      }
		  }
		  flashStack.setPrefSize(frontImage.getWidth(), frontImage.getHeight());
		  flashStack.getChildren().setAll(tiles);
		  flashStack.setOnMousePressed(event -> play());
		//  flashcardStack.setOnKeyPressed(event -> play());
		  play();
	}

	  private void play()
	  {
	      rotateVerticalTilesAroundX(frontImage, backImage, spring, oneSecond, delay);
	      for (Timeline timeline : timelines) 
	          timeline.play();	
	  }

}
