/*
 * original: @(#)AnimationDemo.java 5/19/2013
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */

package image.animation;

import animation.AnimationType;
import animation.AnimationUtils;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.NodeUtil;

public class AppAnimationDemo extends Application
{

    private BorderPane _demoPane;
    private ListView<AnimationType> _animationTypeListView;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
	public void start(Stage primaryStage) throws Exception
	{
		VBox v = new VBox();
		Label line1 = new Label(getDescription());
		_demoPane = getDemoPanel();
		_demoPane.setLeft(getOptionsPanel());
		v.getChildren().addAll(line1, _demoPane);
		primaryStage.setScene(new Scene(v));
		primaryStage.show();
	}
    public String getName() {        return "Animation Demo";    }

    public String getDescription() {
        return "Animation demo shows some pre-defined animations.\nMost of the animations are from the fxexperience project created by Jasper Potts.\n";
    }

    public BorderPane getDemoPanel() {
        Button button = new Button("JideFX Rocks!");
        button.setStyle(
                "-fx-background-color: linear-gradient(#ff5400, #be1d00);\n" +
                        "-fx-background-radius: 10;\n" +
                        "-fx-background-insets: 0;\n" +
                        "-fx-padding: 5;\n" +
                        "-fx-text-fill: white;-fx-font-size: 24px;");

        _demoPane = new BorderPane(button);
        _demoPane.setMinSize(400, 400);
        return _demoPane;
    }

    private void bringBackAfter(Node node) {
        PauseTransition transition = new PauseTransition(Duration.seconds(1));
        transition.setOnFinished((ActionEvent t) -> {    NodeUtil.reset(node);    });
        transition.play();
    }

    public VBox getOptionsPanel() {
        VBox optionPane = new VBox();

        _animationTypeListView = new ListView<>();
        _animationTypeListView.getItems().addAll(AnimationType.values());
        _animationTypeListView.getItems().remove(0); // remove NONE
        _animationTypeListView.setPrefHeight(800);
        _animationTypeListView.getSelectionModel().selectedItemProperty().addListener((ob, old, newVal) -> {  play(); });
        optionPane.getChildren().addAll(_animationTypeListView);
        return optionPane;
    }

    private void play() {
    	AnimationType type =  _animationTypeListView.getSelectionModel().getSelectedItem();
        Transition transition = AnimationUtils.createTransition(_demoPane.getCenter(),type);
        if (transition != null) 
        {
            if (type.toString().contains("OUT") ||  transition.getClass().getName().contains("Out") || transition.getClass().getName().contains("Hinge")) {
                transition.setOnFinished(event-> {    bringBackAfter(_demoPane.getCenter());     });
            }
            transition.play();
        }
    }

	
}
