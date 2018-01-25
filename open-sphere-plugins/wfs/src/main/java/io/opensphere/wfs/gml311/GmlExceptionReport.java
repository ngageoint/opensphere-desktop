package io.opensphere.wfs.gml311;

import org.apache.commons.lang.StringUtils;

/**
 * A simple report, in which an exception received from a remote GML-providing service is encapsulated. The report contains both
 * an exception code, and a message describing the text.
 */
public class GmlExceptionReport
{
    /**
     * The code received from the remote system, used to categorize the failure.
     */
    private String myCode;

    /**
     * The text describing the failure.
     */
    private String myText;

    /**
     * Gets the value of the {@link #myCode} field.
     *
     * @return the value stored in the {@link #myCode} field.
     */
    public String getCode()
    {
        return myCode;
    }

    /**
     * Sets the value of the {@link #myCode} field.
     *
     * @param pCode the value to store in the {@link #myCode} field.
     */
    public void setCode(String pCode)
    {
        myCode = pCode;
    }

    /**
     * Gets the value of the {@link #myText} field.
     *
     * @return the value stored in the {@link #myText} field.
     */
    public String getText()
    {
        return myText;
    }

    /**
     * Sets the value of the {@link #myText} field.
     *
     * @param pText the value to store in the {@link #myText} field.
     */
    public void setText(String pText)
    {
        myText = pText;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (StringUtils.isNotBlank(getCode()))
        {
            return getCode() + ':' + getText();
        }
        return getText();
    }
}
