package io.opensphere.shapefile;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class ShapeFileDataTypeInfo.
 */
public class ShapeFileDataTypeInfo extends DefaultDataTypeInfo
{
    /** The my file source. */
    private final ShapeFileSource myFileSource;

    /**
     * Constructor with key, name, and display name.
     *
     * @param tb - the {@link Toolbox}
     * @param fileSource the file source
     * @param sourcePrefix the source prefix
     * @param typeKey - the type key
     * @param typeName - the type name
     * @param displayName - the display name.
     * @param providerFiltersMetaData - true if the provider of this DataType is
     *            capable of and takes responsibility for filtering metadata
     *            using the {@link DataFilterRegistry} in the core
     *            {@link Toolbox}. If false the Mantle layer will provide the
     *            filtering for data at insert time.
     */
    public ShapeFileDataTypeInfo(Toolbox tb, ShapeFileSource fileSource, String sourcePrefix, String typeKey, String typeName,
            String displayName, boolean providerFiltersMetaData)
    {
        super(tb, sourcePrefix, typeKey, typeName, displayName, providerFiltersMetaData);
        myFileSource = fileSource;
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    /**
     * Gets the file source.
     *
     * @return the file source
     */
    public ShapeFileSource getFileSource()
    {
        return myFileSource;
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public int hashCode()
    {
        return super.hashCode();
    }
}
