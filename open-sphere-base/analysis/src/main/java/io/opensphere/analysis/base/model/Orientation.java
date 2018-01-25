package io.opensphere.analysis.base.model;

/** Chart orientation enum. */
public enum Orientation
{
    /** Vertical. */
    VERTICAL("Vertical"),

    /** Horizontal. */
    HORIZONTAL("Horizontal");

    /** The display text. */
    private final String myText;

    /**
     * Constructor.
     *
     * @param text The display text
     */
    private Orientation(String text)
    {
        myText = text;
    }

    @Override
    public String toString()
    {
        return myText;
    }
}
