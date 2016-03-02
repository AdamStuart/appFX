/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package diagrams.grapheditor.window;

import diagrams.grapheditor.GNodeSkin;
import diagrams.grapheditor.SkinLookup;
import diagrams.grapheditor.model.GNode;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.shape.Rectangle;

/**
 * The minimap representation of a single node in the graph editor.
 */
public class MinimapNode extends Rectangle {

    private static final String STYLE_CLASS = "minimap-node";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private final ChangeListener<? super Boolean> selectionListener = (v, o, n) -> setSelected(n);

    /**
     * Creates a new {@link MinimapNode} instance.
     *
     * @param node the {@link GNode} that this minimap node is representing
     * @param skinLookup the {@link SkinLookup} instance in use by the graph editor
     */
    public MinimapNode(final GNode node, final SkinLookup skinLookup) {

        getStyleClass().setAll(STYLE_CLASS);

        if (skinLookup != null) {

            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

            if (nodeSkin != null) {
                setSelected(nodeSkin.isSelected());
                nodeSkin.selectedProperty().addListener(selectionListener);
            } else {
                setSelected(false);
            }
        } else {
            setSelected(false);
        }
    }

    /**
     * Sets the style class of the minimap node according to whether its node is selected or not.
     *
     * @param selected {@code true} if the corresponding node is selected
     */
    private void setSelected(final boolean selected) {
        pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, selected);
    }
}
