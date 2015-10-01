package image.animation;

//http://a-hackers-craic.blogspot.com/2012/09/javafx-slideshow.html
// this code is public domain

// This is not meant to be wonderful exemplary code, it was just
// my first experiment with JavaFX animations.

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Simple slide show with transition effects.
 */
public class AppSlideShow extends Application {

    StackPane root;
    ImageView current;
    ImageView next;
    int width = 720;
    int height = 580;

    @Override
    public void start(Stage primaryStage) 
    {
        root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("Photos");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        loader = new ScannerLoader();		
        loader.start();					// Start worker thread, 
        startImage(getNextImage());		//and kick off first fade in.
    }
    ScannerLoader loader;

    public void startImage(Image image) {
        ObservableList<Node> c = root.getChildren();
        if (image == null) 	{ System.err.println("empty image");	return;  }
        if (current != null)
            c.remove(current);

        current = next;
        next = null;

        // Create fade-in for new image.
        next = new ImageView(image);

        next.setFitHeight(height);
        next.setFitHeight(width);
        next.setPreserveRatio(true);
        next.setOpacity(0);

        c.add(next);

        FadeTransition fadein = new FadeTransition(Duration.seconds(1), next);

        fadein.setFromValue(0);
        fadein.setToValue(1);

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        SequentialTransition st;
        if (current != null) {

             ScaleTransition dropout = new ScaleTransition(Duration.seconds(1), current);
            dropout.setInterpolator(Interpolator.EASE_OUT);
            dropout.setFromX(1);
            dropout.setFromY(1);
            dropout.setToX(0.75);
            dropout.setToY(0.75);
            st = new SequentialTransition( new ParallelTransition(fadein, dropout), delay);
        } 
        else st = new SequentialTransition( fadein, delay);

        st.setOnFinished(t -> {   startImage(getNextImage());    });

        st.playFromStart();
    }

    @Override
    public void stop() throws Exception {
        loader.interrupt();
        loader.join();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
    BlockingQueue<Image> images = new ArrayBlockingQueue(5);

    Image getNextImage() {
        if (loader.complete) {
            return images.poll();
        }
        try {
            return images.take();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppSlideShow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Scans directories and loads images one at a time.
     */
    class ScannerLoader extends Thread implements FileVisitor<Path> {

        // Directory to start scanning for pics
        String root = "";
        boolean complete;

        @Override  public void run() {
            System.out.println("scanning");
            try {
                Files.walkFileTree(Paths.get(root), this);
                System.out.println("complete");
            } catch (IOException ex) {
                Logger.getLogger(AppSlideShow.class.getName())
                    .log(Level.SEVERE, null, ex);
            } finally {
                complete = true;
            }
        }


        @Override public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException 
        {
            try {
                Image image = new Image(t.toUri().toString(), width, height, true, true, false);

                if (!image.isError()) {         images.put(image);   }
            } catch (InterruptedException ex) 
            {
                Logger.getLogger(AppSlideShow.class.getName()).log(Level.SEVERE, null, ex);
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override  public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa)  throws IOException {    return FileVisitResult.CONTINUE;        }
        @Override  public FileVisitResult visitFileFailed(Path t, IOException ioe)    throws IOException {  return FileVisitResult.CONTINUE;    }
        @Override  public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {  return FileVisitResult.CONTINUE;  }
    }
}
