package io.opensphere.core.geometry.renderproperties;

/** Render properties specific to polyline geometries. */
public interface PolylineRenderProperties extends ScalableRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    PolylineRenderProperties clone();

    /**
     * Get the stipple.
     *
     * @return the stipple
     */
    StippleModelConfig getStipple();

    /**
     * Set the stipple.
     *
     * @param stipple the stipple to set
     */
    void setStipple(StippleModelConfig stipple);
}
