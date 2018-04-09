package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;

import javax.annotation.Nullable;
import com.jogamp.opengl.GL;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * Model for an OpenGL normal buffer object. This handles uploading a buffer,
 * maintaining the id for the buffer, and disposing the buffer.
 */
public class VertexAttributeBufferObject extends AbstractBufferObject<FloatBuffer>
{
    /**
     * The attribute name, which is used to look up the vertex attribute index
     * in the render context.
     */
    private final String myAttributeName;

    /**
     * Constructor.
     *
     * @param attributeName The attribute name, which is used to look up the
     *            vertex attribute index in the render context.
     * @param buffer The buffer to be uploaded to the card.
     */
    public VertexAttributeBufferObject(String attributeName, FloatBuffer buffer)
    {
        this(attributeName, buffer, (AbstractGeometry.RenderMode)null);
    }

    /**
     * Constructor.
     *
     * @param attributeName The attribute name, which is used to look up the
     *            vertex attribute index in the render context.
     * @param buffer The buffer to be uploaded to the card.
     * @param renderMode If non-null, this indicates which render mode the
     *            buffer object is applicable to.
     */
    public VertexAttributeBufferObject(String attributeName, FloatBuffer buffer, @Nullable AbstractGeometry.RenderMode renderMode)
    {
        super(buffer, renderMode);
        myAttributeName = Utilities.checkNull(attributeName, "attributeName");
    }

    @Override
    public boolean bind(RenderContext rc) throws IllegalStateException
    {
        if (super.bind(rc))
        {
            int vertAttrIndex = rc.getShaderRendererUtilities().getVertAttrIndex(myAttributeName);
            if (vertAttrIndex == -1)
            {
                throw new IllegalStateException("No index found for vertex attribute " + myAttributeName);
            }
            rc.getGL2().glEnableVertexAttribArray(vertAttrIndex);
            rc.getGL2().glVertexAttribPointer(vertAttrIndex, 2, GL.GL_FLOAT, false, 0, 0);
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
