/*
 * Copyright (c) 2014 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package chart.heatmap;

import gui.Borders;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import chart.heatmap.HeatMap.OpacityFn;


/**
 * Created by * User: hansolo  27.12.12
 */
public class AppHeatMap extends Application {
    private HeatMap                     heatMap;			// the key ImageView subclass
    private HBox						heatMapBox;
    private VBox                      	pane;
    private Slider                      sliderOpacity;
    private ChoiceBox<ColorMapping>     choiceBoxMapping;
    private CheckBox                    checkBoxFadeColors;
    private Slider                      sliderRadius;
    private ChoiceBox<OpacityFn> 		choiceBoxOpacityFn;
    private Button                      clearHeatMap;
    private Button                      button;
    
    private EventHandler<ActionEvent>   handler;		// a unified event handler built in init()

    static int winSize = 600;

    public static void main(String[] args) {        launch(args);    }

    // ******************** Initialization ************************************
    @Override public void init() {
        pane                         = new VBox(10);
        heatMap                      = new HeatMap(winSize, winSize, ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED);
        heatMapBox 					= new HBox(heatMap);       		 pane.setBorder(Borders.greenBorder);
        button                		= new Button("Add 1000");
        sliderOpacity                = new Slider();
        choiceBoxMapping             = new ChoiceBox<>();
        checkBoxFadeColors           = new CheckBox("Fade colors");
        sliderRadius                 = new Slider();
        choiceBoxOpacityFn = new ChoiceBox<>();
        clearHeatMap                 = new Button("Clear");
        handler                      = EVENT -> {				// TODO strange way to assign handlers 
            final Object SRC = EVENT.getSource();
            if (SRC.equals(choiceBoxMapping)) 
            {
                String s = choiceBoxMapping.getSelectionModel().getSelectedItem().toString();
               heatMap.setColorMapping(ColorMapping.valueOf(s));
            } 
            else if (SRC.equals(choiceBoxOpacityFn)) 
            {
                String str = choiceBoxOpacityFn.getSelectionModel().getSelectedItem().toString();
                heatMap.setOpacityDistribution(OpacityFn.valueOf(str));
                heatMap.updateMonochromeMap(OpacityFn.valueOf(str));
            } 
            else if (SRC.equals(checkBoxFadeColors)) 
            {
                heatMap.setFadeColors(checkBoxFadeColors.isSelected());
            } 
            else if (SRC.equals(clearHeatMap)) 
            {
                heatMap.clearHeatMap();
            }
            else if (SRC.equals(button))
            {
                generateEvents(1000, 300, 0.4, 300, .1);
            }
        };
        registerListeners();
    }


    // ******************** Start *********************************************
	private static final Image IMAGE = new Image(AppHeatMap.class.getResourceAsStream("afront.png"));

	@Override public void start(Stage stage) {
     
    	setupControls();
        HBox line1 = new HBox(10, sliderOpacity, choiceBoxMapping, checkBoxFadeColors);
        HBox line2 = new HBox(10,   sliderRadius, choiceBoxOpacityFn, button, clearHeatMap);
        line1.setPadding(new Insets(10, 10, 10, 10));
        line2.setPadding(new Insets(10, 10, 10, 10));

        heatMap.setImage(IMAGE);
        heatMap.setFitWidth(winSize);
        heatMap.setFitHeight(winSize);
        heatMapBox.setBorder(Borders.blueBorder);
        heatMapBox.setPadding(new Insets(10,100,10,10));
        pane.getChildren().addAll(line1, line2, heatMapBox);
        

        Scene scene = new Scene(pane, winSize, winSize, Color.GRAY);
        stage.setTitle("JavaFX HeatMap Demo");
        stage.setScene(scene);
        stage.show();
    }

     private void generateEvents(int nEvents, double xTarget,double xCv,double yTarget,double yCv)
    {
    	ObservableList<Point2D> pts = FXCollections.observableArrayList();
    	for (int i=0; i<nEvents; i++)
    		pts.add( randomNormal(xTarget, xCv, yTarget, yCv));
		heatMap.addEvents(pts);
   }
    
 	//-----------------------------------------------------------------
 	// Box-Mueller method to generate values in a normal distribution
 	// http://en.wikipedia.org/wiki/Normal_distribution#Generating_values_from_normal_distribution
     //	CV is the coefficient of variance:  stdev / mean
 	static double TAO = 2.0 * Math.PI;

 	public static Point2D randomNormal(double xMean, double xCv, double yMean, double yCV)
 	{
 		double U = Math.random();
 		double V = Math.random();
 		double xStdev = xCv * xMean;
 		double yStdev = yCV * yMean;
 		double x = Math.sqrt(-2.0 * Math.log(U)) * Math.cos(TAO * V);
 		double y = Math.sqrt(-2.0 * Math.log(U)) * Math.sin(TAO * V);
 		return new Point2D(xMean + x * xStdev,yMean + y * yStdev);
 	}
    // ******************** Methods *******************************************
	// this ties all the controls' actions to the handler object
	// that was set up in init()
    // usually (IMO) that code would be spliced in here
	
	private void setupControls()
   {
	    sliderOpacity.setMin(0);
        sliderOpacity.setMax(1);
        sliderOpacity.setValue(heatMap.getOpacity());
        sliderOpacity.valueChangingProperty().addListener((val, a, b) -> heatMap.setOpacity(sliderOpacity.getValue()));

        choiceBoxMapping.getItems().setAll(ColorMapping.values());
        choiceBoxMapping.getSelectionModel().select(heatMap.getColorMapping());
        choiceBoxMapping.addEventHandler(ActionEvent.ACTION, handler);		

        choiceBoxOpacityFn.getItems().setAll(OpacityFn.values());
        choiceBoxOpacityFn.getSelectionModel().select(heatMap.getOpacityDistribution());
        choiceBoxOpacityFn.addEventHandler(ActionEvent.ACTION, handler);		

        checkBoxFadeColors.setSelected(heatMap.isFadeColors());
        checkBoxFadeColors.setOnAction(handler);				

        sliderRadius.setMin(10);
        sliderRadius.setMax(50);
        sliderRadius.setValue(heatMap.getEventRadius());
        sliderRadius.valueChangingProperty().addListener((val, a, b) -> heatMap.setEventRadius(sliderRadius.getValue()));

        button.setOnAction(handler);	
        clearHeatMap.setOnAction(handler);	
   }
	
	private void registerListeners() {
		
//    	heatMap.setOnMousePressed(event -> {System.out.println("MousePressed");});
        
    	heatMapBox.setOnMouseClicked(event -> {
    		System.out.println("setOnMouseClicked");
            double r = heatMap.getEventRadius();
            double x = PIN(event.getX() - r / 2, r, heatMap.getLayoutBounds().getWidth() - r);
            double y = PIN(event.getY() - r / 2, r, heatMap.getLayoutBounds().getHeight() - r);
            heatMap.addEvent(x, y);
        });
//        pane.widthProperty().addListener((ov, oldWidth, newWidth) -> heatMapBox.setPrefWidth(newWidth.doubleValue()-200));
//        pane.heightProperty().addListener((ov, oldHeight, newHeight) -> heatMapBox.setPrefHeight(newHeight.doubleValue()-200));
    }
	
	private double PIN(double v, double min, double max)	{	
		return (v < min)  ? min : ((v > max) ?  max : v);
	}

}
