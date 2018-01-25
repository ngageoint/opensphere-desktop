package io.opensphere.osh.model;

import java.util.Collection;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/** OpenSensorHub data group. */
public class OSHDataGroupInfo extends DefaultDataGroupInfo
{
    /** The offering. */
    private final Offering myOffering;

    /** The server URL. */
    private final String myUrl;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param providerType the provider type
     * @param url the server URL
     * @param offering the Offering offering
     */
    public OSHDataGroupInfo(Toolbox toolbox, String providerType, String url, Offering offering)
    {
        super(false, toolbox, providerType, StringUtilities.concat(url, "/", offering.getId()), offering.getName());
        myUrl = url;
        myOffering = offering;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Gets the offering.
     *
     * @return the offering
     */
    public Offering getOffering()
    {
        return myOffering;
    }

    /**
     * Gets the data types under this group.
     *
     * @return the data types
     */
    public Collection<OSHDataTypeInfo> getDataTypes()
    {
        return CollectionUtilities.filterDowncast(getMembers(false), OSHDataTypeInfo.class);
    }
}
