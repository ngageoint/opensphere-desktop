package io.opensphere.core.geometry.renderproperties;

/** Render properties specific to mesh geometries used for visualization. */
public interface ScalableMeshRenderProperties extends ScalableRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    ScalableMeshRenderProperties clone();

    /**
     * Get the height.
     *
     * @return The height.
     */
    float getHeight();

    /**
     * Get the length.
     *
     * @return The length.
     */
    float getLength();

    /**
     * Set the height.
     *
     * @param height The height.
     */
    void setHeight(float height);

    /**
     * Set the length.
     *
     * @param length The length.
     */
    void setLength(float length);
}
