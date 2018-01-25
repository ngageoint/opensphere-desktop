package io.opensphere.xyztile.model;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * A {@link DataTypeInfo} that represents an XYZ tile layer.
 */
public class XYZDataTypeInfo extends DefaultDataTypeInfo
{
    /**
     * Contains information about the layer.
     */
    private final XYZTileLayerInfo myLayerInfo;

    /**
     * Constructs a new XYZ data type.
     *
     * @param tb The system toolbox.
     * @param layerInfo Contains informaton about the XYZ layer this represents.
     */
    public XYZDataTypeInfo(Toolbox tb, XYZTileLayerInfo layerInfo)
    {
        super(tb, layerInfo.getServerUrl(), layerInfo.getServerUrl() + layerInfo.getName(), layerInfo.getName(),
                layerInfo.getDisplayName(), false);
        setUrl(layerInfo.getServerUrl());
        myLayerInfo = layerInfo;
        if (layerInfo.isVisibilitySpecified())
        {
            setVisible(layerInfo.isVisible(), this);
        }
    }

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
        XYZDataTypeInfo other = (XYZDataTypeInfo)obj;
        if (myLayerInfo == null)
        {
            if (other.myLayerInfo != null)
            {
                return false;
            }
        }
        else if (!myLayerInfo.equals(other.myLayerInfo))
        {
            return false;
        }
        return true;
    }

    /**
     * Gets information about the layer.
     *
     * @return The xyz layer information.
     */
    public XYZTileLayerInfo getLayerInfo()
    {
        return myLayerInfo;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myLayerInfo == null ? 0 : myLayerInfo.hashCode());
        return result;
    }
}
