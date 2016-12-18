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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

/**
 * A BoxWhiskerChart is a style of bar-chart used primarily to describe distributions
 * of y values for a given x.  
 *
 * The min, 25th percentile, median, 75th percentile and max values are each shown with horizontal lines
 */
public class BoxWhiskerChart extends XYChart<Number, Number>
{

	// -------------- CONSTRUCTORS ----------------------------------------------
	/**
	 * Construct a new BoxWhiskerChart with the given axis.
	 *
	 * @param xAxis
	 *            The x axis to use
	 * @param yAxis
	 *            The y axis to use
	 */
	public BoxWhiskerChart(Axis<Number> xAxis, Axis<Number> yAxis)
		{
			super(xAxis, yAxis);
			getStylesheets().add(BoxWhiskerChart.class.getResource("BoxWhiskerChart.css").toExternalForm());
			setAnimated(false);
			xAxis.setAnimated(false);
			yAxis.setAnimated(false);
		}

	/**
	 * Construct a new BoxWhiskerChart with the given axis and data.
	 *
	 * @param xAxis
	 *            The x axis to use
	 * @param yAxis
	 *            The y axis to use
	 * @param data
	 *            The data to use, this is the actual list used so any changes
	 *            to it will be reflected in the chart
	 */
	public BoxWhiskerChart(Axis<Number> xAxis, Axis<Number> yAxis, ObservableList<XYChart.Series<Number, Number>> data)
		{
			this(xAxis, yAxis);
			setData(data);
		}

	// -------------- METHODS -----------------------------------------------------------------------
	/** Called to update and layout the content for the plot */
	@Override
	protected void layoutPlotChildren()
	{
		if (getData() == null)			return;			//  nothing to layout if no data is present
		// update box positions
		for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++)
		{
			XYChart.Series<Number, Number> series = getData().get(seriesIndex);
			Iterator<XYChart.Data<Number, Number>> iter = getDisplayedDataIterator(series);
			double boxWidth = -1;
			if (getXAxis() instanceof NumberAxis)
			{
				NumberAxis xa = (NumberAxis) getXAxis();
				boxWidth = xa.getDisplayPosition(xa.getTickUnit()) * .50;
			}
			while (iter.hasNext())
			{
				XYChart.Data<Number, Number> item = iter.next();
				Number a = getCurrentDisplayedXValue(item);
				Number b = getCurrentDisplayedYValue(item);
				
				double x = getXAxis().getDisplayPosition(a);	
				double y = getYAxis().getDisplayPosition(b);
				Node itemNode = item.getNode();
				BoxWhiskerDistribution extra = (BoxWhiskerDistribution) item.getExtraValue();
				if (itemNode instanceof BoxWhiskers && extra != null)
				{
					BoxWhiskers box = (BoxWhiskers) itemNode;
					double[] yVals = new double[6];
					yVals[0] = 0;
					for (int i=1; i<6; i++)
						yVals[i] =  getYAxis().getDisplayPosition(extra.getVal(i));		
					box.update(yVals, x , boxWidth);	
					box.updateTooltip(extra.getVals());
				}
			}
		}
	}

	@Override protected void dataItemChanged(XYChart.Data<Number, Number> item)
	{
	}

	@Override protected void dataItemAdded(XYChart.Series<Number, Number> series, int itemIndex, XYChart.Data<Number, Number> item)
	{
		Node box = createBox(getData().indexOf(series), item, itemIndex);
		if (shouldAnimate())
		{
			box.setOpacity(0);
			getPlotChildren().add(box);
			// fade in new box
			FadeTransition ft = new FadeTransition(Duration.millis(500), box);
			ft.setToValue(1);
			ft.play();
		} else getPlotChildren().add(box);
		
		// always draw average line on top
		if (series.getNode() != null)
			series.getNode().toFront();
	}

	@Override protected void dataItemRemoved(XYChart.Data<Number, Number> item, XYChart.Series<Number, Number> series)
	{
		final Node box = item.getNode();
		if (shouldAnimate())
		{
			// fade out old box
			FadeTransition ft = new FadeTransition(Duration.millis(500), box);
			ft.setToValue(0);
			ft.setOnFinished((ActionEvent actionEvent) -> {		getPlotChildren().remove(box);	});
			ft.play();
		} else	getPlotChildren().remove(box);
	}

	@Override protected void seriesAdded(XYChart.Series<Number, Number> series, int seriesIndex)
	{
		// handle any data already in series
		for (int j = 0; j < series.getData().size(); j++)
		{
			Data<Number, Number> item = series.getData().get(j);
			Node box = createBox(seriesIndex, item, j);
			if (shouldAnimate())
			{
				box.setOpacity(0);
				getPlotChildren().add(box);
				// fade in new box
				FadeTransition ft = new FadeTransition(Duration.millis(500), box);
				ft.setToValue(1);
				ft.play();
			} else	getPlotChildren().add(box);
		}
		// create series path
//		Path seriesPath = new Path();
//		seriesPath.getStyleClass().setAll("box-average-line", "series" + seriesIndex);
//		series.setNode(seriesPath);
//		getPlotChildren().add(seriesPath);
	}

	@Override
	protected void seriesRemoved(XYChart.Series<Number, Number> series)			// remove all box nodes
	{
		for (XYChart.Data<Number, Number> d : series.getData())
		{
			final Node box = d.getNode();
			if (shouldAnimate())
			{
				// fade out old box
				FadeTransition ft = new FadeTransition(Duration.millis(500), box);
				ft.setToValue(0);
				ft.setOnFinished((ActionEvent actionEvent) -> {	getPlotChildren().remove(box); });
				ft.play();
			} else	getPlotChildren().remove(box);
		}
	}

	/**
	 * Create a new boxwhiskers node to represent a single data item
	 *
	 * @param seriesIndex   The index of the series the data item is in
	 * @param item          The data item to create node for
	 * @param itemIndex     The index of the data item in the series
	 * @return New boxwhiskers node to represent the give data item
	 */
	private Node createBox(int seriesIndex, final XYChart.Data item, int itemIndex)
	{
		Node box = item.getNode();
		// check if boxwhiskers has already been created
		if (box instanceof BoxWhiskers)
		{
			((BoxWhiskers) box).setSeriesAndDataStyleClasses("series" + seriesIndex, "data" + itemIndex);
		} else
		{
			box = new BoxWhiskers("series" + seriesIndex, "data" + itemIndex);
			item.setNode(box);
		}
		return box;
	}

	/**
	 * This is called when the range has been invalidated and we need to update
	 * it. If the axis are auto ranging then we compile a list of all data that
	 * the given axis has to plot and call invalidateRange() on the axis passing
	 * it that data.
	 */
	@Override
	protected void updateAxisRange()
	{
		// For boxwhiskers chart we need to override this method as we need to
		// scale the Y axis to the range of maximum values -- which are the last in our array
		
		final Axis<Number> xa = getXAxis();
		final Axis<Number> ya = getYAxis();
		List<Number> xData = null;
		List<Number> yData = null;
		if (xa.isAutoRanging())			xData = new ArrayList<Number>();
		if (ya.isAutoRanging())			yData = new ArrayList<Number>();
		if (xData != null || yData != null)
		{
			for (XYChart.Series<Number, Number> series : getData())
			{
				for (XYChart.Data<Number, Number> data : series.getData())
				{
					if (xData != null)
						xData.add(data.getXValue());
					
					if (yData != null)
					{
						BoxWhiskerDistribution extras = (BoxWhiskerDistribution) data.getExtraValue();
						if (extras != null)
						{
							yData.add(extras.getVal(5));		// we only care about the max value
						} else	yData.add(data.getYValue());		// this is 0 in box-whisker impl
					}
				}
			}
			if (xData != null)		xa.invalidateRange(xData);
			if (yData != null)		ya.invalidateRange(yData);

		}
	}
}