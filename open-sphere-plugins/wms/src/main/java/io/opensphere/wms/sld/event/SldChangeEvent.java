package io.opensphere.wms.sld.event;

import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * This class encompasses the set of events that can be generated when using the
 * SLD tool.
 */
public class SldChangeEvent
{
    /** The Source. */
    private final Object mySource;

    /** The Sld. */
    private final StyledLayerDescriptor mySld;

    /** The name of the SLD. */
    private final String mySldName;

    /** The Layer key. */
    private final String myLayerKey;

    /**
     * Instantiates a new sld change event.
     *
     * @param source the source
     * @param layerKey the layer key
     * @param sldName the sld name
     * @param sld the sld
     */
    public SldChangeEvent(Object source, String layerKey, String sldName, StyledLayerDescriptor sld)
    {
        mySource = source;
        mySld = sld;
        mySldName = sldName;
        myLayerKey = layerKey;
    }

    /**
     * Gets the layer key.
     *
     * @return the layer key
     */
    public String getLayerKey()
    {
        return myLayerKey;
    }

    /**
     * Gets the sld.
     *
     * @return the sld
     */
    public StyledLayerDescriptor getSld()
    {
        return mySld;
    }

    /**
     * Gets the name of the affected {@link StyledLayerDescriptor}.
     *
     * @return the SLD name
     */
    public String getSldName()
    {
        return mySldName;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public Object getSource()
    {
        return mySource;
    }
}
