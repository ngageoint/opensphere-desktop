package io.opensphere.mantle.data.geom;

/**
 * Support for circle geometries.
 */
public interface MapCircleGeometrySupport extends MapLocationGeometrySupport
{
    /**
     * Gets the radius.
     *
     * @return the radius.
     */
    float getRadius();

    /**
     * Sets the radius.
     *
     * @param radius the radius
     */
    void setRadius(float radius);
}
