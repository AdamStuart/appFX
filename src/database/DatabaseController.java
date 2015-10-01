package database;

import java.net.URL;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

import database.model.DBCitation;
import database.model.DBEvent;
import database.model.DBGroup;
import database.model.DBPerson;
import database.model.DBProtocol;
import database.model.DBToDo;
import dialogs.ConnectDialog;
import dialogs.FindDialog;
import dialogs.LoginDialog;
import dialogs.RulesDialog;
import gui.BorderPaneAnimator;
import icon.FontAwesomeIcons;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import task.DatabaseAccess;
import task.IDBTable;
import util.DBUtil;
import util.DialogUtil;

public class DatabaseController implements Initializable
{
	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
	@FXML private Button saveFormButton;
//	@FXML private Button revertFormButton;
	@FXML private Button clearFormButton;
	@FXML private Button deleteButton;
	@FXML private Label status1;
	@FXML private Label status2;

	@FXML private Button signUpButton;
	@FXML private Button signInButton;
//	@FXML private Button connectButton;
//	@FXML private Button browseButton;
	@FXML private Button filterButton;
//	@FXML private Button searchButton;
	@FXML private TableView<ObservableList<String>> tableView;
	@FXML private BorderPane container;
	@FXML private ChoiceBox<String> tableChoice;
	@FXML private StackPane formContainer;
	@FXML private TextArea console;
	@FXML private void doNew() 	{		AppDatabase.getInstance().doNew(null);	}
	@FXML private void open() 	{		System.err.println("open");	}
	@FXML private void close() 	{		System.err.println("close");	}
	@FXML private void print() 	{		System.err.println("print");	}
	@FXML private void save() 	{		System.err.println("save");	}
	@FXML private void saveas() {		System.err.println("saveas");	}
	@FXML private void quit() 	{		System.err.println("quit");	}

	@FXML private void undo() 	{		System.err.println("undo");	}
	@FXML private void redo() 	{		System.err.println("redo");	}
	@FXML private void cut() 	{		System.err.println("cut");	}
	@FXML private void copy() 	{		System.err.println("copy");	}
	@FXML private void paste() 	{		System.err.println("paste");	}
	@FXML private void deleteSelection() 	{		System.err.println("deleteSelection");	}
	@FXML private void selectAll() 	{		System.err.println("selectAll");	}

	@FXML private void find() 	
	{		
		FindDialog dlog = new FindDialog(false);
		dlog.showAndWait();
		System.err.println("find");	
	}
	@FXML private void replace() 	
	{		System.err.println("replace");	
		FindDialog dlog = new FindDialog(true);
		dlog.showAndWait();
	}
	@FXML private void rules() 		
	{		
		System.err.println("rules");	
		if (getDatabase() != null)
		{
			RulesDialog dlog = new RulesDialog(getDatabase().getFieldList());
			dlog.showAndWait();
			String output = dlog.getResult().toString();
			System.err.println(output);
		}
	}
	@FXML private void filter() 	{		System.err.println("Filtering, ev = unknown");	}
	
	
	@FXML private void saveForm() 	{		db.update();   db.doSelect();  System.err.println("db.update");	}
	@FXML private void revertForm() {		System.err.println("revertForm");	}
	@FXML private void clearForm() 	{		DBUtil.clearForm(db.getTable().getForm());	}
	@FXML private void deleteRecord() 	{	db.deleteActiveRecord(); 	}

	public void toConsole(String s)
	{
		console.insertText(0, s + "\n");
	}
	
	// **-------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
//		assert attributeCol != null : missing("attributeCol");
//		assert valueCol != null : missing("valueCol");
//		assert attributeTable != null : missing("attributeTable");
		tableChoice.getItems().addAll("ToDo", "Person", "Event", "Citation", "Protocol", "Group");
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 80);
		new BorderPaneAnimator(container, rightSideBarButton, Side.RIGHT, false, 180);
		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 100);
		tableChoice.getSelectionModel().select(0);
		DialogUtil.useGlyph(leftSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		DialogUtil.useGlyph(rightSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_LEFT);
		DialogUtil.useGlyph(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);

		tableChoice.getSelectionModel().selectedIndexProperty().addListener((a,b,c) -> {	connect(c.intValue());	});
		tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
		{	
			@Override public void onChanged(Change<? extends Integer> change) 		{  db.fillForm();	}		
		});


	}
	@FXML private void search(ActionEvent ev) 
	{
		new FindDialog(true).showAndWait();
	}
	@FXML private void signUp() 
	{
		System.err.println("signUp");
		Dialog<?> dialog = new Dialog<>();
		dialog.initOwner(container.getScene().getWindow());
		dialog.setHeight(150);
		dialog.setWidth(300);
		dialog.setTitle("Sign Up ");
		dialog.getDialogPane().setContent(new VBox(new Label("TODO SignupForm goes here")));			//TODO new SignUpForm()
		ButtonType done = ButtonType.FINISH;
		dialog.getDialogPane().getButtonTypes().add(done);
		dialog.show();
	}
	DatabaseAccess getDatabase() { return db; 	}
	DatabaseAccess db;
	@FXML private void signIn() 
	{
		Optional<Pair<String, String>> result = (new LoginDialog()).showAndWait();
		result.ifPresent(keyValue -> {  
		if (getDatabase() != null) getDatabase().authenticate(keyValue); 
		toConsole("Username=" + keyValue.getKey() + ", Password=" + keyValue.getValue());
	});

	}
	private Integer credentials = null;
	
	@FXML private void connect(int index) 
	{
//		checkCredentials();
		String tableName = tableChoice.getItems().get(index);
		if (credentials == null)
		{
			ConnectDialog dlog = new ConnectDialog();
			dlog.setDefaults("org.h2.Driver", "jdbc:h2:~/test", "Adam");
			Optional<ResultSet> result = dlog.showAndWait();
			credentials = new Integer(32);
		}
		if (db != null) db.disconnect();
		IDBTable table = null;
			
		
		table = lookup(tableName);
		if (table != null)
		{
			
			db = new DatabaseAccess(table, tableView);
			formContainer.getChildren().removeAll(formContainer.getChildren());
			formContainer.getChildren().add(getForm());
			setStatus(db.getConnection() != null ? "Valid" : "NOT Valid");
			db.doSelect();
		}
	}
	
	private Node getForm()
	{
		return 	db.getTable().getForm();	
	}

	private static IDBTable lookup(String tableName)
	{
		if ("ToDo".equals(tableName)) 		return new DBToDo();
		if ("Citation".equals(tableName)) 	return new DBCitation();
		if ("Person".equals(tableName)) 	return new DBPerson();
		if ("Event".equals(tableName)) 		return new DBEvent();
		if ("Group".equals(tableName)) 		return new DBGroup();
		if ("Protocol".equals(tableName)) 	return new DBProtocol();
		return null;
	}

	private void setStatus(String s)
	{
		status1.setText(s);
	}
	private void setStatus(String s, String t)
	{
		status1.setText(s);
		status2.setText(t);
	}
//	@FXML private void browse() 
//	{
//		if (db != null)
//		{
//			db.doSelect(table);
//			System.err.println("browse");
//		}
//	}
//	
	

 
}
