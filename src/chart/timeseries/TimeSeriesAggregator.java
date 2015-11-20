package chart.timeseries;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javafx.scene.chart.XYChart;
import model.AggregationLevel;

public class TimeSeriesAggregator
{
	List<XYChart.Series<String, Number>>  dataSeries;
	public TimeSeriesAggregator(List<XYChart.Series<String, Number>> inSeries) 
	{
		dataSeries = inSeries;
	}

	public void generateSeries( List<TreeMap<LocalDateTime, Integer>> values, AggregationLevel level) 
	{
		dataSeries.clear();
		for (LocalDateTime time : values.get(0).keySet())
			System.out.println(time.toLocalDate() + " = " + values.get(0).get(time));
		genSeries(values,level );
	}
	
	//-----------------------------------------------------------------------
	public void genSeries(List<TreeMap<LocalDateTime, Integer>> chartValues, AggregationLevel level) {
		
		for(TreeMap<LocalDateTime, Integer> chartValue:chartValues)
		{	
			XYChart.Series<String, Number> data = new XYChart.Series<String, Number>();
			for (HashMap.Entry<LocalDateTime, Integer> entry : chartValue.entrySet()) 
			{
				LocalDateTime time = entry.getKey();
				int value = entry.getValue();
				data.getData().add( new XYChart.Data<String, Number>(timeToStr(time, level), value));
			}
			dataSeries.add(data);
		}
	}

	//-----------------------------------------------------------------------
	static String timeToStr(LocalDateTime time, AggregationLevel level)
	{
		switch (level) 
		{
			case MINUTES:	return minutesStr(time);	
			case HOUR:		return hoursStr(time);		
			case DAY:		return dayStr(time);	
			case WEEK:		return dayStr(time);		
			case MONTH:		return monthsStr(time);	
			case YEAR:		return time.getYear()+"";	
			default:		return "";
		}
	}
	static String minutesStr(LocalDateTime time)	{	return time.getHour() + " Hrs" + time.getMinute() + " Mins";	}
	static String hoursStr(LocalDateTime time)		{	return time.getDayOfMonth() + " " + time.getHour()+ " Hrs";	}
	static String monthsStr(LocalDateTime time)		{	return	time.getMonth().toString() + "," + time.getYear();	}
	static String dayStr(LocalDateTime time)		{	return	time.getMonthValue() + " / " + time.getDayOfMonth();	}

}


