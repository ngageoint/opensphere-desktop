package io.opensphere.core.map;

import java.awt.Component;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangeSupport.ProjectionChangeListener;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Initializer for map-related menu items.
 */
public abstract class MapMenuControl implements ProjectionChangeListener
{
    /** A listener for application events. */
    private final EventListener<ApplicationLifecycleEvent> myEventListener = new EventListener<ApplicationLifecycleEvent>()
    {
        @Override
        public void notify(ApplicationLifecycleEvent event)
        {
            if (event.getStage() == ApplicationLifecycleEvent.Stage.LAF_INSTALLED)
            {
                addMenuItems();
            }
        }
    };

    /** The menu bar registry. */
    private final MenuBarRegistry myMenuBarRegistry;

    /** The projections in the system. */
    private final Collection<? extends Projection> myProjections;

    /**
     * Constructor.
     *
     * @param menuBarRegistry The menu bar registry.
     * @param eventManager The event manager.
     * @param projections The projections.
     */
    public MapMenuControl(MenuBarRegistry menuBarRegistry, EventManager eventManager,
            Collection<? extends Projection> projections)
    {
        myMenuBarRegistry = menuBarRegistry;
        myProjections = projections;

        eventManager.subscribe(ApplicationLifecycleEvent.class, myEventListener);
    }

    /**
     * Add map menu items to the registry.
     */
    public void addMenuItems()
    {
        EventQueueUtilities.invokeLater(() ->
        {
            JMenu menu = getProjectionMenu();

            ButtonGroup group = new ButtonGroup();
            for (final Projection projection : myProjections)
            {
                JMenuItem menuItem = new JRadioButtonMenuItem(projection.getName());
                menuItem.addActionListener(e ->
                {
                    if (((AbstractButton)e.getSource()).isSelected())
                    {
                        setCurrentProjection(projection);
                    }
                });
                group.add(menuItem);
                menu.add(menuItem);
            }
        });
    }

    @Override
    public void projectionChanged(final ProjectionChangedEvent evt)
    {
        EventQueueUtilities.invokeLater(() ->
        {
            JMenu menu = getProjectionMenu();

            Component[] components = menu.getMenuComponents();
            for (Component component : components)
            {
                if (((AbstractButton)component).getText().equals(evt.getProjection().getName()))
                {
                    ((AbstractButton)component).setSelected(true);
                    break;
                }
            }
        });
    }

    /**
     * Get the projection menu.
     *
     * @return The menu.
     */
    protected JMenu getProjectionMenu()
    {
        return myMenuBarRegistry.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.VIEW_MENU,
                MenuBarRegistry.PROJECTION_MENU);
    }

    /**
     * Hook to set the current projection when a menu item is selected.
     *
     * @param projection The projection to set.
     */
    protected abstract void setCurrentProjection(Projection projection);
}
