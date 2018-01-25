package io.opensphere.mantle.mappoint.impl;

import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;

/**
 * Marker class for tile geometries used for map bubbles in map annotations.
 */
public class AnnotationPointTileGeometry extends TileGeometry
{
    /**
     * Instantiates a new annotation point tile geometry.
     *
     * @param builder the builder
     * @param renderProperties the render properties
     * @param constraints the constraints
     */
    public AnnotationPointTileGeometry(Builder<?> builder, TileRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
    }
}
