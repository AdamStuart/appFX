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

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 *
 */
public class USMapController implements Initializable {
    @FXML public UnitedStatesMapPane map;
    @FXML public AnchorPane root;
    @FXML public Pane mapPane;
    @FXML private VBox mapHolder;
    @FXML public ChoiceBox<String> regionChoiceBox;
    @FXML public ChoiceBox<String> statChoiceBox;

    private static final ObservableList<String> statTypes = FXCollections.observableArrayList();
    
    @Override public void initialize(URL url, ResourceBundle rb) {				// populate live data regions choicebox
	    System.out.println("USMapController.initialize()");
	    root.setStyle("-fx-background-color:BROWN");
	    map = new UnitedStatesMapPane();
        mapPane.getChildren().add(map);
        
        statTypes.addAll("Births", "Deaths", "Population");
        regionChoiceBox.setItems(USMapRegion.americanRegions);
        regionChoiceBox.getSelectionModel().selectFirst();
        regionChoiceBox.getSelectionModel().selectedItemProperty().addListener((ov,  t,  newVal) ->{  map.zoomRegion(newVal.toString());   }  );
        
        statChoiceBox.setItems(statTypes);
        statChoiceBox.getSelectionModel().selectFirst();
        statChoiceBox.getSelectionModel().selectedItemProperty().addListener((ov,  t,  newVal) -> {  fetchResults();     }  );
        
        fetchResults();
    }
   
    private void fetchResults() 
    {
//        System.err.println("fetchResults() ");
    	HashMap<String, Integer> statePop = new HashMap<String, Integer>();
    	for (String st : USMapRegion.ALL_STATES)
    		statePop.put(st, new Integer(((int)(100000 + (1000000 * Math.random())))));
    	
    	HashMap<String, Color> stateColorMap = ColorUtil.makeColorMap(statePop);
    	map.setStateColors(stateColorMap);
    }
}
