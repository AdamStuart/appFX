package database.forms;

import animation.WindowSizeAnimator;
import gui.Forms;
import gui.Forms.ValidationType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.StringUtil;

/*
 * FormsGallery
 * 
 * Examples of using FormsUtil libraries to build standard forms,
 * and skin them with CSS
 * 
 * createValidationForm -- example of all the field level validation 
 * createSignUpForm() -- an example of form level validation
 * createCytometryMLform -- for Bob
 * createExperimentForm -- MIFlowCyt
 */
public class FormsGallery
{

	// ----------------------------------------------------
	// VARIOUS VALIDATORS TAB
	
	public static Region createValidationForm()
	{
		VBox pane = new VBox(12);
		pane.setSpacing(8);
		pane.setPadding(new Insets(8));

		pane.getChildren().add(Forms.makeEmailBox("", "", 200, true));
		pane.getChildren().add(Forms.makeURLBox("", "URL", 200, 200, "Internet Resource Location"));
		pane.getChildren().add(Forms.makeISBNBox("isbn"));

		Region rgn1 = Forms.makeValidatedBox("IP4", "IP4", ValidationType.IP4, false, 200, 100);
		Region rgn2 = Forms.makeValidatedBox("Integer", "int", ValidationType.INT, true, 200, 80);
		Region rgn3 = Forms.makeValidatedBox("Double", "double", ValidationType.DOUBLE, true, 200, 100);
		Region rgn4 = Forms.makeValidatedBox("Currency", "currency", ValidationType.CURRENCY, false, 200, 80);
		Region rgn5 = Forms.makeValidatedBox("Percent", "percent", ValidationType.PERCENT, false, 200, 60);
		Region rgn6 = Forms.makeValidatedBox("Date", "date", ValidationType.DATE, true, 200, 100);
		Region rgn7 = Forms.makeValidatedBox("CC", "cc", ValidationType.CREDITCARD, false, 200, 200);
		pane.getChildren().addAll(rgn1, rgn2, rgn3, rgn4, rgn5, rgn6, rgn7);
		return pane;
	}
	
	// ----------------------------------------------------
	// SIGNUP FORM TAB

	static StringProperty email1 = new SimpleStringProperty();
	static StringProperty email2 = new SimpleStringProperty();
	static StringProperty password1 = new SimpleStringProperty();
	static StringProperty password2 = new SimpleStringProperty();
	static StringProperty country = new SimpleStringProperty();
	static StringProperty zip = new SimpleStringProperty();
	static Button signUpButton = new Button("Sign Up");
	static CheckBox agreeCheckBox = new CheckBox("Yes, I agree to the term of use");
	static Label statusLabel = new Label("Form is Empty");
	
	public static Region createSignUpForm()
	{
		VBox pane = new VBox(); 
		pane.setSpacing(12);
		pane.setPadding(new Insets(8));
		pane.setPrefHeight(200);
		pane.setPrefWidth(300);
		
		HBox email = Forms.makeEmailBox();
		TextField fld = (TextField) email.lookup("#email");
		fld.textProperty().bindBidirectional(email1);
		if (fld != null) fld.textProperty().addListener((obs, old, val) ->	{	setFormValidity(old, val);	});

		HBox confirmEmail = Forms.makeEmailBox();
		fld = (TextField) confirmEmail.lookup("#email");
		if (fld != null) fld.textProperty().addListener((obs, old, val) ->	{	setFormValidity(old, val);	});
		fld.textProperty().bindBidirectional(email2);

		Label countryLabel = new Label("Country");
		ChoiceBox<String> countryChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("United States", "Canada", "Mexico"));
		countryChoiceBox.setId("countryChoiceBox");
//		countryChoiceBox.getSelectionModel().selectedItemProperty().bindBidirectional(email2);
		country.bind(countryChoiceBox.getSelectionModel().selectedItemProperty());
		countryChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) ->		{if (val != null && !val.equals(old)) setFormValidity("b", "a");	});
		
		agreeCheckBox.setId("agreeCheckBox");
		agreeCheckBox.setSelected(false);
		agreeCheckBox.selectedProperty().addListener((obs, old, val) ->	{ if (old != val) setFormValidity("a", "b");	});
		
		signUpButton.setId("signUpButton");

		Region zip = Forms.makeValidatedBox("Zip Code",  "ZIP", ValidationType.ZIP, true, 0, 90);
//		fld = (TextField) zip.lookup("#" + "ZIP");
//		if (fld != null) fld.setPrefWidth(70);

		pane.getChildren().addAll(email, confirmEmail, new HBox(10, countryLabel, countryChoiceBox));
		pane.getChildren().add(zip);
		pane.getChildren().addAll(makePasswordField("pass", "Password", password1), makePasswordField("confirm", "Confirm Password", password2));
		pane.getChildren().add(new HBox(10, agreeCheckBox, signUpButton, statusLabel));
		return pane;
	}

	static HBox makeFormField(String id, String prompt)
	{
		Label label = new Label(prompt);
		TextField field = new TextField();
		field.setId(field + "Field");
		return new HBox(8, label, field);
	}

	static HBox makePasswordField(String id, String prompt, StringProperty bound)
	{
		Label label = new Label(prompt);
		PasswordField field = new PasswordField();
		field.setId(field + "Field");
		bound.bind(field.textProperty());
		field.textProperty().addListener((obs, old, val) -> { setFormValidity(old, val); });
		return new HBox(8, label, field);
	}

	enum FormError { OK, EMAIL_REQ, EMAIL_INVALID, EMAIL_MATCH, PASSWORD_REQ, PASSWORD_MATCH, PASSWORD_STRENGTH, US_REQ, NO_CONSENT };

	public static void setFormValidity(String old, String val)
	{
		if (val == null || val.equals(old)) return;
		FormError err = isFormValid();
		signUpButton.setDisable(err != FormError.OK);
		statusLabel.setText("FormValidity:  " + err);
	}

	public static FormError isFormValid()
	{
		String addr = email1.getValue();
		if (addr.isEmpty()) 	return FormError.EMAIL_REQ;
		if (!StringUtil.isValidEmail(addr)) return FormError.EMAIL_INVALID;
		if (!email1.getValue().equals(email2.getValue())) return FormError.EMAIL_MATCH;
		String nation = country.getValue();
		if (!"United States".equals(nation)) return FormError.US_REQ;
		
		if (StringUtil.isEmpty(password1.getValue())) return FormError.PASSWORD_REQ;
		if (!(password1.getValue().trim().equals(password2.getValue().trim()))) return FormError.PASSWORD_MATCH;
		if (password1.getValue().length() < 6) 	return FormError.PASSWORD_STRENGTH;
		if (!agreeCheckBox.isSelected()) 		return FormError.NO_CONSENT;
		// TODO -- stricter password security would go here
		return FormError.OK;
	}

// ----------------------------------------------------
// CYTOMETRY TAB
	static String maintainerTip = "The person or institution responsible for this work.";
	static String authorTip = "A contributor to this work";
	static String sourceTip = "Originating contributors";
		
	static String descTip = "An explanation of the referenced experiment";
	static String disclaimerTip = "Legal language to protect your boss";
	static String patentTip = "Patent";
	static String permissionTip = "Permissions";
	static String acknowledgementTip = "Acknowledgements";
	static String futureTip = "The element, Future_Work, which is not mandatory was added to" + 
			"this about schema on 5 July, 2009. It will permit the addition of references to suggested extensions.";

	static String subjectTip = "subjectTip";
	static String versionTip = "versionTip";
	static String locationTip = "locationTip";
	static String schemaStatusTip = "schemaStatusTip";
	static String regStatusTip = "regStatusTip";
	static String copyrightHolderTip = "copyrightHolderTip";
	static String copyrightLocationTip = "copyrightLocationTip";
	static String permTip = "permTip";
	static String releaseDateTip = "releaseDateTip";
	static String supplementaryTip = "supplementaryTip";
	static String keywordsTip = "keywordsTip";
	static String verifTip = "This is a string that will be created by the manufacturer or others to verify that the schema is identical to the " +
			"	one sent out or downloaded. The algorithm employed by the manufacturer or similar party can be secret. An example would be a " +
			"	cyclic redundancy check, CRC, value.";

	 
	public static Region createCytometryMLform(String prefix)
	{
		/*The complex type about_Type should be usable for XML and XHTML web pages. The use of attributes, where reasonable, will result in simpler text 
		for the web pages. The Maintainer_Type must be part of the about_Type, since the user should be provided with a contact, who can provide help.*/
		

		HBox m = Forms.promptedText("Maintainer", "maintainer", 150, maintainerTip);
		HBox author = Forms.promptedText("Author", "author", 150, authorTip);
		HBox src = Forms.promptedText("Source", "source", 100, sourceTip);
		HBox des = Forms.promptedText("Description", "desc", 500, descTip);
		HBox disc = Forms.promptedText("Status_Disclaimer", "disclaimer", 200, disclaimerTip);
		HBox patent = Forms.promptedText("Patent Disclaimer", "patent", 200, patentTip);
		HBox perm = Forms.promptedText("Permissions", "permission", 100, permissionTip);
		HBox ack = Forms.promptedText("Acknowledgement", "acknowledgement", 400, acknowledgementTip);
		HBox future = Forms.promptedText("Future Work", "futureWork", 400, futureTip);
		
		HBox sub = Forms.promptedText("Subject", "subject", 200, subjectTip);
		HBox ver = Forms.promptedText("Version", "version", 60, versionTip);
		HBox loc = Forms.makeURLBox("", "Latest Location", 200, 200, locationTip);
		HBox schema = Forms.makeSchemaStatusBox("Schema Status", schemaStatusTip);
		HBox reg = Forms.makeRegulatoryStatusChoiceBox(true, regStatusTip);   
		HBox c = Forms.promptedText("Copyright Holder", "copyright", 80, copyrightHolderTip);
		HBox curi = Forms.makeURLBox("", "Copyright URI", 200, 200, copyrightLocationTip);
		HBox perm2 = Forms.promptedText("Permissions", "permission", 50, permTip);
		HBox release = Forms.makeDateBox( "Release Date", true,  200,releaseDateTip); 
		HBox info = Forms.promptedText("Supplementary Info", "info", 100, supplementaryTip);
		HBox keys = Forms.promptedText("Keywords", "keywords", 150, keywordsTip);
		HBox verif = Forms.promptedText("Verification Value", "verification", 50, verifTip);
		
		VBox top = new VBox(10);
		VBox container = new VBox(6);		container.setPadding(new Insets(20));
		VBox container2 = new VBox(6);		container2.setPadding(new Insets(20));
		
		boolean bobsLayout = true;
		if (bobsLayout)
		{
			container.getChildren().addAll(m, author, src, des, disc, patent, perm, ack, future);
			container2.getChildren().addAll(sub, ver, loc, schema, reg, c, curi, perm2, release, info, keys, verif);
		}
		else
		{
			container.getChildren().addAll(release, author, sub, loc,src, des, ver, info, keys );
			container2.getChildren().addAll(m, schema, reg, c, curi, perm2, disc, patent, perm, ack, future, verif);
		}
		top.getChildren().addAll(container, container2);
		return top;
	}
	
	// ----------------------------------------------------
	// EXPERIMNET TAB
	static String endDateTooltip = "endDateTooltip";
//	static String projectTooltip = "endDateTooltip";
//	static String reasearcherTooltip = "endDateTooltip";
//	static String investigatorTooltip = "endDateTooltip";
//	static String organizationTooltip = "endDateTooltip";
//	static String keywordsTooltip = "endDateTooltip";
//	static String overviewTooltip = "endDateTooltip";
	static String tooltip = "Undefined Tooltip";

	public static Region createExperimentForm(String prefix)
	{
		HBox expid = Forms.makeFormField("ID", "id", 50, "createdDateTooltip");
		HBox createdDate = Forms.makeDateBox("Created", true, 80, tooltip);
		HBox endDate = Forms.makeDateBox("End Date", true, 80, endDateTooltip);
		HBox organization = Forms.makeFormField("Organization", "orgnization", 100, tooltip);
		HBox lab = Forms.makeFormField("Lab", "lab", 50, tooltip);
		HBox title = new HBox(20);
		title.getChildren().addAll(expid, new VBox(6, createdDate, endDate), lab, organization);

		HBox project = Forms.makeFormField("Project", "project", 400, tooltip);
		HBox reasearcher = Forms.makeFormField("Primary Researcher", "reasearcher", 150, tooltip);
		HBox investigator = Forms.makeFormField("Primary Investigator", "investigator", 150, tooltip);
		HBox researchers = new HBox(20, reasearcher, investigator);
		HBox keywords = Forms.makeFormField("Keywords", "keywords", 400, tooltip);
		VBox overview = new VBox(6);
		overview.getChildren().addAll(project, keywords, researchers);

		HBox description = Forms.makeFormField("Description", "description", 400, tooltip);
		HBox source = Forms.makeFormField("Source", "source", 150, tooltip);
		HBox organism = Forms.makeFormField("Organism", "organism", 150, tooltip);
		HBox age = Forms.makeFormField("Age", "age", 50, tooltip);
		HBox gender = Forms.makeFormField("Gender", "gender", 40, tooltip);
		HBox phenotype = Forms.makeFormField("Phenotype", "phenotype", 150, tooltip);
		HBox characteristics = Forms.makeFormField("Characteristics", "characteristics", 400, tooltip);
		HBox treatment = Forms.makeFormField("Treatment", "treatment", 400, tooltip);
		VBox specimen = new VBox(6);
		HBox specimenLine1 = new HBox(20,  organism, age, gender, source );
		specimen.getChildren().addAll(description, specimenLine1, phenotype, characteristics, treatment);

		HBox model = Forms.makeFormField("Instrument", "instrument", 100, tooltip);
		HBox vendor = Forms.makeFormField("Vendor", "vendor", 100, tooltip);
		HBox location = Forms.makeFormField("Location", "location", 50, tooltip);
		HBox instrumentation = new HBox(20);
		instrumentation.getChildren().addAll(model, vendor, location);

		HBox protocol = Forms.makeFormField("Protocol", "protocol", 150, tooltip);
		HBox gating = Forms.makeFormField("Gating", "gates", 150, tooltip);
		HBox panel = Forms.makeFormField("Panel", "panel", 150, tooltip);
		VBox analysis = new VBox(6);
		HBox anal = new HBox(20,  protocol, gating, panel );
		analysis.getChildren().addAll(anal);
	
		VBox top = new VBox();
		VBox container = new VBox(50);
		container.setPadding(new Insets(20));
		container.getChildren().addAll(title, overview, specimen, instrumentation, analysis);
		ScrollPane scroller = new ScrollPane(container);
		top.getChildren().addAll(scroller);
		return top;
	}
	 
// ----------------------------------------------------
		private static Region createToDoForm()			// UNUSED
		{
			VBox pane = new VBox();
	    	pane.setSpacing(12);
	    	pane.setPadding(new Insets(8));
	    	pane.setPrefHeight(200);
	    	pane.setPrefWidth(300);
	        Label taskLabel = new Label("Task");
	        taskLabel.setId("taskLabel");
	        TextField taskField = new TextField();
	        taskField.setId("taskField");

	        Label descLabel = new Label("Description");
	        descLabel.setId("descLabel");
	        TextField descField = new TextField();
	        descField.setId("descField");
	        pane.getChildren().addAll(new HBox(6, taskLabel, taskField), new HBox(6, descLabel, descField));
			return pane;
		}


}
