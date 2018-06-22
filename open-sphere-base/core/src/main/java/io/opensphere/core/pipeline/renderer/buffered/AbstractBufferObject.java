package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.Buffer;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.media.opengl.GL;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Utilities;

/**
 * Model for an OpenGL buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 *
 * @param <E> The type of buffer used by this buffer object.
 */
public abstract class AbstractBufferObject<E extends Buffer> implements BufferObject
{
    /** Atomic updater for myBuffer. */
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<AbstractBufferObject, Buffer> BUFFER_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(AbstractBufferObject.class, Buffer.class, "myBuffer");

    /**
     * The buffer itself, which will be {@code null} after being uploaded.
     */
    @SuppressWarnings("unused")
    private volatile Buffer myBuffer;

    /** The id for the buffer if it has been created, else -1. */
    private int myBufferId;

    /**
     * If non-null, this indicates which render mode the buffer object is
     * applicable to.
     */
    @Nullable
    private final AbstractGeometry.RenderMode myRenderMode;

    /** The size of the uploaded buffer in bytes. */
    private volatile int mySizeGPUBytes;

    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    protected AbstractBufferObject(E buffer, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super();
        if (buffer.capacity() == 0)
        {
            throw new IllegalArgumentException("Buffer has zero size.");
        }
        myBuffer = buffer;
        myRenderMode = renderMode;

        // Set the size before binding to ensure that the size in the cache is
        // correct (this will likely be added to the cache before being bound).
        mySizeGPUBytes = buffer.capacity() * getElementSizeBytes();
    }

    /**
     * Bind the on-card buffer. If the on-card buffer has not been created and
     * there is data waiting to be uploaded, this will create the buffer and
     * upload the data. If the on-card buffer has not been created and there is
     * no data waiting, this will do nothing.
     *
     * @param rc The render context.
     * @return {@code true} if a buffer was bound.
     */
    public boolean bind(RenderContext rc)
    {
        if (myRenderMode != null && !Utilities.sameInstance(myRenderMode, rc.getRenderMode()))
        {
            return false;
        }
        Buffer buffer = BUFFER_UPDATER.getAndSet(this, null);
        if (buffer != null && buffer.limit() > 0)
        {
            int[] tmpId = new int[1];
            rc.getGL().glGenBuffers(1, tmpId, 0);
            myBufferId = tmpId[0];
            rc.getGL().glBindBuffer(getBufferTarget(), myBufferId);
            rc.getGL().glBufferData(getBufferTarget(), mySizeGPUBytes, buffer.rewind(), GL.GL_STATIC_DRAW);
            return true;
        }
        else if (myBufferId == -1)
        {
            return false;
        }
        else
        {
            rc.getGL().glBindBuffer(getBufferTarget(), myBufferId);
            return true;
        }
    }

    @Override
    public void dispose(GL gl)
    {
        if (myBufferId != -1)
        {
            gl.glDeleteBuffers(1, new int[] { myBufferId }, 0);
            myBufferId = -1;
        }
        mySizeGPUBytes = 0;
    }

    @Override
    public boolean draw(RenderContext rc, int drawMode)
    {
        return bind(rc);
    }

    @Override
    public long getSizeGPU()
    {
        return mySizeGPUBytes;
    }

    /**
     * Get the buffer target.
     *
     * @return The buffer target.
     */
    protected int getBufferTarget()
    {
        return GL.GL_ARRAY_BUFFER;
    }

    /**
     * Get the size of each element in the buffer.
     *
     * @return The size in bytes.
     */
    protected abstract int getElementSizeBytes();
}
