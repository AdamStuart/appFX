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
package chart.candlestick;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * A custom candlestick chart. This sample shows how to extend XYChart base class
 * to create your own two axis chart type.
 *
 * @sampleName Candle Stick Chart
 * @preview preview.png
 * @see javafx.scene.chart.NumberAxis
 * @see javafx.scene.chart.XYChart
 * @docUrl http://docs.oracle.com/javafx/2/charts/jfxpub-charts.htm Using JavaFX Charts Tutorial
 * @related /Charts/Scatter/Scatter Chart
 * @related /Charts/Scatter/Advanced Scatter Chart
 * @highlight
 * @playground chart.data
 * 
 * @playground - (name="xAxis")
 * @playground xAxis.autoRanging
 * @playground xAxis.forceZeroInRange
 * @playground xAxis.lowerBound (min=-100,max=100,step=1)
 * @playground xAxis.upperBound (step=1)
 * @playground xAxis.tickUnit (step=0.5)
 * @playground xAxis.minorTickCount (max=16)
 * @playground xAxis.minorTickLength (max=15)
 * @playground xAxis.minorTickVisible
 * 
 * @playground xAxis.animated
 * @playground xAxis.label
 * @playground xAxis.side
 * @playground xAxis.tickLabelFill
 * @playground xAxis.tickLabelGap
 * @playground xAxis.tickLabelRotation (min=-180,max=180,step=1)
 * @playground xAxis.tickLabelsVisible
 * @playground xAxis.tickLength
 * @playground xAxis.tickMarkVisible
 * 
 * @playground - (name="yAxis")
 * @playground yAxis.autoRanging
 * @playground yAxis.forceZeroInRange
 * @playground yAxis.lowerBound (min=-100,max=30,step=1)
 * @playground yAxis.upperBound (step=1)
 * @playground yAxis.tickUnit (step=0.5)
 * @playground yAxis.minorTickCount (max=16)
 * @playground yAxis.minorTickLength (max=15)
 * @playground yAxis.minorTickVisible
 * 
 * @playground yAxis.animated
 * @playground yAxis.label
 * @playground yAxis.side
 * @playground yAxis.tickLabelFill
 * @playground yAxis.tickLabelGap
 * @playground yAxis.tickLabelRotation (min=-180,max=180,step=1)
 * @playground yAxis.tickLabelsVisible
 * @playground yAxis.tickLength
 * @playground yAxis.tickMarkVisible
 * 
 * @playground - (name="chart")
 * @playground chart.horizontalGridLinesVisible
 * @playground chart.horizontalZeroLineVisible
 * @playground chart.verticalGridLinesVisible
 * @playground chart.verticalZeroLineVisible
 * 
 * @playground chart.animated
 * @playground chart.legendSide
 * @playground chart.legendVisible
 * @playground chart.title
 * @playground chart.titleSide
 */
public class AppCandleStickChart extends Application {
    
    // DAY, CLOSE, LOW, OPEN, AVG, HIGH
    private static double[][] data = new double[][]{
            {1,  10, 20, 30, 40, 150},
            {2,  3, 10, 33, 62, 125},
            {3,  23, 132, 70, 28, 132},
            {4,  2, 10, 34, 72, 130},
            {5,  5, 16, 40, 64, 132},
            {6,  1, 68, 45, 75, 114},
            {7,  5, 20, 44, 68, 139},
            {8,  3, 18, 36, 96, 131},
            {9,  4, 10, 52, 66, 141},
            {10, 8, 14, 38, 78, 136},
            {11, 4, 52, 30, 88,  132.4},
            {12, 4, 20, 46, 85, 131.6},
            {13, 4, 18, 36, 74, 232.6},
            {14, 5, 30, 40, 56, 310.6},
            {15, 4, 33, 40, 58, 120.6},
            {16, 4, 40, 32, 56,  130.1},
            {17, 9, 30, 42, 58, 127.3},
            {18, 2, 18, 30, 50, 111.9},
            {19, 2, 10, 30, 55,  121.9},
            {20, 2, 16, 32, 60, 117.9},
            {21, 5, 10, 44, 62, 138.9},
            {22, 8, 20, 41, 62, 148.9},
            {23, 4, 18, 34, 60, 168.9},
            {24, 3, 23, 26, 52, 168.2},
            {25, 3, 20, 45, 56, 158.9},
            {26, 35, 65, 38, 60, 141.4},
            {27, 4, 12, 30, 8,  169.6},
            {28, 4, 44, 46, 65, 152.2},
            {29, 3, 18, 30, 62, 123},
            {30, 1, 18, 30, 62, 223.2},
            {31, 7, 18, 30, 62, 122}
    };

    private CandleStickChart chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
           
    XYChart.Data<Number,Number> makeDataPoint(double[] day)
    {
    	return new XYChart.Data<Number,Number>(day[0],day[1],new CandleStickExtraValues(day[2],day[3],day[4],day[5]));
    }
    
    public Parent createContent() {
        xAxis = new NumberAxis(0,32,1);
        xAxis.setMinorTickCount(0);
        yAxis = new NumberAxis();
        chart = new CandleStickChart(xAxis,yAxis);
        
        xAxis.setLabel("Day");					// setup chart
        yAxis.setLabel("Price");
        XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();    // add starting data
        for (int i=0; i< data.length; i++) 
            series.getData().add(makeDataPoint(data[i]));

        ObservableList<XYChart.Series<Number,Number>> data = chart.getData();
        if (data == null) 
        {
            data = FXCollections.observableArrayList(series);
            chart.setData(data);
        } 
        else chart.getData().add(series);
        
        return chart;
    }
    
    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }
    
    /** 
     * Java main for when running without JavaFX launcher 
     * @param args command line arguments
     */
    public static void main(String[] args) { launch(args); }
}
