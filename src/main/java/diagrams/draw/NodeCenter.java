package diagrams.draw;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public class NodeCenter {
    private ReadOnlyDoubleWrapper centerX = new ReadOnlyDoubleWrapper();
    private ReadOnlyDoubleWrapper centerY = new ReadOnlyDoubleWrapper();
    public ReadOnlyDoubleProperty centerXProperty() {   return centerX.getReadOnlyProperty();    }
    public ReadOnlyDoubleProperty centerYProperty() {   return centerY.getReadOnlyProperty();    }
   
    public NodeCenter(Node node) {
        calcCenter(node.getBoundsInParent());
        node.boundsInParentProperty().addListener((obs, old, bounds) -> {  calcCenter(bounds);  } );
    }

    private void calcCenter(Bounds bounds) {
        centerX.set(bounds.getMinX() + bounds.getWidth()  / 2);
        centerY.set(bounds.getMinY() + bounds.getHeight() / 2);
    }
}