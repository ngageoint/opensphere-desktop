package io.opensphere.core.geometry.renderproperties;

/** A render property that specifies point size only. */
public interface PointSizeRenderProperty extends RenderProperties, Comparable<PointSizeRenderProperty>
{
    /** The default size to use for highlighted points. */
    float DEFAULT_HIGHLIGHT_SIZE = 10f;

    @Override
    PointSizeRenderProperty clone();

    /**
     * Get the size to use when the geometry is highlighted.
     *
     * @return the size
     */
    float getHighlightSize();

    /**
     * Get the size.
     *
     * @return the size
     */
    float getSize();

    /**
     * Set the size to use when the geometry is highlighted.
     *
     * @param size the size to set
     */
    void setHighlightSize(float size);

    /**
     * Set the size.
     *
     * @param size the size to set
     */
    void setSize(float size);
}
