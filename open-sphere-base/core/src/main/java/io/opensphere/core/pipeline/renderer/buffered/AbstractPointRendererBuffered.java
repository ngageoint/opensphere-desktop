package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Level;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractPointRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.TimeFilteringRenderer;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Common behavior for buffered renderers of point-like geometries.
 *
 * @param <T> The geometry type.
 */
public abstract class AbstractPointRendererBuffered<T extends AbstractRenderableGeometry> extends AbstractPointRenderer<T>
implements TimeFilteringRenderer
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT
            | GL2.GL_TEXTURE_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

    /** Flag indicating if the renderer has been closed. */
    private volatile boolean myClosed;

    /** The helper for faded rendering. */
    private final FadedRenderingHelper myFadedRenderingHelper = new FadedRenderingHelper();

    /**
     * The time span that represents the earliest and latest times that might be
     * in the geometry constraints.
     */
    private volatile TimeSpan myGroupTimeSpan;

    /** The last render data used. */
    private TimeRenderData myLastRenderData;

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public AbstractPointRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void close()
    {
        super.close();
        myClosed = true;
        cleanBuffersFromCache();
    }

    @Override
    public void doRender(final RenderContext rc, final Collection<? extends T> input, Collection<? super T> rejected,
            final PickManager pickManager, MapContext<?> mapContext, final ModelDataRetriever<T> dataRetriever)
    {
        final TimeRenderData renderData = (TimeRenderData)getRenderData();
        if (renderData == null)
        {
            if (isLoggingEnabled(Level.TRACE))
            {
                log(Level.TRACE, "renderData is null in " + toString());
            }
            return;
        }
        try
        {
            if (myLastRenderData != null && !renderData.equals(myLastRenderData))
            {
                cleanBuffersFromCache();
            }
            myLastRenderData = renderData;
            prepareRenderContext(rc);

            TimeSpan groupTimeSpan = renderData.getGroupTimeSpan();
            getFadedRenderingHelper().renderEachTimeSpan(
                    rc, groupTimeSpan, () -> doRenderPoints(rc, input, rejected, pickManager, dataRetriever, renderData));

            // If close() was called during rendering, clean the buffers one
            // more time to catch anything that was just generated.
            if (isClosed())
            {
                cleanBuffersFromCache();
            }
        }
        finally
        {
            rc.getShaderRendererUtilities().cleanupShaders(rc.getGL());
            rc.popAttributes();
        }
    }

    @Override
    public int getAttribBits()
    {
        return ATTRIB_BITS;
    }

    @Override
    public int getClientAttribBits()
    {
        return CLIENT_ATTRIB_BITS;
    }

    /**
     * Get if the processor has been closed.
     *
     * @return {@code true} if the processor has been closed.
     */
    public final boolean isClosed()
    {
        return myClosed;
    }

    @Override
    public boolean setGroupInterval(TimeSpan span)
    {
        if (EqualsHelper.equals(span, myGroupTimeSpan))
        {
            return false;
        }
        myGroupTimeSpan = span;
        return true;
    }

    @Override
    public void setTimeManager(TimeManager timeManager)
    {
        myFadedRenderingHelper.setTimeManager(timeManager);
    }

    /**
     * Hook method for subclasses to know when the render data has changed.
     */
    protected void cleanBuffersFromCache()
    {
    }

    /**
     * Render each of the render data buffers.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param rejected Geometries that could not be rendered and need
     *            reprocessing.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     */
    protected abstract void doRenderPoints(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejected,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, TimeRenderData renderData);

    /**
     * Get the helper for faded rendering.
     *
     * @return The helper.
     */
    protected FadedRenderingHelper getFadedRenderingHelper()
    {
        return myFadedRenderingHelper;
    }

    /**
     * Get the group time span.
     *
     * @return The group time span.
     */
    protected TimeSpan getGroupInterval()
    {
        return myGroupTimeSpan;
    }

    /**
     * Prepare the render context for rendering.
     *
     * @param rc The render context.
     */
    protected void prepareRenderContext(RenderContext rc)
    {
        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            rc.getGL().glEnable(GL.GL_BLEND);
            rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /**
     * A factory for creating this renderer.
     *
     * @param <T> The geometry type.
     */
    protected abstract static class Factory<T extends Geometry> extends AbstractRenderer.Factory<T>
    {
        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.is15Available(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }
}
