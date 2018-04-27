package io.opensphere.wps.ui.detail.validator;

import org.apache.commons.lang3.StringUtils;

/**
 * An implementation of a validator in which text must be supplied to be valid.
 * Blank or empty text is considered invalid.
 */
public class RequiredTextExaminer implements ValueExaminer
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.ui.detail.validator.ValueExaminer#isValid(java.lang.String)
     */
    @Override
    public boolean isValid(String pValue)
    {
        return StringUtils.isNotBlank(pValue);
    }
}
