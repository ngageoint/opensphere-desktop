package io.opensphere.mantle.mappoint.impl;

import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;

/**
 * Marker class for a AnnotationPointGeometry.
 */
public class AnnotationPointGeometry extends PointGeometry
{
    /**
     * Instantiates a new annotation point geometry.
     *
     * @param builder the builder
     * @param renderProperties the render properties
     * @param constraints the constraints
     */
    public AnnotationPointGeometry(Builder<?> builder, PointRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
    }
}
