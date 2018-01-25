package io.opensphere.kml.mantle.controller;

import io.opensphere.core.Toolbox;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * The Class KMLDataGroupInfo.
 */
public class KMLDataGroupInfo extends DefaultDataGroupInfo
{
    /** The KML data source. */
    private KMLDataSource myKMLDataSource;

    /**
     * Instantiates a new kML data group info.
     *
     * @param rootNode the root node
     * @param aToolbox the a toolbox
     * @param providerType the provider type
     * @param id the id
     * @param displayName the display name
     */
    public KMLDataGroupInfo(boolean rootNode, Toolbox aToolbox, String providerType, String id, String displayName)
    {
        super(rootNode, aToolbox, providerType, id, displayName);
    }

    /**
     * Gets the kML data source.
     *
     * @return the kML data source
     */
    public KMLDataSource getKMLDataSource()
    {
        return myKMLDataSource;
    }

    /**
     * Sets the kML data source.
     *
     * @param kMLDataSource the new kML data source
     */
    public void setKMLDataSource(KMLDataSource kMLDataSource)
    {
        myKMLDataSource = kMLDataSource;
    }
}
