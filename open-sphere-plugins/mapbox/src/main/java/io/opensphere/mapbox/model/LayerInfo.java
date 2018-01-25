package io.opensphere.mapbox.model;

/** Mapbox layer info. */
public class LayerInfo
{
    /** The name. */
    private final String myName;

    /** The display name. */
    private final String myDisplayName;

    /**
     * Constructor.
     *
     * @param name The name
     * @param displayName The display name
     */
    public LayerInfo(String name, String displayName)
    {
        myName = name;
        myDisplayName = displayName;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName()
    {
        return myDisplayName;
    }

    @Override
    public String toString()
    {
        return "LayerInfo [myName=" + myName + ", myDisplayName=" + myDisplayName + "]";
    }
}
