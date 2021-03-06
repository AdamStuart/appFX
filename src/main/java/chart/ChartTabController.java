package chart;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import chart.boxWhiskers.BoxWhiskersController;
import chart.flexiPie.FlexiPieController;
import chart.timeseries.AppTimeSeries;
import chart.treemap.BudgetItem;
import chart.treemap.Treemap;
import chart.trendlines.TrendlineController;
import chart.wordcloud.ColorPalette;
import chart.wordcloud.WordCloud;
import chart.wordcloud.WordcloudController;
import chart.wordcloud.bg.Background;
import chart.wordcloud.bg.CircleBackground;
import chart.wordcloud.bg.RectangleBackground;
import chart.wordcloud.collide.CollisionMode;
import chart.wordcloud.font.scale.LinearFontScalar;
import chart.wordcloud.nlp.FrequencyAnalyzer;
import chart.wordcloud.nlp.WordFrequency;
import gui.Backgrounds;
import gui.Borders;
import gui.Effects;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import util.FileUtil;

public class ChartTabController implements Initializable
{
	@FXML private StackPane pieContainer;
	@FXML private StackPane hoverContainer;
	@FXML private StackPane whiskersContainer;
	@FXML private StackPane drillDownContainer;
	@FXML private StackPane histogramContainer;
	@FXML private StackPane fancyContainer;
	@FXML private StackPane timeSeriesContainer;
	@FXML private StackPane usMapContainer;
	@FXML private StackPane wordcloudContainer;
	@FXML private StackPane treemapContainer;
	@FXML private StackPane trendlineContainer;
	@FXML private ListView<File> textfileList;
	@FXML private TreeTableView<BudgetItem> budgetTable;
	@FXML private TreeTableColumn<TreeTableView<BudgetItem>, BudgetItem> categoryColumn;
	@FXML private TreeTableColumn<TreeTableView<BudgetItem>, Double> budgetColumn;
    

	@FXML private RadioButton circular;
	@FXML private RadioButton rectangular;
	@FXML private RadioButton custom;
	@FXML private RadioButton normal;
	@FXML private RadioButton polar;
	@FXML private RadioButton layered;
	private WordcloudController wcc;

    static private String FXML = "";
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("initialize");		
		Objects.requireNonNull(pieContainer);
		Objects.requireNonNull(hoverContainer);
		Objects.requireNonNull(whiskersContainer);
		Objects.requireNonNull(drillDownContainer);
		Objects.requireNonNull(histogramContainer);
		Objects.requireNonNull(fancyContainer);
		Objects.requireNonNull(timeSeriesContainer);
		Objects.requireNonNull(usMapContainer);
		Objects.requireNonNull(wordcloudContainer);
		Objects.requireNonNull(treemapContainer);
		Objects.requireNonNull(budgetTable);
		Objects.requireNonNull(categoryColumn);
		Objects.requireNonNull(budgetColumn);
		
		
	    //----------------------------------------- Histograms
	    FXMLLoader fxmlLoader = new FXMLLoader();
	    URL url = getClass().getResource(FXML + "histograms/HistogramChart.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
			histogramContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)		{	e.printStackTrace();	}
		
	    //----------------------------------------- Box Whiskers
	    BoxWhiskersController contl = new BoxWhiskersController();
	    contl.createContent(whiskersContainer);
	    
	    //----------------------------------------- FlexiPie
	    FlexiPieController pieCon = new FlexiPieController();
	    pieContainer.getChildren().add( pieCon.createContent());
	    
	    //----------------------------------------- FancyChart
	    fxmlLoader = new FXMLLoader();
	    url = getClass().getResource("fancychart/FancyChart.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
	    	fancyContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)		{	e.printStackTrace();	}

	    //----------------------------------------- US Map
	    fxmlLoader = new FXMLLoader();
	    url = getClass().getResource("usMap/us-map.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
	    	usMapContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)		{	e.printStackTrace();}
	    
	    //----------------------------------------- Word Map
	    addDropHandler(textfileList);
	    textfileList.setBorder(Borders.thinGold);
	    textfileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	    textfileList.setCellFactory(p -> { return new ListCell<File>()
			{
				@Override protected void updateItem(File item, boolean empty)
				{
					super.updateItem(item, empty);
					if (item == null) setText(""); 
					else setText(item.getName());
				}
			};
		});
	    //----------------------------------------- Tree Map
	    addDropHandler(textfileList);
	    BudgetItem root = BudgetItem.makeBudget();
	    treemapContainer.getChildren().add(new Treemap(root));
	    treemapContainer.layout();
		TreeItem<BudgetItem> tree = BudgetItem.createTreeItems(root);
		budgetTable.setRoot(tree);
		tree.setExpanded(true);
		categoryColumn.setCellValueFactory(new TreeItemPropertyValueFactory("label"));  
		budgetColumn.setCellValueFactory(new TreeItemPropertyValueFactory("amount"));  
		budgetColumn.setCellFactory( p ->	{	return new TwoDigitCell();	});
		

	    //----------------------------------------- Trendlines
    	TrendlineController controller = new TrendlineController();
	    trendlineContainer.getChildren().add(controller.createContent());
		
	    //----------------------------------------- Time Series
	    fxmlLoader = new FXMLLoader();
	    url = AppTimeSeries.class.getResource(FXML + "TimeSeries.fxml");
	    fxmlLoader.setLocation(url);
	    try
		{
	    	timeSeriesContainer.getChildren().add(fxmlLoader.load());
		} catch (IOException e)		{	e.printStackTrace();}
	    

//	    wcc = new WordcloudController();
	    
	}
	class TwoDigitCell extends TreeTableCell<TreeTableView<BudgetItem>, Double>
	{
		@Override protected void updateItem(Double item, boolean empty)
		{
			super.updateItem(item, empty);
			setText(item == null || empty ? "" : String.format("%.2f", item));
		}	
	}
	private void addDropHandler(ListView<File> fileList)
	{
		fileList.setOnDragEntered(e ->
		{
			fileList.setEffect(Effects.innershadow);
			fileList.setBackground(Backgrounds.tan);
			e.consume();
		});
		// drops don't work without this line!
		fileList.setOnDragOver(e ->	{	e.acceptTransferModes(TransferMode.ANY);  e.consume();	});
		
		fileList.setOnDragExited(e ->
		{
			fileList.setEffect(null);
			fileList.setBackground(Backgrounds.white);
			e.consume();
		});
		
		fileList.setOnDragDropped(e -> {	e.acceptTransferModes(TransferMode.ANY);
			Dragboard db = e.getDragboard();
//			Set<DataFormat> formats = db.getContentTypes();
//			formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
			fileList.setEffect(null);
			fileList.setBackground(Backgrounds.white);
			if (db.hasFiles())  addFiles(db.getFiles());
		});
	}
	//--------------------------------------------------------------------------------
	   private static final ColorPalette COLORS = new ColorPalette(Color.web("4055F1FF"), 
	    				Color.web("408DF1FF"),	Color.web("40AAF1FF"),	Color.web("40C5F1FF"),
	    				Color.web("40D3F1FF"),	Color.web("FFFFFFFF"));


	@FXML public void create()
	{
		System.out.println("create");

		final FrequencyAnalyzer frequencyAnalizer = new FrequencyAnalyzer();
		List<WordFrequency> wordFrequencies; // = frequencyAnalizer.load(strs);
		String url = null;   //"http://www.ncbi.nlm.nih.gov/pubmed/12746906";
		try
		{
			if (url != null)
				wordFrequencies = frequencyAnalizer.load(url);		//"http://www.nytimes.com/"
		}
		catch (Exception e)
		{
			return;
		}
		

		List<File> selectedFiles = textfileList.getSelectionModel().getSelectedItems();
		if (selectedFiles.size() > 0 || url != null)
		{
			for(File f : selectedFiles)
				System.out.println(f.getName());

			List<String> strs = new ArrayList<String>();
			for(File f : selectedFiles)
				strs.add(FileUtil.readFileIntoString(f.getAbsolutePath()));
			
//			if (wordFrequencies == null || wordFrequencies.isEmpty())
//				System.out.println("frequencyAnalizer.load failed");
			wordFrequencies = frequencyAnalizer.load(strs);
			final WordCloud wordCloud = new WordCloud(600, 600, CollisionMode.RECTANGLE);
			wordCloud.setPadding(2);
			boolean isCircular = circular.isSelected();
			Background bg = isCircular ? new CircleBackground(300) : new RectangleBackground(600, 600);
			wordCloud.setBackground(bg);
			wordCloud.setColorPalette(COLORS);
			wordCloud.setFontScalar(new LinearFontScalar(6, 48));
			wordCloud.build(wordFrequencies);
			
			WritableImage wimg = new WritableImage(600,  600);
			wimg = SwingFXUtils.toFXImage(wordCloud.getBufferedImage(), wimg);
			
			ImageView view = new ImageView(wimg);
			wordcloudContainer.getChildren().clear();
			wordcloudContainer.getChildren().add(view);
		}
	}
	public void addFiles(List<File> inFiles)
	{
		System.out.println("addFiles");
		if (inFiles != null)
		{
//			for(File f : inFiles)			System.out.println(f.getName());
			for(File f : inFiles)
				textfileList.getItems().add(f);
		}
	}
	
	@FXML public void addfile()
	{
		FileChooser chooser = new FileChooser();
		File file = chooser.showOpenDialog(AppChartTabs.getInstance().getStage());
		addFiles(FXCollections.observableArrayList(file));
	}
	
	@FXML public void addurl()
	{
		System.out.println("addurl");
	
	}
}
