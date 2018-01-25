package io.opensphere.core.util;

/**
 * The different validation results.
 *
 */
public enum ValidationStatus
{
    /**
     * Validation was successful.
     */
    VALID,

    /**
     * Validation was successful but with warnings.
     */
    WARNING,

    /**
     * Validation was unsuccessful.
     */
    ERROR;
}
