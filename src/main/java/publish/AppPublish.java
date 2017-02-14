package publish;

import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AppPublish extends Application
{
	   private Pane splashLayout;
	    private ProgressBar loadProgress;
	    private Label progressText;
	    private Stage mainStage;
	    private static final int SPLASH_WIDTH = 676;
	    private static final int SPLASH_HEIGHT = 227;

    public static void main(String[] args)    {        launch(args);    }
//	static final String RESOURCE = "publisher.fxml";
	static final String RESOURCE = "apms.fxml";
    static final String STYLE = "publish.css";

	public AppPublish() 					{	   instance = this;	}
	public static AppPublish getInstance() 	{      return instance;	}
	public static Stage getStage() 			{      return stage;	}
	static Stage stage;
	private static AppPublish instance;
	//-----------------------------------------------------------------------------------------
	public void start(Stage initStage) throws Exception 
	{
		stage = initStage;
	    final Task<ObservableList<String>> friendTask = new Task<ObservableList<String>>() {
	     @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> foundFriends = FXCollections.<String>observableArrayList();
                ObservableList<String> availableFriends =
                    FXCollections.observableArrayList("BridgeDB", "GEO", "GO", "STRING", "CluGO",
                       "BINGO", "PubMed", "Entrez", "NCBI", "DAVID", "RServer", "Cluster" );

                updateMessage("Finding services . . .");
                for (int i = 0; i < availableFriends.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 1, availableFriends.size());
                    String nextFriend = availableFriends.get(i);
                    foundFriends.add(nextFriend);
                    updateMessage("Finding services . . . found " + nextFriend);
                }
                Thread.sleep(200);
                updateMessage("All services found.");

                  return foundFriends;
            }
       };
       showSplash( initStage, friendTask, () -> showMainStage(friendTask.valueProperty()) );
       new Thread(friendTask).start();

	}
	//-----------------------------------------------------------------------------------------
	public void doNew(Stage stage) throws Exception 
	{
        URL resource = getClass().getResource(RESOURCE);
        FXMLLoader loader = new FXMLLoader(resource);
        Scene scene = new Scene(loader.load());
        PublishController controller = (PublishController) loader.getController();
		scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());
        stage.setTitle("Cytoscape Protocol: Affinity Purification Mass Spectometry");
        stage.setX(20);
		stage.setWidth(1100);
		stage.setHeight(650);
		controller.start();
		stage.setScene(scene);
		stage.show();
	}
	   public static final String APPLICATION_ICON =
	            "https://www.google.com/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0ahUKEwiT_tS5mujRAhUUImMKHRJnCOwQjRwIBw&url=https%3A%2F%2Ftwitter.com%2Fcytoscape&psig=AFQjCNEXjLVlpXdmACXppxbU6_jfrI2QVQ&ust=1485808496502776";
	    public static final String SPLASH_IMAGE = "file://Users/adamtreister/git/javaFX/appFX/src/main/java/publish/cytoscape.png";


    @Override
    public void init() {
        Image splashImage = new Image(SPLASH_IMAGE);

        ImageView splash = new ImageView(splashImage);
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Loading Cytoscape Services");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                "-fx-background-color: cornsilk; " +
                "-fx-border-width:5; " +
                "-fx-border-color: " +
                    "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                    ");"
        );
        splashLayout.setEffect(new DropShadow());
    

     }

    private void showMainStage( ReadOnlyObjectProperty<ObservableList<String>> friends ) {
        mainStage = new Stage(StageStyle.DECORATED);
        mainStage.setTitle("Cytoscape");
        mainStage.getIcons().add(new Image( APPLICATION_ICON ));
        try
        {
        	doNew(mainStage);
        }
        catch(Exception e){ e.printStackTrace();}
    }

    private void showSplash( final Stage initStage,
            Task<?> task, InitCompletionHandler initCompletionHandler ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public interface InitCompletionHandler {
        void complete();
    }
}