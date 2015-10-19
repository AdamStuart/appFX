/*
 * Derived from:
 * @(#)ValidationDemo.java 5/19/2013
 *
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */
package database.forms;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.CurrencyValidator;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.PercentValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;

import validation.DecorationPane;
import validation.Decorator;
import validation.SimpleValidator;
import validation.ValidationEvent;
import validation.ValidationUtils;
import validation.ValidationUtils.ValidationMode;

public class ValidatorsForm extends VBox
{
	Region root;
	   public Region getRegion(){ return root;}
	    public static final String PREFIX_VALIDATION_FORM = "ValidationForm";
 

    public ValidatorsForm() {
    	root = createValidationForm();
	    getChildren().add(root);
//	    getChildren().add( installValidatorsForValidationForm(root));
	    }

	    private Region installValidatorsForValidationForm(Region pane) {
	        String prefix = PREFIX_VALIDATION_FORM;

	        ValidationUtils.install(pane.lookup("#" + prefix + "emailField"), new SimpleValidator(EmailValidator.getInstance()), ValidationMode.ON_FLY);

	        TextField urlField = (TextField) pane.lookup("#" + prefix + "urlField");
	        ValidationUtils.install(urlField, new SimpleValidator(UrlValidator.getInstance()), ValidationMode.ON_FLY);
	        urlField.addEventHandler(ValidationEvent.ANY, new EventHandler<ValidationEvent>() {
	            @Override
	            public void handle(ValidationEvent event) {
	                pane.lookup("#" + prefix + "urlButton").setDisable(event.getEventType() != ValidationEvent.VALIDATION_OK);
	            }
	        });

	        TextField ISBNField = (TextField) pane.lookup("#" + prefix + "ISBNField");
	        ValidationUtils.install(ISBNField, new SimpleValidator(ISBNValidator.getInstance()), ValidationMode.ON_FLY);
	        ISBNField.addEventHandler(ValidationEvent.ANY, new EventHandler<ValidationEvent>() {
	            @Override
	            public void handle(ValidationEvent event) {
	                pane.lookup("#" + prefix + "amazonButton").setDisable(event.getEventType() != ValidationEvent.VALIDATION_OK);
	            }
	        });

	        ValidationUtils.install(pane.lookup("#" + prefix + "IP4Field"), new SimpleValidator(new RegexValidator(PATTERN_IP4)), ValidationMode.ON_FLY);

	        ValidationUtils.install(pane.lookup("#" + prefix + "intField"), new SimpleValidator(IntegerValidator.getInstance()), ValidationMode.ON_FLY);
	        ValidationUtils.install(pane.lookup("#" + prefix + "doubleField"), new SimpleValidator(DoubleValidator.getInstance()), ValidationMode.ON_FLY);

	        ValidationUtils.install(pane.lookup("#" + prefix + "currencyField"), new SimpleValidator(CurrencyValidator.getInstance()), ValidationMode.ON_FLY);
	        ValidationUtils.install(pane.lookup("#" + prefix + "percentField"), new SimpleValidator(PercentValidator.getInstance()), ValidationMode.ON_FLY);
	        ValidationUtils.install(pane.lookup("#" + prefix + "dateField"), new SimpleValidator(DateValidator.getInstance()), ValidationMode.ON_FLY);

	        TextField cardField = (TextField) pane.lookup("#" + prefix + "cardField");
	        ImageView cardImage = (ImageView) pane.lookup("#" + prefix + "cardImage");
	        String imagePath = "/database/forms/images/";
	        ValidationUtils.install(cardField, new SimpleValidator(new CreditCardValidator(CreditCardValidator.AMEX | CreditCardValidator.VISA | CreditCardValidator.MASTERCARD + CreditCardValidator.DISCOVER | CreditCardValidator.DINERS)), ValidationMode.ON_FLY);
	        cardField.addEventHandler(ValidationEvent.ANY, new EventHandler<ValidationEvent>() {
	            @Override
	            public void handle(ValidationEvent event) {
	                if (event.getEventType() == ValidationEvent.VALIDATION_OK) {
	                    if (new CreditCardValidator(CreditCardValidator.VISA).isValid(cardField.getText())) {
	                        cardImage.setImage(new Image(imagePath + "VISA.png"));
	                    } else if (new CreditCardValidator(CreditCardValidator.MASTERCARD).isValid(cardField.getText())) {
	                        cardImage.setImage(new Image(imagePath + "MasterCard.png"));
	                    } else if (new CreditCardValidator(CreditCardValidator.AMEX).isValid(cardField.getText())) {
	                        cardImage.setImage(new Image(imagePath + "AMEX.png"));
	                    } else if (new CreditCardValidator(CreditCardValidator.DISCOVER).isValid(cardField.getText())) {
	                        cardImage.setImage(new Image(imagePath + "Discover.png"));
	                    } else if (new CreditCardValidator(CreditCardValidator.DINERS).isValid(cardField.getText())) {
	                        cardImage.setImage(new Image(imagePath + "DinersClub.png"));
	                    }
	                }

	            }
	        });

	        final DatePicker dpDate = (DatePicker) pane.lookup("#" + prefix + "dpDate");
	        final DatePicker dpDateTwo = (DatePicker) pane.lookup("#" + prefix + "dpDateTwo");

//	        Validator validator = param -> dpDate.getValue() != null && dpDateTwo.getValue() != null &&
//	                dpDate.getValue().isBefore(dpDateTwo.getValue()) &&
//	                dpDateTwo.getValue().isAfter(dpDate.getValue())
//	                ?
//	                new ValidationEvent(ValidationEvent.VALIDATION_OK) :
//	                new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Date must follow start date");

//	        ValidationUtils.install(dpDate, validator, ValidationMode.ON_FLY);
//	        ValidationUtils.install(dpDateTwo, dpDate.valueProperty(), validator, ValidationMode.ON_FLY,
//	                ValidationUtils.createDefaultValidationEventHandler(dpDateTwo, ValidationDecorators::fontAwesomeDecoratorCreator));
//
//	        Consumer<Object> validateDates = o -> {
//	            ValidationUtils.forceValidate(dpDate, ValidationMode.ON_FLY);
//	            ValidationUtils.forceValidate(dpDateTwo, ValidationMode.ON_FLY);
//	        };
//
//	        ValidationDecorators.installRequiredDecorator(dpDate, ValidationDecorators::graphicRequiredCreator);
//	        dpDate.valueProperty().addListener((observable1, oldValue1, newValue1) -> validateDates.accept(null));
//	        dpDateTwo.valueProperty().addListener((observable1, oldValue1, newValue1) -> validateDates.accept(null));

//	        validateDates.accept(null);

	        Label validationLabel = null;
	        Optional<List<Decorator>> ovalidationDecorators = ValidationUtils.getValidationDecorators(dpDate);
	        if (ovalidationDecorators.isPresent()) {
	            Optional<Node> nodeOptional = Optional.of(ovalidationDecorators.get().get(0).getNode());
	            if (nodeOptional.isPresent() && (nodeOptional.get() instanceof Label)) {
	                validationLabel = (Label) nodeOptional.get();
	            }
	        }

	        final Label finalValidationLabel = validationLabel;
	        // Show Validation tooltip on date picker focus
	        dpDate.focusedProperty().addListener((observable, oldValue, newValue) -> {  if (newValue) {       ValidationUtils.showTooltip(finalValidationLabel);    }
	        });

	        dpDate.addEventHandler(ValidationEvent.VALIDATION_ERROR, event -> {    ValidationUtils.showTooltip(finalValidationLabel);     });
	        pane.addEventHandler(ValidationEvent.ANY, event -> {         System.out.println(event);      });

	        return new DecorationPane(pane);
	    }
	    public static final String PATTERN_IP4 = "\\b(([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\b";

	    public static Region createValidationForm() {
	
	    	VBox pane = new VBox();
	    	pane.setSpacing(8);
	    	pane.setPadding(new Insets(8));
	       Label emailLabel = new Label("Email:");
	        emailLabel.setId(PREFIX_VALIDATION_FORM + "emailLabel");
	        TextField emailField = new TextField("");
	        emailField.setId(PREFIX_VALIDATION_FORM + "emailField");
	        pane.getChildren().add(new HBox(emailLabel,emailField)); 
	
	        Label urlLabel = new Label("URL:");
	        urlLabel.setId(PREFIX_VALIDATION_FORM + "urlLabel");
	        TextField urlField = new TextField("");
	        urlField.setId(PREFIX_VALIDATION_FORM + "urlField");
	        Button urlButton = new Button("Open", new ImageView(new Image("validation/web.png")));
	        urlButton.setId(PREFIX_VALIDATION_FORM + "urlButton");
	        urlButton.setOnAction(event -> {
	            try {
	                java.awt.Desktop.getDesktop().browse(new URI(urlField.getText()));
	            }
	            catch (Exception e) {}// ignore
	        });
	        urlButton.setDisable(true);
	        pane.getChildren().add(new HBox(urlLabel, urlField, urlButton)); 
	
	        Label ISBNLabel = new Label("ISBN:");
	        ISBNLabel.setId(PREFIX_VALIDATION_FORM + "ISBNLabel");
	        TextField ISBNField = new TextField("");
	        ISBNField.setId(PREFIX_VALIDATION_FORM + "ISBNField");
	        Button amazonButton = new Button("Amazon", new ImageView(new Image("validation/Amazon.png")));
	        amazonButton.setId(PREFIX_VALIDATION_FORM + "amazonButton");
	        amazonButton.setOnAction(new EventHandler<ActionEvent>() {
	            @Override public void handle(ActionEvent event) {
	                try {
	                    java.awt.Desktop.getDesktop().browse(new URI("http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Dstripbooks&field-keywords=" + ISBNField.getText()));
	                }
	                catch (Exception e) {     } // ignore
	            }
	        });
	        amazonButton.setDisable(true);
	        pane.getChildren().add(new HBox(6, ISBNLabel,ISBNField, amazonButton)); //, new CC());
	
	        Label IP4Label = new Label("IP4:");
	        IP4Label.setId(PREFIX_VALIDATION_FORM + "IP4Label");
	        TextField IP4Field = new TextField("");
	        IP4Field.setId(PREFIX_VALIDATION_FORM + "IP4Field");
	        pane.getChildren().add(new HBox(IP4Label,IP4Field)); 
	
	        Label intLabel = new Label("Integer:");
	        intLabel.setId(PREFIX_VALIDATION_FORM + "intLabel");
	        TextField intField = new TextField("");
	        intField.setId(PREFIX_VALIDATION_FORM + "intField");
	        pane.getChildren().add(new HBox(intLabel, intField)); 
	
	        Label doubleLabel = new Label("Double:");
	        doubleLabel.setId(PREFIX_VALIDATION_FORM + "doubleLabel");
	        TextField doubleField = new TextField("");
	        doubleField.setId(PREFIX_VALIDATION_FORM + "doubleField");
	        pane.getChildren().add(new HBox(doubleLabel, doubleField)); 
	
	        Label currencyLabel = new Label("Currency:");
	        currencyLabel.setId(PREFIX_VALIDATION_FORM + "currencyLabel");
	        TextField currencyField = new TextField("");
	        currencyField.setId(PREFIX_VALIDATION_FORM + "currencyField");
	        pane.getChildren().add(new HBox(currencyLabel, currencyField)); 
	
	        Label percentLabel = new Label("Percent:");
	        percentLabel.setId(PREFIX_VALIDATION_FORM + "percentLabel");
	        TextField percentField = new TextField("");
	        percentField.setId(PREFIX_VALIDATION_FORM + "percentField");
	        pane.getChildren().add(new HBox(percentLabel,percentField)); 
	
	        Label dateLabel = new Label("Date:");
	        dateLabel.setId(PREFIX_VALIDATION_FORM + "dateLabel");
	        TextField dateField = new TextField("");
	        dateField.setId(PREFIX_VALIDATION_FORM + "dateField");
	        pane.getChildren().add(new HBox(dateLabel, dateField)); 
	
	        Label cardLabel = new Label("Credit Card:");
	        cardLabel.setId(PREFIX_VALIDATION_FORM + "cardLabel");
	        TextField cardField = new TextField("");
	        cardField.setId(PREFIX_VALIDATION_FORM + "cardField");
	        pane.getChildren().add(new HBox(cardLabel, cardField)); //, new CC());
	        ImageView cardImage = new ImageView();
	        cardImage.setId(PREFIX_VALIDATION_FORM + "cardImage");
	        pane.getChildren().add(new HBox(cardImage)); 
	
	        Label dpLabel = new Label("Date Picker: ");
	        dpLabel.setId(PREFIX_VALIDATION_FORM + "dpLabel");
	        DatePicker dpDate = new DatePicker();
	        dpDate.setId(PREFIX_VALIDATION_FORM + "dpDate");
	        AnchorPane anchorPane = new AnchorPane(dpDate);
	        anchorPane.setLeftAnchor(dpDate, 0.0);
	        anchorPane.setRightAnchor(dpDate, 0.0);
	
	        pane.getChildren().add(new HBox(dpLabel,anchorPane));
	
	        Label dpLabelTwo = new Label("Date Picker: ");
	        dpLabelTwo.setId(PREFIX_VALIDATION_FORM + "dpLabelTwo");
	        DatePicker dpDateTwo = new DatePicker();
	        dpDateTwo.setId(PREFIX_VALIDATION_FORM + "dpDateTwo");
	        AnchorPane anchorPaneTwo = new AnchorPane(dpDateTwo);
	        anchorPaneTwo.setLeftAnchor(dpDateTwo, 0.0);
	        anchorPaneTwo.setRightAnchor(dpDateTwo, 0.0);
	
	        pane.getChildren().add(new HBox(dpLabelTwo,anchorPaneTwo)); 
	
	        return pane;
	    }
	
}
