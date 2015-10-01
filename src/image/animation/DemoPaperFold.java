package image.animation;

/*
 * Copyright (c) 2014 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import gui.Backgrounds;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import util.UtilTransitions;
import util.UtilTransitions.Transition;

public class DemoPaperFold extends Application {
	
    private ImageView imagePane;
    private AnchorPane pane;
    Button showButton;
Image front, back;
StackPane root;

     @Override public void start(Stage stage) {
        
          front =  new Image(getClass().getResource("front.png").toExternalForm());
          back =  new Image(getClass().getResource("back.png").toExternalForm());
       pane = new AnchorPane();        
        GridPane grid = new GridPane();
        grid.setBackground(Backgrounds.lightGray);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(20);
        grid.add(new Label("Paper folding is an interesting transition"), 0, 0);
        grid.add(new TextField(), 0, 1);
        grid.add(new Label("Doesn't look that bad... :)"), 0, 2);
        Button hideButton = new Button("Hide");
        hideButton.setOnAction(event -> { hideEffect();	});
        grid.add(hideButton, 0, 3);        

    	showButton = new Button("show");
        showButton.setOnAction(event1 -> {	showEffect(); });
        showButton.toBack();
        imagePane = new ImageView(front);
        pane.getChildren().add(grid);
        grid.add(imagePane, 0, 4);
       
  	
        root = new StackPane();
        root.getChildren().addAll(showButton, imagePane, pane);

        Scene scene = new Scene(root);
        scene.setCamera(new PerspectiveCamera());

        stage.setScene(scene);
        stage.show();        
    }
    
 
    private void showEffect()
	{
        imagePane.setVisible(false);
        pane.setVisible(true);            
    	if (pf == null)
    		pf = new UtilTransitions(front, back);
      pf.play(Transition.PAPER_FOLD);
	}
	UtilTransitions	pf;

    private void hideEffect()
	{
    	if (pf == null)
    	{
    		pf = new UtilTransitions(front, back);
    		root.getChildren().addAll(pf.getTiles());
    	}
        pf.play(Transition.PAPER_FOLD);
	}

	public static void main(String[] args) {
        launch(args);
    }

 
}