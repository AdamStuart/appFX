package chart.wordcloud;

import java.io.File;
import java.util.List;

import chart.wordcloud.bg.CircleBackground;
import chart.wordcloud.collide.CollisionMode;
import chart.wordcloud.font.scale.LinearFontScalar;
import chart.wordcloud.nlp.FrequencyAnalyzer;
import chart.wordcloud.nlp.WordFrequency;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//* http://kennycason.com/posts/2014-07-03-kumo-wordcloud.html

public class AppWordcloud extends Application
{
    public static void main(String[] args) { launch(args); }
   private static final ColorPalette COLORS = new ColorPalette(Color.web("4055F1FF"), 
    				Color.web("408DF1FF"),
    				Color.web("40AAF1FF"),
    				Color.web("40C5F1FF"),
    				Color.web("40D3F1FF"),
    				Color.web("FFFFFFFF"));
	
	@Override public void start(Stage primaryStage) throws Exception
	{
		final FrequencyAnalyzer frequencyAnalizer = new FrequencyAnalyzer();
		
		final String url= "https://www.fastcodesign.com/3053406/how-apple-is-giving-design-a-bad-name";
		final List<WordFrequency> wordFrequencies = frequencyAnalizer.load(url);		//"http://www.nytimes.com/"
		if (wordFrequencies == null || wordFrequencies.isEmpty())
			System.out.println("frequencyAnalizer.load failed");
		final WordCloud wordCloud = new WordCloud(700, 750, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new CircleBackground(300));
		wordCloud.setColorPalette(COLORS);
		wordCloud.setFontScalar(new LinearFontScalar(10, 80));
		wordCloud.build(wordFrequencies);
		
		WritableImage wimg = new WritableImage(600,  600);
		wimg = SwingFXUtils.toFXImage(wordCloud.bufferedImage, wimg);
		
		ImageView view = new ImageView(wimg);
    	Pane root = new Pane();
    	root.getChildren().add(view);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
	}
	

	private File getInputStream(Stage primaryStage)
	{
		String filename = null;
		File file = null;
		if (filename == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Open Text File");
			file = chooser.showOpenDialog(primaryStage);
			if (file == null)			return null;			// open was canceled
		}
		return file;
	}

}
