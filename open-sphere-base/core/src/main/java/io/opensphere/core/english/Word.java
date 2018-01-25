package io.opensphere.core.english;

import org.apache.commons.lang.WordUtils;

/** An English word. */
public class Word
{
    /** The word. */
    private final String myWord;

    /**
     * Constructor.
     *
     * @param word the word
     */
    public Word(String word)
    {
        myWord = word;
    }

    /**
     * Returns the normal case version of the word.
     *
     * @return the normal case version of the word
     */
    public String normalCase()
    {
        return myWord;
    }

    /**
     * Returns the title case version of the word.
     *
     * @return the title case version of the word
     */
    public String titleCase()
    {
        return WordUtils.capitalize(myWord);
    }

    @Override
    public String toString()
    {
        return myWord;
    }
}
