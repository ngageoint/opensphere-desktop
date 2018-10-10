package io.opensphere.core.pipeline;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.media.nativewindow.AbstractGraphicsDevice;
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.cache.LRUMemoryCache;
import io.opensphere.core.pipeline.processor.GeometryProcessor;
import io.opensphere.core.pipeline.processor.GeometryRendererSet;
import io.opensphere.core.pipeline.processor.ProcessorBuilder;
import io.opensphere.core.pipeline.processor.RenderableGeometryProcessor;
import io.opensphere.core.pipeline.processor.TextureDisposalHelper;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.InlineExecutorService;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.core.util.swing.AutohideMessageDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Helper class that performs GL-related initialization for the Pipeline.
 */
public class PipelineGLInit
{
    /** A warning to display if display lists are disabled. */
    private static final String DISPLAY_LIST_WARNING = StringUtilities
            .expandProperties(System.getProperty("opensphere.pipeline.displayListWarning"), System.getProperties());

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PipelineGLInit.class);

    /** A warning to display if safe mode is enabled. */
    private static final String SAFE_MODE_WARNING = StringUtilities
            .expandProperties(System.getProperty("opensphere.pipeline.safeModeWarning"), System.getProperties());

    /** A warning to display if a rendering component cannot be used. */
    private static final String VIABILITY_WARNING = StringUtilities
            .expandProperties(System.getProperty("opensphere.pipeline.viabilityWarning"), System.getProperties());

    /** The component used to position my dialogs. */
    private final Component myComponent;

    /**
     * Latch to count down before init can proceed.
     */
    private final AtomicInteger myInitBarrier = new AtomicInteger();

    /** The preferences. */
    private final Preferences myPrefs;

    /** The preferences key for using safe mode. */
    private final String mySafeModePrefsKey;

    /** The preferences key for using display lists. */
    private final String myDisplayListPrefsKey;

    /** The driver checker. */
    private final DriverChecker myDriverChecker;

    /**
     * Constructor.
     *
     * @param component The component used to place dialogs.
     * @param prefs The preferences.
     * @param safeModePrefsKey The preferences key for using safe mode.
     * @param displayListPrefsKey The preferences key for using display lists.
     * @param preferencesRegistry The preferences registry
     */
    public PipelineGLInit(Component component, Preferences prefs, String displayListPrefsKey, String safeModePrefsKey,
            PreferencesRegistry preferencesRegistry)
    {
        super();
        myComponent = component;
        myPrefs = prefs;
        myPrefs.printPrefs();
        myDisplayListPrefsKey = displayListPrefsKey;
        mySafeModePrefsKey = safeModePrefsKey;
        myDriverChecker = new DriverChecker(component, prefs, preferencesRegistry, displayListPrefsKey);
    }

    /**
     * Display the dialogs that must be shown before the Pipeline is
     * initialized.
     */
    public void displayInitDialogs()
    {
        boolean useDisplayLists = myPrefs.getBoolean(getDisplayListsPrefsKey(),
                Boolean.getBoolean("opensphere.pipeline.useDisplayLists"));
        final boolean useDisplayListsInitialized = myPrefs.getBoolean("init" + getDisplayListsPrefsKey(), false);
        if (!useDisplayListsInitialized && !getDisplayListsPrefsKey().contains("ATI"))
        {
            useDisplayLists = true;
            myPrefs.putBoolean(getDisplayListsPrefsKey(), true, this);
        }

        myPrefs.putBoolean("init" + getDisplayListsPrefsKey(), true, this);

        final boolean safemode = myPrefs.getBoolean(getSafeModePrefsKey(), Boolean.getBoolean("opensphere.pipeline.safemode"));

        if (safemode)
        {
            showSafeModeDialog();
        }
        else if (!useDisplayLists)
        {
            showDisplayListDialog();
        }
        // TODO: when the GL context is switched, there's nothing to make the
        // component visible again.
        //        else
        //        {
        //            myComponent.setVisible(false);
        //        }
    }

    /**
     * Get the disposal helpers.
     *
     * @param builder The processor builder.
     * @return The disposal helpers.
     */
    public Collection<? extends DisposalHelper> getDisposalHelpers(ProcessorBuilder builder)
    {
        final Collection<DisposalHelper> disposalHelpers = builder.getRendererSet().getDisposalHelpers();
        disposalHelpers.add(new TextureDisposalHelper(builder.getCache()));
        return disposalHelpers;
    }

    /**
     * Initialize the pipeline menus.
     *
     * @param environmentIdentifier An identifier that uniquely describes the
     *            current graphics environment.
     * @param pipelineMenuInit The pipeline menu initializer.
     */
    public void initializeMenus(String environmentIdentifier, PipelineMenuInit pipelineMenuInit)
    {
        pipelineMenuInit.setSafemodePrefsKey(getSafeModePrefsKey());
    }

    /**
     * Get if the initializer is ready to initialize.
     *
     * @return {@code true} if ready to initialize.
     */
    public boolean isReadyToInit()
    {
        return myInitBarrier.get() == 0;
    }

    /**
     * Perform GL initialization.
     *
     * @param rc The render context.
     * @param cache The geometry cache.
     * @param builder The processor builder.
     * @param renderingCapabilities The rendering capabilities (to be
     *            populated).
     */
    protected void init(RenderContext rc, LRUMemoryCache cache, ProcessorBuilder builder,
            RenderingCapabilitiesImpl renderingCapabilities)
    {
        rc.getGL().glClearColor(0f, 0f, 0f, 0f);
        rc.getGL().glEnable(GL.GL_DEPTH_TEST);
        rc.getGL().setSwapInterval(0);

        logGLInfo(rc.getGL());

        myDriverChecker.checkGraphicsDriver(rc.getGL());

        initializeCache(rc, cache);

        final ProcessorBuilder dryRunBuilder = builder.clone();
        final GeometryRendererSetFactory geometryRendererSetFactory = new GeometryRendererSetFactory();
        dryRunBuilder.setRendererSet(geometryRendererSetFactory.createGeometryRendererSet(rc, cache, true, false));

        dryRunBuilder.getProcessorFactory().initialize(rc, dryRunBuilder);

        final InlineExecutorService inlineExecutor = new InlineExecutorService();
        dryRunBuilder.setExecutorService(inlineExecutor);
        dryRunBuilder.setGLExecutor(inlineExecutor).setLoadSensitiveExecutor(inlineExecutor);
        dryRunBuilder.setFixedPoolExecutorService(inlineExecutor);

        performDryRuns(rc, dryRunBuilder, geometryRendererSetFactory);

        renderingCapabilities.setCapabilities(dryRunBuilder.getRendererSet().getCapabilities());
        renderingCapabilities.setRendererIdentifier(rc.getGL().glGetString(GL.GL_RENDERER));

        builder.setRendererSet(dryRunBuilder.getRendererSet());
    }

    /**
     * Add a menu that can be used to copy the contact info to the clipboard.
     *
     * @param component The component.
     */
    private void addCopyToClipboardMenu(final JComponent component)
    {
        final String contactInfo = StringUtilities.expandProperties(System.getProperty("contact.info"), System.getProperties());
        if (!StringUtils.isBlank(contactInfo))
        {
            final JPopupMenu menu = new JPopupMenu();
            final JMenuItem mi = new JMenuItem("Copy contact info to clipboard");
            mi.addActionListener(e ->
            {
                try
                {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(contactInfo), null);
                }
                catch (final IllegalStateException ex)
                {
                    JOptionPane.showMessageDialog(component, "Failed to copy to clipboard.");
                }
            });
            menu.add(mi);
            component.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(final MouseEvent me)
                {
                    if (me.getButton() == MouseEvent.BUTTON3)
                    {
                        menu.show(me.getComponent(), me.getX(), me.getY());
                    }
                }
            });
        }
    }

    /**
     * Create a details panel for displaying some detailed warning text.
     *
     * @param details The detail text.
     * @return The panel.
     */
    private Component createDetailsPanel(String details)
    {
        final JPanel detailsPanel = new JPanel(new BorderLayout());

        final JTextArea detailsText = new JTextArea(details);
        detailsText.setBackground(detailsPanel.getBackground());
        detailsText.setBorder(BorderFactory.createEmptyBorder());
        detailsText.setEditable(false);
        detailsPanel.add(new JScrollPane(detailsText));
        return detailsPanel;
    }

    /**
     * Create a summary panel for displaying some summary warning text.
     *
     * @param summary The summary text.
     * @return The panel.
     */
    private Component createSummaryPanel(String summary)
    {
        final JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        final JLabel summaryLabel = new JLabel(summary);
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        summaryPanel.add(summaryLabel, gbc);
        final JTextArea contactText = new JTextArea(
                StringUtilities.expandProperties(System.getProperty("contact.info"), System.getProperties()));
        contactText.setBackground(summaryPanel.getBackground());
        contactText.setBorder(BorderFactory.createEmptyBorder());
        contactText.setEditable(false);
        summaryPanel.add(contactText, gbc);

        addCopyToClipboardMenu(contactText);
        return summaryPanel;
    }

    /**
     * Get the preferences key for using display lists.
     *
     * @return The preferences key.
     */
    private String getDisplayListsPrefsKey()
    {
        return myDisplayListPrefsKey;
    }

    /**
     * Get the preferences key for using safe mode.
     *
     * @return The preferences key.
     */
    private String getSafeModePrefsKey()
    {
        return mySafeModePrefsKey;
    }

    /**
     * Initialize the cache based on the available memory. The size for the VM
     * cache will be determined from the max available VM memory. The size for
     * the GL cache will be determined based on the absolute size given by the
     * pipeline.
     *
     * @param rc The render context.
     * @param cache The geometry cache.
     */
    private void initializeCache(RenderContext rc, LRUMemoryCache cache)
    {
        final long absoluteCacheSizeBytesGPU = rc.getGPUMemorySizeBytes();
        LOGGER.info("GPU memory is " + absoluteCacheSizeBytesGPU / (1 << 20) + " MiB (" + absoluteCacheSizeBytesGPU + " B)");

        final double defaultGPUSizeFraction = .95;
        final double gpuSizeFraction = Utilities.parseSystemProperty("opensphere.geometryCache.gpuSizeFraction", defaultGPUSizeFraction);

        final double defaultLowWaterFraction = .7;
        final double lowWaterFraction = Utilities.parseSystemProperty("opensphere.geometryCache.gpuLowWaterFraction",
                defaultLowWaterFraction);

        final long cacheSizeBytesGPU = (long)(gpuSizeFraction * absoluteCacheSizeBytesGPU);
        final long cacheLowBytesGPU = (long)(cacheSizeBytesGPU * lowWaterFraction);

        cache.setLowWaterBytesGPU(cacheLowBytesGPU);
        cache.setMaxSizeBytesGPU(cacheSizeBytesGPU);

        cache.initialize();
    }

    /**
     * Log info about the GL interface.
     *
     * @param gl The GL interface.
     */
    private void logGLInfo(GL gl)
    {
        final AbstractGraphicsDevice device = gl.getContext().getGLDrawable().getNativeSurface().getGraphicsConfiguration().getScreen()
                .getDevice();
        final GLContext ctx = gl.getContext();

        ToStringHelper helper = new ToStringHelper((Class<?>)null, 0);
        helper.add("======= Graphics Information =======");
        helper.add(device.getClass().getSimpleName() + "[type " + device.getType() + ", connection " + device.getConnection()
        + "]: " + GLProfile.glAvailabilityToString(device));
        helper.add("Swap Interval", gl.getSwapInterval());
        helper.add("GL Profile", gl.getGLProfile());
        helper.add("CTX VERSION", gl.getContext().getGLVersion());
        helper.add("GL", gl);
        helper.add("GL_RENDERER", gl.glGetString(GL.GL_RENDERER));
        helper.add("GL_VENDOR", gl.glGetString(GL.GL_VENDOR));
        helper.add("GL_VERSION", gl.glGetString(GL.GL_VERSION));
        helper.add("GL_EXTENSIONS", ctx.getGLExtensionsString());
        helper.add("GLX_EXTENSIONS", ctx.getPlatformExtensionsString());
        helper.add("GLSL", gl.hasGLSL() + ", shader-compiler: " + gl.isFunctionAvailable("glCompileShader"));
        LOGGER.info(helper.toStringPreferenceDump());
    }

    /**
     * Perform a dry-run on the current set of renderers and throw out any bad
     * ones.
     *
     * @param rc The render context.
     * @param builder The processor builder.
     * @param warnings Optional warnings to be populated if a dry run fails.
     */
    private void performDryRun(RenderContext rc, ProcessorBuilder builder, Collection<String> warnings)
    {
        final long t0 = System.nanoTime();
        RenderContext renderContext;

        final Set<DisposalHelper> disposalHelpers = builder.getRendererSet().getDisposalHelpers();
        for (final DisposalHelper disposalHelper : disposalHelpers)
        {
            disposalHelper.open();
        }
        for (final RenderMode mode : new RenderMode[] { RenderMode.PICK, RenderMode.DRAW })
        {
            renderContext = rc.derive(rc.getMapContext(), mode);
            RenderContext.setCurrent(renderContext);
            try
            {
                performDryRunRender(renderContext, builder, warnings);
            }
            finally
            {
                RenderContext.setCurrent((RenderContext)null);
            }
        }
        for (final DisposalHelper disposalHelper : disposalHelpers)
        {
            disposalHelper.forceDispose(rc.getGL());
            disposalHelper.close();
        }

        final long t1 = System.nanoTime();
        LOGGER.info(StringUtilities.formatTimingMessage("Performed dry-runs in ", t1 - t0));
    }

    /**
     * Perform a dry-run render pass.
     *
     * @param renderContext The render context.
     * @param builder The processor builder.
     * @param warnings Optional warnings to be populated if a dry run fails.
     */
    private void performDryRunRender(RenderContext renderContext, ProcessorBuilder builder, Collection<String> warnings)
    {
        for (final Class<? extends Geometry> type : builder.getProcessorFactory().getGeometryTypes())
        {
            boolean dryRunSuccess = false;
            while (!dryRunSuccess)
            {
                final GeometryProcessor<? extends Geometry> proc = builder.createProcessorForClass(type);
                if (proc instanceof RenderableGeometryProcessor)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Performing dry-run in " + renderContext.getRenderMode() + " mode on processor: " + proc);
                    }
                    try
                    {
                        ((RenderableGeometryProcessor<?>)proc).generateDryRunGeometries();

                        // Do more than one render pass so that any display
                        // lists get called.
                        for (int dryRunCount = 0; dryRunCount < 2; ++dryRunCount)
                        {
                            ((RenderableGeometryProcessor<?>)proc).render(renderContext);
                        }
                        dryRunSuccess = true;
                    }
                    catch (final RuntimeException e)
                    {
                        final GeometryRenderer.Factory<?> factory = builder.getRendererSet().disableRenderer(type);
                        warnings.add(factory.getClass() + " is disabled because of a dry-run failure.");
                        LOGGER.warn("Disabling renderer [" + factory.getClass().getName()
                                + "] because an exception occurred during dry-run: " + e, e);
                    }
                    catch (final Error e)
                    {
                        final GeometryRenderer.Factory<?> factory = builder.getRendererSet().disableRenderer(type);
                        warnings.add(factory.getClass() + " is disabled because of a dry-run failure.");
                        LOGGER.warn("Disabling renderer [" + factory.getClass().getName()
                                + "] because an exception occurred during dry-run: " + e, e);
                    }
                    if (builder.getRendererSet().getRenderer(type) == null)
                    {
                        warnings.add("Processor for " + type.getSimpleName()
                        + " geometries is disabled because no renderers are available.");
                        break;
                    }
                }
                else
                {
                    dryRunSuccess = true;
                }
                proc.close();
            }
        }
    }

    /**
     * Perform dry runs to ensure that the rendering components are viable.
     *
     * @param rc The render context.
     * @param builder The processor builder.
     * @param geometryRendererSetFactory The geometry renderer set factory.
     */
    private void performDryRuns(RenderContext rc, ProcessorBuilder builder, GeometryRendererSetFactory geometryRendererSetFactory)
    {
        final boolean useDisplayLists = myPrefs.getBoolean(getDisplayListsPrefsKey(),
                Boolean.getBoolean("opensphere.pipeline.useDisplayLists"));

        final boolean safemode = myPrefs.getBoolean(getSafeModePrefsKey(), Boolean.getBoolean("opensphere.pipeline.safemode"));

        GeometryRendererSet geometryRendererSet = null;
        final Collection<String> warnings = New.set();

        if (safemode)
        {
            warnings.add("Safe mode is active because of a user setting or a failure during a previous session.");

            geometryRendererSet = geometryRendererSetFactory.createGeometryRendererSet(rc, builder.getCache(), useDisplayLists,
                    safemode);
            builder.setRendererSet(geometryRendererSet);
        }
        else
        {
            if (useDisplayLists)
            {
                myPrefs.putBoolean(getDisplayListsPrefsKey(), false, this);
                myPrefs.waitForPersist();

                geometryRendererSet = geometryRendererSetFactory.createGeometryRendererSet(rc, builder.getCache(),
                        useDisplayLists, safemode);
                builder.setRendererSet(geometryRendererSet);

                performDryRun(rc, builder, warnings);

                myPrefs.putBoolean(getDisplayListsPrefsKey(), true, this);
            }
            else
            {
                if (!getDisplayListsPrefsKey().contains("ATI"))
                {
                    warnings.add(
                            "Fast text rendering is disabled because of a user setting or a failure during a previous session.");
                }

                myPrefs.putBoolean(getSafeModePrefsKey(), true, this);
                myPrefs.waitForPersist();

                geometryRendererSet = geometryRendererSetFactory.createGeometryRendererSet(rc, builder.getCache(),
                        useDisplayLists, safemode);
                builder.setRendererSet(geometryRendererSet);

                performDryRun(rc, builder, warnings);
            }
            myPrefs.putBoolean(getSafeModePrefsKey(), false, this);
        }

        warnings.addAll(builder.getProcessorFactory().getWarnings());
        warnings.addAll(geometryRendererSetFactory.getWarnings());

        rc.isExtensionAvailable("GL_EXT_texture_compression_s3tc",
                "Compressed textures are not supported by this graphics environment", warnings);

        reportWarnings(warnings);
    }

    /**
     * Report any warnings from the rendering components.
     *
     * @param warnings The warnings.
     */
    private void reportWarnings(Collection<String> warnings)
    {
        if (warnings.isEmpty())
        {
            return;
        }
        final StringBuilder sb = new StringBuilder();
        for (final String warning : warnings)
        {
            LOGGER.warn(warning);
            sb.append(warning).append(StringUtilities.LINE_SEP);
        }
        EventQueueUtilities.invokeLater(() ->
        {
            final AutohideMessageDialog dialog = new AutohideMessageDialog(myComponent, ModalityType.APPLICATION_MODAL);
            dialog.setTitle("Warning");
            dialog.initialize(createSummaryPanel(VIABILITY_WARNING), createDetailsPanel(sb.toString()), myPrefs,
                    "hideViabilityMessage");
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(myComponent);
            dialog.setVisible(true);
        });
    }

    /**
     * Show the dialog to alert the user that display lists are disabled.
     */
    private void showDisplayListDialog()
    {
        if (!getDisplayListsPrefsKey().contains("ATI"))
        {
            myInitBarrier.incrementAndGet();
            EventQueueUtilities.invokeLater(() ->
            {
                final AutohideMessageDialog dialog = new AutohideMessageDialog(myComponent, ModalityType.APPLICATION_MODAL);
                dialog.setTitle("Fast Text Rendering Disabled");
                final String enableLabel = "Re-enable Fast Text Rendering";

                dialog.initialize(createSummaryPanel(DISPLAY_LIST_WARNING), (Component)null, myPrefs, "hideDisplayListMessage",
                        "OK", enableLabel);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setLocationRelativeTo(myComponent);
                dialog.setVisible(true);
                if (dialog.getSelection() == enableLabel)
                {
                    myPrefs.putBoolean(getDisplayListsPrefsKey(), true, this);
                }
                myInitBarrier.decrementAndGet();
            });
        }
    }

    /**
     * Show the dialog to alert the user that the pipeline is in safe-mode.
     */
    private void showSafeModeDialog()
    {
        myInitBarrier.incrementAndGet();
        EventQueueUtilities.invokeLater(() ->
        {
            final AutohideMessageDialog dialog = new AutohideMessageDialog(myComponent, ModalityType.APPLICATION_MODAL);
            dialog.setTitle("Safe Mode");
            final String disableLabel = "Disable Safe Mode";
            dialog.initialize(createSummaryPanel(SAFE_MODE_WARNING), (Component)null, myPrefs, "hideSafeModeMessage", "OK",
                    disableLabel);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(myComponent);
            dialog.setVisible(true);
            if (dialog.getSelection() == disableLabel)
            {
                myPrefs.putBoolean(getSafeModePrefsKey(), false, this);

                if (!myPrefs.getBoolean(getDisplayListsPrefsKey(), false))
                {
                    showDisplayListDialog();
                }
            }
            myInitBarrier.decrementAndGet();
        });
    }
}
