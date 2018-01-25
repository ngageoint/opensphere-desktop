package io.opensphere.geopackage.mantle;

import io.opensphere.core.Toolbox;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * A {@link DataTypeInfo} for a GeoPackage layer that will contain
 * {@link GeoPackageLayer}.
 */
public class GeoPackageDataTypeInfo extends DefaultDataTypeInfo
{
    /**
     * The layer this data type represents.
     */
    private final GeoPackageLayer myLayer;

    /**
     * Constructs a new data type info.
     *
     * @param tb The system toolbox.
     * @param layer The layer this data type represents.
     * @param typeKey The unique key for this type.
     */
    public GeoPackageDataTypeInfo(Toolbox tb, GeoPackageLayer layer, String typeKey)
    {
        super(tb, layer.getPackageFile(), typeKey, layer.getName(), layer.getName(), false);
        myLayer = layer;
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
        GeoPackageDataTypeInfo other = (GeoPackageDataTypeInfo)obj;
        if (myLayer == null)
        {
            if (other.myLayer != null)
            {
                return false;
            }
        }
        else if (!myLayer.equals(other.myLayer))
        {
            return false;
        }
        return true;
    }

    /**
     * Gets the {@link GeoPackageLayer} this data type represents.
     *
     * @return The {@link GeoPackageLayer}.
     */
    public GeoPackageLayer getLayer()
    {
        return myLayer;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myLayer == null ? 0 : myLayer.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return myLayer.getName();
    }
}
