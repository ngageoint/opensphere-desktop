package io.opensphere.mantle.data.geom.factory;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Interface that defines a converter that will transform a
 * {@link MapGeometrySupport} implementer into a {@link Geometry}.
 */
public interface MapGeometrySupportToGeometryConverter
{
    /**
     * Converts the provided {@link MapGeometrySupport} to a {@link Geometry}.
     *
     * @param geomSupport the geometry support to convert
     * @param id the id - the id of the element in the registry
     * @param dti the {@link DataTypeInfo}
     * @param vs - the {@link VisualizationState}
     * @param renderPropertyPool the render property pool
     * @return the geometry resultant geometry.
     * @throws IllegalArgumentException if the geomSupport is not the same as
     *             the class ( or extension of the class) returned by
     *             getConvertedClassType.
     */
    AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti, VisualizationState vs,
            RenderPropertyPool renderPropertyPool) throws IllegalArgumentException;

    /**
     * Gets the class for which this converter will make conversions from
     * something that implements MapGeometrySupport to a Geometry.
     *
     * @return the converted class type
     */
    Class<?> getConvertedClassType();

    /**
     * Gets the Toolbox.
     *
     * @return the toolbox
     */
    Toolbox getToolbox();
}
