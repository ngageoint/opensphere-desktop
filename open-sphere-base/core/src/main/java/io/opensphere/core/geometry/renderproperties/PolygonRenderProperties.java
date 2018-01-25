package io.opensphere.core.geometry.renderproperties;

/** Render properties specific to polygon geometries. */
public interface PolygonRenderProperties extends PolylineRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    PolygonRenderProperties clone();

    /**
     * Get the fill color render properties. If the polygon is not filled, this
     * is {@code null}.
     *
     * @return The render properties.
     */
    ColorRenderProperties getFillColorRenderProperties();
}
