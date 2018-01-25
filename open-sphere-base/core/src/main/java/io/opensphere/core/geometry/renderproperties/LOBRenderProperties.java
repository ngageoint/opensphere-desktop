package io.opensphere.core.geometry.renderproperties;

/** Render properties specific to line of bearing geometries. */
public interface LOBRenderProperties extends PolylineRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    LOBRenderProperties clone();

    /**
     * Accessor for the directional arrow line lengths.
     *
     * @return The directional arrow line lengths.
     */
    float getDirectionalArrowLength();

    /**
     * Accessor for the line length.
     *
     * @return The line length.
     */
    float getLineLength();

    /**
     * Set lengths of the directional arrow lines (meters).
     *
     * @param length The new length.
     */
    void setDirectionalArrowLength(float length);

    /**
     * Set length of the line (meters).
     *
     * @param length The length to set.
     */
    void setLineLength(float length);
}
