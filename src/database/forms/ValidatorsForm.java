/*
 * Derived from:
 * @(#)ValidationDemo.java 5/19/2013
 *
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */
package database.forms;

import java.util.List;
import java.util.Optional;

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

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import util.FormsUtil;
import util.FormsUtil.ValidationType;
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

	    
    public static final String PATTERN_IP4 = "\\b(([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\b";

    public static Region createValidationForm() {

    	VBox pane = new VBox(12);
    	pane.setSpacing(8);
    	pane.setPadding(new Insets(8));

        pane.getChildren().add(FormsUtil.makeEmailBox()); 
        pane.getChildren().add(FormsUtil.makeURLBox()); 
        pane.getChildren().add(FormsUtil.makeISBNBox(PREFIX_VALIDATION_FORM)); 
        
        Region rgn1 = FormsUtil.makeValidatedBox("IP4:", PREFIX_VALIDATION_FORM + "IP4", ValidationType.IP4);
        Region rgn2 = FormsUtil.makeValidatedBox("Integer:", PREFIX_VALIDATION_FORM + "int", ValidationType.INT);
        Region rgn3 = FormsUtil.makeValidatedBox("Double:", PREFIX_VALIDATION_FORM + "double", ValidationType.DOUBLE);
        Region rgn4 = FormsUtil.makeValidatedBox("Currency:", PREFIX_VALIDATION_FORM + "currency", ValidationType.CURRENCY);
        Region rgn5 = FormsUtil.makeValidatedBox("Percent:", PREFIX_VALIDATION_FORM + "percent", ValidationType.PERCENT);
        Region rgn6 = FormsUtil.makeValidatedBox("Date:", PREFIX_VALIDATION_FORM + "date", ValidationType.DATE);
        Region rgn7 = FormsUtil.makeValidatedBox("CC:", PREFIX_VALIDATION_FORM + "cc", ValidationType.CREDITCARD);
        pane.getChildren().addAll(rgn1, rgn2, rgn3, rgn4, rgn5, rgn6, rgn7);
        return pane;
    }
//------------------------------------------------------

	
	
   private Region installValidatorsForValidationForm(Region pane) {
        String prefix = PREFIX_VALIDATION_FORM;
        
        
        TextField email = (TextField) pane.lookup("#" + prefix + "emailField");
        ValidationUtils.install(email, new SimpleValidator(EmailValidator.getInstance()), ValidationMode.ON_FLY);

        TextField urlField = (TextField) pane.lookup("#" + prefix + "urlField");
        ValidationUtils.install(urlField, new SimpleValidator(UrlValidator.getInstance()), ValidationMode.ON_FLY);
        urlField.addEventHandler(ValidationEvent.ANY, event-> {
                pane.lookup("#" + prefix + "urlButton").setDisable(event.getEventType() != ValidationEvent.VALIDATION_OK);
        });

        TextField ISBNField = (TextField) pane.lookup("#" + prefix + "ISBNField");
        ValidationUtils.install(ISBNField, new SimpleValidator(ISBNValidator.getInstance()), ValidationMode.ON_FLY);
        ISBNField.addEventHandler(ValidationEvent.ANY, event-> {
                pane.lookup("#" + prefix + "amazonButton").setDisable(event.getEventType() != ValidationEvent.VALIDATION_OK);
        });

        ValidationUtils.install(pane.lookup("#" + prefix + "IP4Field"), new SimpleValidator(new RegexValidator(PATTERN_IP4)), ValidationMode.ON_FLY);

        ValidationUtils.install(pane.lookup("#" + prefix + "intField"), new SimpleValidator(IntegerValidator.getInstance()), ValidationMode.ON_FLY);
        ValidationUtils.install(pane.lookup("#" + prefix + "doubleField"), new SimpleValidator(DoubleValidator.getInstance()), ValidationMode.ON_FLY);

        ValidationUtils.install(pane.lookup("#" + prefix + "currencyField"), new SimpleValidator(CurrencyValidator.getInstance()), ValidationMode.ON_FLY);
        ValidationUtils.install(pane.lookup("#" + prefix + "percentField"), new SimpleValidator(PercentValidator.getInstance()), ValidationMode.ON_FLY);
        ValidationUtils.install(pane.lookup("#" + prefix + "dateField"), new SimpleValidator(DateValidator.getInstance()), ValidationMode.ON_FLY);

        installCCValidator(pane, prefix);
        final DatePicker dpDate = (DatePicker) pane.lookup("#" + prefix + "dpDate");
        final DatePicker dpDateTwo = (DatePicker) pane.lookup("#" + prefix + "dpDateTwo");

//        Validator validator = param -> dpDate.getValue() != null && dpDateTwo.getValue() != null &&
//                dpDate.getValue().isBefore(dpDateTwo.getValue()) &&
//                dpDateTwo.getValue().isAfter(dpDate.getValue())
//                ?
//                new ValidationEvent(ValidationEvent.VALIDATION_OK) :
//                new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Date must follow start date");

//        ValidationUtils.install(dpDate, validator, ValidationMode.ON_FLY);
//        ValidationUtils.install(dpDateTwo, dpDate.valueProperty(), validator, ValidationMode.ON_FLY,
//                ValidationUtils.createDefaultValidationEventHandler(dpDateTwo, ValidationDecorators::fontAwesomeDecoratorCreator));
//
//        Consumer<Object> validateDates = o -> {
//            ValidationUtils.forceValidate(dpDate, ValidationMode.ON_FLY);
//            ValidationUtils.forceValidate(dpDateTwo, ValidationMode.ON_FLY);
//        };
//
//        ValidationDecorators.installRequiredDecorator(dpDate, ValidationDecorators::graphicRequiredCreator);
//        dpDate.valueProperty().addListener((observable1, oldValue1, newValue1) -> validateDates.accept(null));
//        dpDateTwo.valueProperty().addListener((observable1, oldValue1, newValue1) -> validateDates.accept(null));

//        validateDates.accept(null);

        Label validationLabel = null;
        Optional<List<Decorator>> ovalidationDecorators = ValidationUtils.getValidationDecorators(dpDate);
        if (ovalidationDecorators.isPresent()) {
            Optional<Node> nodeOptional = Optional.of(ovalidationDecorators.get().get(0).getNode());
            if (nodeOptional.isPresent() && (nodeOptional.get() instanceof Label)) 
                validationLabel = (Label) nodeOptional.get();
        }

        final Label finalValidationLabel = validationLabel;
        // Show Validation tooltip on date picker focus
        dpDate.focusedProperty().addListener((ob, old, v) -> {  if (v) {   ValidationUtils.showTooltip(finalValidationLabel);  }  });

        dpDate.addEventHandler(ValidationEvent.VALIDATION_ERROR, event -> {    ValidationUtils.showTooltip(finalValidationLabel);     });
        pane.addEventHandler(ValidationEvent.ANY, event -> {         System.out.println(event);      });

        return new DecorationPane(pane);
    }
  //------------------------------------------------------

    
   void installCCValidator(Region pane, String prefix)
    {
        TextField cardField = (TextField) pane.lookup("#" + prefix + "cardField");
        ImageView cardImage = (ImageView) pane.lookup("#" + prefix + "cardImage");
        String imagePath = "/database/forms/images/";
        ValidationUtils.install(cardField, new SimpleValidator(new CreditCardValidator(CreditCardValidator.AMEX | CreditCardValidator.VISA | CreditCardValidator.MASTERCARD + CreditCardValidator.DISCOVER | CreditCardValidator.DINERS)), ValidationMode.ON_FLY);
        cardField.addEventHandler(ValidationEvent.ANY, event -> {
                if (event.getEventType() == ValidationEvent.VALIDATION_OK) 
                	cardImage.setImage(new Image(imagePath + getCCImageName(cardField.getText())));
        	});
    }
   private String getCCImageName(String text)
   {
	    if (new CreditCardValidator(CreditCardValidator.VISA).isValid(text))   return "VISA.png";
	    if (new CreditCardValidator(CreditCardValidator.MASTERCARD).isValid(text))   return "MasterCard.png";
	    if (new CreditCardValidator(CreditCardValidator.AMEX).isValid(text))   return "AMEX.png";
	    if (new CreditCardValidator(CreditCardValidator.DISCOVER).isValid(text))   return "Discover.png";
	    if (new CreditCardValidator(CreditCardValidator.DINERS).isValid(text))   return "DinersClub.png";
	      return "";  
   }

}
