package io.opensphere.core.help.data;

/**
 * This class holds information to register with the help manager.
 */
public class PluginHelpInfo
{
    /** The display name. */
    private String myDisplayName;

    /** The indexing information. */
    private HelpIndex myIndexInfo;

    /** The mapping file information. */
    private HelpMap myMapInfo;

    /** The plugin name. */
    private String myName;

    /** The table of contents information. */
    private HelpTOC myTOCInfo;

    /**
     * Default constructor.
     */
    public PluginHelpInfo()
    {
    }

    /**
     * Standard accessor for the display name.
     *
     * @return The display name.
     */
    public String getDisplayName()
    {
        return myDisplayName;
    }

    /**
     * Standard accessor for the indexing information.
     *
     * @return The help indexing information.
     */
    public HelpIndex getIndexInfo()
    {
        return myIndexInfo;
    }

    /**
     * Standard accessor for help mapping information.
     *
     * @return The help mapping information.
     */
    public HelpMap getMappingInfo()
    {
        return myMapInfo;
    }

    /**
     * Standard accessor for the name.
     *
     * @return The name.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Standard accessor for help table of contents information.
     *
     * @return The help table of contents information.
     */
    public HelpTOC getTOCInfo()
    {
        return myTOCInfo;
    }

    /**
     * Standard mutator for the display name.
     *
     * @param displayName The display name.
     */
    public void setDisplayName(String displayName)
    {
        myDisplayName = displayName;
    }

    /**
     * Standard mutator for help indexing information.
     *
     * @param helpIndex The help indexing information.
     */
    public void setIndexInfo(HelpIndex helpIndex)
    {
        myIndexInfo = helpIndex;
    }

    /**
     * Standard mutator for the help mapping information.
     *
     * @param mapInfo The help mapping information.
     */
    public void setMappingInfo(HelpMap mapInfo)
    {
        myMapInfo = mapInfo;
    }

    /**
     * Standard mutator for the name.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Standard mutator for help table of contents information.
     *
     * @param tocInfo The help table of contents information.
     */
    public void setTOCInfo(HelpTOC tocInfo)
    {
        myTOCInfo = tocInfo;
    }
}
