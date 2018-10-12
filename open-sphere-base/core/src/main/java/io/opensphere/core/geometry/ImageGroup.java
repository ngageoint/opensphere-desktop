package io.opensphere.core.geometry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.image.Image;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.Utilities;

/**
 * A map of {@link RenderMode}s to {@link Image}s.
 */
@net.jcip.annotations.Immutable
public class ImageGroup implements SizeProvider
{
    /** A map of render mode to the associated image for that mode. */
    private final Map<RenderMode, ? extends Image> myImageMap;

    /**
     * Construct the tile image group.
     *
     * @param imageMap The map of modes to images.
     */
    public ImageGroup(Map<RenderMode, ? extends Image> imageMap)
    {
        if (imageMap.size() == 1)
        {
            Entry<RenderMode, ? extends Image> entry = imageMap.entrySet().iterator().next();
            myImageMap = Collections.singletonMap(entry.getKey(), entry.getValue());
        }
        else
        {
            myImageMap = Collections.unmodifiableMap(new EnumMap<RenderMode, Image>(imageMap));
        }
    }

    /** Dispose the images in this image group. */
    public void dispose()
    {
        for (Image image : myImageMap.values())
        {
            image.dispose();
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation only indicates two ImageGroups are equal if they
     * contain the same Image instances.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Map<RenderMode, ? extends Image> otherMap = ((ImageGroup)obj).myImageMap;
        if (myImageMap.size() == otherMap.size())
        {
            for (Entry<RenderMode, ? extends Image> entry : myImageMap.entrySet())
            {
                if (!Utilities.sameInstance(otherMap.get(entry.getKey()), entry.getValue()))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get the imageMap.
     *
     * @return the imageMap
     */
    public Map<RenderMode, ? extends Image> getImageMap()
    {
        return myImageMap;
    }

    @Override
    public long getSizeBytes()
    {
        int size = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        if (myImageMap.size() == 1)
        {
            // SingletonMap
            size += MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES * 7,
                    Constants.MEMORY_BLOCK_SIZE_BYTES);
        }
        else
        {
            size += Utilities.sizeOfEnumMapBytes(RenderMode.class);
        }
        for (Image img : myImageMap.values())
        {
            size += img.getSizeInBytes();
        }
        return size;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myImageMap == null ? 0 : myImageMap.hashCode());
        return result;
    }
}
