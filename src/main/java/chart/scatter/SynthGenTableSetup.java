package chart.scatter;

import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class SynthGenTableSetup
{


	public static void setup(SynthGenController ctrolr, TableView<SynthGenRecord> theTable)
	{
		
		theTable.setEditable(true);
		
		
		ctrolr.idCol.setCellFactory(TextFieldTableCell.<SynthGenRecord, Integer>forTableColumn(new IntegerStringConverter()));
		ctrolr.idCol.setCellValueFactory(new PropertyValueFactory<SynthGenRecord, Integer>("id"));
		ctrolr.idCol.setOnEditCommit(
					    new EventHandler<CellEditEvent<SynthGenRecord, Integer>>() {
					        @Override
					        public void handle(CellEditEvent<SynthGenRecord, Integer> t) {
					            ((SynthGenRecord) t.getTableView().getItems().get(
					                t.getTablePosition().getRow())
					                ).setId(t.getNewValue());
					        }
					    });
		
		ctrolr.countCol.setCellFactory(TextFieldTableCell.<SynthGenRecord, Integer>forTableColumn(new IntegerStringConverter()));
		ctrolr.countCol.setCellValueFactory(new PropertyValueFactory<SynthGenRecord, Integer>("count"));
		ctrolr.countCol.setOnEditCommit(
					    new EventHandler<CellEditEvent<SynthGenRecord, Integer>>() {
					        @Override
					        public void handle(CellEditEvent<SynthGenRecord, Integer> t) {
					            ((SynthGenRecord) t.getTableView().getItems().get(
					                t.getTablePosition().getRow())
					                ).setCount(t.getNewValue());
					        }
					    });

		ctrolr.meanXCol.setCellFactory(TextFieldTableCell.<SynthGenRecord, Double>forTableColumn(new DoubleStringConverter()));
		ctrolr.meanXCol.setCellValueFactory(new PropertyValueFactory<SynthGenRecord, Double>("meanX"));
		ctrolr.meanXCol.setOnEditCommit(
					    new EventHandler<CellEditEvent<SynthGenRecord, Double>>() {
					        @Override
					        public void handle(CellEditEvent<SynthGenRecord, Double> t) {
					            ((SynthGenRecord) t.getTableView().getItems().get(
					                t.getTablePosition().getRow())
					                ).setMeanX(t.getNewValue());
					        }
					    });
		ctrolr.meanYCol.setCellFactory(TextFieldTableCell.<SynthGenRecord, Double>forTableColumn(new DoubleStringConverter()));
		ctrolr.meanYCol.setCellValueFactory(new PropertyValueFactory<SynthGenRecord, Double>("meanY"));
		ctrolr.meanYCol.setOnEditCommit(
					    new EventHandler<CellEditEvent<SynthGenRecord, Double>>() {
					        @Override
					        public void handle(CellEditEvent<SynthGenRecord, Double> t) {
					            ((SynthGenRecord) t.getTableView().getItems().get(
					                t.getTablePosition().getRow())
					                ).setMeanY(t.getNewValue());
					        }
					    });
		ctrolr.cvXCol.setCellFactory(TextFieldTableCell.<SynthGenRecord, Double>forTableColumn(new DoubleStringConverter()));
		ctrolr.cvXCol.setCellValueFactory(new PropertyValueFactory<SynthGenRecord, Double>("cvX"));
		ctrolr.cvXCol.setOnEditCommit(
					    new EventHandler<CellEditEvent<SynthGenRecord, Double>>() {
					        @Override
					        public void handle(CellEditEvent<SynthGenRecord, Double> t) {
					            ((SynthGenRecord) t.getTableView().getItems().get(
					                t.getTablePosition().getRow())
					                ).setCvX(t.getNewValue());
					        }
					    });
		ctrolr.cvYCol.setCellFactory(TextFieldTableCell.<SynthGenRecord, Double>forTableColumn(new DoubleStringConverter()));
		ctrolr.cvYCol.setCellValueFactory(new PropertyValueFactory<SynthGenRecord, Double>("cvY"));
		ctrolr.cvYCol.setOnEditCommit(
					    new EventHandler<CellEditEvent<SynthGenRecord, Double>>() {
					        @Override
					        public void handle(CellEditEvent<SynthGenRecord, Double> t) {
					            ((SynthGenRecord) t.getTableView().getItems().get(
					                t.getTablePosition().getRow())
					                ).setCvY(t.getNewValue());
					        }
					    });
	}
	// ------------------------------------------------------
}


