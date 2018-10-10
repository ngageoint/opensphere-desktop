package io.opensphere.core.pipeline.util;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ServiceLoader;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.TextureData;

import io.opensphere.core.geometry.AbstractColorGeometry;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.image.DDSDecoder;
import io.opensphere.core.image.DDSEncodableImage;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * OpenGL drawing utilities.
 */
@SuppressWarnings("PMD.GodClass")
public final class GLUtilities
{
    /** Key used for looking up the handle for the solid white texture. */
    public static final Object SOLID_WHITE_TEXTURE_KEY = new Object();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GLUtilities.class);

    /** Public GLU instance. */
    private static final GLU ourGlu = new GLU();

    /** Flag indicating if running in production mode. */
    private static boolean ourProduction = true;

    /** Message used when a texture cannot be created. */
    private static final String TEXTURE_FAILURE_MSG = "Failed to create texture for geometry [";

    /** Length of the side of the white texture square. */
    private static final int WHITE_TEXTURE_LENGTH = 1;

    /**
     * Bind the generated solid white texture.
     *
     * @param rc The render context.
     * @param cache The geometry cache.
     * @param inputTextureHandle The handle to bind, which may be {@code null}.
     * @return The handle that was bound.
     */
    public static TextureHandle bindSolidWhiteTexture(RenderContext rc, CacheProvider cache, TextureHandle inputTextureHandle)
    {
        TextureHandle textureHandle;
        if (inputTextureHandle == null)
        {
            textureHandle = cache.getCacheAssociation(GLUtilities.SOLID_WHITE_TEXTURE_KEY, TextureHandle.class);
            if (textureHandle == null)
            {
                int id = GLUtilities.generateSolidTexture(rc.getGL(), Color.WHITE, WHITE_TEXTURE_LENGTH, WHITE_TEXTURE_LENGTH);
                textureHandle = new TextureHandle(id, WHITE_TEXTURE_LENGTH * WHITE_TEXTURE_LENGTH * 4, WHITE_TEXTURE_LENGTH,
                        WHITE_TEXTURE_LENGTH);
                cache.putCacheAssociation(GLUtilities.SOLID_WHITE_TEXTURE_KEY, textureHandle, TextureHandle.class, 0L,
                        textureHandle.getSizeGPU());
            }
            else
            {
                rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, textureHandle.getTextureId());
            }
        }
        else
        {
            textureHandle = inputTextureHandle;
            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, textureHandle.getTextureId());
        }
        return textureHandle;
    }

    /**
     * If not in production mode, check the current error code and log it if it
     * is not NO_ERROR.
     *
     * @param gl The OpenGL reference.
     * @param logger The logger to use.
     * @param messages Messages to log with any errors. The messages are
     *            concatenated. This is to avoid constructing the message until
     *            there is actually an error to report.
     * @return <code>true</code> if there is <b>no</b> error condition.
     */
    public static boolean checkGLErrors(GL gl, Logger logger, Object... messages)
    {
        boolean result;
        if (!isProduction())
        {
            int errCode = gl.glGetError();
            if (errCode != GL.GL_NO_ERROR)
            {
                StringBuilder errStr = new StringBuilder(128);
                errStr.append("OpenGL error [");
                for (Object msg : messages)
                {
                    errStr.append(msg);
                }
                errStr.append("]: ").append(ourGlu.gluErrorString(errCode));
                logger.log(GLUtilities.class.getName(), Level.WARN, errStr, (Throwable)null);
                result = true;
            }
            else
            {
                result = false;
            }
        }
        else
        {
            result = true;
        }

        return result;
    }

    /**
     * Create a texture data object from a DDS image. This will encode the image
     * to DDS if it isn't DDS already. The returned {@code TextureData} may
     * share data with either the input image or the transcoded DDS image. The
     * {@link TextureData#flush()} method must be called on the return object,
     * as well as the {@link Image#dispose()} method on the input image to
     * ensure that resources are released.
     *
     * @param geom The geometry. This is only used for error messages.
     * @param image The input image.
     * @param compressionSupported If compressed textures are supported by the
     *            graphics environment.
     *
     * @return The texture data.
     */
    public static TextureData createTextureData(Object geom, Image image, boolean compressionSupported)
    {
        try
        {
            TextureData textureData;
            if (image instanceof DDSEncodableImage)
            {
                DDSImage ddsImage = ((DDSEncodableImage)image).asDDSImage();
                io.opensphere.core.image.Image.CompressionType compressionType = ddsImage.getCompressionType();
                switch (compressionType)
                {
                    case D3DFMT_A8R8G8B8:
                    case D3DFMT_R8G8B8:
                        textureData = createTextureDataDDS(ddsImage);
                        break;
                    case D3DFMT_DXT1:
                    case D3DFMT_DXT2:
                    case D3DFMT_DXT3:
                    case D3DFMT_DXT4:
                    case D3DFMT_DXT5:
                        if (compressionSupported)
                        {
                            textureData = createTextureDataDDS(ddsImage);
                        }
                        else
                        {
                            textureData = createTextureDataUncompressed(ddsImage);
                        }
                        break;
                    default:
                        throw new UnexpectedEnumException(compressionType);
                }
            }
            else
            {
                throw new UnsupportedOperationException("Image type is not supported: " + image);
            }

            return textureData;
        }
        catch (IOException | RuntimeException e)
        {
            LOGGER.warn(TEXTURE_FAILURE_MSG + geom + "]: " + e, e);
            return null;
        }
    }

    /**
     * Helper method that changes the line smoothing state if necessary.
     *
     * @param gl The GL context.
     * @param smoothing The required smoothing state..
     * @param lastSmoothing <code>true</code> when line smoothing is already
     *            enabled.
     * @return the current state of line smoothing after completion of this
     *         method.
     */
    public static boolean enableSmoothingIfNecessary(GL gl, boolean smoothing, boolean lastSmoothing)
    {
        if (smoothing && !lastSmoothing)
        {
            gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_FASTEST);
            return true;
        }
        else if (!smoothing && lastSmoothing)
        {
            gl.glDisable(GL.GL_LINE_SMOOTH);
            return false;
        }

        return lastSmoothing;
    }

    /**
     * Helper method that changes the line smoothing state if necessary.
     *
     * @param gl The GL context.
     * @param geom The geometry defining the smoothing state.
     * @param lastSmoothing <code>true</code> when line smoothing is already
     *            enabled.
     * @return the current state of line smoothing after completion of this
     *         method.
     */
    public static boolean enableSmoothingIfNecessary(GL gl, PolylineGeometry geom, boolean lastSmoothing)
    {
        return enableSmoothingIfNecessary(gl, geom.isLineSmoothing(), lastSmoothing);
    }

    /**
     * Generate a solid texture.
     *
     * @param gl The OpenGL interface.
     * @param color The color for the texture.
     * @param width The width of the texture.
     * @param height The height of the texture.
     * @return The id of the created texture.
     */
    public static int generateSolidTexture(GL gl, Color color, int width, int height)
    {
        int[] ai = new int[1];
        gl.glGenTextures(1, ai, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, ai[0]);
        final int colors = 4;
        byte[] arr = new byte[width * height * colors];
        int i = 0;
        arr[i++] = (byte)color.getRed();
        arr[i++] = (byte)color.getGreen();
        arr[i++] = (byte)color.getBlue();
        arr[i++] = (byte)color.getAlpha();
        while (i < arr.length)
        {
            arr[i++] = arr[0];
            arr[i++] = arr[1];
            arr[i++] = arr[2];
            arr[i++] = arr[3];
        }
        ByteBuffer buf = BufferUtilities.newByteBuffer(arr.length);
        buf.put(arr);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA4, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf.flip());
        return ai[0];
    }

    /**
     * Get the GLU singleton. This should only be accessed from the GL thread.
     *
     * @return The GLU singleton.
     */
    public static GLU getglu()
    {
        return ourGlu;
    }

    /**
     * Set the line width if the new width is different from the old width.
     *
     * @param gl The GL context.
     * @param width The new width.
     * @param lastWidth The old width.
     * @return The (possibly new) current width.
     */
    public static float glLineWidth(GL gl, float width, float lastWidth)
    {
        if (lastWidth == width)
        {
            return lastWidth;
        }
        gl.glLineWidth(width);
        return width;
    }

    /**
     * Determine if the current thread is the GL thread.
     *
     * @return <code>true</code> if this is the GL thread.
     */
    public static boolean isGLThread()
    {
        return GLContext.getCurrent() != null;
    }

    /**
     * Get if the application is running in production mode.
     *
     * @return <code>true</code> if production mode.
     */
    public static boolean isProduction()
    {
        return ourProduction;
    }

    /**
     * Set if the application is running in production mode.
     *
     * @param production Indicates production mode.
     */
    public static void setProduction(boolean production)
    {
        ourProduction = production;
    }

    /**
     * Set the color in the text renderer.
     *
     * @param renderer The renderer.
     * @param color The new color, in ARGB.
     */
    public static void setRendererColor(TextRenderer renderer, int color)
    {
        renderer.setColor((color >> 16 & 0xff) / (float)0xff, (color >> 8 & 0xff) / (float)0xff, (color & 0xff) / (float)0xff,
                (color >> 24 & 0xff) / (float)0xff);
    }

    /**
     * Set the text renderer color to a geometry's color (or the highlight color
     * if the geometry is picked) unless the color is the same as the last
     * color. Enable blend if necessary.
     *
     * @param renderer The text renderer.
     * @param pickManager Determines if the geometry is picked.
     * @param mode The rendering mode.
     * @param geometry The geometry.
     * @param lastColor The last color, in ARGB.
     * @return The color set.
     */
    public static int setRendererColor(TextRenderer renderer, PickManager pickManager, AbstractGeometry.RenderMode mode,
            AbstractColorGeometry geometry, int lastColor)
    {
        int color;
        if (mode == AbstractGeometry.RenderMode.DRAW)
        {
            if (geometry.getRenderProperties().isPickable() && pickManager.getPickedGeometries().contains(geometry))
            {
                color = geometry.getRenderProperties().getHighlightColorARGB();
            }
            else
            {
                color = geometry.getRenderProperties().getColorARGB();
            }
            if (color != lastColor)
            {
                setRendererColor(renderer, color);
            }
        }
        else if (mode == AbstractGeometry.RenderMode.PICK)
        {
            color = pickManager.getPickColor(geometry);
            setRendererColor(renderer, color | 0xff000000);
        }
        else
        {
            throw new UnexpectedEnumException(mode);
        }
        return color;
    }

    /**
     * Create a DDS texture data from an image.
     *
     * @param dds The image.
     * @return The texture data.
     * @throws IOException If the image cannot be decoded.
     */
    private static TextureData createTextureDataDDS(final DDSImage dds) throws IOException
    {
        ByteBuffer byteBuffer = dds.getByteBuffer();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        final com.jogamp.opengl.util.texture.spi.DDSImage oglImage = com.jogamp.opengl.util.texture.spi.DDSImage.read(byteBuffer);

        int internalFormat;
        int pixelFormat;
        switch (oglImage.getPixelFormat())
        {
            case com.jogamp.opengl.util.texture.spi.DDSImage.D3DFMT_DXT1:
                internalFormat = GL.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
                pixelFormat = GL.GL_RGBA;
                break;
            case com.jogamp.opengl.util.texture.spi.DDSImage.D3DFMT_R8G8B8:
                internalFormat = GL.GL_RGB;
                pixelFormat = GL.GL_RGB;
                break;
            case com.jogamp.opengl.util.texture.spi.DDSImage.D3DFMT_A8R8G8B8:
                internalFormat = GL.GL_RGBA;
                pixelFormat = GL.GL_RGBA;
                break;
            case com.jogamp.opengl.util.texture.spi.DDSImage.D3DFMT_DXT3:
                internalFormat = GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                pixelFormat = GL.GL_RGBA;
                break;
            case com.jogamp.opengl.util.texture.spi.DDSImage.D3DFMT_DXT5:
                internalFormat = GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                pixelFormat = GL.GL_RGBA;
                break;
            default:
                LOGGER.warn("Unsupported DDS image compression: " + oglImage.getPixelFormat());
                return null;
        }

        TextureData.Flusher flusher = () ->
        {
            oglImage.close();
            dds.dispose();
        };

        return new TextureData(GLProfile.getDefault(), internalFormat, dds.getWidth(), dds.getHeight(), 0, pixelFormat,
                GL.GL_UNSIGNED_BYTE, false, oglImage.isCompressed(), true, oglImage.getMipMap(0).getData(), flusher);
    }

    /**
     * Create an uncompressed texture data from a DDS image.
     *
     * @param image The image.
     * @return The texture data.
     * @throws IOException If the texture data cannot be created.
     */
    private static TextureData createTextureDataUncompressed(final DDSImage image) throws IOException
    {
        TextureData.Flusher flusher = () -> image.dispose();

        int pixelFormat = image.getCompressionType() == Image.CompressionType.D3DFMT_DXT5 ? GL.GL_RGBA : GL.GL_RGB;
        int internalFormat = pixelFormat;
        ByteBuffer data = null;
        for (DDSDecoder e : ServiceLoader.load(DDSDecoder.class))
        {
            data = e.decode(image.getByteBuffer(), image.getCompressionType(), image.getWidth(), image.getHeight());
            if (data != null)
            {
                return new TextureData(GLProfile.getDefault(), internalFormat, image.getWidth(), image.getHeight(), 0,
                        pixelFormat, GL.GL_UNSIGNED_BYTE, false, false, true, data, flusher);
            }
        }

        throw new IOException("Image could not be decoded: no suitable decoder could be found.");
    }

    /** Disallow class instantiation. */
    private GLUtilities()
    {
    }
}
