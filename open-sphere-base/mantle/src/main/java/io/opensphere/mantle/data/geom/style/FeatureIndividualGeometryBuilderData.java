package io.opensphere.mantle.data.geom.style;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Interface FeatureIndividualGeometryBuilderData.
 *
 * Provides data for individual geometries that are built by
 * {@link FeatureVisualizationStyle}s.
 */
public interface FeatureIndividualGeometryBuilderData
{
    /**
     * Gets the {@link DataTypeInfo}.
     *
     * @return the data type
     */
    DataTypeInfo getDataType();

    /**
     * Gets the element id.
     *
     * @return the element id
     */
    long getElementId();

    /**
     * Gets the geometry id.
     *
     * @return the geometry id
     */
    long getGeomId();

    /**
     * Gets the {@link MetaDataProvider}.
     *
     * @return the {@link MetaDataProvider}
     */
    MetaDataProvider getMDP();

    /**
     * Gets the {@link MapGeometrySupport}.
     *
     * @return the {@link MapGeometrySupport}
     */
    MapGeometrySupport getMGS();

    /**
     * Gets the {@link VisualizationState}.
     *
     * @return the {@link VisualizationState}
     */
    VisualizationState getVS();
}
