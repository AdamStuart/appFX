package diagrams.grapheditor.demo.customskins;

import diagrams.grapheditor.Commands;
import diagrams.grapheditor.GraphEditor;
import diagrams.grapheditor.GraphEditorContainer;
import diagrams.grapheditor.demo.customskins.tree.TreeConnectionSkin;
import diagrams.grapheditor.demo.customskins.tree.TreeConnectorSkin;
import diagrams.grapheditor.demo.customskins.tree.TreeNodeSkin;
import diagrams.grapheditor.demo.customskins.tree.TreeSkinConstants;
import diagrams.grapheditor.demo.customskins.tree.TreeTailSkin;
import diagrams.grapheditor.model.GConnector;
import diagrams.grapheditor.model.GNode;
import diagrams.grapheditor.model.GraphFactory;
import javafx.geometry.Side;

/**
 * Responsible for tree-skin specific logic in the graph editor demo.
 */
public class TreeSkinController implements SkinController {

    protected static final int TREE_NODE_INITIAL_Y = 19;

    private final GraphEditor graphEditor;
    private final GraphEditorContainer graphEditorContainer;

    /**
     * Creates a new {@link TreeSkinController} instance.
     * 
     * @param graphEditor the graph editor on display in this demo
     * @param graphEditorContainer the graph editor container on display in this demo
     */
    public TreeSkinController(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        this.graphEditor = graphEditor;
        this.graphEditorContainer = graphEditorContainer;

        graphEditor.setNodeSkin(TreeSkinConstants.TREE_NODE, TreeNodeSkin.class);
        graphEditor.setConnectorSkin(TreeSkinConstants.TREE_INPUT_CONNECTOR, TreeConnectorSkin.class);
        graphEditor.setConnectorSkin(TreeSkinConstants.TREE_OUTPUT_CONNECTOR, TreeConnectorSkin.class);
        graphEditor.setConnectionSkin(TreeSkinConstants.TREE_CONNECTION, TreeConnectionSkin.class);
        graphEditor.setTailSkin(TreeSkinConstants.TREE_INPUT_CONNECTOR, TreeTailSkin.class);
        graphEditor.setTailSkin(TreeSkinConstants.TREE_OUTPUT_CONNECTOR, TreeTailSkin.class);
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.windowXProperty().get() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.windowYProperty().get() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(TREE_NODE_INITIAL_Y + windowYOffset);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(output);

        final double initialX = graphEditorContainer.getWidth() / (2 * currentZoomFactor) - node.getWidth() / 2;
        node.setX(Math.floor(initialX) + windowXOffset);

        node.setType(TreeSkinConstants.TREE_NODE);
        output.setType(TreeSkinConstants.TREE_OUTPUT_CONNECTOR);
        // This allows multiple connections to be created from the output.
        output.setConnectionDetachedOnDrag(false);

        Commands.addNode(graphEditor.getModel(), node);
    }

    @Override
    public void addConnector(final Side position, final boolean input) {
        // Not implemented for tree nodes.
    }

    @Override
    public void clearConnectors() {
        // Not implemented for tree nodes.
    }

    @Override
    public void handlePaste() {
        graphEditor.getSelectionManager().paste();
    }
}
