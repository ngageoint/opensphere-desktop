package io.opensphere.core.pipeline.renderer.buffered;

import com.jogamp.opengl.GL;

import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.GPUSizeProvider;

/**
 * Common interface for OpenGL buffer objects.
 */
public interface BufferObject extends GPUSizeProvider
{
    /**
     * Delete any existing server-side buffers.
     *
     * @param gl The OpenGL interface.
     */
    void dispose(GL gl);

    /**
     * Bind and draw my server-side buffer(s).
     *
     * @param rc The render context.
     * @param drawMode The GL draw mode (e.g., {@link GL#GL_POINTS},
     *            {@link GL#GL_LINE_STRIP}, etc.)
     * @return {@code true} if anything was drawn.
     */
    boolean draw(RenderContext rc, int drawMode);
}
