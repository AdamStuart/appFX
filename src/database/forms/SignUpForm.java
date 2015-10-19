/*
 * Derived from:
 * @(#)ValidationDemo.java 5/19/2013
 *
 * Copyright 2002 - 2013 JIDE Software Inc. All rights reserved.
 */


package database.forms;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SignUpForm extends VBox
{
	Region root;
	   public Region getRegion(){ return root;}
    public static final String PREFIX_SIGNUP_FORM = "SignUpForm";
    
   public SignUpForm()
    {
    	super(8);
    	root = createSignUpForm();
//    	root = installValidatorsForSignUpForm(createSignUpForm());
        getChildren().add(root);
//        root.setBorder(Borders.redBorder);
    }
    
   
   Region createSignUpForm() 
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
//        emailLabel.setBorder(value);
        pane.getChildren().add(new HBox(emailLabel, emailField));
        pane.getChildren().add(new HBox(confirmEmailLabel, confirmEmailField));
        pane.getChildren().add(new HBox(countryLabel,countryChoiceBox));
        pane.getChildren().add(new HBox(zipCodeLabel, zipCodeField));
        pane.getChildren().add(new HBox(passwordLabel,passwordField));
        pane.getChildren().add(new HBox(confirmPasswordLabel, confirmPasswordField));
        pane.getChildren().add(new HBox(agreeCheckBox,signUpButton));
 
        return pane;
    }
//
//    private Region installValidatorsForSignUpForm(final Region pane) {
//        String prefix = PREFIX_SIGNUP_FORM;
//
//        TextField emailField = (TextField) pane.lookup("#" + prefix + "emailField");
//        TextField confirmEmailField = (TextField) pane.lookup("#" + prefix + "confirmEmailField");
//        SimpleValidator emailValidator = new SimpleValidator(EmailValidator.getInstance()) {
//            @Override
//            public ValidationEvent call(ValidationObject param) {
//                ValidationEvent event = super.call(param);
//                if (!ValidationEvent.VALIDATION_OK.equals(event.getEventType()) || emailField.getText().equals(confirmEmailField.getText())) {
//                    return event;
//                } else {
//                    return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Two emails are not the same!");
//                }
//            }
//        };
//        ValidationUtils.install(emailField, new SimpleValidator(EmailValidator.getInstance()));
//        ValidationUtils.install(emailField, new Validator() {
//            @Override
//            public ValidationEvent call(ValidationObject param) {
//                if (emailField.getText().trim().length() == 0) {
//                    return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "The email cannot be empty!");
//                } else return new SimpleValidator(EmailValidator.getInstance()).call(param);
//            }
//        }, ValidationMode.ON_DEMAND);
//        ValidationUtils.install(confirmEmailField, emailValidator);
//
//        Validator countryValidator = new Validator() {
//            @Override public ValidationEvent call(ValidationObject param) {
//                if ("United States".equals(param.getNewValue()))       return ValidationEvent.OK;
//                if (param.getNewValue() == null)          
//                	return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "Please select a country!");
//                return new ValidationEvent(ValidationEvent.VALIDATION_WARNING, 0, "We only support signing up in United States at the moment!");
//            }
//        };
//        ChoiceBox<String> countryChoiceBox = (ChoiceBox<String>) pane.lookup("#" + prefix + "countryChoiceBox");
//        ValidationUtils.install(countryChoiceBox, countryValidator, ValidationMode.ON_DEMAND);
//        ValidationUtils.install(countryChoiceBox, countryValidator, ValidationMode.ON_FLY);
//
//        TextField passwordField = (TextField) pane.lookup("#" + prefix + "passwordField");
//        TextField confirmPasswordField = (TextField) pane.lookup("#" + prefix + "confirmPasswordField");
//        Validator passwordEmptyValidator = new Validator() {
//            @Override public ValidationEvent call(ValidationObject param) {
//                if (passwordField.getText().trim().length() == 0) {
//                    return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "The password cannot be empty!");
//                } else return ValidationEvent.OK;
//            }
//        };
//        ValidationUtils.install(passwordField, passwordEmptyValidator, ValidationMode.ON_DEMAND);
//        ValidationUtils.install(passwordField, passwordEmptyValidator, ValidationMode.ON_FLY);
//
//        ValidationUtils.install(confirmPasswordField, new Validator() {
//            @Override public ValidationEvent call(ValidationObject param) {
//                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
//                    return new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, "The two password are different!");
//                } else return ValidationEvent.OK;
//            }
//        }, ValidationMode.ON_FOCUS_LOST);
//
//        Button signUpButton = (Button) pane.lookup("#" + prefix + "signUpButton");
//        signUpButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(ActionEvent event) {  ValidationUtils.validateOnDemand(pane);  }
//        });
//
//        ValidationGroup validationGroup = new ValidationGroup(emailField, passwordField);
//        signUpButton.disableProperty().bind(validationGroup.invalidProperty());
//
//        validationGroup.invalidProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println(newValue);
//        });
//
//        ValidationUtils.forceValidate(emailField, ValidationMode.ON_FLY);
//        ValidationUtils.forceValidate(passwordField, ValidationMode.ON_FLY);
//
//        // add a large help icon to the sign-up button
//        Label helpLabel = new Label("", new ImageView(new Image("/forms/images/help.png")));
//        DecorationUtils.install(signUpButton, new Decorator<>(helpLabel, Pos.CENTER_RIGHT, new Point2D(100, 0)));
//        helpLabel.setTooltip(new Tooltip("This is a demo to show you how to do validation involving two fields. Both email and password fields" +
//                "\ninvolves validation to ensure both value are the same. In addition to that, it also shows you how to use" +
//                "\nEmailValidator to check for a valid email address. The country choice box will show a validation warning" +
//                "\nif the selected country is not United States."));
//
//        return new DecorationPane(pane);
//    }

}
