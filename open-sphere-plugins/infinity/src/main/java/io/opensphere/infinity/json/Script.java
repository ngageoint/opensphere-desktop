package io.opensphere.infinity.json;

import io.opensphere.mantle.infinity.InfinityUtilities;

/**
 * Elasticsearch script JSON bean.
 */
public class Script
{
    /** The scripting language */
    private String myLang;

    /** The script source. */
    private String mySource;

    /**
     * Constructor.
     */
    public Script()
    {
        // Intentionally left blank
    }

    /**
     * Constructor
     *
     * @param field the field binning binned
     * @param dayOfWeek whether dayOfWeek or hourOfDay
     */
    public Script(String field, boolean dayOfWeek)
    {
        myLang = InfinityUtilities.DEFAULT_SCRIPT_LANGUAGE;
        setSource(field, dayOfWeek);
    }

    /**
     * Get the lang.
     *
     * @return the lang
     */
    public String getLang()
    {
        return myLang;
    }

    /**
     * Set the lang.
     *
     * @param lang the lang to set
     */
    public void setLang(String lang)
    {
        myLang = lang;
    }

    /**
     * Get the source.
     *
     * @return the source
     */
    public String getSource()
    {
        return mySource;
    }

    /**
     * Set the source.
     *
     * @param field the field being binned
     * @param dayOfWeek whether dayOfWeek or hourOfDay
     */
    public void setSource(String field, boolean dayOfWeek)
    {
        if(dayOfWeek)
        {
            mySource = new String("doc['" + field + "'].value.dayOfWeek");
        }
        else
        {
            mySource = new String("doc['" + field + "'].value.hourOfDay");
        }
    }

}
