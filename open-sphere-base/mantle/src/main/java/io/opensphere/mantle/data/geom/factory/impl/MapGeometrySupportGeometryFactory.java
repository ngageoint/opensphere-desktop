package io.opensphere.mantle.data.geom.factory.impl;

import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportToGeometryConverter;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;

/**
 * A factory for creating {@link Geometry} objects from
 * {@link MapGeometrySupport}.
 */
public class MapGeometrySupportGeometryFactory
{
    /** The converter registry. */
    private final MapGeometrySupportConverterRegistry myConverterRegistry;

    /**
     * Instantiates a new map geometry support geometry factory.
     *
     * @param mtb the MantleToolbox
     */
    public MapGeometrySupportGeometryFactory(MantleToolbox mtb)
    {
        myConverterRegistry = mtb.getMapGeometrySupportConverterRegistry();
    }

    /**
     * Given a MapGeometrySupport creates a set that contains all the.
     *
     * @param mgs the {@link MapGeometrySupport} to transform
     * @param id the id for the AbstractRenderableGeometry
     * @param dti the {@link DataTypeInfo}
     * @param visState - the additional visualization state information
     * @param renderPropertyPool the render property pool
     * @return the set that are created from the MapGeometrySupport
     *         {@link AbstractRenderableGeometry} that comprise it and its
     *         children.
     */
    public Set<Geometry> createGeometries(MapGeometrySupport mgs, long id, DataTypeInfo dti, VisualizationState visState,
            RenderPropertyPool renderPropertyPool)
    {
        return createGeometries(new HashSet<Geometry>(), mgs, id, dti, visState, renderPropertyPool);
    }

    /**
     * Given a MapGeometrySupport creates a set that contains all the.
     *
     * @param addToSet if not null all geometries will be added to this set,
     *            which will also be the returned list.
     * @param mgs the {@link MapGeometrySupport} to transform
     * @param id the id for the Geometry
     * @param dti the {@link DataTypeInfo}
     * @param visState - the additional visualization state information
     * @param renderPropertyPool the render property pool
     * @return the set that are created from the MapGeometrySupport
     *         {@link Geometry} that comprise it and its children.
     */
    public Set<Geometry> createGeometries(Set<Geometry> addToSet, MapGeometrySupport mgs, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        if (mgs == null)
        {
            throw new IllegalArgumentException("Recieved null MapGeometrySupport for id " + id);
        }

        MapGeometrySupportToGeometryConverter converter = myConverterRegistry.getConverter(mgs);

        if (converter == null)
        {
            throw new IllegalArgumentException(mgs.getClass().getName() + " is not currently supported");
        }

        Set<Geometry> resultSet = addToSet == null ? new HashSet<>() : addToSet;

        if (addToSet != null)
        {
            addToSet.add(converter.createGeometry(mgs, id, dti, visState, renderPropertyPool));

            // Now create the Geometry for each of the children, which will do a
            // recursive build.
            if (mgs.hasChildren())
            {
                mgs.getChildren().forEach(c -> createGeometries(resultSet, c, id, dti, visState, renderPropertyPool));
            }
        }

        return resultSet;
    }
}
