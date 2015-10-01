package chart.timeseries;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import model.AggregationLevel;
import javafx.scene.chart.XYChart;

public class TimeSeriesAggregator
{
	List<XYChart.Series<String, Number>>  dataSeries;
	public TimeSeriesAggregator(List<XYChart.Series<String, Number>> inSeries) 
	{
		dataSeries = inSeries;
	}

	public void generateSeries(
			List<TreeMap<LocalDateTime, Integer>> chartValues, AggregationLevel level) 
	{
	
		for (LocalDateTime time : chartValues.get(0).keySet())
			System.out.println(time.toLocalDate() + " = " + chartValues.get(0).get(time));

		switch (level) 
		{
			case MINUTES:	generateMinuteSeries(chartValues);		break;
			case HOUR:		generateHourSeries(chartValues);		break;
			case DAY:		generateDaySeries(chartValues);			break;
			case WEEK:		generateDaySeries(chartValues);			break;
			case MONTH:		generateMonthSeries(chartValues);		break;
			case YEAR:		generateYearSeries(chartValues);		break;
			default:		break;
		}

	}

	//-----------------------------------------------------------------------
	
	public void generateMinuteSeries(List<TreeMap<LocalDateTime, Integer>> chartValues) {
		
		for(TreeMap<LocalDateTime, Integer> chartValue:chartValues)
		{	
			XYChart.Series<String, Number> data = new XYChart.Series<String, Number>();
			for (HashMap.Entry<LocalDateTime, Integer> entry : chartValue.entrySet()) 
			{
			LocalDateTime time = entry.getKey();
			int value = entry.getValue();

			data.getData().add(
					new XYChart.Data<String, Number>(minutesStr(time), value));
		}
		dataSeries.add(data);
	}
	}

	String minutesStr(LocalDateTime time)
	{
		return time.getHour() + " Hrs" + time.getMinute() + " Mins";
	}
	String hoursStr(LocalDateTime time)
	{
		return time.getDayOfMonth()+ " " + time.getHour()+ " HRS";
	}
	String monthsStr(LocalDateTime time)
	{
		return	time.getMonth().toString() + "," + time.getYear();
	}
	String dayStr(LocalDateTime time)
	{
		return	time.getMonthValue() + " / " + time.getDayOfMonth();
	}
	/*@param
	 *@return
	 *@throws
	 *
	 * 
	 */
	public void generateHourSeries(List<TreeMap<LocalDateTime, Integer>> chartValues) {
		
		for(TreeMap<LocalDateTime, Integer> chartValue:chartValues)
		{	
			XYChart.Series<String, Number> data = new XYChart.Series<String, Number>();
			for (HashMap.Entry<LocalDateTime, Integer> entry : chartValue.entrySet()) {
			LocalDateTime time = entry.getKey();
			int value = entry.getValue();
			
			data.getData().add( new XYChart.Data<String, Number>(hoursStr(time), value));
		}
		dataSeries.add(data);
	}
	}
	/*@param  chartValues:  Date / Integer pairs
	 *@return
	 *@throws
	 *
	 * 
	 */
	public void generateDaySeries(List<TreeMap<LocalDateTime, Integer>> chartValues) {
		
		for(TreeMap<LocalDateTime, Integer> chartValue:chartValues)
		{	
			XYChart.Series<String, Number> data = new XYChart.Series<String, Number>();
			for (HashMap.Entry<LocalDateTime, Integer> entry : chartValue.entrySet()) 
			{
				LocalDateTime time = entry.getKey();
				int value = entry.getValue();
				data.getData().add(new XYChart.Data<String, Number>(dayStr(time), value));
			}
			dataSeries.add(data);
		}
	}

	/*@param chartValues:  Date / Integer pairs
	 *@return
	 *@throws
	 *
	 * 
	 */
	public void generateMonthSeries(List<TreeMap<LocalDateTime, Integer>> chartValues) {
		
		for(TreeMap<LocalDateTime, Integer> chartValue:chartValues)
		{	
			XYChart.Series<String, Number> data = new XYChart.Series<String, Number>();
			for (HashMap.Entry<LocalDateTime, Integer> entry : chartValue.entrySet()) 
			{
				LocalDateTime time = entry.getKey();
				int value = entry.getValue();
				data.getData().add(new XYChart.Data<String, Number>(monthsStr(time), value));
			}
			dataSeries.add(data);
		}
	}

	/*@param chartValues:  Date / Integer pairs
	 * 
	 */
	public void generateYearSeries(List<TreeMap<LocalDateTime, Integer>> chartValues) {
		
		for(TreeMap<LocalDateTime, Integer> chartValue:chartValues)
		{	
			XYChart.Series<String, Number> data = new XYChart.Series<String, Number>();
			for (HashMap.Entry<LocalDateTime, Integer> entry : chartValue.entrySet()) 
			{
				LocalDateTime time = entry.getKey();
				int value = entry.getValue();
				data.getData().add(new XYChart.Data<String, Number>(time.getYear()+"", value));
			}
			dataSeries.add(data);
		}
	}
}


