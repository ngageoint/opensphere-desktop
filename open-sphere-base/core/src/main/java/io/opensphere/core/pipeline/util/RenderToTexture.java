package io.opensphere.core.pipeline.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.util.Utilities;

/**
 * Handle frame buffer setup and texture binding for rendering to a texture.
 */
public class RenderToTexture
{
    /** The RenderToTexture which is currently being used for rendering. */
    private static RenderToTexture ourActiveInstance;

    /** The color used to clear the buffer. */
    private float[] myClearColor = { 0f, 0f, 0f, 0f };

    /** The Frame buffer reference I use for rendering. */
    private final int[] myFBOid = { -1 };

    /** Height of the texture. */
    private final int myHeight;

    /**
     * The render buffer and frame buffer must match the texture which was bound
     * when they were created. When using a new texture, be sure to regenerate
     * the buffers.
     */
    private int myLastUsedTextureId = -1;

    /**
     * Frame buffers cannot be reused if the GL context has changed.
     */
    private GL myLastUsedGL;

    /** The render buffer object reference. */
    private final int[] myRBOid = { -1 };

    /** The renderer who will actually draw onto the texture. */
    private final RenderToTextureRenderer myRenderer;

    /** The texture handle key. */
    private final Object myTextureHandleKey = new Object();

    /** Width of the texture. */
    private final int myWidth;

    /**
     * Get the activeInstance.
     *
     * @return the activeInstance
     */
    public static RenderToTexture getActiveInstance()
    {
        return ourActiveInstance;
    }

    /**
     * Set the activeInstance.
     *
     * @param activeInstance the activeInstance to set
     */
    public static void setActiveInstance(RenderToTexture activeInstance)
    {
        ourActiveInstance = activeInstance;
    }

    /**
     * Construct me.
     *
     * @param renderer Renderer who writes to the texture.
     * @param width width of the texture.
     * @param height height of the texture.
     */
    public RenderToTexture(RenderToTextureRenderer renderer, int width, int height)
    {
        myRenderer = renderer;
        myWidth = width;
        myHeight = height;
    }

    /**
     * Dispose and recreate my buffers.
     *
     * @param gl The GL context.
     */
    public void clean(GL gl)
    {
        // TODO: need a way to dispose the buffers while the old GL is still
        // active.
        if (Utilities.sameInstance(gl, myLastUsedGL))
        {
            // dispose my texture, my rbo and my fbo
            gl.glDeleteRenderbuffers(1, myRBOid, 0);
            gl.glDeleteFramebuffers(1, myFBOid, 0);
        }
        myRBOid[0] = -1;
        myFBOid[0] = -1;
    }

    /**
     * Do the actual rendering after the buffers are configured.
     *
     * @param rc The render context.
     */
    public void doRenderToTexture(RenderContext rc)
    {
        if (!rc.isFBOAvailable())
        {
            throw new IllegalStateException("Frame buffers are not supported by the OpenGL environment.");
        }

        setActiveInstance(this);
        try
        {
            rc.getGL().getGL2().glPushAttrib(GL2.GL_VIEWPORT_BIT | GL.GL_COLOR_BUFFER_BIT);
            // Bind the off line frame buffer
            rc.getGL().glBindFramebuffer(GL.GL_FRAMEBUFFER, myFBOid[0]);

            rc.getGL().glViewport(0, 0, myWidth, myHeight);

            // clear buffer
            rc.getGL().glClearColor(myClearColor[0], myClearColor[1], myClearColor[2], myClearColor[3]);
            rc.getGL().glClear(GL.GL_COLOR_BUFFER_BIT);
            rc.getGL().glClear(GL.GL_DEPTH_BUFFER_BIT);

            // render to the frame
            myRenderer.onRenderToTexture(rc);
        }
        finally
        {
            // un-bind the frame buffer to return to normal
            // window-system provided frame buffer
            rc.getGL().glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
            rc.getGL().getGL2().glPopAttrib();
            setActiveInstance(null);
        }
    }

    /**
     * Get the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Get the size of my texture in bytes.
     *
     * @return the size of my texture in bytes.
     */
    public final int getSize()
    {
        return myWidth * myHeight * 4;
    }

    /**
     * Get the texutre coordinates for the bounding box mapping the box to it's
     * location over the render buffer.
     *
     * @param bbox the boxing box within my frame.
     * @return the texture coordinates.
     */
    public FloatBuffer getTexCoords(ScreenBoundingBox bbox)
    {
        List<Vector2d> textureCoords = new ArrayList<>();

        double lx = bbox.getUpperLeft().getX();
        double uy = bbox.getUpperLeft().getY();
        double rx = bbox.getLowerRight().getX();
        double ly = bbox.getLowerRight().getY();

        double lxc = lx / myWidth;
        double uxc = rx / myWidth;
        double lyc = 1f - ly / myHeight;
        double uyc = 1f - uy / myHeight;

        textureCoords.add(new Vector2d(lxc, uyc));
        textureCoords.add(new Vector2d(lxc, lyc));
        textureCoords.add(new Vector2d(uxc, lyc));
        textureCoords.add(new Vector2d(uxc, uyc));

        return VectorBufferUtilities.vec2dtoFloatBuffer(textureCoords);
    }

    /**
     * Get the texture handle key associated with this object.
     *
     * @return The texture metadata.
     */
    public Object getTexture()
    {
        return myTextureHandleKey;
    }

    /**
     * Get the width.
     *
     * @return the width
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Initialize the required buffers for rendering.
     *
     * @param gl The GL context.
     * @param textureId The texture object to use.
     */
    public void init(GL gl, int textureId)
    {
        if (myLastUsedTextureId != textureId || !Utilities.sameInstance(gl, myLastUsedGL))
        {
            myLastUsedTextureId = textureId;
            if (myRBOid[0] != -1)
            {
                clean(gl);
            }
            myLastUsedGL = gl;
            genFBO(gl, textureId);
        }
    }

    /**
     * Get the image off of the card. If the image is to be compressed using the
     * DDS encoder, use little endian. If the image will be passed through the
     * DDS encoder but left uncompressed, it is OK to leave it big endian.
     *
     * @param gl The GL context.
     * @param textureId The texture object to use.
     * @param littleEndian When true swap the byte order.
     * @return The rendered texture.
     */
    public Image readBackTexture(GL gl, int textureId, boolean littleEndian)
    {
        ByteBuffer buf = ByteBuffer.allocate(getSize());

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, myFBOid[0]);
        gl.getGL2().glBindRenderbuffer(GL.GL_RENDERBUFFER, myRBOid[0]);

        // Reading back as RGBA because this it comes back BIG endian and we are
        // going to use it as if it were LITTLE endian and we really want ABGR.
        gl.glReadPixels(0, 0, myWidth, myHeight, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

        byte[] array = buf.array();
        BufferedImage bImg = null;

        if (littleEndian)
        {
            byte[] flippedArray = new byte[array.length];
            int rowBytes = myWidth * 4;
            for (int row = 0; row < myHeight; row++)
            {
                int flippedRow = myHeight - row - 1;
                System.arraycopy(array, row * rowBytes, flippedArray, flippedRow * rowBytes, rowBytes);
            }
            array = flippedArray;
            bImg = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_4BYTE_ABGR);
        }
        else
        {
            bImg = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_4BYTE_ABGR);
        }

        WritableRaster wr = bImg.getRaster();
        wr.setDataElements(0, 0, myWidth, myHeight, array);

        Image realImg = new ImageIOImage(bImg);
        realImg.setCompressionHint(Image.CompressionType.D3DFMT_A8R8G8B8);
        return realImg;
    }

    /**
     * Set the clearColor.
     *
     * @param clearColor the clearColor to set
     */
    public void setClearColor(Color clearColor)
    {
        if (clearColor != null)
        {
            myClearColor = clearColor.getComponents(null);
        }
    }

    /**
     * Set the clearColor.
     *
     * @param clearColor the clearColor to set
     */
    public void setClearColor(float[] clearColor)
    {
        myClearColor = clearColor.clone();
    }

    /**
     * Generate a new frame buffer object and render buffer object.
     *
     * @param gl The GL context.
     * @param textureId The texture object to use.
     */
    private void genFBO(GL gl, int textureId)
    {
        // create a texture object for each buffer
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, myWidth, myHeight, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

        gl.glGenFramebuffers(1, myFBOid, 0);
        gl.glGenRenderbuffers(1, myRBOid, 0);

        // FrameBuffer setup
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, myFBOid[0]);

        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, myRBOid[0]);
        gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL2ES2.GL_DEPTH_COMPONENT, myWidth, myHeight);
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

        // attach a texture to FBO color attachment point
        gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, textureId, 0);

        // attach a renderbuffer to depth attachment point
        gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, myRBOid[0]);

        // Bind back to the regular rendering buffer.
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
    }
}
