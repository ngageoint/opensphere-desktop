package io.opensphere.core.pipeline.util;

import java.util.EnumMap;
import java.util.Map;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.Utilities;

/**
 * A group of <code>Texture</code>s.
 */
public class TextureGroup implements SizeProvider
{
    /** Texture coordinates for the image. */
    private final TextureCoords myImageTexCoords;

    /**
     * Map of render mode to the key for the associated
     * <code>TextureHandle</code>.
     */
    private Map<AbstractGeometry.RenderMode, Object> myTextureMap = new EnumMap<AbstractGeometry.RenderMode, Object>(
            AbstractGeometry.RenderMode.class);

    /**
     * Construct a tile texture group from some pre-loaded textures.
     *
     * @param imageMap A map of render modes to pre-loaded textures.
     */
    public TextureGroup(Map<AbstractGeometry.RenderMode, PreloadedTextureImage> imageMap)
    {
        for (Map.Entry<AbstractGeometry.RenderMode, PreloadedTextureImage> entry : imageMap.entrySet())
        {
            myTextureMap.put(entry.getKey(), entry.getValue().getTextureHandleKey());
        }
        myImageTexCoords = new TextureCoords(0f, 0f, 1f, 1f);
    }

    /**
     * Construct a texture group from a map of textures.
     *
     * @param textures The textures.
     * @param texCoords The image texture coordinates.
     */
    public TextureGroup(Map<RenderMode, Object> textures, TextureCoords texCoords)
    {
        myTextureMap.putAll(textures);
        myImageTexCoords = Utilities.checkNull(texCoords, "texCoords");
    }

    /**
     * Get the imageTexCoords.
     *
     * @return the imageTexCoords
     */
    public TextureCoords getImageTexCoords()
    {
        return myImageTexCoords;
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + 2 * Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES) + Utilities.sizeOfEnumMapBytes(AbstractGeometry.RenderMode.class)
                + myTextureMap.size() * Constants.OBJECT_SIZE_BYTES;
    }

    /**
     * Get the map of render modes to keys for <code>TextureHandle</code>.
     *
     * @return the textureMap
     */
    public Map<AbstractGeometry.RenderMode, Object> getTextureMap()
    {
        return myTextureMap;
    }

    /**
     * Set the textureMap.
     *
     * @param textureMap the textureMap to set
     */
    public void setTextureMap(Map<AbstractGeometry.RenderMode, Object> textureMap)
    {
        myTextureMap = textureMap;
    }
}
