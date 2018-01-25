package io.opensphere.core.pipeline.util;

import java.util.Map;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;

/**
 * A group of <code>Texture</code>s.
 */
public class MagnifiedTextureGroup extends TextureGroup
{
    /** The magnification factors. */
    private final Vector2d myMagnification;

    /** The origin of the image within the source image. */
    private final Vector2d myOrigin;

    /**
     * Construct a tile texture group which is a magnified portion of another
     * texture group's images.
     *
     * @param textures The textures.
     * @param magnifiedTexCoords the texture coords for the magnified image.
     * @param magnification The X,Y scaling that should be applied to the image.
     * @param origin The origin of the image relative to the source image.
     */
    public MagnifiedTextureGroup(Map<RenderMode, Object> textures, TextureCoords magnifiedTexCoords, Vector2d magnification,
            Vector2d origin)
    {
        super(textures, magnifiedTexCoords);
        myMagnification = magnification;
        myOrigin = origin;
    }

    /**
     * Get the magnification.
     *
     * @return the magnification
     */
    public Vector2d getMagnification()
    {
        return myMagnification;
    }

    /**
     * Get the origin.
     *
     * @return the origin
     */
    public Vector2d getOrigin()
    {
        return myOrigin;
    }

    @Override
    public long getSizeBytes()
    {
        return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + 5 * Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES) + Vector2d.SIZE_BYTES + Vector2d.SIZE_BYTES
                + Utilities.sizeOfEnumMapBytes(AbstractGeometry.RenderMode.class)
                + getTextureMap().size() * Constants.OBJECT_SIZE_BYTES;
    }
}
