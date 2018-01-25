package io.opensphere.analysis.base.model;

/** The type of label enum. */
public enum LabelType
{
    /** Title. */
    TITLE("Title"),

    /** Axis label. */
    AXIS("Axis"),

    /** Category axis label. */
    AXIS_CATEGORY("Category"),

    /** Count axis label. */
    AXIS_COUNT("Count");

    /** The display text. */
    private final String myText;

    /**
     * Constructor.
     *
     * @param text the display text
     */
    private LabelType(String text)
    {
        myText = text;
    }

    @Override
    public String toString()
    {
        return myText;
    }
}
