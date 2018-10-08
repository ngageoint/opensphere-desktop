package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.fixedfunc.GLPointerFunc;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Constants;

/**
 * Model for an OpenGL vertex buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class VertexBufferObject extends AbstractBufferObject<FloatBuffer>
{
    /** Number of coordinates used to define a vertex position. */
    public static final int VERTEX_DIMENSIONS = 3;

    /** Flag indicating if I should draw my elements. */
    private final boolean myDraw;

    /** The number of vertices to be drawn. */
    private final int myVertexCount;

    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     * @param draw Indicates if this object should draw its elements.
     */
    public VertexBufferObject(FloatBuffer buffer, @Nullable AbstractGeometry.RenderMode renderMode, boolean draw)
    {
        super(buffer, renderMode);
        myDraw = draw;
        myVertexCount = buffer.limit() / VERTEX_DIMENSIONS;
    }

    /**
     * Constructor with no render mode.
     *
     * @param buffer The buffer to be uploaded to the card.
     * @param draw Indicates if this object should draw its elements.
     */
    public VertexBufferObject(FloatBuffer buffer, boolean draw)
    {
        super(buffer, (AbstractGeometry.RenderMode)null);
        myDraw = draw;
        myVertexCount = buffer.limit() / VERTEX_DIMENSIONS;
    }

    @Override
    public boolean bind(RenderContext rc) throws IllegalStateException
    {
        if (super.bind(rc))
        {
            rc.getGL2().glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
            rc.getGL2().glVertexPointer(VERTEX_DIMENSIONS, GL.GL_FLOAT, 0, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean draw(RenderContext rc, int drawMode)
    {
        if (super.draw(rc, drawMode))
        {
            if (myDraw)
            {
                rc.getGL().glDrawArrays(drawMode, 0, myVertexCount);
            }
            return true;
        }
        return false;
    }

    @Override
    protected int getElementSizeBytes()
    {
        return Constants.FLOAT_SIZE_BYTES;
    }
}
