package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.media.opengl.GL;

import com.jogamp.common.nio.PointerBuffer;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Utilities;

/**
 * Model for an OpenGL index buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class MultiDrawIndexBufferObject implements BufferObject
{
    /** A buffer that contains the number of vertices of each geometry. */
    private final IntBuffer mySizeBuffer;

    /**
     * If non-null, this indicates which render mode the buffer object is
     * applicable to.
     */
    @Nullable
    private final AbstractGeometry.RenderMode myRenderMode;

    /**
     * Buffer of references to the index buffers that contain the vertex indices
     * for each geometry.
     */
    private final PointerBuffer myPointerBuffer;

    /**
     * Constructor.
     *
     * @param indices A buffer for each geometry that contains the vertex
     *            indices to be drawn for that geometry.
     */
    public MultiDrawIndexBufferObject(Collection<? extends IntBuffer> indices)
    {
        this(indices, (AbstractGeometry.RenderMode)null);
    }

    /**
     * Constructor.
     *
     * @param indices A buffer for each geometry that contains the vertex
     *            indices to be drawn for that geometry.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    public MultiDrawIndexBufferObject(Collection<? extends IntBuffer> indices, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        myRenderMode = renderMode;

        mySizeBuffer = ByteBuffer.allocateDirect(indices.size() * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();

        myPointerBuffer = PointerBuffer.allocateDirect(indices.size());
        indices.forEach(b ->
        {
            mySizeBuffer.put(b.limit());
            myPointerBuffer.referenceBuffer(b);
        });
    }

    @Override
    public boolean draw(RenderContext rc, int drawMode)
    {
        if (myRenderMode != null && !Utilities.sameInstance(myRenderMode, rc.getRenderMode()))
        {
            return false;
        }
        else
        {
            rc.getGL2().glMultiDrawElements(drawMode, (IntBuffer)mySizeBuffer.rewind(), GL.GL_UNSIGNED_INT,
                    myPointerBuffer.rewind(), myPointerBuffer.limit());
            return true;
        }
    }

    @Override
    public long getSizeGPU()
    {
        return 0;
    }

    @Override
    public void dispose(GL gl)
    {
    }
}
