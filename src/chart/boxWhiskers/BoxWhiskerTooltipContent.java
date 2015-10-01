/*
 * Copyright (c) 2008, 2013 Oracle and/or its affiliates.
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
package chart.boxWhiskers;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * The content for box plot tool tips
 */
public class BoxWhiskerTooltipContent  extends GridPane {
    private Label minValue = new Label();
    private Label maxValue = new Label();
    private Label value25 = new Label();
    private Label value50 = new Label();
    private Label value75 = new Label();

    BoxWhiskerTooltipContent() {
        Label valMin = new Label("MIN:");
        Label valMax = new Label("MAX:");
        Label val25 = new Label("25th:");
        Label val50 = new Label("50th:");
        Label val75 = new Label("75th:");
        valMin.getStyleClass().add("box-tooltip-label");
        valMax.getStyleClass().add("box-tooltip-label");
        val25.getStyleClass().add("box-tooltip-label");
        val50.getStyleClass().add("box-tooltip-label");
        val75.getStyleClass().add("box-tooltip-label");
        setConstraints(valMin, 0, 0);        	setConstraints(minValue, 1, 0);
        setConstraints(val25, 0, 1);        	setConstraints(value25, 1, 1);
        setConstraints(val50, 0, 2);        	setConstraints(value50, 1, 2);
        setConstraints(val75, 0, 3);        	setConstraints(value75, 1, 3);
        setConstraints(valMax, 0, 4);        	setConstraints(maxValue, 1, 4);
      getChildren().addAll(valMin, minValue, valMax, maxValue, val25, value25, val50, value50, val75, value75);
    }

    public void update(double[] vals) {
    	minValue.setText(String.format("%3.2f",  vals[1]));
    	value25.setText(String.format("%3.2f",  vals[2]));
    	value50.setText(String.format("%3.2f",  vals[3]));
    	value75.setText(String.format("%3.2f",  vals[4]));
    	maxValue.setText(String.format("%3.2f",  vals[5]));
    }
}