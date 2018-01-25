package io.opensphere.wps.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.response.WPSProcessResult;
import io.opensphere.wps.util.WPSConstants;

/**
 * A WPS-specific extension of the default datatype class.
 */
public class WpsDataTypeInfo extends WFSDataType
{
    /**
     * The source prefix used for all WPS data types.
     */
    public static final String SOURCE_PREFIX = "WPS";

    /**
     * The identifier of the process described by the datatype.
     */
    private String myProcessId;

    /**
     * The process configuration defined by the user.
     */
    private WpsProcessConfiguration myProcessConfiguration;

    /**
     * Creates a new data type, initialized with the supplied parameters.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pSourcePrefix the prefix used for the data source.
     * @param pTypeKey the key used to identify the type.
     * @param pTypeName the system name used to identify the type.
     * @param pDisplayName the display name used to identify the type.
     * @param pMetadata the metadata info wrapper with which the data type is
     *            configured
     */
    public WpsDataTypeInfo(Toolbox pToolbox, String pSourcePrefix, String pTypeKey, String pTypeName, String pDisplayName,
            MetaDataInfo pMetadata)
    {
        super(pToolbox, pSourcePrefix, pTypeKey, pTypeName, pDisplayName,
                ServerToolboxUtils.getServerToolbox(pToolbox).getLayerConfigurationManager().getConfigurationFromName("wfs"),
                false);
        setMantleProps(pMetadata);
        getBasicVisualizationInfo().setSupportedLoadsToTypes(DefaultBasicVisualizationInfo.LOADS_TO_STATIC_AND_TIMELINE);
    }

    /**
     * Populates the WPS Process Result from the describe process server
     * endpoint.
     *
     * @param pLoadsTo the layer type to which data is loaded.
     * @param pResult the WPS result from which data is extracted.
     */
    public void setResult(LoadsTo pLoadsTo, WPSProcessResult pResult)
    {
        initialize(pLoadsTo, pResult);
    }

    /**
     * Auto-Populate the Mantle interface part of this class.
     *
     * @param pLoadsTo the layer type to which data is loaded.
     * @param pResult the WPS result from which data is extracted.
     */
    protected void initialize(LoadsTo pLoadsTo, WPSProcessResult pResult)
    {
        BasicVisualizationInfo basicInfo = new DefaultBasicVisualizationInfo(pLoadsTo, null, true);
        setBasicVisualizationInfo(basicInfo);

        MapVisualizationInfo mapInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS);

        mapInfo.setDataTypeInfo(this);
        setMetaDataInfo(pResult);

        addBoundingBox(findBoundingBox(pResult.getLocations()));
        setTimeExtents(new DefaultTimeExtents(pResult.getTimespan()), this);
    }

    /**
     * Helper method to find the minimum bounding box for a collection of
     * locations.
     *
     * @param pLocations A collection of LatLonAlt locations.
     * @return The geographic bounding box.
     */
    protected GeographicBoundingBox findBoundingBox(Set<LatLonAlt> pLocations)
    {
        List<GeographicPosition> positions = new ArrayList<>(pLocations.size());
        pLocations.forEach(coordinate -> positions.add(new GeographicPosition(coordinate)));
        return GeographicBoundingBox.getMinimumBoundingBox(positions);
    }

    /**
     * Helper method to set the meta data info.
     *
     * @param pResult The WPS result we are working with.
     */
    protected void setMetaDataInfo(WPSProcessResult pResult)
    {
        DefaultMetaDataInfo metaDataInfo = new DefaultMetaDataInfo();
        metaDataInfo.setDataTypeInfo(this);

        // Set common values
        metaDataInfo.addKey(WPSConstants.KEY, this.getClass(), this);
        metaDataInfo.addKey(WPSConstants.TIME_FIELD, String.class, this);
        metaDataInfo.addKey(WPSConstants.LAT, Double.class, this);
        metaDataInfo.addKey(WPSConstants.LON, Double.class, this);

        // Set optional values
        if (!pResult.getFeatures().isEmpty())
        {
            // The properties are all be the same for each feature, no need to
            // go through all of them (just look at first one).
            pResult.getFeatures().iterator().next().getProperties()
                    .forEach((key, value) -> metaDataInfo.addKey(key, String.class, this));
        }
        metaDataInfo.copyKeysToOriginalKeys();

        setMetaDataInfo(metaDataInfo);
    }

    /**
     * Sets the value of the {@link #myProcessId} field.
     *
     * @param pProcessId the value to store in the {@link #myProcessId} field.
     */
    public void setProcessId(String pProcessId)
    {
        myProcessId = pProcessId;
    }

    /**
     * Gets the value of the {@link #myProcessId} field.
     *
     * @return the value stored in the {@link #myProcessId} field.
     */
    public String getProcessId()
    {
        return myProcessId;
    }

    /**
     * Sets the value of the {@link #myProcessConfiguration} field.
     *
     * @param pProcessConfiguration the value to store in the
     *            {@link #myProcessConfiguration} field.
     */
    public void setProcessConfiguration(WpsProcessConfiguration pProcessConfiguration)
    {
        myProcessConfiguration = pProcessConfiguration;
    }

    /**
     * Gets the value of the {@link #myProcessConfiguration} field.
     *
     * @return the value stored in the {@link #myProcessConfiguration} field.
     */
    public WpsProcessConfiguration getProcessConfiguration()
    {
        return myProcessConfiguration;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myProcessId == null ? 0 : myProcessId.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        WpsDataTypeInfo other = (WpsDataTypeInfo)obj;
        if (myProcessId == null)
        {
            if (other.myProcessId != null)
            {
                return false;
            }
        }
        else if (!myProcessId.equals(other.myProcessId))
        {
            return false;
        }
        return true;
    }
}
