package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;

import javax.annotation.Nullable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Constants;

/**
 * Model for an OpenGL texture coordinate buffer object. This handles uploading
 * a buffer, maintaining the id for the buffer, and disposing the buffer.
 */
public class TextureCoordinateBufferObject extends AbstractBufferObject<FloatBuffer>
{
    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     */
    public TextureCoordinateBufferObject(FloatBuffer buffer)
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
    public TextureCoordinateBufferObject(FloatBuffer buffer, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super(buffer, renderMode);
    }

    @Override
    public boolean bind(RenderContext rc) throws IllegalStateException
    {
        if (super.bind(rc))
        {
            rc.getGL2().glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
            rc.getGL2().glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
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
