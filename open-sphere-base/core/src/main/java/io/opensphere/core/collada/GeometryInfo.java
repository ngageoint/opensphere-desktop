package io.opensphere.core.collada;

import java.util.List;

import io.opensphere.core.collada.jaxb.Geometry;
import io.opensphere.core.collada.jaxb.InstanceGeometry;
import io.opensphere.core.util.collections.New;

/**
 * Stores various pieces of geometry information.
 */
public class GeometryInfo
{
    /** The instance geometry. */
    private final InstanceGeometry myInstanceGeometry;

    /** The library geometry. */
    private final Geometry myGeometry;

    /** The shape info. */
    private final List<ShapeInfo<?>> myShapes = New.list();

    /**
     * Constructor.
     *
     * @param instanceGeometry The instance geometry
     * @param geometry The library geometry
     */
    public GeometryInfo(InstanceGeometry instanceGeometry, Geometry geometry)
    {
        myInstanceGeometry = instanceGeometry;
        myGeometry = geometry;
    }

    /**
     * Gets the instanceGeometry.
     *
     * @return the instanceGeometry
     */
    public InstanceGeometry getInstanceGeometry()
    {
        return myInstanceGeometry;
    }

    /**
     * Gets the geometry.
     *
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return myGeometry;
    }

    /**
     * Adds the shape.
     *
     * @param shape the shape
     */
    public void addShape(ShapeInfo<?> shape)
    {
        myShapes.add(shape);
    }

    /**
     * Gets the shapes.
     *
     * @return the shapes
     */
    public List<ShapeInfo<?>> getShapes()
    {
        return myShapes;
    }
}
