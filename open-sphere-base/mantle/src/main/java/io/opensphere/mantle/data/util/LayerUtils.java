package io.opensphere.mantle.data.util;

import java.io.File;
import java.net.URI;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * Layer utilities.
 */
public final class LayerUtils
{
    /** This probably isn't comprehensive, but it's a start. */
    private static final char[] DISALLOWED_LAYER_NAME_CHARS = new char[] { '(', ')' };

    /**
     * Gets a layer name from the given URI.
     *
     * @param uri the URI
     * @return the layer name
     */
    public static String getLayerName(URI uri)
    {
        String layerName;
        try
        {
            File file = new File(uri);
            layerName = file.getName();
        }
        catch (IllegalArgumentException e)
        {
            String path = uri.getPath();
            int lastSep = path.lastIndexOf('/');
            lastSep = lastSep < 0 ? 0 : lastSep;
            int dotIndex = path.lastIndexOf('.');
            int dot = dotIndex > lastSep ? dotIndex : path.length();
            layerName = path.substring(lastSep + 1, dot);
        }

        // Remove file extension
        if (layerName.indexOf('.') != -1)
        {
            layerName = layerName.split("\\.")[0];
        }

        // Replace disallowed characters
        char replacementChar = '_';
        for (char ch : DISALLOWED_LAYER_NAME_CHARS)
        {
            layerName = layerName.replace(ch, replacementChar);
        }
        layerName = StringUtilities.trim(layerName, replacementChar);

        return layerName;
    }

    /**
     * Gets the disallowedLayerNameChars.
     *
     * @return the disallowedLayerNameChars
     */
    public static char[] getDisallowedLayerNameChars()
    {
        return DISALLOWED_LAYER_NAME_CHARS.clone();
    }

    /** Disallow instantiation. */
    private LayerUtils()
    {
    }
}
