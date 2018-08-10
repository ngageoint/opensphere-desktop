package io.opensphere.core.control.ui.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.function.Supplier;

import javax.swing.JFrame;

import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.impl.ContextActionManagerImpl;
import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.RegionSelectionManager;
import io.opensphere.core.control.ui.SharedComponentRegistry;
import io.opensphere.core.control.ui.ToolbarComponentRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.iconlegend.IconLegendRegistry;
import io.opensphere.core.iconlegend.impl.IconLegendRegistryImpl;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.options.impl.OptionsRegistryImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.timeline.TimelineRegistry;
import io.opensphere.core.timeline.TimelineRegistryImpl;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Implementation of the UI registry.
 */
public class UIRegistryImpl implements UIRegistry
{
    /** Registry for internal JComponents to be drawn within the main frame. */
    private InternalComponentRegistry myComponentRegistry;

    /** Manager for creating context menus. */
    private final ContextActionManager myControlActionManager;

    /** The main frame for the application. */
    private final JFrame myMainFrame;

    /** The menu bar registry. */
    private final MenuBarRegistry myMenuBarRegistry = new MenuBarRegistryImpl();

    /** The timeline registry. */
    private final TimelineRegistry myTimelineRegistry = new TimelineRegistryImpl();

    /** The Toolbar component registry. */
    private final ToolbarComponentRegistry myToolbarComponentRegistry;

    /** The Options registry. */
    private final OptionsRegistry myOptionsRegistry = new OptionsRegistryImpl();

    /** The main application pane. */
    private Component myMainPaneComponent;

    /** The manager for region selection when done by the user. */
    private volatile RegionSelectionManager myRegionSelectionManager;

    /** The shared component registry. */
    private final SharedComponentRegistryImpl mySharedComponentRegistry = new SharedComponentRegistryImpl();

    /** The Icon legend registry. */
    private final IconLegendRegistry myIconLegendRegistry = new IconLegendRegistryImpl();

    /** The supplier for the main frame. */
    private final Supplier<? extends JFrame> myFrameSupplier = new Supplier<>()
    {
        @Override
        public JFrame get()
        {
            assert EventQueue.isDispatchThread();
            return myMainFrame;
        }
    };

    /**
     * Constructor.
     *
     * @param controlRegistry The Control registry.
     * @param mainFrame The top level frame for the application.
     * @param preferencesRegistry The preferences registry.
     */
    public UIRegistryImpl(ControlRegistry controlRegistry, JFrame mainFrame, PreferencesRegistry preferencesRegistry)
    {
        myMainFrame = mainFrame;
        myControlActionManager = new ContextActionManagerImpl(controlRegistry);
        myToolbarComponentRegistry = new ToolbarComponentRegistryImpl(preferencesRegistry);
    }

    @Override
    public InternalComponentRegistry getComponentRegistry()
    {
        synchronized (this)
        {
            while (myComponentRegistry == null)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                }
            }
            return myComponentRegistry;
        }
    }

    @Override
    public ContextActionManager getContextActionManager()
    {
        return myControlActionManager;
    }

    @Override
    public IconLegendRegistry getIconLegendRegistry()
    {
        return myIconLegendRegistry;
    }

    @Override
    public Supplier<? extends JFrame> getMainFrameProvider()
    {
        return myFrameSupplier;
    }

    @Override
    public MenuBarRegistry getMenuBarRegistry()
    {
        return myMenuBarRegistry;
    }

    @Override
    public OptionsRegistry getOptionsRegistry()
    {
        return myOptionsRegistry;
    }

    @Override
    public RegionSelectionManager getRegionSelectionManager()
    {
        return myRegionSelectionManager;
    }

    @Override
    public SharedComponentRegistry getSharedComponentRegistry()
    {
        return mySharedComponentRegistry;
    }

    @Override
    public TimelineRegistry getTimelineRegistry()
    {
        return myTimelineRegistry;
    }

    @Override
    public ToolbarComponentRegistry getToolbarComponentRegistry()
    {
        return myToolbarComponentRegistry;
    }

    @Override
    public void registerAsRegionSelectionManager(RegionSelectionManager manager)
    {
        myRegionSelectionManager = manager;
    }

    /**
     * Set the internal frame container.
     *
     * @param internalFrameContainer The internal frame container.
     */
    public void setInternalFrameContainer(Container internalFrameContainer)
    {
        synchronized (this)
        {
            if (myComponentRegistry != null)
            {
                throw new IllegalStateException("Internal frame container has already been set.");
            }
            myComponentRegistry = new InternalComponentRegistryImpl(internalFrameContainer);
            notifyAll();
        }
    }

    /**
     * Set the main application pane component.
     *
     * @param component The component.
     */
    public void setMainPaneComponent(Component component)
    {
        assert EventQueue.isDispatchThread();
        synchronized (this)
        {
            if (myMainPaneComponent != null)
            {
                throw new IllegalStateException("The main pane component has already been set.");
            }
            myMainPaneComponent = component;
            notifyAll();
        }
    }

    @Override
    public void setMainPaneSize(final int width, final int height)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (this)
                {
                    while (myMainPaneComponent == null)
                    {
                        try
                        {
                            wait();
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }

                    // Adjust the pane's size by resizing the main frame by the
                    // required amount.
                    int deltaX = width - myMainPaneComponent.getWidth();
                    int deltaY = height - myMainPaneComponent.getHeight();
                    int mainFrameWidth = myMainFrame.getWidth();
                    int mainFrameHeight = myMainFrame.getHeight();
                    if (deltaX != 0 || deltaY != 0)
                    {
                        myMainFrame.setSize(mainFrameWidth + deltaX, mainFrameHeight + deltaY);
                    }
                }
            }
        });
    }
}
