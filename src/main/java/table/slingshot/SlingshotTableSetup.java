package table.slingshot;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class SlingshotTableSetup
{

	public static void setup(SlingshotController ctrolr)
	{
		DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		DecimalFormat myFormatter = new DecimalFormat("0");
		DecimalFormat my2PlaceFormatter = new DecimalFormat("0.00");

		DecimalFormat intFormatter = new DecimalFormat("00000");
		ctrolr.idCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Integer>("id"));
		ctrolr.idCol.setCellFactory(column ->
		{
			return new TableCell<SlingshotDataRow, Integer>()
			{
				@Override
				protected void updateItem(Integer item, boolean empty)
				{
					super.updateItem(item, empty);

					if (item == null || empty)
					{
						setText(null);
						setStyle("");
					} else
						setText(intFormatter.format(item));
				}
			};
		});
		ctrolr.nameCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, String>("name"));
		ctrolr.nameCol.setCellFactory(column ->
		{
			return new TableCell<SlingshotDataRow, String>()
			{
				@Override
				protected void updateItem(String item, boolean empty)
				{
					super.updateItem(item, empty);

					if (item == null || empty)
					{
						setText(null);
						setStyle("");
					} else
						setText(item);
				}
			};
		});
		ctrolr.dateCol.setCellValueFactory(row -> new ReadOnlyObjectWrapper<LocalDate>(row.getValue().date));
		ctrolr.dateCol.setCellFactory(column ->
		{
			return new TableCell<SlingshotDataRow, LocalDate>()
			{
				@Override
				protected void updateItem(LocalDate item, boolean empty)
				{
					super.updateItem(item, empty);

					if (item == null || empty)
					{
						setText(null);
						setStyle("");
					} else
						setText(myDateFormatter.format(item));
				}
			};
		});

		for (TableColumn<SlingshotDataRow, Double> col : ctrolr.varCols)
		{
			col.setCellFactory(column -> makeDoubleTableCell(my2PlaceFormatter));
			String id = col.getId();
			System.out.println("id = " + id);
			// col.setCellValueFactory(new
			// PropertyValueFactory<SlingshotDataRow, Double>(id)); // doesn't
			// work???
		}

		for (TableColumn<SlingshotDataRow, Double> col : ctrolr.intNumCols)
		{
			col.setCellFactory(column -> makeDoubleTableCell(myFormatter));
			String id = col.getId();
			col.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>(id)); // doesn't
																								// work???
		}

		// ----------------- these lines shouldn't be necessary because the
		// cellvaluefactory should be set in the loop above
		ctrolr.varCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("var"));
		ctrolr.varXCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("varX"));
		ctrolr.varYCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("varY"));
		ctrolr.metricXCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("metricX"));
		ctrolr.metricYCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("metricY"));
		ctrolr.medXCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("medX"));
		ctrolr.medYCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("medY"));
		ctrolr.cvXCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("cvX"));
		ctrolr.cvYCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("cvY"));
		ctrolr.targXCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("targX"));
		ctrolr.targYCol.setCellValueFactory(new PropertyValueFactory<SlingshotDataRow, Double>("targY"));
	}
	// ------------------------------------------------------

	static private TableCell<SlingshotDataRow, Double> makeDoubleTableCell(DecimalFormat fmt)
	{
		return new TableCell<SlingshotDataRow, Double>()
		{
			@Override
			protected void updateItem(Double item, boolean empty)
			{
				super.updateItem(item, empty);

				if (item == null || empty)
				{
					setText(null);
					setStyle("");
				} else
				{
					// setStyle("-fx-alignment: CENTER-RIGHT;"); // this works
					// too
					setAlignment(Pos.CENTER_RIGHT);
					setText(fmt.format(item));
				}
			}
		};
	}

}
