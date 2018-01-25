package io.opensphere.kml.common.model;

/** Scaling method (for icons and labels). */
public enum ScalingMethod
{
    /** Use the default (global) setting. */
    DEFAULT("Use default setting"),

    /**
     * Keep the icons a fixed screen size when zoomed it, but fix them to the
     * earth past a certain zoom level.
     */
    GOOGLE_EARTH("Google Earth"),

    /** Keep the icons a fixed screen size. */
    FIXED_SIZE("Fixed Size");

    /** The display string. */
    private final String myDisplay;

    /**
     * Constructor.
     *
     * @param display The display string
     */
    ScalingMethod(String display)
    {
        myDisplay = display;
    }

    @Override
    public String toString()
    {
        return myDisplay;
    }
}
