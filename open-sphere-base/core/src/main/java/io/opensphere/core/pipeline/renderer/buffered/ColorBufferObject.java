package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;
import javax.media.opengl.GL;
import javax.media.opengl.fixedfunc.GLPointerFunc;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;

/**
 * Model for an OpenGL color buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class ColorBufferObject extends AbstractBufferObject<ByteBuffer>
{
    /** How many bytes per color. */
    private final int myBytesPerColor;

    /**
     * Constructor.
     *
     * @param buffer The buffer to be uploaded to the card.
     * @param bytesPerColor The number of bytes per color.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    public ColorBufferObject(ByteBuffer buffer, int bytesPerColor, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super(buffer, renderMode);
        myBytesPerColor = bytesPerColor;
    }

    @Override
    public boolean bind(RenderContext rc) throws IllegalStateException
    {
        if (super.bind(rc))
        {
            rc.getGL2().glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
            rc.getGL2().glColorPointer(myBytesPerColor, GL.GL_UNSIGNED_BYTE, 0, 0);
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
        return 1;
    }
}
