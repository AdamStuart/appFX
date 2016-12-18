package container;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
//https://gist.github.com/jewelsea/6375211

/** Reports load times for pages loaded in a WebView */
public class LoadTimer extends Application {
  public static void main(String[] args) { launch(args); }
  static String URL = "http://www.fxexperience.com";

 
  @Override public void start(final Stage stage) {
    final WebView webview  = new WebView();
    VBox layout = new VBox();
    layout.getChildren().setAll(createProgressReport(webview.getEngine()),  webview );
    stage.setScene(new Scene(layout));
    stage.show();
    webview.getEngine().load(URL);
  }

  /** @return a HBox containing a ProgressBar bound to engine load progress and a Label showing load times */
  private HBox createProgressReport(WebEngine engine) {
    final LongProperty startTime   = new SimpleLongProperty();
    final LongProperty endTime     = new SimpleLongProperty();
    final LongProperty elapsedTime = new SimpleLongProperty();

    final ProgressBar loadProgress = new ProgressBar();
    loadProgress.progressProperty().bind(engine.getLoadWorker().progressProperty());

    final Label loadTimeLabel = new Label();
    loadTimeLabel.textProperty().bind(
        Bindings.when( elapsedTime.greaterThan(0))
              .then(  Bindings.concat("Loaded page in ", elapsedTime.divide(1_000_000), "ms")  )
              .otherwise( "Loading..."  ));

    elapsedTime.bind(Bindings.subtract(endTime, startTime));
    engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
      @Override  public void changed(ObservableValue<? extends Worker.State> obs, Worker.State old, Worker.State state) {
        switch (state) {
          case RUNNING:    startTime.set(System.nanoTime());  break;
          case SUCCEEDED:  endTime.set(System.nanoTime());    break;
        }
      } });

    HBox progressReport = new HBox(10);
    progressReport.getChildren().setAll( loadProgress,loadTimeLabel );
    progressReport.setPadding(new Insets(5));
    progressReport.setAlignment(Pos.CENTER_LEFT);
    return progressReport;
  }
}