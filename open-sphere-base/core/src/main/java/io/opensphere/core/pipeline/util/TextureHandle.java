package io.opensphere.core.pipeline.util;

import com.jogamp.opengl.GL;

import io.opensphere.core.util.GPUSizeProvider;

/**
 * A handle for the on-card texture.
 */
public class TextureHandle implements GPUSizeProvider
{
    /** Estimated size of the texture on card. */
    private final int myEstimatedMemorySize;

    /** The height of the texture. */
    private final int myHeight;

    /** The texture id. */
    private final int myTextureId;

    /** The width of the texture. */
    private final int myWidth;

    /**
     * Constructor.
     *
     * @param joglTex The JOGL texture object.
     * @param gl The OpenGL context.
     */
    public TextureHandle(com.jogamp.opengl.util.texture.Texture joglTex, GL gl)
    {
        this(joglTex.getTextureObject(gl), joglTex.getEstimatedMemorySize(), joglTex.getWidth(), joglTex.getHeight());
    }

    /**
     * Constructor.
     *
     * @param textureId The id of the on-card texture.
     * @param estimatedMemorySize The estimated memory size on card in bytes.
     * @param width The width of the texture.
     * @param height The height of the texture.
     */
    public TextureHandle(int textureId, int estimatedMemorySize, int width, int height)
    {
        myTextureId = textureId;
        myEstimatedMemorySize = estimatedMemorySize;
        myWidth = width;
        myHeight = height;
    }

    /**
     * Dispose of the texture on the card.
     *
     * @param gl The OpenGL context.
     */
    public void dispose(GL gl)
    {
        gl.glDeleteTextures(1, new int[] { myTextureId }, 0);
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && obj instanceof TextureHandle && myTextureId == ((TextureHandle)obj).myTextureId;
    }

    /**
     * Accessor for the height.
     *
     * @return The height.
     */
    public int getHeight()
    {
        return myHeight;
    }

    @Override
    public long getSizeGPU()
    {
        return myEstimatedMemorySize;
    }

    /**
     * Get the texture id.
     *
     * @return The id.
     */
    public int getTextureId()
    {
        return myTextureId;
    }

    /**
     * Accessor for the width.
     *
     * @return The width.
     */
    public int getWidth()
    {
        return myWidth;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myTextureId;
        return result;
    }
}
