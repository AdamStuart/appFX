package table.treetable.dirbro;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

public class DirectoryBrowserFactory {

	static NumberFormat nf = new DecimalFormat();
	static DateTimeFormatter df = DateTimeFormatter.ISO_DATE_TIME;
	
	
public TreeTableView buildFileBrowserTreeTableView(DirectoryModel inModel) {
    TreeItem<File> root = inModel.getRoot();  
    root.setExpanded(true);
 
    final TreeTableView<File> treeTableView = new TreeTableView<File>();
    treeTableView.setShowRoot(true);
    treeTableView.setRoot(root);
 
    // --- name column
    TreeTableColumn<File, String> nameColumn = new TreeTableColumn<File, String>("Name");
    nameColumn.setPrefWidth(300);
    nameColumn.setCellValueFactory(new Callback<CellDataFeatures<File, String>, ObservableValue<String>>() {
        @Override public ObservableValue<String> call(CellDataFeatures<File, String> p) {
            File f = p.getValue().getValue();
            String text = f.getParentFile() == null ? "/" : f.getName();
            return new ReadOnlyObjectWrapper<String>(text);
        }
    });
 
    // --- size column
    TreeTableColumn<File, File> sizeColumn = new TreeTableColumn<File, File>("Size");
    sizeColumn.setPrefWidth(100);
    sizeColumn.setCellValueFactory(new Callback<CellDataFeatures<File, File>, ObservableValue<File>>() {
        @Override public ObservableValue<File> call(CellDataFeatures<File, File> p) {
            return new ReadOnlyObjectWrapper<File>(p.getValue().getValue());
        }
    });
    sizeColumn.setCellFactory(new Callback<TreeTableColumn<File, File>, TreeTableCell<File, File>>() {
        @Override public TreeTableCell<File, File> call(final TreeTableColumn<File, File> p) {
            return new TreeTableCell<File, File>() {
                @Override protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
 
                    TreeTableView treeTable = p.getTreeTableView();
 
                    // if the File is a directory, it has no size...
                    if (getIndex() >= treeTable.getExpandedItemCount()) {
                        setText(null);
                    } else {
                        TreeItem<File> treeItem = treeTable.getTreeItem(getIndex());
                        if (item == null || empty || treeItem == null ||
                                treeItem.getValue() == null || treeItem.getValue().isDirectory()) {
                            setText(null);
                        } else {
                            setText(nf.format(item.length()) + " Bytes");
                        }
                    }
                }
            };
        }
    });
    sizeColumn.setComparator(new Comparator<File>() {
        @Override public int compare(File f1, File f2) {
            long s1 = f1.isDirectory() ? 0 : f1.length();
            long s2 = f2.isDirectory() ? 0 : f2.length();
            long result = s1 - s2;
            if (result < 0)             return -1;
            if (result == 0)            return 0;
            return 1;
        }
    });
 
    // --- modified column
    TreeTableColumn<File, Date> lastModifiedColumn = new TreeTableColumn<File, Date>("Last Modified");
    lastModifiedColumn.setPrefWidth(130);
    lastModifiedColumn.setCellValueFactory(new Callback<CellDataFeatures<File, Date>, ObservableValue<Date>>() {
        @Override public ObservableValue<Date> call(CellDataFeatures<File, Date> p) {
            return new ReadOnlyObjectWrapper<Date>(new Date(p.getValue().getValue().lastModified()));
        }
    });
    lastModifiedColumn.setCellFactory(new Callback<TreeTableColumn<File, Date>, TreeTableCell<File, Date>>() {
        @Override public TreeTableCell<File, Date> call(TreeTableColumn<File, Date> p) {
            return new TreeTableCell<File, Date>() {
                @Override protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
 
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
        }
    });
 
    treeTableView.getColumns().setAll(nameColumn, sizeColumn, lastModifiedColumn);
 
    return treeTableView;
}
 
}
