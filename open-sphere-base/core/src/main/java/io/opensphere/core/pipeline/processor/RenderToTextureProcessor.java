package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.RenderToTextureGeometry;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.PreloadedTextureImage;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.RenderToTexture;
import io.opensphere.core.pipeline.util.RenderToTextureImageProvider;
import io.opensphere.core.pipeline.util.RenderToTextureRenderer;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Process RenderToTextureGeometry and cause them to be rendered into a texture.
 */
public class RenderToTextureProcessor extends GeometryGroupProcessor<RenderToTextureGeometry>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RenderToTextureProcessor.class);

    /** The executor for the GL thread. */
    private final Executor myGLExecutor;

    /**
     * Construct me.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public RenderToTextureProcessor(ProcessorBuilder builder, GeometryRenderer<RenderToTextureGeometry> renderer)
    {
        super(RenderToTextureGeometry.class, builder, renderer);
        myGLExecutor = builder.getGLExecutor();
    }

    @Override
    public void close()
    {
        Collection<? extends RenderToTextureGeometry> allGeoms = getStateMachine().getAllObjects();
        for (RenderToTextureGeometry rttg : allGeoms)
        {
            RenderToTextureImageProvider tp = (RenderToTextureImageProvider)rttg.getTileGeometry().getImageManager()
                    .getImageProvider();
            myGLExecutor.execute(new Disposer(tp));
        }
        super.close();
    }

    @Override
    public RepaintListener createRepaintListener(RenderToTextureGeometry rttg)
    {
        return new RenderToTextureRepaintListener(rttg);
    }

    @Override
    public void generateDryRunGeometries()
    {
        receiveObjects(this, RenderToTextureProcessorHelper.generateDryRunGeometries(),
                Collections.<RenderToTextureGeometry>emptySet());
    }

    /**
     * Generate a texture by rendering to an off-line frame buffer.
     *
     * @param rc The render context.
     * @param imageProvider The image provider which will provide the generated
     *            texture.
     * @param rendToTex Handler for rendering to the off-line frame buffer.
     */
    public void generateTexture(RenderContext rc, RenderToTextureImageProvider imageProvider, RenderToTexture rendToTex)
    {
        Object handleKey = rendToTex.getTexture();
        TextureHandle handle = getCache().getCacheAssociation(handleKey, TextureHandle.class);
        if (handle == null)
        {
            int[] texId = new int[1];
            rc.getGL().glGenTextures(1, texId, 0);
            handle = new TextureHandle(texId[0], rendToTex.getSize(), rendToTex.getWidth(), rendToTex.getHeight());
            getCache().putCacheAssociation(handleKey, handle, TextureHandle.class, 0L, handle.getSizeGPU());
        }
        rendToTex.init(rc.getGL(), handle.getTextureId());
        rendToTex.doRenderToTexture(rc);

        if (!imageProvider.isReady())
        {
            PreloadedTextureImage image = new PreloadedTextureImage(handleKey, rendToTex.getWidth(), rendToTex.getHeight());

            // Provide the image to the RenderToTextureImageProvider.
            // Setting this will cause the observer for the tile
            // processor to receive a dataReady() which will re-process
            // the geometry.
            imageProvider.setImage(image, rc.getRenderMode());
        }
    }

    @Override
    public boolean isViable(RenderContext rc, Collection<String> warnings)
    {
        return super.isViable(rc, warnings) && rc.isMultiTextureAvailable(getClass().getSimpleName(), warnings)
                && rc.isFBOAvailable(getClass().getSimpleName(), warnings);
    }

    @Override
    protected synchronized void doReceiveObjects(Object source, Collection<? extends RenderToTextureGeometry> adds,
            Collection<? extends Geometry> removes)
    {
        for (Geometry geom : removes)
        {
            if (geom instanceof RenderToTextureGeometry)
            {
                RenderToTextureGeometry rttg = (RenderToTextureGeometry)geom;
                RenderToTextureImageProvider tp = (RenderToTextureImageProvider)rttg.getTileGeometry().getImageManager()
                        .getImageProvider();
                myGLExecutor.execute(new Disposer(tp));
            }
        }
        super.doReceiveObjects(source, adds, removes);
    }

    @Override
    protected MapContext<?> getMapContextForGroup(RenderToTextureGeometry group)
    {
        return group.getMapContext();
    }

    @Override
    protected void processRenderSubGeometries(Collection<? extends RenderToTextureGeometry> objects,
            StateController<RenderToTextureGeometry> controller)
    {
        RenderContext mainRenderContext = RenderContext.getCurrent();
        if (mainRenderContext == null)
        {
            LOGGER.error("Cannot render sub-geometries without a render context.");
            return;
        }

        for (RenderToTextureGeometry rttg : objects)
        {
            RenderToTextureImageProvider tp = (RenderToTextureImageProvider)rttg.getTileGeometry().getImageManager()
                    .getImageProvider();
            if (!tp.isReady())
            {
                // create the texture renderer and render to texture
                ScreenBoundingBox box = rttg.getRenderBox();
                ModelGeometryDistributor modelData = (ModelGeometryDistributor)getCachedData(rttg, null);
                if (modelData == null)
                {
                    // this can happen if during removal since repainting is
                    // triggered by the removal of the sub-geometries.
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Cannot find the model data for the render to texture geometry. " + rttg);
                    }
                    continue;
                }

                RenderToTexture pickRendToTex = tp.getRendToTexures().get(RenderMode.PICK);
                if (pickRendToTex == null)
                {
                    PrivateRenderer rend = new PrivateRenderer(modelData.getDistributor());
                    pickRendToTex = new RenderToTexture(rend, (int)box.getWidth(), (int)box.getHeight());
                    tp.setRenderToTexture(RenderMode.PICK, pickRendToTex);
                }
                RenderContext rc = mainRenderContext.derive(rttg.getMapContext(), RenderMode.PICK);
                generateTexture(rc, tp, pickRendToTex);

                RenderToTexture drawRendToTex = tp.getRendToTexures().get(AbstractGeometry.RenderMode.DRAW);
                if (drawRendToTex == null)
                {
                    PrivateRenderer rend = new PrivateRenderer(modelData.getDistributor());
                    drawRendToTex = new RenderToTexture(rend, (int)box.getWidth(), (int)box.getHeight());
                    drawRendToTex.setClearColor(rttg.getBackgroundColor());
                    tp.setRenderToTexture(AbstractGeometry.RenderMode.DRAW, drawRendToTex);
                }
                rc = mainRenderContext.derive(rttg.getMapContext(), RenderMode.DRAW);
                generateTexture(rc, tp, drawRendToTex);
            }
        }

        controller.changeState(objects, State.READY);
    }

    /**
     * Dispose the Textures for the RenderToTexture. This must occur on the GL
     * thread.
     */
    private static class Disposer implements Runnable
    {
        /** Textures which are to be disposed. */
        private final RenderToTextureImageProvider myImageProvider;

        /**
         * Constructor.
         *
         * @param tp The RenderToTexture(s) whose textures are to be disposed.
         */
        public Disposer(RenderToTextureImageProvider tp)
        {
            myImageProvider = tp;
        }

        @Override
        public void run()
        {
            GLContext currentGL = GLContext.getCurrent();
            if (currentGL == null)
            {
                LOGGER.error("Cannot render sub-geometries without the GL context.");
                return;
            }
            GL gl = currentGL.getGL();
            for (RenderToTexture tex : myImageProvider.getRendToTexures().values())
            {
                tex.clean(gl);
            }
        }
    }

    /** Provide a callback for the RenderToTexture. */
    private static class PrivateRenderer implements RenderToTextureRenderer
    {
        /** The distributor whose geometries will be rendered to the texture. */
        private final GeometryDistributor myDistributor;

        /**
         * Construct me.
         *
         * @param distrib The distributor whose geometries will be rendered to
         *            the texture.
         */
        public PrivateRenderer(GeometryDistributor distrib)
        {
            myDistributor = distrib;
        }

        @Override
        public void onRenderToTexture(RenderContext rc)
        {
            myDistributor.renderGeometries(rc);
        }
    }

    /**
     * Listener for repaint events from my sub processors. When a repaint is
     * requested, the sub-geometries can be checked to determine if the
     * GeometryGroupGeometry is ready to be rendered.
     */
    private class RenderToTextureRepaintListener implements RepaintListener
    {
        /** The geometry associated with this repaint listener. */
        private final RenderToTextureGeometry myRttg;

        /**
         * Construct me.
         *
         * @param rttg The geometry associated with this repaint listener.
         */
        public RenderToTextureRepaintListener(RenderToTextureGeometry rttg)
        {
            myRttg = rttg;
            ImageManager imageManager = myRttg.getTileGeometry().getImageManager();
            RenderToTextureImageProvider tp = (RenderToTextureImageProvider)imageManager.getImageProvider();
            tp.setRepaintListener(this);
        }

        @Override
        public void repaint()
        {
            ImageManager imageManager = myRttg.getTileGeometry().getImageManager();
            RenderToTextureImageProvider tp = (RenderToTextureImageProvider)imageManager.getImageProvider();
            tp.resetImages();
            imageManager.clearImages();
            resetState(Collections.<RenderToTextureGeometry>singleton(myRttg), GroupState.PROCESSING_STARTED);
        }
    }
}
