package io.opensphere.core.appl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogManager;

import javafx.application.Platform;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXBusyLabel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.Notify;
import io.opensphere.core.Plugin;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheInit;
import io.opensphere.core.cache.DatabaseAlreadyOpenException;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.pipeline.Pipeline;
import io.opensphere.core.pipeline.PipelineImpl;
import io.opensphere.core.util.MemoryUtilities;
import io.opensphere.core.util.SystemPropertyLoader;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.core.util.net.OpenSphereContentHandlerFactory;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * This is the nucleus for the application, from whence all else springs. It
 * owns all the top-level sub-systems, including:
 * <ul>
 * <li>The graphics pipeline</li>
 * <li>The map manager</li>
 * <li>The UI registry</li>
 * <li>The control registry</li>
 * <li>The data registry</li>
 * <li>The geometry registry</li>
 * <li>The plug-ins</li>
 * </ul>
 */
@SuppressWarnings("PMD.GodClass")
public class Kernel
{
    static
    {
        // If the splash screen is not forced to update, sometimes the full
        // image is not displayed.
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null)
        {
            splash.createGraphics();
            splash.update();
        }

        // Load additional system properties from a file. Do this before
        // anything else so that the properties can be used in the other
        // initialization routines.
        SystemPropertyLoader.loadSystemProperties();

        // This doesn't get set for JNLP, so set it explicitly
        TimeZone.setDefault(TimeZone.getTimeZone(System.getProperty("user.timezone")));

        // Initialize Java logging in case it is used by other components.
        initJavaLogging();

        // Set a handler for uncaught exceptions.
        UncaughtExceptionHandler.install();

        // Install the custom security manager.
        if (Boolean.getBoolean("opensphere.enableEnvoySecurity"))
        {
            new SecurityInit().setupSecurity();
        }

        // Initializing log4j causes Java logging to be initialized, so do this
        // after the custom Java logging initialization.
        LOGGER = Logger.getLogger(Kernel.class);
        MEM_LOGGER = Logger.getLogger(Kernel.class.getName() + ".Memory");
    }

    /** Log message indicating the main frame is about to be shown. */
    public static final String DISPLAYING_MAIN_FRAME_MSG = "Displaying main frame.";

    /** Log message indicating the app is shutting down. */
    public static final String SHUTTING_DOWN_MSG = "Shutting down.";

    /** Logger reference. */
    private static final Logger LOGGER;

    /** Logger for memory usage output. */
    private static final Logger MEM_LOGGER;

    /** Hold a reference to my listeners. */
    private final List<BoundEventListener> myControlEventListeners = new ArrayList<>();

    /** The collection of envoys. */
    private final Collection<Envoy> myEnvoys = new ArrayList<>();

    /** The manager for the executors. */
    private final ExecutorManager myExecutorManager = new ExecutorManager();

    /** Subscriber for lifecycle events. */
    private EventListener<ApplicationLifecycleEvent> myLifecycleSubscriber = new EventListener<ApplicationLifecycleEvent>()
    {
        /** Flag indicating if the pipeline has been initialized. */
        private boolean myPipelineInitialized;

        /** Flag indicating if the plugins have been initialized. */
        private boolean myPluginsInitialized;

        @Override
        public void notify(ApplicationLifecycleEvent event)
        {
            boolean recheck;
            switch (event.getStage())
            {
                case PIPELINE_INITIALIZED:
                    myPipelineInitialized = true;
                    recheck = true;
                    break;
                case PLUGINS_INITIALIZED:
                    myPluginsInitialized = true;
                    recheck = true;
                    break;
                default:
                    recheck = false;
                    break;
            }
            if (recheck && myPipelineInitialized && myPluginsInitialized)
            {
                myToolbox.getEventManager().unsubscribe(ApplicationLifecycleEvent.class, myLifecycleSubscriber);
                myLifecycleSubscriber = null;
                EventQueueUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LOGGER.info(DISPLAYING_MAIN_FRAME_MSG);
                        myPipelineComponent.setVisible(true);
                        if (Boolean.getBoolean("opensphere.enablePopToBack"))
                        {
                            // Toggle alwaysOnTop true then false to pop the
                            // window to the front, since some operating systems
                            // ignore the toFront call.
                            myMainFrame.setAlwaysOnTop(true);
                            myMainFrame.setAlwaysOnTop(false);
                        }
                        ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(),
                                ApplicationLifecycleEvent.Stage.MAIN_FRAME_VISIBLE);

                        if ("x86".equals(System.getProperty("os.arch"))
                                && StringUtils.isNotEmpty(System.getenv("ProgramW6432")))
                        {
                            JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                                    "<html>You are running with 32-bit Java on a 64-bit operating system. "
                                            + "The application may be unstable. Please use 64-bit Java if possible.</html>",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                        else
                        {
                            final String version = System.getProperty("java.version");
                            final String minVersion = System.getProperty("opensphere.min.java.version", "1.6.0_33");
                            if (compareVersions(version, minVersion) < 0)
                            {
                                JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                                        "<html>You are running an obsolete Java version (" + version
                                                + "). Please upgrade your Java if possible.</html>",
                                        "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        }

                        myToolbox.getMapManager().getStandardViewer().startAnimationToPreferredPosition();
                    }
                });
            }
        }

        /**
         * Compares the two versions.
         *
         * @param javaVersion The currently installed java version.
         * @param minVersion The minimum version.
         * @return 0 if equal, less than 0 if javaVersion is older than the
         *         minVersion, or greater than 0 if the javaVersion is newer
         *         than the min version.
         */
        private int compareVersions(String javaVersion, String minVersion)
        {
            int compare = 0;

            final String splitRegex = "\\.|_";
            final String[] javaVersions = javaVersion.split(splitRegex);
            final String[] minVersions = minVersion.split(splitRegex);

            for (int i = 0; i < javaVersions.length && i < minVersions.length && compare == 0; i++)
            {
                final int javaVersionPart = Integer.parseInt(javaVersions[i]);
                final int minVersionPart = Integer.parseInt(minVersions[i]);
                compare = Integer.compare(javaVersionPart, minVersionPart);
            }

            return compare;
        }
    };

    /** Helper for handling listeners for transformer and envoy adds/removes. */
    private final ToolboxListenerHelper myListenerHelper;

    /** The main JFrame. */
    private final JFrame myMainFrame;

    /**
     * The component created by the pipeline.
     */
    private Component myPipelineComponent;

    /** The collection of pipelines. */
    private final Collection<PipelineImpl> myPipelines = new ArrayList<>();

    /** The plug-in instances. */
    private final Collection<Plugin> myPluginInstances = new ArrayList<>();

    /** The post plugin initializer. */
    private final PostPluginInit myPostPluginInit;

    /** The toolbox, containing various application control interfaces. */
    private final ToolboxImpl myToolbox;

    /** The collection of transformers. */
    private final Collection<Transformer> myTransformers = new ArrayList<>();

    /** The cache initializer. */
    private final CacheInit myCacheInit = new CacheInit(myExecutorManager.createCacheExecutor());

    /**
     * Add the memory monitor if the logger is enabled.
     */
    private static void initializeMemoryLogger()
    {
        if (MEM_LOGGER.isTraceEnabled())
        {
            final double memoryDelta = .01;
            MemoryUtilities.addMemoryMonitor(memoryDelta, new MemoryUtilities.Callback()
            {
                @Override
                public void memoryMonitored(String usageString)
                {
                    MEM_LOGGER.trace("Memory: " + MemoryUtilities.getCurrentMemoryUse());
                }
            });
        }
    }

    /**
     * Initialize Java logging using a file on the classpath. If either
     * java.util.logging.config.class or java.util.logging.config.file is set,
     * this method takes no action.
     */
    private static void initJavaLogging()
    {
        try
        {
            LogManager.getLogManager();
        }
        catch (RuntimeException | Error e)
        {
            Logger.getLogger(Kernel.class).error("Failed to initialize java logging: " + e, e);
        }
    }

    /**
     * Construct the Kernel.
     */
    @SuppressFBWarnings("DM_EXIT")
    public Kernel()
    {
        logSystemInfo();

        initJavaFx();
        final Cache cache = initializeCache();

        final MainFrameInit mainFrameInit = new MainFrameInit();
        myMainFrame = mainFrameInit.getMainFrame();

        myToolbox = new ToolboxImpl(myExecutorManager, cache, myMainFrame)
        {
            @Override
            void requestRestart()
            {
                shutdown(2);
            }
        };
        myToolbox.getSystemToolbox().getSplashScreenManager().setInitMessage("Initializing Kernel");

        myListenerHelper = new ToolboxListenerHelper(myToolbox, myExecutorManager);
        myListenerHelper.open();

        myToolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifecycleSubscriber);

        myCacheInit.initializeCacheOptions(myToolbox.getUIRegistry().getOptionsRegistry(), myToolbox.getPreferencesRegistry());

        Notify.setToolbox(myToolbox);

        new PreConfigurationUpdateManager().checkForConfigChanges(myToolbox.getPreferencesRegistry());
        myPostPluginInit = new PostPluginInit(myToolbox);

        try
        {
            URLConnection.setContentHandlerFactory(OpenSphereContentHandlerFactory.getInstance());

            myPipelineComponent = initializePipeline();

            EventQueue.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        new LookAndFeelInit().setLookAndFeel();
                    }
                    catch (final UnsupportedLookAndFeelException e)
                    {
                        LOGGER.error(e, e);
                    }

                    ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(),
                            ApplicationLifecycleEvent.Stage.LAF_INSTALLED);

                    mainFrameInit.initialize(Kernel.this, myToolbox, myPipelineComponent);

                    myToolbox.finishBinding();
                }
            });

            myPluginInstances.addAll(new PluginInit(myToolbox).initializePlugins());

            ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(),
                    ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED);

            myPostPluginInit.open();

            initializeMemoryLogger();

            final ControlInit controlInit = new ControlInit(myToolbox);
            final BoundEventListener gcListener = controlInit.addGarbageCollectionControl(new Runnable()
            {
                @Override
                @SuppressFBWarnings("DM_GC")
                public void run()
                {
                    LOGGER.info("Memory before garbage collection: " + MemoryUtilities.getCurrentMemoryUse());
                    LOGGER.info("Requesting garbage collection...");
                    System.gc();
                    LOGGER.info("Memory after garbage collection: " + MemoryUtilities.getCurrentMemoryUse());
                }
            });
            myControlEventListeners.add(gcListener);
        }
        catch (InvocationTargetException | RuntimeException | InterruptedException e)
        {
            LOGGER.fatal("Exception during initialization: " + e, e);
            doShutdown(-1);
        }
    }

    /**
     * Initializes JavaFX stuff.
     */
    private void initJavaFx()
    {
        Platform.setImplicitExit(false);
        Platform.startup(() ->
        {
        });
    }

    /**
     * Shutdown the plug-ins, terminate the executors, and close the toolbox.
     * Exit the VM.
     *
     * @param exitCode The exit code.
     */
    @SuppressFBWarnings("DM_EXIT")
    protected final void doShutdown(int exitCode)
    {
        SuppressableRejectedExecutionHandler.getInstance().setSuppressed(true);

        try
        {
            LOGGER.info(SHUTTING_DOWN_MSG);
            ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(), ApplicationLifecycleEvent.Stage.BEGIN_SHUTDOWN);
            for (final Envoy envoy : myEnvoys)
            {
                try
                {
                    envoy.close();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Failed to close envoy: " + e, e);
                }
            }
            ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(), ApplicationLifecycleEvent.Stage.ENVOYS_CLOSED);
            for (final Transformer transformer : myTransformers)
            {
                try
                {
                    transformer.close();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Failed to close transformer: " + e, e);
                }
            }
            ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(),
                    ApplicationLifecycleEvent.Stage.TRANSFORMERS_CLOSED);
            for (final Plugin plugin : getPluginInstances())
            {
                try
                {
                    plugin.close();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Failed to close plug-in: " + e, e);
                }
            }
            ApplicationLifecycleEvent.publishEvent(myToolbox.getEventManager(), ApplicationLifecycleEvent.Stage.PLUGINS_CLOSED);

            for (final Pipeline pipe : myPipelines)
            {
                try
                {
                    pipe.close();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Failed to close pipeline: " + e, e);
                }
            }

            myPostPluginInit.close();
            myExecutorManager.shutdown();
            myListenerHelper.close();
            myToolbox.close();
        }
        finally
        {
            System.exit(exitCode);
        }
    }

    /**
     * Shutdown the system cleanly.
     *
     * @param exitCode The process exit code.
     */
    protected final synchronized void shutdown(final int exitCode)
    {
        final JDialog dialog = new JDialog((Window)null);
        dialog.setUndecorated(true);
        final JXBusyLabel label = new JXBusyLabel();
        label.setBusy(true);
        label.setPreferredSize(new Dimension(150, 75));
        label.setText("Shutting down...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.getContentPane().add(label);
        dialog.setLocationRelativeTo(null);
        dialog.pack();
        dialog.setVisible(true);
        if (myMainFrame != null)
        {
            myMainFrame.setVisible(false);
        }

        new Thread(() -> doShutdown(exitCode), "Shutdown").start();
    }

    /**
     * Access the collection of pipelines.
     *
     * @return The pipelines.
     */
    Collection<? extends Pipeline> getPipelines()
    {
        return myPipelines;
    }

    /**
     * Get the current plug-in instances.
     *
     * @return The plug-in instances.
     */
    private Collection<Plugin> getPluginInstances()
    {
        return myPluginInstances;
    }

    /**
     * Initialize the cache.
     *
     * @return The cache, or <code>null</code> if there is none.
     */
    private Cache initializeCache()
    {
        Cache cache;

        String path = System.getProperty("opensphere.db.path");
        if (path == null)
        {
            LOGGER.info("No opensphere.db.path property found; cache will not be initialized.");
            cache = null;
        }
        else
        {
            path = StringUtilities.expandProperties(path, System.getProperties());

            try
            {
                cache = myCacheInit.initializeCache(path);
            }
            catch (final CacheException e)
            {
                LOGGER.fatal(e, e);
                final String title = System.getProperty("opensphere.title");
                if (e.getCause() instanceof DatabaseAlreadyOpenException)
                {
                    JOptionPane.showMessageDialog(null,
                            "<html>Another instance of " + title
                                    + " is already running. Please close the other instance and try again.</html>",
                            title + " Fatal Initialization Error", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    try
                    {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (final IOException ioe)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug(ioe, ioe);
                        }
                    }
                    JOptionPane.showMessageDialog(null,
                            "<html>Cache initialization failed." + "<p/><p/>Try deleting the directory at <tt>" + path
                                    + "</tt> and restarting.</html>",
                            title + " Fatal Initialization Error", JOptionPane.ERROR_MESSAGE);
                }

                cache = null;
            }
        }
        if (cache == null)
        {
            LOGGER.error("No cache could be initialized.");
            shutdown(1);
        }

        return cache;
    }

    /**
     * Initialize the configured pipeline class and return its top-level
     * component.
     *
     * @return the top-level component
     */
    private Component initializePipeline()
    {
        final long start = System.nanoTime();

        final boolean productionMode = Boolean.getBoolean("opensphere.productionMode");
        PipelineImpl.setProduction(productionMode);

        LOGGER.info("Initializing Pipeline.");
        myToolbox.getSystemToolbox().getSplashScreenManager().setInitMessage("Initializing Pipeline");
        final PipelineImpl pipeline = new PipelineImpl();
        myPipelines.add(pipeline);

        Integer height = Integer.getInteger("opensphere.windowHeight");
        Integer width = Integer.getInteger("opensphere.windowWidth");
        final float defaultAspectRatio = 1.6f;
        float aspectRatio = defaultAspectRatio;
        final String aspectRatioString = System.getProperty("opensphere.windowAspectRatio");
        if (aspectRatioString != null)
        {
            try
            {
                aspectRatio = Float.parseFloat(aspectRatioString);
            }
            catch (final NumberFormatException e)
            {
                LOGGER.warn("Cannot parse float for opensphere.windowAspectRatio: " + e, e);
            }
        }
        if (height == null)
        {
            if (width == null)
            {
                final double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
                height = Integer.valueOf((int)(screenHeight - 150));
            }
            else
            {
                height = Integer.valueOf((int)(width.intValue() / aspectRatio));
            }
        }
        if (width == null)
        {
            width = Integer.valueOf((int)(height.intValue() * aspectRatio));
        }

        final Component pipelineComponent = pipeline.initialize(new Dimension(width.intValue(), height.intValue()), myToolbox,
                myExecutorManager.getPipelineExecutorService(), myExecutorManager.getPipelineScheduledExecutorService());

        final String frameRateString = System.getProperty("framerate");
        if (frameRateString != null)
        {
            try
            {
                final int frameRate = Integer.parseInt(frameRateString);
                pipeline.setFrameRate(frameRate);
            }
            catch (final NumberFormatException e)
            {
                LOGGER.warn("Failed to parse framerate property: " + e, e);
            }
        }

        myToolbox.getGeometryRegistry().setRenderingCapabilities(pipeline.getRenderingCapabilities());
        myToolbox.getGeometryRegistry().addSubscriber(pipeline.getGeometrySubscriber());

        LOGGER.info(StringUtilities.formatTimingMessage("Initialized pipeline in ", System.nanoTime() - start));

        return pipelineComponent;
    }

    /**
     * Log information about the runtime environment.
     */
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private void logSystemInfo()
    {
        try
        {
            ToStringHelper helper = new ToStringHelper((Class<?>)null, 0);
            helper.add("============== SYSTEM INFO =============");
            helper.add("CPU cores", Runtime.getRuntime().availableProcessors());
            OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
            if (mxBean instanceof com.sun.management.OperatingSystemMXBean)
            {
                com.sun.management.OperatingSystemMXBean sunBean = (com.sun.management.OperatingSystemMXBean)mxBean;
                helper.add("Physical memory", format(sunBean.getTotalPhysicalMemorySize()) + " ("
                        + format(sunBean.getFreePhysicalMemorySize()) + " free)");
                helper.add("Swap space",
                        format(sunBean.getTotalSwapSpaceSize()) + " (" + format(sunBean.getFreeSwapSpaceSize()) + " free)");
            }
            helper.add("Operating system", System.getProperty("os.name") + " v" + System.getProperty("os.version") + " ("
                    + System.getProperty("os.arch") + ')');
            helper.add("user.dir", System.getProperty("user.dir"));
            helper.add("user.timezone", System.getProperty("user.timezone"));
            helper.add("Java vendor", System.getProperty("java.vendor"));
            helper.add("Java version", System.getProperty("java.version"));
            helper.add("Java VM name", System.getProperty("java.vm.name"));
            helper.add("Java home", System.getProperty("java.home"));
            helper.add("Java max memory",
                    Runtime.getRuntime().maxMemory() / (1 << 20) + " MiB (" + Runtime.getRuntime().maxMemory() + " B)");
            helper.add("opensphere.path.runtime",
                    StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties()));
            helper.add("Application version", System.getProperty("opensphere.version", "unknown"));

            LOGGER.info("Starting " + System.getProperty("opensphere.title") + " at " + new Date());
            LOGGER.info(helper.toStringPreferenceDump());

            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("System properties: " + System.getProperties());
            }
        }
        catch (final SecurityException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to get system properties: " + e, e);
            }
        }
    }

    /**
     * Formats the value.
     *
     * @param bytes the value in bytes
     * @return the formatted string
     */
    private static String format(long bytes)
    {
        long mb = bytes / 1_000_000;
        return mb + " MB";
    }
}
