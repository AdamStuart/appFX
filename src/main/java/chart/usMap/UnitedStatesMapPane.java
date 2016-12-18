/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package chart.usMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * A US Map Component which can zoom in on regions and has changeable 
 * colors for each state.
 */
public class UnitedStatesMapPane extends Region {
    private final Translate mapPreTranslate = new Translate();
    private final Scale mapScale = new Scale(1,1,0,0);
    private final Translate mapPostTranslate = new Translate();
    private final Group liveMap = new Group();
    private final Group overlayGroup = new Group();
    private Group statesGroup;
    private Group regionGroup;
    private double lastWidth = -1, lastHeight = -1;
    private double zoomedInScale = 1;
    

    public UnitedStatesMapPane() {
        USMapRegion.initRegions();
       getStyleClass().add("map-pane");
        setMinSize(600, 600);
        setTranslateX(-60);
        setScaleX(0.7);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setPrefWidth(750);
     
        liveMap.setId("liveMap");
        liveMap.setManaged(false);
        liveMap.setCache(true);
        liveMap.setCacheHint(CacheHint.SCALE);
        getChildren().add(liveMap);
        overlayGroup.setId("overlay");
        overlayGroup.setVisible(true);
    
        liveMap.getTransforms().setAll(mapPreTranslate, mapScale, mapPostTranslate);    // set up map transforms
       
        try {
            statesGroup = FXMLLoader.load(UnitedStatesMapPane.class.getResource("us-states-map.fxml")); 		// load map fxml
        } catch (IOException e) {    e.printStackTrace();    }
       
       liveMap.getChildren().addAll(statesGroup, overlayGroup);		 // set live map children
       createStateLabels();
       }
    
    public void setStateColor(String state, Color color) {
        Node stateNode = statesGroup.lookup("#"+state);
        if (stateNode instanceof Shape)            ((Shape)stateNode).setFill(color);
        else if (stateNode instanceof Group)       setGroupColor((Group)stateNode,color);
        
    }
    
    public void setStateColors(HashMap<String, Color> colorMap)
    {
    	for (String key : colorMap.keySet())
    		setStateColor(key, colorMap.get(key));
    }
    
    private void setGroupColor(Group group, Color color) {
        if (group != null) {
            for(Node child: group.getChildren()) 
               if (child instanceof Shape)         ((Shape)child).setFill(color);
               else if (child instanceof Group)    setGroupColor((Group)child,color);
        }
    }
    
    public Bounds getMapBounds() {        return liveMap.getBoundsInLocal();    }
    public Group getOverlayGroup() {        return overlayGroup;    }
    
    private Map<String,Label> stateLabelMap = new HashMap<String,Label>();
 
    
	//-----------------------------------------------------------------------------------------

    private void createStateLabels() {
//    	showKids(this, "");
    	Group overlay = getOverlayGroup();
//    	showKids(overlay, "");
       for(String state: USMapRegion.ALL_STATES) 
       {
            Node stateNode = lookup("#"+state);
            if (stateNode == null)  System.err.println("state not found: " + state);
            else
            {
                Label label = new Label("+10");
                label.setFont(new Font("Times", 24));
                label.getStyleClass().add("heatmap-label");
                label.setTextAlignment(TextAlignment.CENTER);
                label.setAlignment(Pos.CENTER);
                label.setManaged(false);
                label.setOpacity(1);
                label.setVisible(true);
                Bounds stateBounds = stateNode.getBoundsInParent();
                if ("DE".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX()-25, stateBounds.getMinY(), 
                            stateBounds.getWidth()+50, stateBounds.getHeight());
                } else if ("VT".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX(), stateBounds.getMinY()-25, 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else if ("NH".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX(), stateBounds.getMinY()+30, 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else if ("MA".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX()-20, stateBounds.getMinY()-18, 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else if ("RI".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX(), stateBounds.getMinY(), 
                            stateBounds.getWidth()+40, stateBounds.getHeight());
                } else if ("ID".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX(), stateBounds.getMinY()+60, 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else if ("MI".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX()+60, stateBounds.getMinY(), 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else if ("FL".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX()+95, stateBounds.getMinY(), 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else if ("LA".equals(state)) {
                    label.resizeRelocate(stateBounds.getMinX()-50, stateBounds.getMinY(), 
                            stateBounds.getWidth(), stateBounds.getHeight());
                } else {
                    label.resizeRelocate(stateBounds.getMinX(), stateBounds.getMinY(), 
                            stateBounds.getWidth(), stateBounds.getHeight());
                }
                stateLabelMap.put(state, label);
                overlay.getChildren().add(label);
            }
        }
    }
   public void zoomRegion(String regionName) {
        if (regionName == null) return;
        String outlineName = regionName+"-OUTLINE";
        this.regionGroup = null;
        Timeline animateZoom = new Timeline();
            
        for (Node regionGroup: statesGroup.getChildren()) {
            if(regionGroup instanceof Group) {
                if (regionName.equals(regionGroup.getId())) {
                    this.regionGroup = (Group)regionGroup;
                    animateZoom.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(1000), 
                            new KeyValue(regionGroup.opacityProperty(),regionGroup.getOpacity())
                        ),
                        new KeyFrame(Duration.millis(2000), 
                            new KeyValue(regionGroup.opacityProperty(),1, Interpolator.EASE_BOTH)
                        )
                    );
                } else {
                    animateZoom.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(1000), 
                            new KeyValue(regionGroup.opacityProperty(),regionGroup.getOpacity())
                        ),
                        new KeyFrame(Duration.millis(2000), 
                            new KeyValue(regionGroup.opacityProperty(),0.4, Interpolator.EASE_BOTH)
                        )
                    );
                }
            } else { // OUTLINES
                final Node outlineNode = regionGroup;
                if (outlineName.equals(regionGroup.getId())) {
                    regionGroup.setOpacity(0);
                    regionGroup.setVisible(true);
                    animateZoom.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(1000), 
                            new KeyValue(regionGroup.opacityProperty(),regionGroup.getOpacity())
                        ),
                        new KeyFrame(Duration.millis(2000), 
                            new KeyValue(regionGroup.opacityProperty(),0.5, Interpolator.EASE_BOTH)
                        )
                    );
                } else if (regionGroup.isVisible()) { // ONLY NEED TO HIDE VISIBLE OUTLINES
                    animateZoom.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(1000),   new KeyValue(regionGroup.opacityProperty(),regionGroup.getOpacity())
                        ),
                        new KeyFrame(Duration.millis(2000),  t -> {    outlineNode.setVisible(false);    },
                            new KeyValue(regionGroup.opacityProperty(),0, Interpolator.EASE_BOTH)
                        )
                    );
                }
            }
        }
        
        Bounds mapBounds = (regionGroup == null) ? statesGroup.getBoundsInLocal() : 
                regionGroup.getBoundsInLocal();
        final double w = getWidth() - getInsets().getLeft() - getInsets().getRight();
        final double h = getHeight() - getInsets().getTop() - getInsets().getBottom();
        final double mapX = getInsets().getLeft() + ((w - (mapBounds.getWidth()*zoomedInScale))/2);
        final double mapY = getInsets().getTop() + ((h - (mapBounds.getHeight()*zoomedInScale))/2);

        animateZoom.getKeyFrames().add(
                new KeyFrame(Duration.millis(1000), 
                    new KeyValue(mapPreTranslate.xProperty(),mapX, Interpolator.EASE_BOTH),
                    new KeyValue(mapPreTranslate.yProperty(),mapY, Interpolator.EASE_BOTH),
                    new KeyValue(mapScale.xProperty(),zoomedInScale, Interpolator.EASE_BOTH),
                    new KeyValue(mapScale.yProperty(),zoomedInScale, Interpolator.EASE_BOTH),
                    new KeyValue(mapPostTranslate.xProperty(),-mapBounds.getMinX(), Interpolator.EASE_BOTH),
                    new KeyValue(mapPostTranslate.yProperty(),-mapBounds.getMinY(), Interpolator.EASE_BOTH)
                )
            );
        System.out.println("usStates.getCache() = " + statesGroup.isCache());
        animateZoom.play();
    }
    
    public void zoomAll() {
        this.regionGroup = null;
        Timeline animateZoom = new Timeline();
        for (Node regionGroup: statesGroup.getChildren()) {
            if (regionGroup instanceof Group) {
                animateZoom.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(1000),  new KeyValue(regionGroup.opacityProperty(),regionGroup.getOpacity()) ),
                    new KeyFrame(Duration.millis(2000),  new KeyValue(regionGroup.opacityProperty(),1))
                );
            } else if (regionGroup.isVisible()) { // OUTLINE IS VISIBLE
                final Node outlineNode = regionGroup;
                animateZoom.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(1000), 
                        new KeyValue(regionGroup.opacityProperty(),regionGroup.getOpacity())  ),
                    new KeyFrame(Duration.millis(2000), t -> { outlineNode.setVisible(false);   },
                        new KeyValue(regionGroup.opacityProperty(),0) )
                );
            }
        }
        Bounds mapBounds = statesGroup.getBoundsInLocal();
        final double w = getWidth() - getInsets().getLeft() - getInsets().getRight();
        final double h = getHeight() - getInsets().getTop() - getInsets().getBottom();
        final double scaleX = w/mapBounds.getWidth();
        final double scaleY = h/mapBounds.getHeight();
        final double scale = Math.min(scaleX,scaleY);
        final double mapX = getInsets().getLeft() + ((w - (mapBounds.getWidth()*scale))/2);
        final double mapY = getInsets().getTop() + ((h - (mapBounds.getHeight()*scale))/2);
        animateZoom.getKeyFrames().add(
                new KeyFrame(Duration.millis(1000), 
                    new KeyValue(mapPreTranslate.xProperty(),mapX),
                    new KeyValue(mapPreTranslate.yProperty(),mapY),
                    new KeyValue(mapScale.xProperty(), scale),
                    new KeyValue(mapScale.yProperty(), scale),
                    new KeyValue(mapPostTranslate.xProperty(),-mapBounds.getMinX()),
                    new KeyValue(mapPostTranslate.yProperty(),-mapBounds.getMinY())
                )
            );
        animateZoom.play();
    }
    
    @Override protected double computePrefHeight(double d) {       return 100;   }

    @Override protected double computePrefWidth(double d) {        return 100;    }

    @Override protected void layoutChildren() {
        if (getWidth() != lastWidth || getHeight() != lastHeight) {
            final double w = getWidth() - getInsets().getLeft() - getInsets().getRight();
            final double h = getHeight() - getInsets().getTop() - getInsets().getBottom();
            
            // calculate the zoomed in scale to fit southwest
            {
                Bounds mapBounds = lookup("#Southwest").getBoundsInLocal();
                final double scaleX = w/mapBounds.getWidth();
                final double scaleY = h/mapBounds.getHeight();
                Bounds mapBoundsMW = lookup("#Mid-West").getBoundsInLocal();
                final double scaleXmw = w/mapBoundsMW.getWidth();
                final double scaleYmw = h/mapBoundsMW.getHeight();
                zoomedInScale = Math.min(Math.min(scaleX,scaleY), Math.min(scaleXmw,scaleYmw));
            }
            
            Bounds mapBounds = ((regionGroup == null) ? statesGroup : regionGroup).getBoundsInLocal();
            final double scaleX = w/mapBounds.getWidth();
            final double scaleY = h/mapBounds.getHeight();
            final double scale = (regionGroup == null) ? Math.min(scaleX,scaleY) : zoomedInScale;
            final double mapX = getInsets().getLeft() + ((w - (mapBounds.getWidth()*scale))/2);
            final double mapY = getInsets().getTop() + ((h - (mapBounds.getHeight()*scale))/2);
            
            mapPreTranslate.setX(mapX);
            mapPreTranslate.setY(mapY);
            mapScale.setY(scale);
            mapScale.setX(scale);
            mapPostTranslate.setX(-mapBounds.getMinX());
            mapPostTranslate.setY(-mapBounds.getMinY());
        }
    }
    
}
