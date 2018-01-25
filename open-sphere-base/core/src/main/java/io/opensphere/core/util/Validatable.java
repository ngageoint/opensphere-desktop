package io.opensphere.core.util;

/**
 * Interface for an object that provides a validator support.
 */
@FunctionalInterface
public interface Validatable
{
    /**
     * Gets the validator support.
     *
     * @return The validator support.
     */
    ValidatorSupport getValidatorSupport();
}
