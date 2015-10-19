package database.forms;

import gui.Borders;

import java.net.URI;
import java.util.ArrayList;
import java.util.function.Supplier;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.IntegerValidator;

import validation.Decorator;
import validation.SimpleValidator;
import validation.ValidationEvent;
import validation.ValidationUtils;
import validation.ValidationUtils.ValidationMode;
 	
public class FormsUtil
{
	public static VBox makeFormContainer()
	{
		VBox pane = new VBox();
		pane.setSpacing(12);
		pane.setPadding(new Insets(8));
		pane.setPrefHeight(500);
		pane.setPrefWidth(500);
		return pane;
	}

	public static HBox makeLabelFieldHBox(String prompt, String id)
	{
		Label label = makePrompt(prompt, id);  		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();
		field.setId(id);
		return new HBox(4, label, field);
	}

	public static VBox makeMultipleInstanceBox(String prompt, String id)
	{
		VBox lines = new VBox(4);
		addInstance(lines, prompt, id);
		return lines; 
	}
	
	private static int MAX_OCCURS= 4;
	public static void addInstance(VBox lines, String prompt, String id)
	{
		HBox line = makeLabelFieldHBox(prompt, id);
		Button plusButton = new Button("+");
		plusButton.setOnAction( event -> { 
		    	addInstance(lines, prompt, id+"+"); 
		    	plusButton.setVisible(false);
		});				
		if (lines.getChildren().size() < MAX_OCCURS-1)
			line.getChildren().add(plusButton);
		lines.getChildren().add(line);		
	}
	
	
	public static HBox makeLabelFieldHBox(String prompt, String id, int labelWidth, int fldWidth)
	{
		Label label = makePrompt(prompt, id);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();
		field.setId( id + "Field");
		if (fldWidth > 0) 	field.setPrefWidth(fldWidth);
		return new HBox(label, field);
	}

	public static HBox makeLabelNumberFieldHBox(String prompt, String id, int labelWidth, int fldWidth)
	{
		Label label = makePrompt(prompt, id);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();
		field.setId( id + "Field");
		field.setStyle(" -fx-alignment: CENTER-RIGHT;");
		if (fldWidth > 0) 	field.setPrefWidth(fldWidth);
		return new HBox(label, field);
	}

	public static HBox makeLabelFieldHBox(String prompt, String id, int fldWidth)
	{
		Label label = makePrompt(prompt, id);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();
		field.setId( id + "Field");
		if (fldWidth > 0) 	field.setPrefWidth(fldWidth);
		return new HBox(label, field);
	}
	public static HBox promptedText(String prompt, String id, int fldWidth)
	{
		return makeRightLabelFieldHBox( prompt, id, fldWidth, "");
	}
	public static HBox promptedText(String prompt, String id, int fldWidth, String tooltip)
	{
		return makeRightLabelFieldHBox( prompt, id, fldWidth, tooltip);
	}

	public static HBox roLabel(String prompt, String id, int fldWidth)
	{
		return  new HBox(makePrompt(prompt, id, fldWidth));
	}

	public static HBox makeRightLabelFieldHBox(String prompt, String id, int fldWidth, String tooltip)
	{
		
		Label label = makePrompt(prompt, id, 200);
//		label.setTextAlignment(TextAlignment.RIGHT);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();
		Tooltip.install(field, new Tooltip(tooltip));
		Tooltip.install(label, new Tooltip(tooltip));
		field.setId(id );
		if (fldWidth > 0) 	field.setPrefWidth(fldWidth);
		return new HBox(label, field);
	}

	public static HBox makeInternalLabelField(String prompt, String id, int fldWidth, String tooltip)
	{
		
		Label label = makePrompt(prompt, id);
//		label.setTextAlignment(TextAlignment.RIGHT);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();
		Tooltip.install(field, new Tooltip(tooltip));
		Tooltip.install(label, new Tooltip(tooltip));
		field.setId(id);
		if (fldWidth > 0) 	field.setPrefWidth(fldWidth);
		return new HBox(label, field);
	}

	
	public static HBox makeURLBox()	{		return makeURLBox("URL", 0, "The address for a service on the Internet");	}

	public static HBox makeURLBox(String prompt, int fldWidth, String toolTip)
	{
		Label label = makePrompt(prompt, "", fldWidth);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		TextField field = new TextField();		field.setId("url"); field.setPrefWidth(300);
	    Button urlButton = new Button("Open", new ImageView(new Image("validation/web.png")));
	    urlButton.setId("urlButton");
	    urlButton.setOnAction(new EventHandler<ActionEvent>() {
	            @Override  public void handle(ActionEvent event) {
	                try 
	                {
	                    java.awt.Desktop.getDesktop().browse(new URI(field.getText()));
	                }
	                catch (Exception e) {}  // ignore
	            }
	        });
	        urlButton.setDisable(true);
		
	        field.addEventHandler(ValidationEvent.ANY, new EventHandler<ValidationEvent>() {
	            @Override
	            public void handle(ValidationEvent event) {
	            	urlButton.setDisable(event.getEventType() != ValidationEvent.VALIDATION_OK);
	            }
	        });

//	        ValidationUtils.install(field, new SimpleValidator(UrlValidator.getInstance()), ValidationMode.ON_FLY);
		return new HBox(label, field, urlButton);
	}

	public static HBox makeEmailBox()
	{
		Label label = makePrompt("Email", "");
		TextField field = new TextField();
		field.setId("email");
		ValidationUtils.install(field, new SimpleValidator(EmailValidator.getInstance()), ValidationMode.ON_FLY);
//        ValidationUtils.install(field, new Validator() {
//            @Override
//            public ValidationEvent call(ValidationObject param) {
//                if (field.getText().trim().length() == 0) {
//                    return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "The email cannot be empty!");
//                } else return new SimpleValidator(EmailValidator.getInstance()).call(param);
//            }
//        }, ValidationMode.ON_DEMAND);
        Supplier<Decorator> requiredFactory = new Supplier<Decorator>() {
            @Override public Decorator get() {
            	Image i = new Image("validation/overlay_required.png");
                return new Decorator<>(new ImageView(i));
            }
        };
        HBox box = new HBox(label, field);
        ValidationUtils.forceValidate(field, ValidationMode.ON_FLY);
		return box;
	}

	public static HBox makeDateBox( boolean editable, String toolTip)
	{
		return makeDateBox("Date", 0, editable, toolTip);
	}
	static int DATE_WIDTH = 140;
	public static HBox makeDateBox( boolean editable)	{		return makeDateBox( "Date", 0, editable, "");	}

	public static HBox makeDateBox( String prompt,int width,  boolean editable, String toolTip)
	{
		Label label = makePrompt(prompt, width);	
		label.setAlignment(Pos.BOTTOM_RIGHT);
		DatePicker field = new DatePicker();
		field.setId("date");
		field.setPrefWidth(DATE_WIDTH);
		field.setDisable(!editable);
		Tooltip.install(field, new Tooltip(toolTip));
		Tooltip.install(label, new Tooltip(toolTip));
		ValidationUtils.install(field, new SimpleValidator(DateValidator.getInstance()), ValidationMode.ON_FLY);
		return new HBox(label, field);
	}


	public static HBox releaseDateBox(String string, String string2, String tooltip) 
	{		return makeDateBox( "Release Date", 200, true, tooltip);	}
	
	public static HBox makeTimeBox(int width, boolean editable, String tooltip)
	{
		Label label = makePrompt("Start", width);
		label.setAlignment(Pos.BOTTOM_RIGHT);
//        final CalendarTimePicker field = new CalendarTimePicker();		PART of JXExtras, but I copied it here to omit that library
        
        Slider hours = new Slider(0, 23, 9);
        hours.setShowTickMarks(true);
        hours.setMajorTickUnit(12);
        hours.setMinorTickCount(6);
        Slider minutes = new Slider(0,59,0);
        minutes.setShowTickMarks(true);
        minutes.setMajorTickUnit(15);
        minutes.setMinorTickCount(4);
       VBox box = new VBox(12, hours, minutes);
        StackPane container = new StackPane();
        Label timelabel = new Label("09:00");
        container.getChildren().addAll(box, timelabel);
        StackPane.setAlignment(timelabel, Pos.CENTER);
        timelabel.setTranslateY(-6);
        hours.setTranslateY(12);
        minutes.setTranslateY(6);
        
        container.setBorder(Borders.lineBorder);

        hours.valueProperty().addListener(new ChangeListener<Object>() {
    	    @Override  public void changed(ObservableValue<? extends Object> obs, Object wasChanging, Object isNowChanging) {
    	    	timelabel.setText(formatTime(hours.getValue(), minutes.getValue()));
    	    }
    	});
        minutes.valueProperty().addListener(new ChangeListener<Object>() {
    	    @Override  public void changed(ObservableValue<? extends Object> obs, Object wasChanging, Object Object) {
    	    	timelabel.setText(formatTime(hours.getValue(), minutes.getValue()));
    	    }
    	});
		box.setPrefWidth(DATE_WIDTH);
		box.setPrefHeight(50);
		box.setTranslateY(-12);
		Tooltip.install(box, new Tooltip(tooltip));
//		ValidationUtils.install(field, new SimpleValidator(TimeValidator.getInstance()), ValidationMode.ON_FLY);
		return new HBox(4, label, container);
	}
	
	static String formatTime(double hrs, double mins)
	{
		String h = "0" + (int) hrs;
		String m = "0" + (int) mins;
		String o = right2chars(h) + ":" + right2chars(m);
		System.out.println(o);
		return o;
	}

	static String right2chars(String s)
	{
		int len = s.length();
		if (len > 2) return s.substring(len-2);
		return s;
	}
	public static HBox makeDurationBox( boolean editable, String tooltip)
	{
		Label label = makePrompt("For",45);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		ObservableList list = FXCollections.observableArrayList();
		list.addAll("Minutes", "Hours", "Days");
		ChoiceBox<String> choice = new ChoiceBox<String>(list);
		choice.setId("units");
		choice.getSelectionModel().select(1);		// default to Hours
		TextField field = new TextField("2");
		field.setPrefWidth(40 );
		field.setId("duration");
		field.setDisable(!editable);
		ValidationUtils.install(field, new SimpleValidator(IntegerValidator.getInstance()), ValidationMode.ON_FLY);
		Tooltip.install(label, new Tooltip(tooltip));
		Tooltip.install(field, new Tooltip(tooltip));
		return new HBox(4, label, field, choice);
	}


	public static HBox makeRegulatoryStatusChoiceBox(boolean editable, String toolTip)
	{
		Label label = makePrompt("Regulartory Status", 200);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		ObservableList list = FXCollections.observableArrayList();
		list.addAll("Under Development", "For Discussion Only", "Alpha Test", "Beta Test", "Research Use Only", "In Clinical Trial", "In Medical Use");
		ChoiceBox<String> choice = new ChoiceBox<String>(list);
		choice.setId("regStatus");
		return new HBox(4, label, choice);
	}

	public static HBox makeSchemaStatusBox(String string, String toolTip) {
		Label label = makePrompt("Schema Status", 200);
		label.setAlignment(Pos.BOTTOM_RIGHT);
		ObservableList list = FXCollections.observableArrayList();
		list.addAll("Draft", "Official", "Alpha Test", "Copy of Official", "Modified");
		ChoiceBox<String> choice = new ChoiceBox<String>(list);
		choice.setId("status");
		return new HBox(4, label, choice);
	}

	public static HBox makeTimeDateDurationBox(boolean showTime, boolean showDate, boolean showDuration)
	{
		ArrayList<Node> kids = new ArrayList<Node>();
		if (showTime)		kids.add(makeDateBox("Date", 70, true, "The calendar day"));
		if (showDate)		kids.add(makeTimeBox(70, true, "The time of day"));
		if (showDuration)		kids.add(makeDurationBox( true, "How long the event will run"));
		HBox h = new HBox();
		h.getChildren().addAll(kids);
        return h;
	}

	public static HBox makeNameHBox()
	{
		TextField field = new TextField();
		field.setId("first");
		TextField lastField = new TextField();
		lastField.setId("last");
		return new HBox(makePrompt("First", "first"), field, makePrompt("Last", "last"), lastField);
	}

	public static VBox makeAddressVBox(int prefWidth, boolean intl)
	{
		TextField addr1 = new TextField();		addr1.setId( "addr1");
		TextField addr2 = new TextField();		addr2.setId( "addr2");
		TextField city = new TextField();		city.setId( "city");
		TextField st = new TextField();			st.setId( "state");
		TextField zip = new TextField();		zip.setId( "zip");
		TextField country = new TextField();	country.setId( "country");

		addr1.setPrefWidth(prefWidth - 60);
		addr2.setPrefWidth(prefWidth);
		st.setPrefWidth(40);
		zip.setPrefWidth(100);
		country.setPrefWidth(90);

		HBox line1 = new HBox(makePrompt("Address", "addr1"), addr1);
		HBox line2 = new HBox(addr2);
		HBox line3 = new HBox(10, makePrompt("City", "city"), city, 
				makePrompt("State", "state"), st,
				makePrompt("Zip", "zip"), zip);
		
		VBox v = new VBox(line1, line2, line3);
		if (intl)
		{
			HBox line4 = new HBox(makePrompt("Country", "country"), country);
			v.getChildren().add(line4);
		}
		v.setSpacing(6);
		return v;
	}

	public static boolean addColon = true;
	public static Label makePrompt(String s, String id, int width)
	{
		return makePrompt( s, id, true, width);
	}

	public static Label makePrompt(String s, String id, boolean addColon, int width)
	{
		String S = lookup(s);
		if (addColon)
			S += ": ";
		Label la = new Label(S);  
		if (id != null)
			la.setId(id + "Label");
		if (width > 0 )
			la.setPrefWidth(width);
		return la;
	}

	public static Label makePrompt(String s, int width)	{		return makePrompt(s, "", width);	}
	public static Label makePrompt(String s, String id)	{		return makePrompt(s, id, 0);	}
	public static Label makePrompt(String s)			{		return makePrompt(s, null, 200);	}
	public static Label makeLabel(String s, int width)	{		return makePrompt(s, null, false, width);	}

	public static String lookup(String s)
	{
		return s;
	} // dummy lookup function supports later localization

	
}
