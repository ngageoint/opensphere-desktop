package io.opensphere.core.pipeline;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.MapManager;
import io.opensphere.core.MemoryManager.MemoryListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.orwell.GraphicsStatistics;
import io.opensphere.core.pipeline.cache.CacheContentListener;
import io.opensphere.core.pipeline.cache.CacheContentListener.CacheContentEvent;
import io.opensphere.core.pipeline.cache.CacheContentListener.ContentChangeType;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.cache.LRUMemoryCache;
import io.opensphere.core.pipeline.options.GraphicsOptionsProvider;
import io.opensphere.core.pipeline.processor.GeometryDistributor;
import io.opensphere.core.pipeline.processor.ProcessorBuilder;
import io.opensphere.core.pipeline.renderer.ShaderRendererUtilitiesGLSL;
import io.opensphere.core.pipeline.util.AnimatorManager;
import io.opensphere.core.pipeline.util.GL2PickManager;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities;
import io.opensphere.core.pipeline.util.TextureDataGroup;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.RateMeter;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.Viewer;

/**
 * Pipeline implementation.
 */
@SuppressWarnings("PMD.GodClass")
public class PipelineImpl implements GLEventListener, Pipeline, GenericSubscriber<Geometry>
{
    /** Logger used for frames-per-second output. */
    private static final Logger FPS_LOGGER = Logger.getLogger(PipelineImpl.class.getName() + ".FPS");

    /**
     * Maximum number of milliseconds that GL-dependent processing tasks are run
     * per render pass.
     */
    private static final long GL_QUEUE_TIME_BUDGET_MILLISECONDS = Long.getLong("opensphere.pipeline.glQueueBudgetMilliseconds", 100)
            .longValue();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PipelineImpl.class);

    /** Flag indicating if running in production mode. */
    private static boolean ourProduction = true;

    /** The time budget for rendering. */
    private static final long RENDER_BUDGET_MILLISECONDS = Long.getLong("opensphere.pipeline.renderBudgetMilliseconds", 1000)
            .longValue();

    /**
     * The time limit before a render pass is forced.
     */
    private static final long REPAINT_TIME_LIMIT_NANOSECONDS = Long.getLong("opensphere.pipeline.repaintTimeLimitNanoseconds", 5000000)
            .longValue();

    /** The animator manager. */
    private final AnimatorManager myAnimatorManager = new AnimatorManager();

    /** Facility for caching data calculated for geometries. */
    private LRUMemoryCache myCache;

    /** Helper class for creating and managing the canvas. */
    private PipelineCanvasHelper myCanvasHelper;

    /**
     * An executor used for running tasks that can happen on a non-render
     * thread.
     */
    private ExecutorService myExecutorService;

    /** My frame rate meter. */
    private final RateMeter myFrameRateMeter = new RateMeter(.7, (instant, average) ->
    {
        if (FPS_LOGGER.isTraceEnabled())
        {
            FPS_LOGGER.trace("fps: instant[" + (int)instant + "] average[" + (int)average + "]");
        }
    });

    /** Organizer and distributor of geometries to processors. */
    private GeometryDistributor myGeometryDistributor;

    /** An executor used for running tasks that must have a GL context. */
    private GLExecutor myGLExecutor;

    /** Flag indicating if my initialization is complete. */
    private volatile boolean myInitComplete;

    /** The initialization helper. */
    private PipelineGLInit myInitHelper;

    /** Flag indicating if initialization has been started. */
    private boolean myInitStarted;

    /**
     * The time as reported by {@link System#nanoTime()} that
     * {@link #display(GLAutoDrawable)} was last called.
     */
    private volatile long myLastDisplayTime;

    /** Variable that keeps track of the last point the mouse was on. */
    private Point myLastMousePoint;

    /** The system time of the last pick render. */
    private long myLastPickTime;

    /** Listener for memory events. */
    private MemoryListener myMemoryListener;

    /** Manager for pick colors. */
    private PickManager myPickManager;

    /** The pipeline menu initializer. */
    private PipelineMenuInit myPipelineMenuInit;

    /**
     * Helper for handling capture of the screen buffer and notification of
     * listeners when the capture has completed.
     */
    private PipelinePostRenderHelper myPostRenderHelper;

    /** The rendering capabilities. */
    private final RenderingCapabilitiesImpl myRenderingCapabilities = new RenderingCapabilitiesImpl()
    {
        @Override
        public void waitForInitComplete()
        {
            PipelineImpl.this.waitForInitComplete();
        }
    };

    /** Listener for repaint requests. */
    private final RepaintListener myRepaintListener = new RepaintListener()
    {
        /**
         * The last time as reported by {@link System#nanoTime()} that a call to
         * {@link AnimatorManager#displayNow()} was queued.
         */
        private volatile long myLastDisplayNowTime;

        /**
         * The last time as reported by {@link System#nanoTime()} that a repaint
         * request was made.
         */
        private volatile long myLastRepaintRequestTime;

        @Override
        public void repaint()
        {
            if (myAnimatorManager.getFramesPerSecond() == 0)
            {
                /**
                 * Enqueue a call to display if the following conditions are
                 * met:
                 * <ul>
                 * <li>The time since the last display pass is longer than the
                 * threshold.</li>
                 * <li>The last time these conditions were met is before the
                 * last display pass.</li>
                 * <li>The last call to repaint is after the last display pass.
                 * </li>
                 * </ul>
                 */
                long nanoTime = System.nanoTime();
                long lastRepaintRequestTime = myLastRepaintRequestTime;
                myLastRepaintRequestTime = nanoTime;
                if (nanoTime - getLastDisplayTime() > REPAINT_TIME_LIMIT_NANOSECONDS
                        && myLastDisplayNowTime < getLastDisplayTime() && lastRepaintRequestTime > getLastDisplayTime())
                {
                    myLastDisplayNowTime = nanoTime;
                    EventQueueUtilities.invokeLater(() -> myAnimatorManager.displayNow());
                }
                else if (myCanvasHelper.getCanvas() != null)
                {
                    myCanvasHelper.getCanvas().repaint();
                }
            }
        }
    };

    /**
     * Swing timer used to repaint the canvas until
     * {@link #init(GLAutoDrawable)} gets called. In some environments, GL will
     * not initialize unless the canvas gets painted, and it will not get
     * painted unless it is visible on-screen (or repaint is called).
     */
    private Timer myRepaintTimer;

    /**
     * An executor used for running tasks that can happen on a non-render
     * thread.
     */
    private ScheduledExecutorService myScheduledExecutorService;

    /** The shader renderer utilities. */
    private final ShaderRendererUtilities myShaderRendererUtilities;

    /**
     * Listener notified when a {@link TextureDataGroup} is removed from the
     * cache.
     */
    private final CacheContentListener<TextureDataGroup> myTextureDataGroupRemovalListener = (event) -> flushDataGroup(event);

    /**
     * My toolbox containing the geometry registry, map manager, control
     * registry, etc.
     */
    private Toolbox myToolbox;

    /**
     * Constructor.
     */
    public PipelineImpl()
    {
        String shaderRendererUtilitiesClass = System.getProperty("opensphere.pipeline.shaderRendererUtilities.class",
                ShaderRendererUtilitiesGLSL.class.getName());

        ShaderRendererUtilities shaderUtilities;
        try
        {
            shaderUtilities = (ShaderRendererUtilities)Class.forName(shaderRendererUtilitiesClass).getDeclaredConstructor().newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            LOGGER.fatal("Cannot load shader utilities class [" + shaderRendererUtilitiesClass + "]: " + e, e);
            shaderUtilities = null;
        }

        myShaderRendererUtilities = shaderUtilities;
    }

    /**
     * Gets the set of changed items from the supplied event, and flushes each.
     *
     * @param event the event to flush.
     */
    protected void flushDataGroup(CacheContentEvent<TextureDataGroup> event)
    {
        for (TextureDataGroup textureDataGroup : event.getChangedItems())
        {
            textureDataGroup.flush();
        }
    }

    /**
     * Get if the pipeline is running in production mode.
     *
     * @return <code>true</code> if production mode.
     */
    public static boolean isProduction()
    {
        return ourProduction;
    }

    /**
     * Set if the pipeline is running in production mode.
     *
     * @param production Indicates production mode.
     */
    public static void setProduction(boolean production)
    {
        ourProduction = production;
        LOGGER.info("Production mode is " + (ourProduction ? "on." : "off."));

        GLUtilities.setProduction(production);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.pipeline.Pipeline#close()
     */
    @Override
    public synchronized void close()
    {
        myCanvasHelper.close();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
     */
    @Override
    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    @SuppressWarnings("PMD.GuardLogStatement")
    public void display(GLAutoDrawable drawable)
    {
        try
        {
            myLastDisplayTime = System.nanoTime();
            LOGGER.trace("Begin render pass");
            determineFPS();

            TimeBudget renderBudget = TimeBudget.startMilliseconds(RENDER_BUDGET_MILLISECONDS);
            TimeBudget queueBudget = renderBudget.subBudgetMilliseconds(GL_QUEUE_TIME_BUDGET_MILLISECONDS);
            RenderContext rc = createRenderContext(drawable.getGL(), (RenderMode)null, queueBudget);
            RenderContext.setCurrent(rc);
            try
            {
                if (!myInitHelper.isReadyToInit())
                {
                    LOGGER.trace("InitHelper is not ready to init.");
                    return;
                }
                LOGGER.trace("Waiting for lock");
                synchronized (this)
                {
                    LOGGER.trace("Got lock");
                    if (!myInitComplete)
                    {
                        if (myInitStarted)
                        {
                            LOGGER.trace("Init is already in progress.");
                            return;
                        }
                        myInitStarted = true;

                        if (myGeometryDistributor != null)
                        {
                            myGeometryDistributor.close();
                        }

                        // Create a new GL executor so that queued tasks that
                        // need the GL context will be discarded.
                        myGLExecutor = new GLExecutor(myRepaintListener);
                        ProcessorBuilder processorBuilder = ProcessorBuilderInit.createProcessorBuilder(myToolbox, myCache,
                                myPickManager, myExecutorService, myScheduledExecutorService, myGLExecutor, myRepaintListener);

                        myToolbox.getSystemToolbox().getSplashScreenManager().setInitMessage("Performing Pipeline Dry-runs");
                        myInitHelper.init(rc, myCache, processorBuilder, myRenderingCapabilities);

                        myPostRenderHelper.setDisposalHelpers(myInitHelper.getDisposalHelpers(processorBuilder));

                        myGeometryDistributor = new GeometryDistributor(processorBuilder);
                        myGeometryDistributor.updateGeometries(myToolbox.getGeometryRegistry().getGeometries(),
                                Collections.<Geometry>emptySet());
                        myInitComplete = true;
                        notifyAll();

                        ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(),
                                ApplicationLifecycleEvent.Stage.PIPELINE_INITIALIZED);

                        captureStatistics(rc);
                    }
                }

                LOGGER.trace("Running runners");
                for (Runnable runner; !queueBudget.isExpired() && (runner = myGLExecutor.poll()) != null;)
                {
                    LOGGER.trace("Running runner");
                    runner.run();
                }
            }
            catch (RuntimeException | Error e)
            {
                delayExit("Exception during render: " + e, e);
            }
            finally
            {
                RenderContext.setCurrent((RenderContext)null);
            }
            if (renderBudget.isExpired())
            {
                LOGGER.debug("Render budget expired before render was called.");

                if (myAnimatorManager.getFramesPerSecond() >= 0)
                {
                    // Schedule a repaint to continue processing the queue.
                    myRepaintListener.repaint();
                }
            }

            LOGGER.trace("Calling render");
            render(drawable.getGL(), renderBudget);
            LOGGER.trace("End render pass");
        }
        catch (RuntimeException | Error e)
        {
            delayExit("Failed to display GL canvas: " + e, e);
        }
    }

    /**
     * Captures statistics for the graphics environment, using the supplied render context as a data source.
     *
     * @param pRenderContext the context from which statistics are read.
     */
    protected void captureStatistics(RenderContext pRenderContext)
    {
        GraphicsStatistics graphicsStatistics = myToolbox.getStatisticsManager().getGraphicsStatistics();
        graphicsStatistics.setEnvironmentIdentifier(pRenderContext.getEnvironmentIdentifier());
        graphicsStatistics.setContextVersion(pRenderContext.getGLVersion());
        graphicsStatistics.setGpuMemorySizeBytes(pRenderContext.getGPUMemorySizeBytes());

        GL gl = pRenderContext.getGL();
        GLContext glContext = gl.getContext();
        GLProfile glProfile = gl.getGLProfile();

        graphicsStatistics.setSwapInterval(gl.getSwapInterval());

        graphicsStatistics.setGlImplBaseClassName(glProfile.getGLImplBaseClassName());
        graphicsStatistics.setImplName(glProfile.getImplName());
        graphicsStatistics.setName(glProfile.getName());
        graphicsStatistics.setHardwareRasterizer(glProfile.isHardwareRasterizer());
        graphicsStatistics.setGlRenderer(gl.glGetString(GL.GL_RENDERER));
        graphicsStatistics.setGlVendor(gl.glGetString(GL.GL_VENDOR));
        graphicsStatistics.setGlVersion(gl.glGetString(GL.GL_VERSION));
        graphicsStatistics.setGlExtensionsString(glContext.getGLExtensionsString());
        graphicsStatistics.setPlatformExtensionsString(glContext.getPlatformExtensionsString());
        graphicsStatistics.setHasGLSL(gl.hasGLSL());
        graphicsStatistics.setGlCompileShaderAvailable(gl.isFunctionAvailable("glCompileShader"));
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.media.opengl.GLEventListener#dispose(javax.media.opengl.GLAutoDrawable)
     */
    @Override
    public void dispose(GLAutoDrawable drawable)
    {
        LOGGER.info("Disposing GL context.");
        synchronized (this)
        {
            myInitComplete = false;
            myInitStarted = false;
        }

        myShaderRendererUtilities.clear(drawable.getGL());
        myPostRenderHelper.forceDispose(drawable.getGL());

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Calling repaint");
        }
        myRepaintListener.repaint();
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Dispose done");
        }
    }

    /**
     * Get the geometry cache.
     *
     * @return The cache provider.
     */
    public CacheProvider getCache()
    {
        return myCache;
    }

    /**
     * Get my geometry subscriber.
     *
     * @return The geometry subscriber.
     */
    @Override
    public GenericSubscriber<Geometry> getGeometrySubscriber()
    {
        return this;
    }

    /**
     * Get the last screen position of the mouse cursor.
     *
     * @return The last screen position.
     */
    public Point getLastMousePoint()
    {
        Point lastMousePoint = myLastMousePoint;
        myLastMousePoint = null;
        return lastMousePoint;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.pipeline.Pipeline#getRenderingCapabilities()
     */
    @Override
    public RenderingCapabilities getRenderingCapabilities()
    {
        return myRenderingCapabilities;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
     */
    @Override
    public void init(GLAutoDrawable drawable)
    {
        if (myRepaintTimer != null)
        {
            myRepaintTimer.stop();
            myRepaintTimer = null;
        }

        RenderContext rc = createRenderContext(drawable.getGL(), RenderMode.DRAW, TimeBudget.INDEFINITE);
        final String environmentIdentifier = rc.getEnvironmentIdentifier();

        String displayListPrefsKey = "useDisplayLists." + environmentIdentifier;
        String safeModePrefsKey = "safemode." + environmentIdentifier;
        Preferences pipelinePrefs = myToolbox.getPreferencesRegistry().getPreferences(Pipeline.class);
        myInitHelper = new PipelineGLInit(myCanvasHelper.getCanvas(), pipelinePrefs, displayListPrefsKey, safeModePrefsKey,
                myToolbox.getPreferencesRegistry());

        GraphicsOptionsProvider optionsProvider = new GraphicsOptionsProvider(myToolbox, pipelinePrefs, displayListPrefsKey,
                safeModePrefsKey);

        myToolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(optionsProvider);

        // Whenever the NEWT canvas is being used, be sure to put all Swing
        // activities on the EDT.
        EventQueueUtilities.runOnEDT(() ->
        {
            myInitHelper.displayInitDialogs();
            myInitHelper.initializeMenus(environmentIdentifier, myPipelineMenuInit);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.pipeline.Pipeline#initialize(java.awt.Dimension,
     *      io.opensphere.core.Toolbox, java.util.concurrent.ExecutorService,
     *      java.util.concurrent.ScheduledExecutorService)
     */
    @Override
    public synchronized Component initialize(Dimension preferredSize, Toolbox toolbox, ExecutorService executorService,
            ScheduledExecutorService scheduledExecutorService)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Initializing pipeline with preferred size of " + preferredSize);
        }

        myAnimatorManager.reset();

        myCanvasHelper = new PipelineCanvasHelper(this, toolbox, preferredSize);

        myRepaintTimer = new RepaintTimer(1000, myCanvasHelper.getCanvas());
        myRepaintTimer.start();

        myToolbox = toolbox;
        myCanvasHelper.getCanvas().addMouseMotionListener(new MouseAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                setLastMousePoint(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                setLastMousePoint(e.getPoint());

                // Repaint after mouse movement for picking.
                getRepaintListener().repaint();
            }
        });

        myPickManager = new GL2PickManager(myToolbox.getControlRegistry());
        PipelineControlInit.initialize(myToolbox.getControlRegistry(), myCanvasHelper.getCanvas(), toolbox.getUIRegistry());

        myPipelineMenuInit = new PipelineMenuInit(toolbox);
        myPipelineMenuInit.init();

        myAnimatorManager.setDrawable(myCanvasHelper.getDrawable());

        myCache = new LRUMemoryCache(toolbox.getMetricsRegistry());
        myCache.registerContentListener(myTextureDataGroupRemovalListener, ContentChangeType.REMOVAL, TextureDataGroup.class);

        myMemoryListener = new PipelineMemoryListener(myCache);
        toolbox.getSystemToolbox().getMemoryManager().addMemoryListener(myMemoryListener);

        myExecutorService = executorService;

        myScheduledExecutorService = scheduledExecutorService;
        myPostRenderHelper = new PipelinePostRenderHelper(myToolbox, myRepaintListener, myCache);

        return myCanvasHelper.getCanvas();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.messaging.GenericSubscriber#receiveObjects(java.lang.Object,
     *      java.util.Collection, java.util.Collection)
     */
    @Override
    public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        waitForInitComplete();

        myGeometryDistributor.updateGeometries(adds, removes);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Callback executed when the canvas is reshaped. This will instruct the
     * current viewer to reshape itself.
     * </p>
     *
     * @param drawable The GL drawable.
     * @param x The x position of the canvas.
     * @param y The y position of the canvas.
     * @param width The width of the canvas.
     * @param height The height of the canvas.
     *
     * @see io.opensphere.core.pipeline.Pipeline#setFrameRate(int)
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        try
        {
            getMapManager().reshape(width, height);
            float[] matrix = getViewer().getProjectionMatrix();

            GL gl = drawable.getGL();
            gl.getGL2().glMatrixMode(GLMatrixFunc.GL_PROJECTION);
            gl.getGL2().glLoadMatrixf(matrix, 0);
        }
        catch (RuntimeException | Error e)
        {
            delayExit("Failed to reshape GL canvas: " + e, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.pipeline.Pipeline#setFrameRate(int)
     */
    @Override
    public synchronized void setFrameRate(int framesPerSecond)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Frame rate set to " + framesPerSecond);
        }
        myAnimatorManager.setFrameRate(framesPerSecond);
    }

    /**
     * Block until the graphics initialization is complete.
     */
    public void waitForInitComplete()
    {
        while (true)
        {
            synchronized (this)
            {
                if (myInitComplete)
                {
                    return;
                }
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                    LOGGER.trace("Pipeline was interrupted during wait.", e);
                }
            }
        }
    }

    /**
     * Cause the VM to exit due to some fatal error, after a slight delay.
     *
     * @param message An error message.
     * @param t An optional throwable.
     */
    @SuppressFBWarnings("DM_EXIT")
    protected final void delayExit(final String message, final Throwable t)
    {
        final long delayMilliseconds = 1000L;
        Runnable task = () ->
        {
            LOGGER.fatal(message, t);
            systemExit(1);
        };
        CommonTimer.schedule(task, delayMilliseconds);
    }

    /**
     * Tell the frame rate meter to count a frame.
     */
    protected void determineFPS()
    {
        if (FPS_LOGGER.isTraceEnabled())
        {
            myFrameRateMeter.increment();
        }
    }

    /**
     * Get the time as reported by {@link System#nanoTime()} that
     * {@link #display(GLAutoDrawable)} was last called.
     *
     * @return The time in nanoseconds.
     */
    protected long getLastDisplayTime()
    {
        return myLastDisplayTime;
    }

    /**
     * Get the map manager.
     *
     * @return The map manager.
     */
    protected MapManager getMapManager()
    {
        return myToolbox.getMapManager();
    }

    /**
     * Get the pick manager.
     *
     * @return The pick manager.
     */
    protected PickManager getPickManager()
    {
        return myPickManager;
    }

    /**
     * Get the current viewer.
     *
     * @return The viewer.
     */
    protected Viewer getViewer()
    {
        return getMapManager().getStandardViewer();
    }

    /**
     * Perform operations that are necessary after each call to
     * {@link GeometryDistributor#renderGeometries(RenderContext)} .
     *
     * @param gl The OpenGL interface.
     */
    protected void postRender(GL gl)
    {
        myPostRenderHelper.postRender(gl);
    }

    /**
     * The main rendering method. This loads the model-view matrix, clears the
     * frame buffer, then makes the following calls to subordinate methods:
     *
     * <ul>
     * <li>{@link GeometryDistributor#renderGeometries(RenderContext)} in PICK
     * mode</li>
     * <li>{@link PickManager#determinePicks(GL, int, int)}</li>
     * <li>{@link GeometryDistributor#renderGeometries(RenderContext)} in DRAW
     * mode</li>
     * <li>{@link #postRender(GL)}</li>
     * </ul>
     *
     * @param gl The OpenGL interface.
     * @param timeBudget The time budget for rendering.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    protected void render(GL gl, TimeBudget timeBudget)
    {
        Viewer viewer = getViewer();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.getGL2().glLineStipple(1, (short)0xFFFF);

        // Do a pick render on every pass if not rendering rapidly.
        boolean pickPass;
        if (myAnimatorManager.getFramesPerSecond() == 0)
        {
            pickPass = true;
        }
        else
        {
            long now = System.currentTimeMillis();
            if (now - myLastPickTime > 100)
            {
                pickPass = true;
                myLastPickTime = now;
            }
            else
            {
                pickPass = false;
            }
        }
        if (pickPass)
        {
            Point pickPoint = getLastMousePoint();
            if (pickPoint != null)
            {
                RenderContext renderContext = createRenderContext(gl, AbstractGeometry.RenderMode.PICK, timeBudget);
                myGeometryDistributor.renderGeometries(renderContext);
                getPickManager().determinePicks(gl, pickPoint.x, viewer.getViewportHeight() - pickPoint.y);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            }
        }

        RenderContext renderContext = createRenderContext(gl, AbstractGeometry.RenderMode.DRAW, timeBudget);
        myGeometryDistributor.renderGeometries(renderContext);

        postRender(gl);
    }

    /**
     * Set the last screen position of the mouse cursor.
     *
     * @param point The screen point.
     */
    protected void setLastMousePoint(Point point)
    {
        myLastMousePoint = point;
    }

    /**
     * Wrapper for the system exit call.
     *
     * @param status The exit status.
     */
    protected final void systemExit(int status)
    {
        System.exit(status);
    }

    /**
     * Create a render context.
     *
     * @param gl The GL context.
     * @param renderMode The render mode.
     * @param timeBudget The time budget.
     * @return The render context.
     */
    protected RenderContext createRenderContext(GL gl, RenderMode renderMode, TimeBudget timeBudget)
    {
        return new RenderContext(gl, getMapManager(), renderMode, myShaderRendererUtilities, timeBudget);
    }

    /**
     * Get the listener for repaint events.
     *
     * @return The listener for repaint events.
     */
    protected RepaintListener getRepaintListener()
    {
        return myRepaintListener;
    }
}
