package io.opensphere.core.english;

import org.apache.commons.text.WordUtils;

/** An English noun. */
public class Noun extends Word
{
    /** The plural version of the word. */
    private final String myPlural;

    /**
     * Constructor.
     *
     * @param singular the singular version of the word
     */
    public Noun(String singular)
    {
        this(singular, singular + "s");
    }

    /**
     * Constructor.
     *
     * @param singular the singular version of the word
     * @param plural the plural version of the word
     */
    public Noun(String singular, String plural)
    {
        super(singular);
        myPlural = plural;
    }

    /**
     * Returns the plural version of the word.
     *
     * @return the plural version of the word
     */
    public String plural()
    {
        return myPlural;
    }

    /**
     * Returns the plural title case version of the word.
     *
     * @return the plural title case version of the word
     */
    public String pluralTitleCase()
    {
        return WordUtils.capitalize(myPlural);
    }
}
