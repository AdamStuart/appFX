/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package diagrams.grapheditor.core.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diagrams.grapheditor.GConnectorValidator;
import diagrams.grapheditor.core.utils.LogMessages;
import diagrams.grapheditor.core.validators.defaults.DefaultConnectorValidator;

/**
 * Responsible for instantiating validators.
 *
 * <p>
 * Stores any custom validators that have been set. If a custom validator class has been set, it will be used. Otherwise
 * the default validator will be created.
 * </p>
 */
public class ValidatorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorFactory.class);

    private Class<? extends GConnectorValidator> connectorValidator;

    /**
     * Sets the custom connector validator class
     *
     * @param connectorValidator a custom validator class that implements {@link GConnectorValidator}
     */
    public void setConnectorValidator(final Class<? extends GConnectorValidator> connectorValidator) {
        this.connectorValidator = connectorValidator;
    }

    /**
     * Creates a new connector validator instance.
     *
     * @return a new {@link GConnectorValidator} instance
     */
    public GConnectorValidator createConnectorValidator() {

        if (connectorValidator == null) {
            return new DefaultConnectorValidator();
        } else {
            try {
                return connectorValidator.getConstructor().newInstance();
            } catch (final ReflectiveOperationException e) {
                LOGGER.error(LogMessages.CANNOT_INSTANTIATE_VALIDATOR, connectorValidator.getName());
                return new DefaultConnectorValidator();
            }
        }
    }
}
