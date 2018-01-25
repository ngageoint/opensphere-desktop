package io.opensphere.core.util.swing.input.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * The model used to edit dates.
 */
public class DateModel extends GhostTextModel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The format of the date string to store in the model.
     */
    private final SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Constructor.
     *
     * Normally view models don't initialize to a particular value, but since
     * the date picker starts with a default time, we need to take control of
     * the model here.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DateModel()
    {
        super.set(myFormat.format(new Date()));
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    public SimpleDateFormat getFormat()
    {
        return myFormat;
    }

    @Override
    public boolean set(String value)
    {
        return super.set(isDateValid(value) ? value : myFormat.format(new Date()));
    }

    /**
     * Determines if the given string is a valid date string.
     *
     * @param s the string
     * @return whether it's valid
     */
    private boolean isDateValid(String s)
    {
        boolean isValid = false;
        if (StringUtils.isNotEmpty(s))
        {
            try
            {
                myFormat.parse(s);
                isValid = true;
            }
            catch (ParseException e)
            {
                isValid = false;
            }
        }
        return isValid;
    }
}
