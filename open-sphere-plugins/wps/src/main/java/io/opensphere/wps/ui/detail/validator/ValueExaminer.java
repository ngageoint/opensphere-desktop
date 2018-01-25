package io.opensphere.wps.ui.detail.validator;

/**
 * A contract for a simple value tester class.
 */
public interface ValueExaminer
{
    /**
     * Tests to determine if the supplied value meets the validation requirements of the implementation.
     *
     * @param pValue the value to verify.
     * @return true if the supplied value is acceptable, false otherwise.
     */
    boolean isValid(String pValue);
}
