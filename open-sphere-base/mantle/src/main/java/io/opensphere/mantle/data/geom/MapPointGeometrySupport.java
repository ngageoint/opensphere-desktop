package io.opensphere.mantle.data.geom;

/**
 * A Map Point Geometry Support.
 */
public interface MapPointGeometrySupport extends MapLocationGeometrySupport
{
    /**
     * Gets the scale.
     *
     * @return the scale
     */
    float getScale();

    /**
     * Sets the scale.
     *
     * @param scale the scale
     */
    void setScale(float scale);
}
