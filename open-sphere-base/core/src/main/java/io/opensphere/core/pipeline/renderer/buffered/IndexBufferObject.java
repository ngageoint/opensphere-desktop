package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.IntBuffer;

import javax.annotation.Nullable;
import javax.media.opengl.GL;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Constants;

/**
 * Model for an OpenGL index buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class IndexBufferObject extends AbstractBufferObject<IntBuffer>
{
    /** The number of vertices to be drawn. */
    private final int myVertexCount;

    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     */
    public IndexBufferObject(IntBuffer buffer)
    {
        super(buffer, (AbstractGeometry.RenderMode)null);
        myVertexCount = buffer.limit();
    }

    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    public IndexBufferObject(IntBuffer buffer, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super(buffer, renderMode);
        myVertexCount = buffer.limit();
    }

    @Override
    public boolean draw(RenderContext rc, int drawMode)
    {
        if (super.draw(rc, drawMode))
        {
            rc.getGL().glDrawElements(drawMode, myVertexCount, GL.GL_UNSIGNED_INT, 0);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Get the number of vertices in the buffer.
     *
     * @return The number of vertices.
     */
    public int getVertexCount()
    {
        return myVertexCount;
    }

    @Override
    protected int getBufferTarget()
    {
        return GL.GL_ELEMENT_ARRAY_BUFFER;
    }

    @Override
    protected int getElementSizeBytes()
    {
        return Constants.INT_SIZE_BYTES;
    }
}
