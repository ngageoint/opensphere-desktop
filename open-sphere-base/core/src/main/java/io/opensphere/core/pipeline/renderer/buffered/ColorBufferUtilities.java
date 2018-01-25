package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;

import io.opensphere.core.geometry.ColorGeometry;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;

/** Utilities for loading colors into buffers. */
public final class ColorBufferUtilities
{
    /**
     * Helper method that loads the draw color or pick color for a geometry into
     * a byte buffers.
     *
     * @param geom The geometry.
     * @param colorProperties The color render properties.
     * @param highlight Flag indicating if the highlight color should be used.
     * @param colors Insert the draw color into this buffer.
     */
    public static void getColors(ColorGeometry geom, ColorRenderProperties colorProperties, boolean highlight, ByteBuffer colors)
    {
        int color = highlight ? colorProperties.getHighlightColorARGB() : colorProperties.getColorARGB();
        colors.put((byte)(color >> 16));
        colors.put((byte)(color >> 8));
        colors.put((byte)color);
        colors.put((byte)(color >> 24));
    }

    /** Disallow instantiation. */
    private ColorBufferUtilities()
    {
    }
}
