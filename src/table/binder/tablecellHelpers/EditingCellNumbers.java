//package table.binder.tablecellHelpers;
//
//import javafx.scene.control.TableCell;
//import javafx.scene.control.TableView;
//import javafx.scene.control.TextField;
//import table.binder.Rect;
//
//public class EditingCellNumbers extends TableCell<Rect, Double>
//{
//
//    private TextField textField;
//    private TableView<Rect> parentTableView;
//    public static int numberOfColumns;
//
//    public EditingCellNumbers(TableView<Rect> parent) {
//        parentTableView = parent;
//        numberOfColumns = parent.getColumns().size();
//    }
//
//    @Override
//    public void startEdit(){
//        if (!isEmpty()) {
//            super.startEdit();
//            createTextField();
//            setText(null);
//            setGraphic(textField);
//            textField.selectAll();
//            textField.requestFocus();
//        }
//    }
//
//    @Override
//    public void cancelEdit() {
//        super.cancelEdit();
//
//        setText(String.valueOf(getItem()));
//        setGraphic(null);
//    }
//
//    @Override
//    public void updateItem(Double item, boolean empty) {
//        super.updateItem(item, empty);
//
//        if (empty) {
//            setText(null);
//            setGraphic(null);
//        } else {
//            if (isEditing()) {
//                if (textField != null) 
//                    textField.setText(getString());
//                
//                setText(null);
//                setGraphic(textField);
//            } else {      setText(getString());       setGraphic(null);      }
//        }
//    }
//
//    private void createTextField() {
//        textField = new TextField(getString());
//        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
//        textField.focusedProperty().addListener((a, b,c ) -> {
//                if (!c)    commitEdit(Double.valueOf(textField.getText()));    });
//        textField.setOnKeyReleased(event -> {
//            try{
//                int i = Integer.valueOf(textField.getText());
//                //digit given...
//                if( (i>=0) && (i<10) ){//making sure cell is filled with just one digit
//                   commitEdit(Double.valueOf(textField.getText()));
//                   int selectedColumn = parentTableView.getSelectionModel().getSelectedCells().get(0).getColumn(); // gets the number of selected column
//                   int selectedRow = parentTableView.getSelectionModel().getSelectedCells().get(0).getRow();
//                   if(selectedColumn < numberOfColumns-1){
//                       parentTableView.getSelectionModel().selectNext();
//                       parentTableView.edit(selectedRow, parentTableView.getColumns().get(selectedColumn+1));
//                   }else{
//                       parentTableView.getSelectionModel().select(selectedRow+1, parentTableView.getColumns().get(0));
//                       parentTableView.edit(selectedRow+1, parentTableView.getColumns().get(0));
//
//                   }
//
//                }else textField.clear();
//            }
//            catch(NumberFormatException e){        textField.clear();         }
//        });
//    }
//
//    private String getString() {
//        return getItem() == null ? "" : getItem().toString();
//    }
//}