package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;

import javax.annotation.Nullable;
import javax.media.opengl.GL;
import javax.media.opengl.fixedfunc.GLPointerFunc;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Constants;

/**
 * Model for an OpenGL normal buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class NormalBufferObject extends AbstractBufferObject<FloatBuffer>
{
    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     */
    public NormalBufferObject(FloatBuffer buffer)
    {
        super(buffer, (AbstractGeometry.RenderMode)null);
    }

    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    public NormalBufferObject(FloatBuffer buffer, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super(buffer, renderMode);
    }

    @Override
    public boolean bind(RenderContext rc) throws IllegalStateException
    {
        if (super.bind(rc))
        {
            rc.getGL2().glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
            rc.getGL2().glNormalPointer(GL.GL_FLOAT, 0, 0);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected int getElementSizeBytes()
    {
        return Constants.FLOAT_SIZE_BYTES;
    }
}
