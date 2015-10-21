package database.forms;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.FormsUtil;
import util.FormsUtil.ValidationType;

/*
 * FormsGallery
 * 
 * Examples of using FormsUtil libraries to build standard forms,
 * and skin them with CSS
 * 
 * createValidationForm -- example of all the field level validation 
 * createCytometryMLform -- for Bob
 * createExperimentForm -- MIFlowCyt
 * createSignUpForm() -- an example of cross field (form level) validation
 */
public class FormsGallery
{
	private static String PREFIX_VALIDATION_FORM = "validation_";

	public static Region createValidationForm()
	{
		VBox pane = new VBox(12);
		pane.setSpacing(8);
		pane.setPadding(new Insets(8));

		pane.getChildren().add(FormsUtil.makeEmailBox());
		pane.getChildren().add(FormsUtil.makeURLBox());
		pane.getChildren().add(FormsUtil.makeISBNBox(PREFIX_VALIDATION_FORM));

		Region rgn1 = FormsUtil.makeValidatedBox("IP4", PREFIX_VALIDATION_FORM + "IP4", ValidationType.IP4);
		Region rgn2 = FormsUtil.makeValidatedBox("Integer", PREFIX_VALIDATION_FORM + "int", ValidationType.INT);
		Region rgn3 = FormsUtil.makeValidatedBox("Double", PREFIX_VALIDATION_FORM + "double", ValidationType.DOUBLE);
		Region rgn4 = FormsUtil.makeValidatedBox("Currency", PREFIX_VALIDATION_FORM + "currency", ValidationType.CURRENCY);
		Region rgn5 = FormsUtil.makeValidatedBox("Percent", PREFIX_VALIDATION_FORM + "percent", ValidationType.PERCENT);
		Region rgn6 = FormsUtil.makeValidatedBox("Date", PREFIX_VALIDATION_FORM + "date", ValidationType.DATE);
		Region rgn7 = FormsUtil.makeValidatedBox("CC", PREFIX_VALIDATION_FORM + "cc", ValidationType.CREDITCARD);
		pane.getChildren().addAll(rgn1, rgn2, rgn3, rgn4, rgn5, rgn6, rgn7);
		return pane;
	}
	// ----------------------------------------------------

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
		HBox loc = FormsUtil.makeURLBox("", "Latest Location", 200, locationTip);
		HBox schema = FormsUtil.makeSchemaStatusBox("Schema Status", schemaStatusTip);
		HBox reg = FormsUtil.makeRegulatoryStatusChoiceBox(true, regStatusTip);   
		HBox c = FormsUtil.promptedText("Copyright Holder", "copyright", 80, copyrightHolderTip);
		HBox curi = FormsUtil.makeURLBox("", "Copyright URI", 200, copyrightLocationTip);
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
	static String endDateTooltip = "endDateTooltip";
	static String projectTooltip = "endDateTooltip";
	static String reasearcherTooltip = "endDateTooltip";
	static String investigatorTooltip = "endDateTooltip";
	static String organizationTooltip = "endDateTooltip";
	static String keywordsTooltip = "endDateTooltip";
	static String overviewTooltip = "endDateTooltip";
	static String tooltip = "Undefined Tooltip";

	public static Region createExperimentForm(String prefix)
	{
		HBox expid = FormsUtil.makeInternalLabelField("ID", "id", 50, "createdDateTooltip");
		HBox createdDate = FormsUtil.makeDateBox("Created", true, 80, tooltip);
		HBox endDate = FormsUtil.makeDateBox("End Date", true, 80, endDateTooltip);
		HBox organization = FormsUtil.makeInternalLabelField("Organization", "orgnization", 100, tooltip);
		HBox lab = FormsUtil.makeInternalLabelField("Lab", "lab", 50, tooltip);
		HBox title = new HBox(20);

		title.getChildren().addAll(expid, new VBox(6, createdDate, endDate), lab, organization);
		

		HBox project = FormsUtil.makeInternalLabelField("Project", "project", 400, tooltip);
		HBox reasearcher = FormsUtil.makeInternalLabelField("Primary Researcher", "reasearcher", 150, tooltip);
		HBox investigator = FormsUtil.makeInternalLabelField("Primary Investigator", "investigator", 150, tooltip);
		HBox researchers = new HBox(20, reasearcher, investigator);
		HBox keywords = FormsUtil.makeInternalLabelField("Keywords", "keywords", 400, tooltip);
		VBox overview = new VBox(6);
		overview.getChildren().addAll(project, keywords, researchers);

		HBox description = FormsUtil.makeInternalLabelField("Description", "description", 400, tooltip);
		HBox source = FormsUtil.makeInternalLabelField("Source", "source", 150, tooltip);
		HBox organism = FormsUtil.makeInternalLabelField("Organism", "organism", 150, tooltip);
		HBox age = FormsUtil.makeInternalLabelField("Age", "age", 50, tooltip);
		HBox gender = FormsUtil.makeInternalLabelField("Gender", "gender", 40, tooltip);
		HBox phenotype = FormsUtil.makeInternalLabelField("Phenotype", "phenotype", 150, tooltip);
		HBox characteristics = FormsUtil.makeInternalLabelField("Characteristics", "characteristics", 400, tooltip);
		HBox treatment = FormsUtil.makeInternalLabelField("Treatment", "treatment", 400, tooltip);
		VBox specimen = new VBox(6);
		HBox specimenLine1 = new HBox(20,  organism, age, gender, source );
		specimen.getChildren().addAll(description, specimenLine1, phenotype, characteristics, treatment);

		HBox model = FormsUtil.makeInternalLabelField("Instrument", "instrument", 100, tooltip);
		HBox vendor = FormsUtil.makeInternalLabelField("Vendor", "vendor", 100, tooltip);
		HBox location = FormsUtil.makeInternalLabelField("Location", "location", 50, tooltip);
		HBox instrumentation = new HBox(20);
		instrumentation.getChildren().addAll(model, vendor, location);

		HBox protocol = FormsUtil.makeInternalLabelField("Protocol", "protocol", 150, tooltip);
		HBox gating = FormsUtil.makeInternalLabelField("Gating", "gates", 150, tooltip);
		HBox panel = FormsUtil.makeInternalLabelField("Panel", "panel", 150, tooltip);
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
	static String PREFIX_SIGNUP_FORM = "signup_";
	  public static Region createSignUpForm() 
	   {

	    	VBox pane = new VBox(); // (new LC().width("450px").height("240px").insets("20 10 10 10"), new AC().index(0).align("right").gap("16px").index(1).grow(), new AC().gap("6px"));
	    	pane.setSpacing(12);
	    	pane.setPadding(new Insets(8));
	    	pane.setPrefHeight(200);
	    	pane.setPrefWidth(300);
	        Label emailLabel = new Label("Your Email");
	        emailLabel.setId(PREFIX_SIGNUP_FORM + "emailLabel");
	        TextField emailField = new TextField();
	        emailField.setId(PREFIX_SIGNUP_FORM + "emailField");

	        Label confirmEmailLabel = new Label("Confirm Email");
	        confirmEmailLabel.setId(PREFIX_SIGNUP_FORM + "confirmEmailLabel");
	        TextField confirmEmailField = new TextField();
	        confirmEmailField.setId(PREFIX_SIGNUP_FORM + "confirmEmailField");

	        Label countryLabel = new Label("Country");
	        ChoiceBox<String> countryChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("United States", "Canada", "Mexico"));
	        countryChoiceBox.setId(PREFIX_SIGNUP_FORM + "countryChoiceBox");

	        Label zipCodeLabel = new Label("Zip Code");
	        TextField zipCodeField = new TextField();
	        zipCodeField.setId(PREFIX_SIGNUP_FORM + "zipCodeField");

	        Label passwordLabel = new Label("Password");
	        PasswordField passwordField = new PasswordField();
	        Label confirmPasswordLabel = new Label("Confirm Password");
	        passwordField.setId(PREFIX_SIGNUP_FORM + "passwordField");

	        PasswordField confirmPasswordField = new PasswordField();
	        confirmPasswordField.setId(PREFIX_SIGNUP_FORM + "confirmPasswordField");

	        CheckBox agreeCheckBox = new CheckBox("Yes, I agree to the term of use");
	        agreeCheckBox.setId(PREFIX_SIGNUP_FORM + "agreeCheckBox");

	        Button signUpButton = new Button("Sign Up");
	        signUpButton.setId(PREFIX_SIGNUP_FORM + "signUpButton");
	        //signUpButton.disableProperty().bind(agreeCheckBox.selectedProperty().not());
//	        emailLabel.setBorder(value);
	        pane.getChildren().add(new HBox(emailLabel, emailField));
	        pane.getChildren().add(new HBox(confirmEmailLabel, confirmEmailField));
	        pane.getChildren().add(new HBox(countryLabel,countryChoiceBox));
	        pane.getChildren().add(new HBox(zipCodeLabel, zipCodeField));
	        pane.getChildren().add(new HBox(passwordLabel,passwordField));
	        pane.getChildren().add(new HBox(confirmPasswordLabel, confirmPasswordField));
	        pane.getChildren().add(new HBox(agreeCheckBox,signUpButton));
	 
	        return pane;
	    }
	


}
