package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.annotation.Nullable;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;

/**
 * Model for an OpenGL vertex buffer object that uses the
 * {@code glMultiDrawArrays} semantic. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class MultiDrawVertexBufferObject extends VertexBufferObject
{
    /** A buffer that contains the start index of each geometry. */
    private final IntBuffer myIndexBuffer;

    /** A buffer that contains the number of vertices of each geometry. */
    private final IntBuffer mySizeBuffer;

    /**
     * Constructor.
     *
     * @param vertexBuffer The vertex buffer.
     * @param indexBuffer Buffer containing the start index of each geometry.
     * @param sizeBuffer Buffer containing the number of vertices in each
     *            geometry.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    public MultiDrawVertexBufferObject(FloatBuffer vertexBuffer, IntBuffer indexBuffer, IntBuffer sizeBuffer,
            @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super(vertexBuffer, renderMode, false);
        myIndexBuffer = indexBuffer;
        mySizeBuffer = sizeBuffer;
    }

    /**
     * Constructor with no render mode.
     *
     * @param vertexBuffer The vertex buffer.
     * @param indexBuffer Buffer containing the start index of each geometry.
     * @param sizeBuffer Buffer containing the number of vertices in each
     *            geometry.
     */
    public MultiDrawVertexBufferObject(FloatBuffer vertexBuffer, IntBuffer indexBuffer, IntBuffer sizeBuffer)
    {
        super(vertexBuffer, false);
        myIndexBuffer = (IntBuffer)indexBuffer.duplicate().rewind();
        mySizeBuffer = (IntBuffer)sizeBuffer.duplicate().rewind();
    }

    @Override
    public boolean draw(RenderContext rc, int drawMode)
    {
        if (super.draw(rc, drawMode))
        {
            rc.getGL2().glMultiDrawArrays(drawMode, myIndexBuffer, mySizeBuffer, myIndexBuffer.remaining());
            return true;
        }
        else
        {
            return false;
        }
    }
}
