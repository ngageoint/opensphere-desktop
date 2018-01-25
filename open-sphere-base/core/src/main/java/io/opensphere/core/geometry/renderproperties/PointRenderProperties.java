package io.opensphere.core.geometry.renderproperties;

/** Render properties specific to point geometries. */
public interface PointRenderProperties extends BaseAltitudeRenderProperties
{
    @Override
    PointRenderProperties clone();

    /**
     * Get the baseRenderProperties.
     *
     * @return the baseRenderProperties
     */
    BaseAltitudeRenderProperties getBaseRenderProperties();

    /**
     * Get the size to use when the geometry is highlighted. This is a
     * convenience method and calls through to the size property.
     *
     * @return the size
     */
    float getHighlightSize();

    /**
     * Get the point roundness property.
     *
     * @return The point roundness property.
     */
    PointRoundnessRenderProperty getRoundnessRenderProperty();

    /**
     * Get the size. This is a convenience method and calls through to the size
     * property.
     *
     * @return the size
     */
    float getSize();

    /**
     * Get the size property.
     *
     * @return The size property.
     */
    PointSizeRenderProperty getSizeProperty();

    /**
     * Get if the point should be round (otherwise it should be square). This is
     * a convenience method and calls through to the roundness property.
     *
     * @return If the point is round.
     */
    boolean isRound();

    /**
     * Set the size to use when the geometry is highlighted. This is a
     * convenience method and calls through to the size property. If the size
     * property is shared, a call to this method will change the size for all
     * geometries that share the property.
     *
     * @param size the size to set
     */
    void setHighlightSize(float size);

    /**
     * Set if the point should be round (otherwise it should be square). This is
     * a convenience method and calls through to the roundness property.
     *
     * @param round If the point is round.
     */
    void setRound(boolean round);

    /**
     * Set the size. This is a convenience method and calls through to the size
     * property. If the size property is shared, a call to this method will
     * change the size for all geometries that share the property.
     *
     * @param size the size to set
     */
    void setSize(float size);
}
