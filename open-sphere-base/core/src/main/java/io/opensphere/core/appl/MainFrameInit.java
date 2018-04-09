package io.opensphere.core.appl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager;
import io.opensphere.core.control.ui.impl.UIRegistryImpl;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.FramePreferencesMonitor;

/**
 * Initializer for the main frame.
 */
class MainFrameInit
{
    /** The Constant MAIN_FRAME_PREFERENCES_NAME_PREFIX. */
    public static final String MAIN_FRAME_PREFERENCES_NAME_PREFIX = "MainFrame";

    /** The Constant FALLBACK_FRAME_HEIGHT. */
    private static final int FALLBACK_FRAME_HEIGHT = 1000;

    /** The Constant FALLBACK_FRAME_WIDTH. */
    private static final int FALLBACK_FRAME_WIDTH = 1400;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MainFrameInit.class);

    /** The main JFrame. */
    private final JFrame myMainFrame;

    /** The main frame preferences monitor. */
    private FramePreferencesMonitor myMainFramePreferencesMonitor;

    /** Whether the current operating system is Mac OS X. */
    private static boolean isOSX = System.getProperty("os.name").contains("OS X");

    /**
     * Because resizing the canvas causes some bad interaction between the
     * pipelineComponent and the layout manager when resizing, create a panel
     * which contains only the pipelineComponent and use a layout manager which
     * does not attempt to layout the pipelineComponent.
     */
    private final JPanel myPiplineComponentPanel = new JPanel(new LayoutManager()
    {
        @Override
        public void addLayoutComponent(String name, Component comp)
        {
        }

        @Override
        public void layoutContainer(Container parent)
        {
        }

        @Override
        public Dimension minimumLayoutSize(Container parent)
        {
            return new Dimension();
        }

        @Override
        public Dimension preferredLayoutSize(Container parent)
        {
            return new Dimension();
        }

        @Override
        public void removeLayoutComponent(Component comp)
        {
        }
    });

    /**
     * Get the title for the main frame.
     *
     * @return The main frame title.
     */
    public static String getMainFrameTitle()
    {
        return StringUtilities.expandProperties(System.getProperty("opensphere.frame.title", "Title not set"),
                System.getProperties());
    }

    /**
     * Construct the initializer.
     */
    public MainFrameInit()
    {
        myMainFrame = new JFrame();
    }

    /**
     * Getter for the main frame.
     *
     * @return The main frame
     */
    public JFrame getMainFrame()
    {
        return myMainFrame;
    }

    /**
     * Gets the main frame preferences monitor.
     *
     * @return the main frame preferences monitor
     */
    public FramePreferencesMonitor getMainFramePreferencesMonitor()
    {
        return myMainFramePreferencesMonitor;
    }

    /**
     * Add the basic menu items to the main menu bar.
     *
     * @param kernel The kernel.
     * @param toolbox The toolbox.
     * @param pipelineComponent The pipeline component.
     */
    public void initialize(final Kernel kernel, final Toolbox toolbox, final Component pipelineComponent)
    {
        myMainFrame.setTitle(getMainFrameTitle());
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        myPiplineComponentPanel.add(pipelineComponent);

        new MenuInit(kernel).addBasicMenuItems(toolbox);

        final boolean showHiddenPane = Boolean.parseBoolean(System.getProperty("opensphere.debug.showJInternalFrames", "false"));
        final int frameOffset = showHiddenPane ? 1 : -1;

        // Create the panel which contains the menu bars and piplineComponent.
        final JPanel containerPanel = getContainerPanel(toolbox);
        final JDesktopPane hiddenPane = new JDesktopPane();

        setupSizeListener(hiddenPane, pipelineComponent);

        myMainFrame.setContentPane(containerPanel);
        myMainFrame.pack();
        myMainFrame.getLayeredPane().add(hiddenPane, Integer.valueOf(JLayeredPane.FRAME_CONTENT_LAYER.intValue() + frameOffset));

        final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if (d != null && d.width > 0 && d.height > 0)
        {
            myMainFrame.setSize(d.width - 100, d.height - 100);
        }
        else
        {
            // Fall back to a fixed value.
            myMainFrame.setSize(FALLBACK_FRAME_WIDTH, FALLBACK_FRAME_HEIGHT);
        }
        myMainFrame.setLocationRelativeTo(null);
        myMainFramePreferencesMonitor = new FramePreferencesMonitor(toolbox.getPreferencesRegistry(),
                MAIN_FRAME_PREFERENCES_NAME_PREFIX, myMainFrame, myMainFrame.getBounds());

        myMainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                kernel.shutdown(0);
            }
        });

        if (isOSX)
        {
            System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        }

        final String iconURLString = System.getProperty("opensphere.frame.icon");
        if (iconURLString != null)
        {
            final InputStream stream = MainFrameInit.class.getResourceAsStream(iconURLString);
            if (stream == null)
            {
                LOGGER.warn("Could not find image at location: " + iconURLString);
            }
            else
            {
                try
                {
                    final BufferedImage image = ImageIO.read(stream);
                    myMainFrame.setIconImage(image);
                    if (isOSX)
                    {
                        setDockIcon(image);
                    }
                }
                catch (final IOException e)
                {
                    LOGGER.error("Failed to load icon image [" + iconURLString + "]: " + e, e);
                }
                finally
                {
                    try
                    {
                        stream.close();
                    }
                    catch (final IOException e)
                    {
                        LOGGER.error("Failed to close image stream: " + e, e);
                    }
                }
            }
        }

        myMainFrame.setVisible(true);
        if (Boolean.getBoolean("opensphere.enablePopToBack"))
        {
            myMainFrame.toBack();
        }

        // Set the transfer handler to handle drag-n-drop
        myMainFrame.setTransferHandler(new EventTransferHandler(toolbox));

        ((UIRegistryImpl)toolbox.getUIRegistry()).setMainPaneComponent(hiddenPane);
        ((UIRegistryImpl)toolbox.getUIRegistry()).setInternalFrameContainer(hiddenPane);
    }

    /**
     * Set the Mac OS X dock icon image.
     *
     * @param iconImage the dock icon image to use
     */
    private void setDockIcon(Image iconImage)
    {
        try
        {
            final Class<?> appClass = Class.forName("com.apple.eawt.Application");
            Method appGetter = null;
            try
            {
                appGetter = appClass.getMethod("getApplication");
            }
            catch (final NoSuchMethodException ex)
            {
                LOGGER.error("com.apple.eawt.Application.getApplication() method not found!");
            }
            if (null != appGetter)
            {
                Object appObj = null;
                try
                {
                    appObj = appGetter.invoke(null);
                }
                catch (final InvocationTargetException ex)
                {
                    LOGGER.error("Error retrieving Mac OS application object!", ex);
                }
                if (null != appObj)
                {
                    Method imageSetter = null;
                    try
                    {
                        imageSetter = appClass.getMethod("setDockIconImage", Image.class);
                    }
                    catch (final NoSuchMethodException ex)
                    {
                        LOGGER.error("com.apple.eawt.Application.setDockIconImage(Image) method not found!");
                    }
                    if (null != imageSetter)
                    {
                        try
                        {
                            imageSetter.invoke(appObj, iconImage);
                        }
                        catch (final InvocationTargetException ex)
                        {
                            LOGGER.error("Error setting Mac OS dock icon image!", ex);
                        }
                    }
                }
            }
        }
        catch (final ClassNotFoundException ex)
        {
            LOGGER.error("Could not find Mac OS Application class type!");
        }
        catch (final SecurityException e1)
        {
            LOGGER.error("SecurityException while setting Mac OS X dock icon!");
        }
        catch (final IllegalAccessException e1)
        {
            LOGGER.error("IllegalAccessException while setting Mac OS X dock icon!");
        }
        catch (final IllegalArgumentException ex)
        {
            LOGGER.error("IllegalArgumentException while setting Mac OS X dock icon!", ex);
        }
    }

    /**
     * Gets the container panel. This panel will contain all components that
     * need to be in the top level panel with the pipelineComponent. This will
     * ensure that the GLSwing mouse event handler gets the mouse clicks at the
     * correct locations when these components are shown/hidden.
     *
     * @param toolbox the toolbox
     * @return the container panel
     */
    private JPanel getContainerPanel(final Toolbox toolbox)
    {
        final JPanel containerPanel = new JPanel(new BorderLayout());

        final ToolbarManager toolbarManager = toolbox.getUIRegistry().getToolbarComponentRegistry().getToolbarManager();

        final JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        final JMenuBar menuBar = toolbox.getUIRegistry().getMenuBarRegistry().getMainMenuBar();
        if (!isOSX)
        {
            northPanel.add(menuBar);
        }
        else
        {
            myMainFrame.setJMenuBar(menuBar);
        }
        northPanel.add(toolbarManager.getNorthToolbar());
        northPanel.add(toolbarManager.getNorthBottomToolbar());
        toolbox.getUIRegistry().getSharedComponentRegistry().registerComponent("core.mainFrame.northPanel", northPanel);
        containerPanel.add(northPanel, BorderLayout.NORTH);

        containerPanel.add(myPiplineComponentPanel, BorderLayout.CENTER);

        final JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(toolbarManager.getSouthToolbar());
        toolbox.getUIRegistry().getSharedComponentRegistry().registerComponent("core.mainFrame.southPanel", southPanel);
        containerPanel.add(southPanel, BorderLayout.SOUTH);

        return containerPanel;
    }

    /**
     * Set a listener on the hidden pane and the pipeline component to properly
     * resize components when the main window is resized.
     *
     * @param hiddenPane The hidden pane which contains the internal frames.
     * @param pipelineComponent The pipeline component.
     */
    private void setupSizeListener(final JDesktopPane hiddenPane, final Component pipelineComponent)
    {
        final ComponentAdapter pipelinePanelListener = new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                final Rectangle bounds = e.getComponent().getBounds();

                // Set the bounds of the hiddenPane within the main frame.
                final Rectangle pipelinePanelBounds = new Rectangle(bounds.x + myMainFrame.getContentPane().getX(),
                        bounds.y + myMainFrame.getContentPane().getY(), bounds.width, bounds.height);
                hiddenPane.setBounds(pipelinePanelBounds);

                // The pipeline component should completely fill its' parent
                final Rectangle pipelineBounds = new Rectangle(bounds.width, bounds.height);
                pipelineComponent.setBounds(pipelineBounds);
            }
        };
        myPiplineComponentPanel.addComponentListener(pipelinePanelListener);
    }
}
