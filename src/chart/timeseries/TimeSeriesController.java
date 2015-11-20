package chart.timeseries;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import chart.AppChartTabs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.AggregationLevel;
import model.FileData;

public class TimeSeriesController implements Initializable
{

	List<Series<String, Number>> dataSeries;
	
	@FXML private ListView<String> listFile;
	@FXML private Label errLabel;
	@FXML private Button draw;
	@FXML private Button browse;
	@FXML private Pane placeholder;

	@FXML private CheckBox multiChart;
	@FXML private RadioButton rbDay,rbMin,rbHour,rbMonth,rbYear,rbWeek;
	@FXML private RadioButton rbLine,rbBar;
	@FXML private DatePicker fromDate;
	@FXML private DatePicker endDate;

	@FXML private VBox root;
	
	private Dimension screenSize;
	
	private FileData data;
	private List<File> inputFile;
//	private CategoryAxis xAxis;
//	private NumberAxis yAxis;
	private LocalDate minDate,maxDate;

	@FXML private LineChart<String, Number> timeSeriesChart;
	
	ObservableList<String> fileNames =        FXCollections.observableArrayList();
	ToggleGroup aggLevelGroup;
	ToggleGroup chartTypeGroup = new ToggleGroup();
	private Object getApplication()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	    System.out.println("TimeSeriesController.initialize()");
		dataSeries = new ArrayList<XYChart.Series<String, Number>>();
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		multiChart = new CheckBox("Multi Line Enabled");
		aggLevelGroup = rbDay.getToggleGroup();
		data = new FileData();
		minDate = null;
		maxDate = null;

		AssertAll(timeSeriesChart);
//		rbLine.setToggleGroup(chartTypeGroup);
//		rbBar.setToggleGroup(chartTypeGroup);
		rbLine.setSelected(true);
		
//		RadioButton[] buttons = new RadioButton[] { rbMin, rbHour, rbMonth, rbYear, rbWeek, rbDay};
//		for (RadioButton b : buttons)
//			b.setToggleGroup(aggLevelGroup);		// define within fxml
		rbDay.setSelected(true);
		
		String path = "/Users/adam/Desktop/timeSample.csv";
		fileNames.add(path);
//		listFile.setPrefSize(100, 100);
//		int yPos = 0;

//		aggLevelGroup.selectedToggleProperty().addListener(
//				new ChangeListener<Toggle>() {
//
//					public void changed(ObservableValue<? extends Toggle> obVal,
//							Toggle tog, Toggle tog1) {
//
//					}
//				});
	}

	private void AssertAll(Object... list)
	{
		for (Object o : list)
			if (o== null)
				System.err.println("Object Missing");
		
	}
//------------------------------------------------------------------------------------
	private void setAggregationLevel(AggregationLevel lev)
	{
		data.setLevel(lev);		
		timeSeriesChart.getXAxis().setLabel(lev.toString());	
	}
	
	public void handleDraw(ActionEvent event) {
//	xAxis = new CategoryAxis();
//	yAxis = new NumberAxis();
	
		String aggLevelVal =((RadioButton)rbDay.getToggleGroup().getSelectedToggle()).getText(); 
		String chartTypeVal =((RadioButton)rbLine.getToggleGroup().getSelectedToggle()).getText(); 
		LocalDate fromDateVal = fromDate.getValue();
		LocalDate endDateVal = endDate.getValue();				
		CategoryAxis xAxis= (CategoryAxis) timeSeriesChart.getXAxis();
		
		
		if (aggLevelVal.equalsIgnoreCase("Day"))		setAggregationLevel(AggregationLevel.DAY);
		if (aggLevelVal.equalsIgnoreCase("Minute"))		setAggregationLevel(AggregationLevel.MINUTES);	
		
		if (aggLevelVal.equalsIgnoreCase("Month"))
		{
			fromDateVal = fromDateVal.withDayOfMonth(1);
			endDateVal = endDateVal.plusMonths(1).withDayOfMonth(1).minusDays(1);
			setAggregationLevel(AggregationLevel.MONTH);
		}
	
		if (aggLevelVal.equalsIgnoreCase("Week"))
		{
			int dayWeek = fromDateVal.getDayOfWeek().getValue();
			fromDateVal = fromDateVal.minusDays(dayWeek-1);
			dayWeek = endDateVal.getDayOfWeek().getValue();
			endDateVal = endDateVal.plusDays(7-dayWeek);
			setAggregationLevel(AggregationLevel.WEEK);
		}
		
		if (aggLevelVal.equalsIgnoreCase("Year"))
		{
			fromDateVal = fromDateVal.withDayOfYear(1);
			endDateVal = endDateVal.plusYears(1).withDayOfYear(1).minusDays(1);		
			setAggregationLevel(AggregationLevel.YEAR);
		}
	
		if (aggLevelVal.equalsIgnoreCase("Hour"))
			setAggregationLevel(AggregationLevel.HOUR);
	
		if (inputFile.isEmpty()) return;
		data.setFiles(inputFile);
		if (validateData())
		{
			String firstFile = inputFile.get(0).getName();
			data.collectData(fromDateVal, endDateVal, multiChart.isSelected());
			// timeSeriesChart = new LineChart<String, Number>(xAxis, yAxis);
			timeSeriesChart.setTitle(firstFile);
			TimeSeriesAggregator ts = new TimeSeriesAggregator(dataSeries);
			ts.generateSeries(data.getChartValues(), data.getLevel());
			timeSeriesChart.getData().clear();
			timeSeriesChart.getData().addAll(dataSeries);
			setToolTips();
		}
	}

	// for(Series<String, Number> data:dataSeries)
//			timeSeriesChart.getData().add(data);

		//lineChart.setCreateSymbols(false);

	// timeSeriesChart.setPrefSize(splitPane1.getWidth(),splitPane1.getHeight()-100);
	//------------------------------------------------------------------------------------
	@FXML
	private void handleBrowse()
	{
		fileNames.clear();
		FileChooser fileChooser = new FileChooser();
		configureFileChooser(fileChooser);

		
		Stage stage = AppChartTabs.getInstance().getStage();		// TODO assumes parent application!

		inputFile = fileChooser.showOpenMultipleDialog(stage);
		if (inputFile != null)
			for (int i = 0; i < inputFile.size(); i++)
				fileNames.add(inputFile.get(i).getName());
		setDate();
		fromDate.setValue(minDate);
		endDate.setValue(maxDate);
		if (inputFile != null)
			listFile.setItems(fileNames);

	}
	

	//------------------------------------------------------------------------------------
	 private void setToolTips()
	 {
		 for (XYChart.Series<String, Number> s : timeSeriesChart.getData())
	         for (XYChart.Data<String, Number> d : s.getData()) 
	             Tooltip.install(d.getNode(), new Tooltip( String.format("%s = %d", d.getXValue(), d.getYValue())));
	}


	//------------------------------------------------------------------------------------
	 private static void configureFileChooser( FileChooser fileChooser) 
	 {
		fileChooser.setTitle("File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}

//------------------------------------------------------------------------------------


	private boolean validateData()
	{	
		String errText = validateDates();
		if (errText != null)
		{
			errLabel.setText(errText);
			return false;
		}
		errLabel.setText("");
		return true;
	}

	private String validateDates()
	{
		final  String TRY_AGAIN = "Please select other Aggregation period";
		final  String UNDER6MO = "Please select Days less than 6 months";
		final  String LESSER = "Please select lesser duration";
		final  String MISSING = "Please enter both start and end dates";
		
		RadioButton chk = (RadioButton) rbDay.getToggleGroup().getSelectedToggle(); 
		LocalDate fromDateVal = fromDate.getValue();
		LocalDate endDateVal = endDate.getValue();
		if (fromDateVal == null || endDateVal == null )  return MISSING;
		Period difference = Period.between(fromDateVal, endDateVal);
		if (chk.getText().equalsIgnoreCase("Day"))
		{
			if (difference.getMonths() > 6)				return UNDER6MO;
			if (difference.getYears() > 0)				return TRY_AGAIN;
		}
		if (chk.getText().equalsIgnoreCase("Minute"))
		{
			if (difference.getDays() > 0) 					return TRY_AGAIN;
			if (difference.getYears() > 0) 					return TRY_AGAIN;
			if (difference.getMonths() > 0)					return TRY_AGAIN;
		}
		
		if (chk.getText().equalsIgnoreCase("Month"))
			if (difference.getYears() > 20)				return LESSER;

		if (chk.getText().equalsIgnoreCase("Hour"))
			if ((difference.getYears() > 0) || difference.getMonths() > 0)	return TRY_AGAIN;
	return null;
}

	//----------------------------------------------------------------------------------
	// reads the first and last record to establish the date range
	// while loop in middle skips all records, leaving last in prevLine
	
	public void setDate() {		FileInputStream finStream = null;
		BufferedReader buffReader = null;
		if (inputFile == null || inputFile.isEmpty()) return;
		for(File file:inputFile)
		{
		try {
			finStream = new FileInputStream(file);
			String line;
			String csvSplitBy = ",";
			String prevLine = new String();
			buffReader = new BufferedReader(new InputStreamReader(finStream));
			String[] points ;
			if((line = buffReader.readLine()) != null) {
				prevLine = line;
				points = line.split(csvSplitBy);
				LocalDate date;
				final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");		
				date = LocalDate.parse(points[0],dtf);
				if(minDate==null) minDate = date;
				if(Period.between(minDate, date).isNegative())
					minDate = date;
				
			}
			
			while ((line = buffReader.readLine()) != null) {
				prevLine = line;	
			}
			
			if(prevLine.length()>0)
			{
				points = prevLine.split(csvSplitBy);
				LocalDate date;
				final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/dd/yyyy");		
				date = LocalDate.parse(points[0],dtf);
				if(maxDate==null)	maxDate = date;
				if(Period.between(date,maxDate).isNegative())
					maxDate = date;
			}

		} 
		catch (FileNotFoundException e) {	e.printStackTrace();} 
		catch (IOException e) 			{	e.printStackTrace();		} 
		finally 
		{
			try 
			{
				finStream.close();
				buffReader.close();
			} catch (IOException e) {	e.printStackTrace();	} 
		}
		}
	}
}
