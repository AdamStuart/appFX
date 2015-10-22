package database.forms;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.FormsUtil;
import util.FormsUtil.ValidationType;
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
	private static String PREFIX_VALIDATION_FORM = "validation_";

	// ----------------------------------------------------
	// VARIOUS VALIDATORS TAB
	
	public static Region createValidationForm()
	{
		VBox pane = new VBox(12);
		pane.setSpacing(8);
		pane.setPadding(new Insets(8));

		pane.getChildren().add(FormsUtil.makeEmailBox("", "", 200));
		pane.getChildren().add(FormsUtil.makeURLBox("", "URL", 200, 200, "Internet Resource Location"));
		pane.getChildren().add(FormsUtil.makeISBNBox(PREFIX_VALIDATION_FORM));

		Region rgn1 = FormsUtil.makeValidatedBox("IP4", PREFIX_VALIDATION_FORM + "IP4", ValidationType.IP4, false);
		Region rgn2 = FormsUtil.makeValidatedBox("Integer", PREFIX_VALIDATION_FORM + "int", ValidationType.INT, true);
		Region rgn3 = FormsUtil.makeValidatedBox("Double", PREFIX_VALIDATION_FORM + "double", ValidationType.DOUBLE, true);
		Region rgn4 = FormsUtil.makeValidatedBox("Currency", PREFIX_VALIDATION_FORM + "currency", ValidationType.CURRENCY, false);
		Region rgn5 = FormsUtil.makeValidatedBox("Percent", PREFIX_VALIDATION_FORM + "percent", ValidationType.PERCENT, false);
		Region rgn6 = FormsUtil.makeValidatedBox("Date", PREFIX_VALIDATION_FORM + "date", ValidationType.DATE, true);
		Region rgn7 = FormsUtil.makeValidatedBox("CC", PREFIX_VALIDATION_FORM + "cc", ValidationType.CREDITCARD, false);
		pane.getChildren().addAll(rgn1, rgn2, rgn3, rgn4, rgn5, rgn6, rgn7);
		return pane;
	}
	
	// ----------------------------------------------------
	// SIGNUP FORM TAB

	static String PREFIX_SIGNUP_FORM = "signup_";
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
//		Label emailLabel = new Label("Your Email");
//		emailLabel.setId(PREFIX_SIGNUP_FORM + "emailLabel");
//		TextField emailField = new TextField();
//		emailField.setId(PREFIX_SIGNUP_FORM + "emailField");
		
		HBox email = FormsUtil.makeEmailBox();
		TextField fld = (TextField) email.lookup("#email");
		fld.textProperty().bindBidirectional(email1);
		if (fld != null) fld.textProperty().addListener((obs, old, val) ->	{	setFormValidity(old, val);	});

		HBox confirmEmail = FormsUtil.makeEmailBox();
		fld = (TextField) confirmEmail.lookup("#email");
		if (fld != null) fld.textProperty().addListener((obs, old, val) ->	{	setFormValidity(old, val);	});
		fld.textProperty().bindBidirectional(email2);

		Label countryLabel = new Label("Country");
		ChoiceBox<String> countryChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("United States", "Canada", "Mexico"));
		countryChoiceBox.setId(PREFIX_SIGNUP_FORM + "countryChoiceBox");
//		countryChoiceBox.getSelectionModel().selectedItemProperty().bindBidirectional(email2);
		country.bind(countryChoiceBox.getSelectionModel().selectedItemProperty());
		countryChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) ->		{if (val != null && !val.equals(old)) setFormValidity("b", "a");	});
		
		agreeCheckBox.setId(PREFIX_SIGNUP_FORM + "agreeCheckBox");
		agreeCheckBox.setSelected(false);
		agreeCheckBox.selectedProperty().addListener((obs, old, val) ->	{ if (old != val) setFormValidity("a", "b");	});
		
		signUpButton.setId(PREFIX_SIGNUP_FORM + "signUpButton");

		pane.getChildren().addAll(email, confirmEmail, new HBox(10, countryLabel, countryChoiceBox));
		pane.getChildren().add(makeFormField("zip", "Zip Code"));
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
		

		HBox m = FormsUtil.promptedText("Maintainer", "maintainer", 150, maintainerTip);
		HBox author = FormsUtil.promptedText("Author", "author", 150, authorTip);
		HBox src = FormsUtil.promptedText("Source", "source", 100, sourceTip);
		HBox des = FormsUtil.promptedText("Description", "desc", 500, descTip);
		HBox disc = FormsUtil.promptedText("Status_Disclaimer", "disclaimer", 200, disclaimerTip);
		HBox patent = FormsUtil.promptedText("Patent Disclaimer", "patent", 200, patentTip);
		HBox perm = FormsUtil.promptedText("Permissions", "permission", 100, permissionTip);
		HBox ack = FormsUtil.promptedText("Acknowledgement", "acknowledgement", 400, acknowledgementTip);
		HBox future = FormsUtil.promptedText("Future Work", "futureWork", 400, futureTip);
		
		HBox sub = FormsUtil.promptedText("Subject", "subject", 200, subjectTip);
		HBox ver = FormsUtil.promptedText("Version", "version", 60, versionTip);
		HBox loc = FormsUtil.makeURLBox("", "Latest Location", 200, 200, locationTip);
		HBox schema = FormsUtil.makeSchemaStatusBox("Schema Status", schemaStatusTip);
		HBox reg = FormsUtil.makeRegulatoryStatusChoiceBox(true, regStatusTip);   
		HBox c = FormsUtil.promptedText("Copyright Holder", "copyright", 80, copyrightHolderTip);
		HBox curi = FormsUtil.makeURLBox("", "Copyright URI", 200, 200, copyrightLocationTip);
		HBox perm2 = FormsUtil.promptedText("Permissions", "permission", 50, permTip);
		HBox release = FormsUtil.makeDateBox( "Release Date", true,  200,releaseDateTip); 
		HBox info = FormsUtil.promptedText("Supplementary Info", "info", 100, supplementaryTip);
		HBox keys = FormsUtil.promptedText("Keywords", "keywords", 150, keywordsTip);
		HBox verif = FormsUtil.promptedText("Verification Value", "verification", 50, verifTip);
		
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
		HBox expid = FormsUtil.makeFormField("ID", "id", 50, "createdDateTooltip");
		HBox createdDate = FormsUtil.makeDateBox("Created", true, 80, tooltip);
		HBox endDate = FormsUtil.makeDateBox("End Date", true, 80, endDateTooltip);
		HBox organization = FormsUtil.makeFormField("Organization", "orgnization", 100, tooltip);
		HBox lab = FormsUtil.makeFormField("Lab", "lab", 50, tooltip);
		HBox title = new HBox(20);
		title.getChildren().addAll(expid, new VBox(6, createdDate, endDate), lab, organization);

		HBox project = FormsUtil.makeFormField("Project", "project", 400, tooltip);
		HBox reasearcher = FormsUtil.makeFormField("Primary Researcher", "reasearcher", 150, tooltip);
		HBox investigator = FormsUtil.makeFormField("Primary Investigator", "investigator", 150, tooltip);
		HBox researchers = new HBox(20, reasearcher, investigator);
		HBox keywords = FormsUtil.makeFormField("Keywords", "keywords", 400, tooltip);
		VBox overview = new VBox(6);
		overview.getChildren().addAll(project, keywords, researchers);

		HBox description = FormsUtil.makeFormField("Description", "description", 400, tooltip);
		HBox source = FormsUtil.makeFormField("Source", "source", 150, tooltip);
		HBox organism = FormsUtil.makeFormField("Organism", "organism", 150, tooltip);
		HBox age = FormsUtil.makeFormField("Age", "age", 50, tooltip);
		HBox gender = FormsUtil.makeFormField("Gender", "gender", 40, tooltip);
		HBox phenotype = FormsUtil.makeFormField("Phenotype", "phenotype", 150, tooltip);
		HBox characteristics = FormsUtil.makeFormField("Characteristics", "characteristics", 400, tooltip);
		HBox treatment = FormsUtil.makeFormField("Treatment", "treatment", 400, tooltip);
		VBox specimen = new VBox(6);
		HBox specimenLine1 = new HBox(20,  organism, age, gender, source );
		specimen.getChildren().addAll(description, specimenLine1, phenotype, characteristics, treatment);

		HBox model = FormsUtil.makeFormField("Instrument", "instrument", 100, tooltip);
		HBox vendor = FormsUtil.makeFormField("Vendor", "vendor", 100, tooltip);
		HBox location = FormsUtil.makeFormField("Location", "location", 50, tooltip);
		HBox instrumentation = new HBox(20);
		instrumentation.getChildren().addAll(model, vendor, location);

		HBox protocol = FormsUtil.makeFormField("Protocol", "protocol", 150, tooltip);
		HBox gating = FormsUtil.makeFormField("Gating", "gates", 150, tooltip);
		HBox panel = FormsUtil.makeFormField("Panel", "panel", 150, tooltip);
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
