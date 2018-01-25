package io.opensphere.controlpanels.animation.model;

/**
 * The preference of which view to display.
 */
public enum ViewPreference
{
    /** Show whatever was last shown. */
    LAST_SHOWN("Last shown"),

    /** Show the time browser. */
    TIME_BROWSER("Time browser"),

    /** Show the timeline. */
    TIMELINE("Timeline");

    /** The display text. */
    private String myText;

    /**
     * Constructor.
     *
     * @param text the display text
     */
    ViewPreference(String text)
    {
        myText = text;
    }

    @Override
    public String toString()
    {
        return myText;
    }
}
