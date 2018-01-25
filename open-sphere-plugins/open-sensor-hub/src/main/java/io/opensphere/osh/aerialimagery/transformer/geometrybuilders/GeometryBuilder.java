package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import java.util.List;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Interface to a builder that builds specific geometries representing data in a
 * specifed model.
 */
public interface GeometryBuilder
{
    /**
     * Builds the geometries to display on the globe that represents the data in
     * the model.
     *
     * @param model The model to build geometries for.
     * @param uavDataType The uav layer the metadata belongs to.
     * @param videoLayer The uav video layer.
     * @return The geometries representing the model.
     */
    Pair<List<Geometry>, List<Geometry>> buildGeometries(PlatformMetadata model, DataTypeInfo uavDataType,
            DataTypeInfo videoLayer);

    /**
     * Indicates if the geometries built by this buider should be cached once
     * published.
     *
     * @return True if they should be cached and not built again.
     */
    boolean cachePublishedGeometries();
}
