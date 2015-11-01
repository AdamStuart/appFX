package chart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import chart.scatter.SynthFileStream;
import chart.scatter.SynthGenMetaFileStream;
import chart.scatter.SynthGenRecord;
import gui.Borders;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

//https://gist.github.com/jewelsea/5072743
public class OffScreenOffThreadCharts {
  private static final String CHART_FILE_PREFIX = "chart_";
  private static final String WORKING_DIR = System.getProperty("user.dir");
  
  private final static int N_CHARTS     = 80;
  private final int PREVIEW_SIZE = 200;
  private final int CHART_SIZE   = 200;

	public static Pane makeChartPane()
	{
		final VBox layout = new VBox(10);
		new OffScreenOffThreadCharts(layout);
		return layout;
	}
	public static void cancel(){	saveChartsTask.cancel();		}
	public static void shutdown() throws InterruptedException
	{
	    saveChartsExecutor.shutdown();
	    saveChartsExecutor.awaitTermination(5, TimeUnit.SECONDS);
	}

	static private SaveChartsTask<?> saveChartsTask;
	static private ExecutorService saveChartsExecutor;
	private OffScreenOffThreadCharts(VBox layout)
	{
	    saveChartsTask = new SaveChartsTask<>(N_CHARTS);
	    saveChartsExecutor = createExecutor("SaveCharts");
		saveChartsExecutor.execute(saveChartsTask);
		layout.getChildren().add( createProgressPane(saveChartsTask));
		layout.getChildren().add( createChartImagePagination(saveChartsTask));
	}

	private ExecutorService createExecutor(final String name) {       
    ThreadFactory factory = new ThreadFactory() {
      @Override public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name);
        t.setDaemon(true);
        return t;
      }
    };
    
    return Executors.newSingleThreadExecutor(factory);
  }  
  

  //----------------------------------------------------------------------------------------
  private Parent createChart(String xName, String yName) {
    // create a chart.
		System.out.println("scatter");
//		Image prevImage = (transitionType == 0) ? null : chartSnapshot();
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel(xName);
		xAxis.setOnMouseClicked(ev -> {		});
		
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel(yName);
		yAxis.setOnMouseClicked(ev -> {		});
		
		OverlaidScatterChart<Number, Number> chart = new OverlaidScatterChart<Number, Number>(xAxis, yAxis);
		// scatter.setTitle("title goes here");
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setBorder(Borders.blueBorder5);
		}
		SynthGenMetaFileStream input = new SynthGenMetaFileStream(this);
		ObservableList<SynthGenRecord> observableList = input.readDefFile(summaryFileName);
		
		for (SynthGenRecord rec : observableList)
		{
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			for (int i = 0; i < rec.getCount(); i++)
			{
				Point2D randPt = SynthFileStream.randomNormal(rec);
				series.getData().add(new XYChart.Data<Number, Number>(randPt.getX(), randPt.getY()));
			}
			chart.getData().add(series);
		}

		final Pane chartContainer = new Pane();		    // Place the chart in a container pane.
	    chartContainer.getChildren().add(chart);
	    chart.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
	    chart.setPrefSize(CHART_SIZE, CHART_SIZE);
	    chart.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
	    chart.setStyle("-fx-font-size: 16px;");
	    return chartContainer;
	  }
	//------------------------------------------------------------------------------
	public static String summaryFileName = "syntheticDefinitions";
  //----------------------------------------------------------------------------------------
  private Pagination createChartImagePagination(final SaveChartsTask saveChartsTask) {
    final Pagination pagination = new Pagination(N_CHARTS);
    pagination.setMinSize(PREVIEW_SIZE + 100, PREVIEW_SIZE + 100);
    pagination.setPageFactory(new Callback<Integer, Node>() {
      @Override public Node call(final Integer pageNumber) {
        final GridPane page = new GridPane();
        page.setStyle("-fx-background-color: antiquewhite;");
        
        if (pageNumber < saveChartsTask.getWorkDone()-4) {
        	createImageViewForChartFile(page, pageNumber);
        } else 
        {
          ProgressIndicator progressIndicator = new ProgressIndicator();
          progressIndicator.setMaxSize(PREVIEW_SIZE * 1/4, PREVIEW_SIZE * 1/4);
          page.getChildren().setAll(progressIndicator);
          
          final ChangeListener<Number> WORK_DONE_LISTENER = new ChangeListener<Number>() 
          {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
              if (pageNumber < saveChartsTask.getWorkDone()) {
                createImageViewForChartFile(page, pageNumber);
                saveChartsTask.workDoneProperty().removeListener(this);
              }
            }
          };
          saveChartsTask.workDoneProperty().addListener(WORK_DONE_LISTENER);
        }
        return page;
      }
    });
    
    return pagination;
  }
 
  //----------------------------------------------------------------------------------------
  private void createImageViewForChartFile(GridPane page, Integer pageNumber) {
	  
	  for (int row = 0; row < 2; row++)
		  for (int col = 0; col < 2; col++)
		  {
			    ImageView imageView = new ImageView(new Image("file:///" + getChartFilePath((pageNumber*4)+row+col)));
			    imageView.setFitWidth(PREVIEW_SIZE);
			    imageView.setPreserveRatio(true);
			    GridPane.setColumnIndex(imageView, col);
			    GridPane.setRowIndex(imageView, row);
		        page.getChildren().add(imageView);
		  
		  }
  }
 
  //----------------------------------------------------------------------------------------
 private Pane createProgressPane(SaveChartsTask saveChartsTask) {
    GridPane progressPane = new GridPane();
    
    progressPane.setHgap(5);
    progressPane.setVgap(5);
    progressPane.addRow(0, new Label("Create:"),     createBoundProgressBar(saveChartsTask.chartsCreationProgressProperty()));
    progressPane.addRow(1, new Label("Snapshot:"),   createBoundProgressBar(saveChartsTask.chartsSnapshotProgressProperty()));
    progressPane.addRow(2, new Label("Save:"),       createBoundProgressBar(saveChartsTask.imagesExportProgressProperty()));
    progressPane.addRow(3, new Label("Processing:"), 
      createBoundProgressBar(
        Bindings.when(saveChartsTask.stateProperty().isEqualTo(Worker.State.SUCCEEDED))
            .then(new SimpleDoubleProperty(1))
            .otherwise(new SimpleDoubleProperty(ProgressBar.INDETERMINATE_PROGRESS))
      )
    );
 
    return progressPane;
  }
 
  //----------------------------------------------------------------------------------------
  private ProgressBar createBoundProgressBar(NumberExpression progressProperty) {
    ProgressBar progressBar = new ProgressBar();
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.progressProperty().bind(progressProperty);
    GridPane.setHgrow(progressBar, Priority.NEVER);			// was ALWAYS, but that looks bad after completion
    return progressBar;
  }
 
  //------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------
  class ChartsCreationTask extends Task<Void> {
    private final int nCharts;
    private final BlockingQueue<Parent> charts;
    
    ChartsCreationTask(BlockingQueue<Parent> charts, final int nCharts) {
      this.charts = charts;
      this.nCharts = nCharts;
      updateProgress(0, nCharts);
    }
    
    @Override protected Void call() throws Exception {
      int i = nCharts;
      while (i > 0) {
        if (isCancelled())      break;
        charts.put(createChart(getXName(i), getYName(i)));
        i--;
        updateProgress(nCharts - i, nCharts);
      }
      return null;
    }
	
    
    String[] dims = new String[]{"FS", "SS","FL1", "FL2", "FL3", "FL4", "FL5", "FL6", "FL7", "FL8" };
	private String getYName(int i)	{	return dims[i%10];	}
	private String getXName(int i)	{	return dims[(i%100)/10];	}
    
  }
  //----------------------------------------------------------------------------------------

  class ChartsSnapshotTask extends Task<Void> {
    private final int nCharts;
    private final BlockingQueue<Parent> charts;
    private final BlockingQueue<Image> images;
    
    ChartsSnapshotTask(BlockingQueue<Parent> charts, BlockingQueue<Image> images, final int nCharts) {
      this.charts = charts;
      this.images = images;
      this.nCharts = nCharts;
      updateProgress(0, nCharts);
    }
    
    @Override protected Void call() throws Exception {
      int i = nCharts;
      while (i > 0) 
      {
        if (isCancelled())           break;
        images.put(snapshotChart(charts.take()));
        i--;
        updateProgress(nCharts - i, nCharts);
      }
      return null;
    }
    
    //----------------------------------------------------------------------------------------
   private Image snapshotChart(final Parent chartContainer) throws InterruptedException {
      final CountDownLatch latch = new CountDownLatch(1);
      // render the chart in an offscreen scene (scene is used to allow css processing) and snapshot it to an image.
      // the snapshot is done in runlater as it must occur on the javafx application thread.
      final SimpleObjectProperty<Image> imageProperty = new SimpleObjectProperty();
      Platform.runLater(() -> {
//          Scene snapshotScene = new Scene(chartContainer);
          final SnapshotParameters params = new SnapshotParameters();
          params.setFill(Color.ALICEBLUE);
          chartContainer.snapshot( result -> {  imageProperty.set(result.getImage()); latch.countDown(); return null; }, params, null );
      });
      latch.await();
      return imageProperty.get();
    }
  }
  //----------------------------------------------------------------------------------------
  class PngsExportTask extends Task<Void> {
    private final int nImages;
    private final BlockingQueue<Image> images;
    
    PngsExportTask(BlockingQueue<Image> images, final int nImages) {
      this.images = images;
      this.nImages = nImages;
      updateProgress(0, nImages);
    }
    
    @Override protected Void call() throws Exception {
      int i = nImages;
      while (i > 0) 
      {
        if (isCancelled())      break;   
        exportPng(images.take(), getChartFilePath(nImages - i));
        i--;
        updateProgress(nImages - i, nImages);
      }
      return null;
    }
    
    private void exportPng(Image image, String filename) {
      try {
    	  BufferedImage img= SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(img, "png", new File(filename));
      } catch (IOException ex) {
        Logger.getLogger(OffScreenOffThreadCharts.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  //----------------------------------------------------------------------------------------
  class SaveChartsTask<Void> extends Task {
    private final BlockingQueue<Parent>        charts         = new ArrayBlockingQueue(10);
    private final BlockingQueue<Image> images = new ArrayBlockingQueue(10);
    private final ExecutorService    chartsCreationExecutor   = createExecutor("CreateCharts");
    private final ExecutorService    chartsSnapshotExecutor   = createExecutor("TakeSnapshots");
    private final ExecutorService    imagesExportExecutor     = createExecutor("ExportImages");
    private final ChartsCreationTask chartsCreationTask;
    private final ChartsSnapshotTask chartsSnapshotTask;
    private final PngsExportTask     imagesExportTask;
    Task[] tasks;
 
    SaveChartsTask(final int nCharts) {
      chartsCreationTask = new ChartsCreationTask(charts, nCharts);
      chartsSnapshotTask = new ChartsSnapshotTask(charts, images, nCharts);
      imagesExportTask   = new PngsExportTask(images, nCharts);
      tasks = new Task[] { chartsCreationTask, chartsSnapshotTask};
      
      setOnCancelled(ev -> { for (Task t: tasks) t.cancel();  } );
      
      imagesExportTask.workDoneProperty().addListener((observable, oldValue, workDone)->{  updateProgress(workDone.intValue(), nCharts);   });
    }
    
    ReadOnlyDoubleProperty chartsCreationProgressProperty() {     return chartsCreationTask.progressProperty();    }
    ReadOnlyDoubleProperty chartsSnapshotProgressProperty() {     return chartsSnapshotTask.progressProperty();    }
    ReadOnlyDoubleProperty imagesExportProgressProperty() 	{     return imagesExportTask.progressProperty();    }
           
    @Override protected Void call() throws Exception {
      chartsCreationExecutor.execute(chartsCreationTask);
      chartsSnapshotExecutor.execute(chartsSnapshotTask);
      imagesExportExecutor.execute(imagesExportTask);
      chartsCreationExecutor.shutdown();
      chartsSnapshotExecutor.shutdown();
      imagesExportExecutor.shutdown();
      
      try 	{     imagesExportExecutor.awaitTermination(1, TimeUnit.DAYS);      } 
      catch (InterruptedException e) {      /** no action required */    } 
      
      return null;
    }
  }
  
  private String getChartFilePath(int chartNumber) { return new File(WORKING_DIR, CHART_FILE_PREFIX + chartNumber + ".png").getPath();  }



 
}