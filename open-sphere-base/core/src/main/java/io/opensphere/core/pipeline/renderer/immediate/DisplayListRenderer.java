package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.DelegatingRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.collections.WeakHashSet;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * This is a special renderer that uses another renderer to create a display
 * list. The first time {@link #render} is called, the display list is created,
 * and then further renderings call the display list.
 *
 * @param <T> The type of Geometry being rendered
 */
public class DisplayListRenderer<T extends Geometry> extends AbstractRenderer<T> implements DelegatingRenderer<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DisplayListRenderer.class);

    /** Flag indicating if the drawing display list needs to be regenerated. */
    private volatile boolean myDrawDirty = true;

    /** The drawing display list id. */
    private int myDrawDisplayListId;

    /**
     * Keep track of last-picked geometries to know when the display lists are
     * obsolete.
     */
    private Set<Geometry> myLastPicked = Collections.emptySet();

    /** Flag indicating if the picking display list needs to be regenerated. */
    private volatile boolean myPickDirty = true;

    /** The picking display list id. */
    private int myPickDisplayListId;

    /** The renderer I delegate to when compiling the display list. */
    private final GeometryRendererImmediate<T> myRenderer;

    /**
     * Create a {@code DisplayListRenderer} factory.
     *
     * @param <U> The type of geometry to be rendered.
     * @param subfactory The delegate factory.
     * @return The DisplayListRenderer factory.
     */
    public static <U extends Geometry> Factory<U> createFactory(GeometryRendererImmediate.Factory<U> subfactory)
    {
        return new Factory<>(subfactory);
    }

    /**
     * Construct the renderer.
     *
     * @param renderer the immediate mode renderer that the display list
     *            renderer will delegate to
     */
    protected DisplayListRenderer(GeometryRendererImmediate<T> renderer)
    {
        super(null);
        myRenderer = renderer;
    }

    @Override
    public void addProjectionReadyListener(ProjectionReadyListener listener)
    {
        myRenderer.addProjectionReadyListener(listener);
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejected, PickManager pickManager,
            MapContext<?> mapContext, ModelDataRetriever<T> dataRetriever)
    {
        int listId = -1;
        boolean compileList = false;
        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            Set<Geometry> pickedGeometries = pickManager.getPickedGeometries();
            if (!myLastPicked.equals(pickedGeometries))
            {
                myDrawDirty = true;
                myLastPicked = new WeakHashSet<>(pickedGeometries);
            }
            if (myDrawDirty)
            {
                compileList = true;
                if (myDrawDisplayListId == 0)
                {
                    myDrawDisplayListId = rc.getGL().getGL2().glGenLists(1);
                }
                myDrawDirty = false;
            }
            listId = myDrawDisplayListId;
        }
        else if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK)
        {
            if (myPickDirty)
            {
                compileList = true;
                if (myPickDisplayListId == 0)
                {
                    myPickDisplayListId = rc.getGL().getGL2().glGenLists(1);
                }
                myPickDirty = false;
            }
            listId = myPickDisplayListId;
        }
        else
        {
            throw new UnexpectedEnumException(rc.getRenderMode());
        }

        myRenderer.initializeShaders(rc, input);

        if (compileList)
        {
            // Changed this from GL_COMPILE_AND_EXECUTE to GL_COMPILE because
            // GL_COMPILE_AND_EXECUTE would cause the application to crash on machines
            // with ATI cards with driver version 14.5.
            rc.getGL().getGL2().glNewList(listId, GL2.GL_COMPILE);
            try
            {
                doRender(rc, input, pickManager, mapContext, rejected, dataRetriever);
            }
            finally
            {
                rc.getGL().getGL2().glEndList();
            }
        }

        // Execute the list if we have a list id.
        if (listId != -1)
        {
            if (rc.getGL().glGetError() != GL.GL_NO_ERROR)
            {
                LOGGER.warn("gl error : " + rc.getGL().glGetError());
            }

            rc.getGL().getGL2().glCallList(listId);
        }

        myRenderer.cleanupShaders(rc, input);
    }

    @Override
    public int getAttribBits()
    {
        return myRenderer.getAttribBits();
    }

    @Override
    public int getClientAttribBits()
    {
        return myRenderer.getClientAttribBits();
    }

    @Override
    public GeometryRenderer<T> getRenderer()
    {
        return myRenderer;
    }

    @Override
    public Class<?> getType()
    {
        return myRenderer.getType();
    }

    @Override
    public boolean handleProjectionChanged(ProjectionChangedEvent evt)
    {
        // TODO I think this shouldn't be done until the projection is switched?
//        if (result)
//        {
//            myDrawDirty = true;
//            myPickDirty = true;
//        }
        return super.handleProjectionChanged(evt) | myRenderer.handleProjectionChanged(evt);
    }

    @Override
    public boolean handleViewChanged(Viewer view, ViewChangeType type)
    {
        boolean result = super.handleViewChanged(view, type) | myRenderer.handleViewChanged(view, type);
        if (result)
        {
            myDrawDirty = true;
            myPickDirty = true;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * This implementation marks the display lists dirty since model coordinates
     * may have been changed.
     */
    @Override
    public void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);

        myDrawDirty = true;
        myPickDirty = true;

        myRenderer.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
    }

    @Override
    public void setDirty()
    {
        myDrawDirty = true;
        myPickDirty = true;
    }

    @Override
    public synchronized void switchToProjection(Projection projectionSnapshot)
    {
        myDrawDirty = true;
        myPickDirty = true;
        myRenderer.switchToProjection(projectionSnapshot);
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Ask my embedded renderer to render.
     *
     * @param rc The render context.
     * @param input The geometries being rendered.
     * @param pickManager The pick manager.
     * @param mapContext The map context.
     * @param rejected Output geometries rejected by the renderer.
     * @param dataRetriever The model data retriever.
     */
    private void doRender(RenderContext rc, Collection<? extends T> input, PickManager pickManager, MapContext<?> mapContext,
            Collection<? super T> rejected, ModelDataRetriever<T> dataRetriever)
    {
        myRenderer.render(rc, input, rejected, pickManager, mapContext, dataRetriever, false);
        if (!rejected.isEmpty())
        {
            if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
            {
                myDrawDirty = true;
            }
            else if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK)
            {
                myPickDirty = true;
            }
            else
            {
                throw new UnexpectedEnumException(rc.getRenderMode());
            }
        }
    }

    /**
     * A factory for creating this renderer.
     *
     * @param <T> The type of geometry being rendered.
     */
    public static class Factory<T extends Geometry> implements GeometryRenderer.Factory<T>
    {
        /** The delegate renderer factory. */
        private final GeometryRendererImmediate.Factory<T> myRendererFactory;

        /**
         * Create the factory.
         *
         * @param subfactory The factory for the immediate-mode renderer.
         */
        public Factory(GeometryRendererImmediate.Factory<T> subfactory)
        {
            myRendererFactory = subfactory;
        }

        @Override
        public GeometryRenderer<T> createRenderer()
        {
            return new DisplayListRenderer<>(myRendererFactory.createRenderer());
        }

        @Override
        public Set<? extends String> getCapabilities()
        {
            return Collections.emptySet();
        }

        @Override
        public Collection<? extends DisposalHelper> getDisposalHelpers()
        {
            return myRendererFactory.getDisposalHelpers();
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return myRendererFactory.getType();
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }

        @Override
        public void setCache(CacheProvider cache)
        {
            myRendererFactory.setCache(cache);
        }
    }
}
