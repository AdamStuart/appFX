package image;

/**
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
 
/**
 * A sample that demonstrates the use of double properties and Image Operators.
 */
public class ImageOperatorSample extends Application {
    public static void main(String[] args) { launch(args); }
     
    SimpleDoubleProperty gridSize = new SimpleDoubleProperty(8.0);
    SimpleDoubleProperty hueFactor = new SimpleDoubleProperty(12.0);
    SimpleDoubleProperty hueOffset = new SimpleDoubleProperty(240.0);
     
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        final WritableImage img = new WritableImage(300, 300);
       gridSize.addListener(observable -> {  renderImage(img);   });
        hueFactor.addListener(observable -> { renderImage(img);   });
        hueOffset.addListener(observable -> { renderImage(img);   });
        ImageView view = new ImageView(img);
        renderImage(img);
       root.getChildren().add(view);
    }
    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }

    
    private void renderImage(WritableImage img) {
        PixelWriter pw = img.getPixelWriter();
        double w = img.getWidth();
        double h = img.getHeight();
        double grid = gridSize.get();
        double hueF = hueFactor.get();
        double hueOff = hueOffset.get();
        double xRatio = 0.0;
        double yRatio = 0.0;
        double hue = 0.0;
        grid *= Math.PI;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) 
            {
                xRatio = x/w;
                yRatio = y/h;
                hue = Math.sin(yRatio * grid)* Math.sin(xRatio * grid)*
                				Math.tan(hueF/20.0)*360.0 + hueOff;
                Color c = Color.hsb(hue, 1.0, 1.0);
                pw.setColor(x, y, c);
            }
        }
    }
 
 }