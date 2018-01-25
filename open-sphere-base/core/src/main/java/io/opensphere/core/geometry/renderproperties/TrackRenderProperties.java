package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/** Render properties specific to track geometries. */
public interface TrackRenderProperties extends PolylineRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    TrackRenderProperties clone();

    /**
     * Get the color of the directional arrows.
     *
     * @return The color of the arrows.
     */
    Color getArrowColor();

    /**
     * Get the scale factor used to determine the length of the directional
     * arrows. This will be multiplied by the line segment distance to determine
     * length.
     *
     * @return The scale factor for arrow length.
     */
    float getArrowLengthScale();

    /**
     * Get the width of the directional arrows.
     *
     * @return The width of the arrows.
     */
    float getArrowWidth();

    /**
     * Get the color of the nodes.
     *
     * @return The color of the nodes.
     */
    Color getNodeColor();

    /**
     * Get the size of the nodes.
     *
     * @return The size of the nodes.
     */
    float getNodeSize();

    /**
     * Set the color of the directional arrows.
     *
     * @param color The color of the arrows.
     */
    void setArrowColor(Color color);

    /**
     * Set the scale factor used to determine the length of the directional
     * arrows. This will be multiplied by the line segment distance to determine
     * length.
     *
     * @param scale The scale factor for arrow length.
     */
    void setArrowLengthScale(float scale);

    /**
     * Set the width of the directional arrows.
     *
     * @param width The width of the arrows.
     */
    void setArrowWidth(float width);

    /**
     * Set the color of the nodes.
     *
     * @param color The color of the nodes.
     */
    void setNodeColor(Color color);

    /**
     * Set the size of the nodes.
     *
     * @param size The size of the nodes.
     */
    void setNodeSize(float size);
}
