package io.opensphere.kml.common.util;

import io.opensphere.core.Toolbox;

/**
 * An pure evil way to get at the toolbox and KML toolbox.
 */
public final class KMLToolboxUtils
{
    /** The toolbox. */
    private static volatile Toolbox ourToolbox;

    /** The KML toolbox. */
    private static volatile KMLToolbox ourKMLToolbox;

    /**
     * Gets the KML toolbox.
     *
     * @return the KML toolbox
     */
    public static KMLToolbox getKmlToolbox()
    {
        return ourKMLToolbox;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public static Toolbox getToolbox()
    {
        return ourToolbox;
    }

    /**
     * Sets the toolboxes.
     *
     * @param toolbox The toolbox
     * @param kmlToolbox The KML toolbox
     */
    public static void set(Toolbox toolbox, KMLToolbox kmlToolbox)
    {
        ourToolbox = toolbox;
        ourKMLToolbox = kmlToolbox;
    }

    /**
     * Private Constructor.
     */
    private KMLToolboxUtils()
    {
    }
}
