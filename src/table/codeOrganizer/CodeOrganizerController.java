package table.codeOrganizer;

import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CodeOrganizerController
{
	@FXML	TreeView<String> appTree;
	@FXML	TreeView<String> libTree;
	@FXML	TreeView<String> viewTree;
	@FXML	TreeView<String> reportTree;
	@FXML	TabPane tabPane;
	@FXML	MetaDataPane metaData;

	HashMap<String, CodeModule> modules = new HashMap<String, CodeModule>();
	ObservableList<Integer> selectedIndexes = FXCollections.observableArrayList();

	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";

	static String missing(String s)	{		return String.format(ctrlStr, s, "CodeOrganizer.fxml");	}

	@FXML	void initialize()
	{
		assert appTree != null : missing("appTree");
		assert viewTree != null : missing("viewTree");
		assert libTree != null : missing("libTree");
		assert reportTree != null : missing("reportTree");
//		assert tabPane != null : missing("tabPane");
//		assert metaData != null : missing("metaData");
		
		TreeItem<String> appRoot = TreeTableModel.getCodeModuleApplication();
		appTree.setRoot(appRoot);
		TreeItem<String> viewRoot = TreeTableModel.getCodeModuleView();
		viewTree.setRoot(viewRoot);
		TreeItem<String> libRoot = TreeTableModel.getCodeModuleLibrary();
		libTree.setRoot(libRoot);
		TreeItem<String> reportRoot = TreeTableModel.getCodeModuleReport();
		reportTree.setRoot(reportRoot);
		
		// change listview observable list
		
		TreeView<String>[] trees = new TreeView[] { appTree, viewTree, libTree, reportTree };
		for (TreeView<String> tree : trees)
		{
			tree.getSelectionModel().getSelectedIndices()
				.addListener(new ListChangeListener<Integer>()
				{	@Override  public void onChanged(Change<? extends Integer> change)
					{ setActiveItem(tree.getSelectionModel().getSelectedItem());	}	});
			tree.setShowRoot(false);
		}
	}
	private void setActiveItem(TreeItem<String> item)
	{
		CodeModule module = getCodeModule(item.getValue());
		if (module != null)
			install(module);
	}

	private void install(CodeModule module)
	{
		if (module == null) return;
		String lastTab = module.getLastTab();
		int idx = findTab(tabPane, lastTab);
		if (idx >= 0)
			tabPane.getSelectionModel().select(idx);
		setCurrentRecord(module);
	}

	private void setCurrentRecord(CodeModule module)
	{
		
	}

	private int findTab(TabPane pane, String target)
	{
		ObservableList<Node> panes = tabPane.getChildrenUnmodifiable();
		for (int i=0; i<panes.size(); i++)
			if (panes.get(i).getId().equals(target))
				return i;
		
		return -1;
	}

	CodeModule getCodeModule(String item)
	{
		return modules.get(item);
	}
}
	