package database.forms;

import java.text.DecimalFormat;

import gui.Forms;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
import util.NodeUtil;

public class InvoiceForm extends VBox
{
	// ----------------------------------------------------
    public static final String INVOICE_FORM = "Invoice";
	
		public static InvoiceForm createInvoiceForm()
		{
			return new InvoiceForm();
		}
		
		public InvoiceForm()
		{
			setPadding(new Insets(8));
			setPrefHeight(500);
			setPrefWidth(500);
			setSpacing(30);
			getChildren().addAll(createInvoiceHeader(), createInvoiceBody(), createInvoiceFooter());
		}
		
		// ----------------------------------------------------
		private VBox createInvoiceHeader()
		{
			VBox container = new VBox(6);
			Label invPrompt = new Label("Invoice Number");		
			Label invNumber = new Label();			invNumber.setMinWidth(100);
			HBox dateBox = Forms.makeDateBox("Date",  false);
			HBox by = Forms.makeLabelFieldHBox( "by", "Sold By", "soldby", BY);
			HBox line1 = new HBox(10, invPrompt, invNumber, dateBox, by);
			HBox nameLine = Forms.makeNameHBox();
			VBox addrLine = Forms.makeAddressVBox(500, false);
			Pane spacer = new Pane();  spacer.setPrefSize(30, 30);
			VBox terms = makeTermsBox(INVOICE_FORM);
			container.getChildren().addAll(line1, nameLine, addrLine, spacer, terms);
			return container;

		}

		enum InvoiceTerms {
			FREE, COD, NET30, NET60, NET90
		};

		enum ContactType {
			Phone, WorkPhone, Cell, Email, Twitter, Facebook, Instagram, Other
		};

		enum AddressType {
			BillAddress, ShipAddress
		};

		static int TERMS = 80;
		static int BY = 60;
		
		static int DATE = 90;
		static int SHIP = DATE;
		static int DUE = DATE;
		static int NOTES = 450;
		
		private VBox makeTermsBox(String prefix)
		{
			VBox container = new VBox(5);

			HBox trms = makeLabelChoiceBox(INVOICE_FORM, "Terms", InvoiceTerms.values(), TERMS);
			HBox ship = Forms.makeLabelFieldHBox("ship", "Shipped", "shipDate", SHIP);
			HBox due = Forms.makeLabelFieldHBox("due", "Due", "dueDate", DUE);
			HBox paid = Forms.makeLabelFieldHBox("paid", "Paid", "paidDate", DUE);
			HBox notes = Forms.makeLabelFieldHBox("notes", "Notes", "notes", NOTES);
			Button addRec = new Button("+");
			addRec.setOnAction(o -> addRecord());
			container.getChildren().addAll(new HBox(6, trms, ship, due, paid), new HBox(6, notes, addRec), 
							new HBox(new Label("(Note: 001 - 005 are the only legal SKUs.)")));
			return container;
		}

		private void addRecord()
		{
			LineItem newItem = new LineItem(this);
			tableView.getItems().add(newItem);
			
		}
		private static HBox makeLabelChoiceBox(String prefix, String string, InvoiceTerms[] invoiceTerms, int width)
		{
			HBox container = new HBox(4, Forms.makePrompt(string));
			ObservableList<InvoiceTerms> terms = FXCollections.observableArrayList();
			terms.addAll(invoiceTerms);
			ChoiceBox<InvoiceTerms> chooser = new ChoiceBox<InvoiceTerms>(terms);
			container.getChildren().add(chooser);
			if (width > 0)
				chooser.setPrefWidth(width);
			chooser.getSelectionModel().select(InvoiceTerms.NET30);
			return container;
			
		}

		//-----------------------------------------------------------------------------
		
		static int STATUS = 50;
		static int QTY = 40;
		static int SKU = 100;
		static int DESC = 200;
		static int AMT = 80;
		static int UNIT = 90;
		static int EXT = 90;
		private static LineItemField fld;

		public enum LineItemField
		{
			status(	"Status", 	"status", Integer.class, 	STATUS),
			quantity(	"Qty.", 	"qty", Integer.class, 	QTY),
			sku(		"SKU", 		"sku", String.class, 	SKU),
			desc(		"Description", "desc", String.class, DESC),
			amount(		"Amount", 	"amt", String.class, 	AMT),
			unitPrice(	"Price", 	"unitPrice", Number.class, 	UNIT),
			extPrice(	"Extended",	"extPrice",  Number.class, 	EXT),
			;
			
			String name;
			public String getName()	{ return name;	}
			
			String field;
			public String getField()	{ return field;	}
			
			int prefWidth;
			public int getWidth()		{ return prefWidth;	}
			
			Class classType;
			Class getClassType(){ return classType;	}
			
			static public String findName(String fld)
			{
				for (LineItemField f : values())
					if (fld.equals(f.getField())) return f.name;
				return "";
			}
			static public int findWidth(String fld)
			{
				for (LineItemField f : values())
					if (fld.equals(f.getField())) return f.prefWidth;
				return 100;
			}
			
			
			LineItemField(String inHeader, String fldName, Class clazz, int inPrefWidth)
			{
				name = inHeader;
				field = fldName;
				classType = clazz;
				prefWidth = inPrefWidth;
//				if (clazz == Integer.class)			installIntFactory();
//				else if (clazz == Boolean.class)	installBoolFactory();
//				else if (clazz == String.class)		installStringFactoroy();
//				else if (clazz == Double.class)		installDecimalFactoroy();
//				else if (clazz == Number.class)		installCurrencyFactoroy();
//				return;
			}
		};
		static TableColumn<LineItem, ?>[] cols;
		static String[] colNames;
		static LineItemField[] flds;
		TableView<LineItem> tableView;
		
		public TableView<LineItem> createLineItemTable()
		{
			tableView = new TableView<LineItem>();
			
			TableColumn<LineItem, Integer> status = new TableColumn<LineItem, Integer>();
			TableColumn<LineItem, Integer> qtyCol = new TableColumn<LineItem, Integer>();
			TableColumn<LineItem, String> skuCol = new TableColumn<LineItem, String>();
			TableColumn<LineItem, String> descCol = new TableColumn<LineItem, String>();
			TableColumn<LineItem, String> amtCol = new TableColumn<LineItem, String>();
			TableColumn<LineItem, Double> unitPriceCol = new TableColumn<LineItem, Double>();
			TableColumn<LineItem, Double> extPriceCol = new TableColumn<LineItem, Double>();
			
//			flds = LineItemField.values();
			cols = new TableColumn[] { status, qtyCol, skuCol, descCol, amtCol, unitPriceCol, extPriceCol };
			colNames = new String[] { "status", "qty", "sku", "desc", "amt", "unitPrice", "extPrice" };
			Class[] types = new Class[] { Integer.class, Integer.class, String.class, String.class, String.class, Double.class, Double.class };
			
			tableView.getColumns().addAll(cols);
			for (int i=0; i<cols.length; i++)
			{
				String name = LineItemField.findName(colNames[i]);
				cols[i].setText(name);
				cols[i].setCellValueFactory(new PropertyValueFactory<>(colNames[i]));
				if (types[i] != String.class)
					cols[i].setStyle(" -fx-alignment: CENTER-RIGHT;");
				cols[i].setPrefWidth(LineItemField.findWidth(colNames[i]));
			}
			cols[0].setVisible(false);		// dont show status (for now)
			qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			qtyCol.setOnEditCommit( t -> {
			        	LineItem row = ((LineItem) t.getTableView().getItems().get( t.getTablePosition().getRow()));
			        	row.setQty(t.getNewValue());
			        	row.recalc();
			    });

			skuCol.setCellFactory(TextFieldTableCell.forTableColumn());
			skuCol.setOnEditCommit( t -> {
			        	LineItem row = ((LineItem) t.getTableView().getItems().get( t.getTablePosition().getRow()));
			        	row.setSKU(t.getNewValue());
			        	skuChanged(row);
			    });

			tableView.setEditable(true);
				
		          
			tableView.getSelectionModel().selectedItemProperty().addListener((ob, old, newVal) -> {
		                tableSelectionChanged((LineItem) old, (LineItem) newVal);
		        });

		    return tableView;
		}
		
		
	    static private void tableSelectionChanged(LineItem oldValue, LineItem newValue) {
	        
			for (int i=0; i<cols.length; i++)
			{
			}

	    }
	    
	    static private void skuChanged(LineItem item) 
	    {
			Product p = Product.findSKU(item.getSKU());
			item.fill(p);
	    }   
	    

		//-----------------------------------------------------------------------------
		private  VBox createInvoiceBody()
		{
			Product.makeDummyCatalog();
			return new VBox(createLineItemTable());
		}

		
		private VBox createInvoiceFooter()
		{
			HBox subline = Forms.makeLabelNumberFieldHBox("Subtotal", "subtotal", 400, EXT);
			HBox taxline = Forms.makeLabelNumberFieldHBox("Tax", "tax", 400, EXT);
			HBox shipline = Forms.makeLabelNumberFieldHBox("Shipping & Handling", "ship", 400, EXT);
			HBox totalline = Forms.makeLabelNumberFieldHBox("Total", "total", 400, EXT);
			VBox column = new VBox(4, subline, taxline, shipline, totalline);

			subline.setAlignment(Pos.CENTER_RIGHT);
			taxline.setAlignment(Pos.CENTER_RIGHT);
			shipline.setAlignment(Pos.CENTER_RIGHT);
			totalline.setAlignment(Pos.CENTER_RIGHT);
			column.setAlignment(Pos.CENTER_RIGHT);
			return column;
		}
		
		double TAX_RATE = 0.085;
		double FIXED_SHIPPING = 80;
		static DecimalFormat fmt = new DecimalFormat("0.00");
		public void retotal()
		{
        	double subtotal = 0;
			for (LineItem row : tableView.getItems())
        		subtotal += row.getExtPrice();

			NodeUtil.showKids(this, "  ");
			double tax = subtotal * TAX_RATE;
			double shipping = FIXED_SHIPPING;
			double total = subtotal + tax + shipping;
			TextField subfld = (TextField) lookup("#"+ "subtotal" + "Field");
	        if (subfld != null)
	        	subfld.setText(fmt.format(subtotal));
	        
	        TextField taxfld = (TextField) lookup("#" + "tax" + "Field");
	        if (taxfld != null)
	        	taxfld.setText(fmt.format(tax));
	        
	        TextField shipfld = (TextField) lookup("#" + "ship" + "Field");
	        if (shipfld != null)
	        	shipfld.setText(fmt.format(shipping));
        
	        TextField totfld = (TextField) lookup("#" + "total" + "Field");
	        if (totfld != null)
	        	totfld.setText(fmt.format(total));

		}

}
