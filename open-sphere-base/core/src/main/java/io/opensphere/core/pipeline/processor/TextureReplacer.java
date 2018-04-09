package io.opensphere.core.pipeline.processor;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.ImageGroup;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.ImageProvidingGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Helper for replacing all or part of the on card image associated with a
 * geometry. This should be executed on the GL thread since it directly modifies
 * video memory.
 */
public class TextureReplacer implements Runnable
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(TextureReplacer.class);

    /** The geometry whose image is being in part or fully replaced. */
    private final List<? extends ImageProvidingGeometry<?>> myGeometries;

    /** The on card texture which is being in part or fully replaced. */
    private final List<? extends TextureHandle> myTextures;

    /**
     * Constructor.
     *
     * @param tiles The geometries whose image is being in part or fully
     *            replaced.
     * @param textures The on card textures which are being in part or fully
     *            replaced.
     */
    public TextureReplacer(List<? extends ImageProvidingGeometry<?>> tiles, List<? extends TextureHandle> textures)
    {
        Utilities.checkNull(tiles, "tiles");
        Utilities.checkNull(textures, "textures");
        myGeometries = New.unmodifiableList(tiles);
        myTextures = New.unmodifiableList(textures);
        if (myGeometries.size() != myTextures.size())
        {
            throw new IllegalArgumentException(
                    "Number of tiles [" + tiles.size() + "] does not match number of textures [" + textures.size() + "]");
        }
    }

    @Override
    public void run()
    {
        for (int index = 0; index < myGeometries.size(); ++index)
        {
            replaceTexture(myGeometries.get(index), myTextures.get(index));
        }
    }

    /**
     * Do the texture replacement.
     *
     * @param geometry The geometry.
     * @param handle The texture handle.
     */
    protected void replaceTexture(ImageProvidingGeometry<?> geometry, TextureHandle handle)
    {
        synchronized (geometry)
        {
            ImageGroup imageData = geometry.getImageManager().pollCachedImageData();
            if (imageData == null)
            {
                // If a previous task already processed my image, then we are
                // done.
                return;
            }
            try
            {
                Image image = imageData.getImageMap().get(AbstractGeometry.RenderMode.DRAW);

                Collection<? extends ImageManager.DirtyRegion> dirtyRegions = geometry.getImageManager().pollDirtyRegions();
                int imageHeight = image.getHeight();
                int imageWidth = image.getWidth();
                if (dirtyRegions.isEmpty())
                {
                    // If there are no dirty regions, but we have an image,
                    // replace the whole thing.
                    dirtyRegions = Collections.singleton(new ImageManager.DirtyRegion(0, imageWidth, 0, imageHeight));
                }

                GLContext currentGL = GLContext.getCurrent();
                if (currentGL == null)
                {
                    LOGGER.error("Cannot replace texture without the GL context.");
                    return;
                }
                GL gl = currentGL.getGL();

                if (image instanceof ImageIOImage
                        && ((ImageIOImage)image).getAWTImage().getType() == BufferedImage.TYPE_4BYTE_ABGR
                        && RenderContext.getCurrent().isExtensionAvailable("GL_EXT_abgr"))
                {
                    gl.glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                    for (ImageManager.DirtyRegion reg : dirtyRegions)
                    {
                        if (reg.getMinX() == 0 && reg.getMinY() == 0 && reg.getWidth() == image.getWidth()
                                && reg.getHeight() == image.getHeight())
                        {
                            ByteBuffer bytes = image.getByteBuffer();
                            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, reg.getWidth(), reg.getHeight(), 0, GL2.GL_ABGR_EXT,
                                    GL.GL_UNSIGNED_BYTE, bytes);
                            break;
                        }
                        else
                        {
                            if (reg.getMinX() < 0 || reg.getMinY() < 0 || reg.getMaxX() > imageWidth
                                    || reg.getMaxY() > imageHeight)
                            {
                                StringBuilder builder = new StringBuilder("The dirty region ");
                                builder.append(reg).append(" is out side of the image bounds (").append(imageWidth).append(", ")
                                        .append(imageHeight).append(").");
                                LOGGER.error(builder.toString());
                                continue;
                            }

                            ByteBuffer bytes = image
                                    .getByteBuffer(new Rectangle(reg.getMinX(), reg.getMinY(), reg.getWidth(), reg.getHeight()));
                            gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, reg.getMinX(), reg.getMinY(), reg.getWidth(), reg.getHeight(),
                                    GL2.GL_ABGR_EXT, GL.GL_UNSIGNED_BYTE, bytes);
                        }
                    }
                }
                else
                {
                    TextureData td = GLUtilities.createTextureData(geometry, image, false);
                    if (td != null)
                    {
                        try
                        {
                            Texture texture = new Texture(handle.getTextureId(), GL.GL_TEXTURE_2D, handle.getWidth(),
                                    handle.getHeight(), handle.getWidth(), handle.getHeight(), true);

                            for (ImageManager.DirtyRegion reg : dirtyRegions)
                            {
                                if (reg.getMinX() == 0 && reg.getMinY() == 0 && reg.getWidth() == image.getWidth()
                                        && reg.getHeight() == image.getHeight())
                                {
                                    texture.updateImage(gl, td, GL.GL_TEXTURE_2D);
                                    break;
                                }
                                else
                                {
                                    if (reg.getMinX() < 0 || reg.getMinY() < 0 || reg.getMaxX() > imageWidth
                                            || reg.getMaxY() > imageHeight)
                                    {
                                        LOGGER.warn("Skipping out of bounds image update region " + reg + " for geometry "
                                                + geometry);
                                        continue;
                                    }

                                    texture.updateSubImage(gl, td, 0, reg.getMinX(), reg.getMinY(), reg.getMinX(), reg.getMinY(),
                                            reg.getWidth(), reg.getHeight());
                                }
                            }
                        }
                        finally
                        {
                            td.flush();
                        }
                    }
                }
                GLUtilities.checkGLErrors(gl, LOGGER, "After texture replacement of image: " + image);
            }
            finally
            {
                imageData.dispose();
            }
        }
    }
}
