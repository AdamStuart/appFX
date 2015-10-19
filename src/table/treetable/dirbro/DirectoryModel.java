package table.treetable.dirbro;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class DirectoryModel {
	TreeItem<File> root;
	public	TreeItem<File> getRoot()		{ return root;  }

	public DirectoryModel(File rootFile)
	{
		root = createNode(rootFile);
	}
	private TreeItem<File> createNode(final File f) {
    final TreeItem<File> node = new TreeItem<File>(f) {
        private boolean isLeaf;
        private boolean isFirstTimeChildren = true;
        private boolean isFirstTimeLeaf = true;
 
        @Override public ObservableList<TreeItem<File>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }
 
        @Override public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                File f = (File) getValue();
                isLeaf = f.isFile();
            }
 
            return isLeaf;
        }
    };
    return node;
}
 boolean showHidden= false;
 
public ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
    File f = (File) TreeItem.getValue();
    if (f != null && f.isDirectory()) {
        File[] files = f.listFiles();
        if (files != null) {
            ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
 
            for (File childFile : files) {
                if (showHidden || !childFile.isHidden())
                	children.add(createNode(childFile));
            }
 
            return children;
        }
    }
 
    return FXCollections.emptyObservableList();
}
}
