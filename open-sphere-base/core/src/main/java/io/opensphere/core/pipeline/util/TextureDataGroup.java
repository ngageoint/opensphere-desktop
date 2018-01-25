package io.opensphere.core.pipeline.util;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.TextureData;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.ImageGroup;
import io.opensphere.core.geometry.ImageProvidingGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Group of {@link TextureData}s.
 */
public class TextureDataGroup
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TextureDataGroup.class);

    /**
     * How much memory {@link #myTextureDataMap} takes up, not counting values.
     */
    private static final int MAP_SIZE_BYTES = Utilities.sizeOfEnumMapBytes(AbstractGeometry.RenderMode.class);

    /** If flushing is disabled. */
    private boolean myFlushingDisabled;

    /** Map of render mode to the associated <code>TextureData</code>. */
    private final Map<AbstractGeometry.RenderMode, TextureData> myTextureDataMap = new EnumMap<AbstractGeometry.RenderMode, TextureData>(
            AbstractGeometry.RenderMode.class);

    /**
     * Dispose of any resources held by this texture data group. Specifically,
     * flush the texture data objects in my map and clear the map.
     */
    public synchronized void flush()
    {
        if (!myFlushingDisabled)
        {
            for (TextureData td : myTextureDataMap.values())
            {
                td.flush();
            }
            myTextureDataMap.clear();
        }
    }

    /**
     * Get the sum of the estimated sizes of my texture data.
     *
     * @return estimated size in bytes.
     */
    public synchronized long getEstimatedMemorySize()
    {
        long mem = MAP_SIZE_BYTES;
        for (TextureData td : myTextureDataMap.values())
        {
            mem += td.getEstimatedMemorySize();
        }
        return mem;
    }

    /**
     * Get the textureDataMap.
     *
     * @return the textureDataMap
     */
    public synchronized Map<AbstractGeometry.RenderMode, TextureData> getTextureDataMap()
    {
        return new EnumMap<AbstractGeometry.RenderMode, TextureData>(myTextureDataMap);
    }

    /**
     * Create texture data objects from some image data to populate a texture
     * data group.
     *
     * @param geom The geometry.
     * @param imageData The image data.
     * @param compressionSupported If the graphics environment supports texture
     *            compression.
     * @param enableMipMaps True if the texture should have a mipmap
     *            automatically generated for it.
     * @return {@code true} if successful.
     */
    public synchronized boolean populateTextureDataGroup(ImageProvidingGeometry<?> geom, ImageGroup imageData,
            boolean compressionSupported, boolean enableMipMaps)
    {
        if (imageData == null)
        {
            return false;
        }
        for (Map.Entry<AbstractGeometry.RenderMode, ? extends Image> entry : imageData.getImageMap().entrySet())
        {
            Image image = entry.getValue();
            if (!myTextureDataMap.containsKey(entry.getKey()) && !(image instanceof PreloadedTextureImage))
            {
                if (image.isBlank())
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(StringUtilities.concat("Image for geometry [", geom, " -- ",
                                geom.getImageManager().getImageProvider(), "] is blank"));
                    }
                    return true;
                }

                TextureData td = GLUtilities.createTextureData(geom, image, compressionSupported && !enableMipMaps);
                if (td == null)
                {
                    return false;
                }
                else
                {
                    td.setMipmap(enableMipMaps);
                    myTextureDataMap.put(entry.getKey(), td);
                }
            }
        }
        return true;
    }

    /**
     * Set flushing disabled.
     *
     * @param disabled If flushing should be disabled.
     */
    public synchronized void setFlushingDisabled(boolean disabled)
    {
        myFlushingDisabled = disabled;
    }
}
