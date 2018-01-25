package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;

import com.bric.swing.ColorPicker;

import io.opensphere.core.control.ContextMenuSelectionListener;
import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.MenuOptionListener;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.geometry.ColorGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Displays a selection region.
 */
public class SelectionRegionMenuProvider implements ContextMenuProvider<GeometryContextKey>, ContextMenuSelectionListener
{
    /** The ExecutorService. */
    private static final ExecutorService ourExecutor = Executors.newFixedThreadPool(1,
            new NamedThreadFactory("ControlMenuOptionContext:Dispatch", 3, 4));

    /** The region selection change support. */
    private final ChangeSupport<MenuOptionListener> myChangeSupport = new WeakChangeSupport<>();

    /**
     * The control action manager, which allows us to provide menus when a
     * geometry is selected on the map.
     */
    private final ContextActionManager myControlActionManager;

    /**
     * Constructor.
     *
     * @param controlActionManager The control action manager which will provide
     *            the menu.
     */
    public SelectionRegionMenuProvider(ContextActionManager controlActionManager)
    {
        myControlActionManager = controlActionManager;
        if (myControlActionManager != null)
        {
            myControlActionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                    GeometryContextKey.class, this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() instanceof JMenuItem)
        {
            notifyMenuOptionListeners(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT,
                    ((JMenuItem)evt.getSource()).getActionCommand());
        }
    }

    /**
     * Add a listener to be notified if an action is taken on a menu.
     *
     * @param listener The menu option listener.
     */
    public void addMenuOptionListener(MenuOptionListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Unregister this menu provider.
     */
    public void close()
    {
        if (myControlActionManager != null)
        {
            myControlActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                    GeometryContextKey.class, this);
        }
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, GeometryContextKey key)
    {
        final Geometry geom = key.getGeometry();
        if (!(geom instanceof PolygonGeometry))
        {
            return null;
        }

        List<JMenuItem> menuItems = new ArrayList<>();
        final int color = ((ColorGeometry)geom).getRenderProperties().getColorARGB();

        if ((color & 0xff000000) == 0)
        {
            JMenuItem show = new JMenuItem("Show");
            show.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ((ColorGeometry)geom).getRenderProperties().setColorARGB(color | 0xff000000);
                }
            });
            menuItems.add(show);
        }
        else
        {
            JMenuItem hide = new JMenuItem("Hide");
            hide.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ((ColorGeometry)geom).getRenderProperties().setColorARGB(color & 0x00ffffff);
                }
            });
            menuItems.add(hide);
        }

        JMenuItem setColor = new JMenuItem("Override Color...");
        setColor.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Color oldColor = ((ColorGeometry)geom).getRenderProperties().getColor();
                Color selectColor = ColorPicker.showDialog(null, "Choose Color", oldColor, true);
                if (selectColor != null && !selectColor.equals(oldColor))
                {
                    ((ColorGeometry)geom).getRenderProperties().setColor(selectColor);
                }
            }
        });
        menuItems.add(setColor);

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 11100;
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent arg0)
    {
        myChangeSupport.notifyListeners(new ChangeSupport.Callback<MenuOptionListener>()
        {
            @Override
            public void notify(MenuOptionListener listener)
            {
                listener.handleMenuCancelled();
            }
        }, ourExecutor);
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0)
    {
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent arg0)
    {
    }

    /**
     * Remove a menu option listener.
     *
     * @param listener The listener.
     */
    public void removeMenuOptionListener(MenuOptionListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Show the menu generated by the control action manager.
     *
     * @param mouseEvent The mouse event.
     * @param contextId The context id.
     * @param selectionBoxGeometries The selection box geometries.
     */
    public void showMenu(final MouseEvent mouseEvent, final String contextId,
            final List<? extends Geometry> selectionBoxGeometries)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                ActionContext<GeometryContextKey> context = myControlActionManager.getActionContext(contextId,
                        GeometryContextKey.class);
                context.doAction(new GeometryContextKey(selectionBoxGeometries.get(0)), (Component)mouseEvent.getSource(),
                        mouseEvent.getX(), mouseEvent.getY(), SelectionRegionMenuProvider.this);
            }
        });
    }

    /**
     * Notify menu option listeners.
     *
     * @param context the context
     * @param command the command
     */
    private void notifyMenuOptionListeners(final String context, final String command)
    {
        myChangeSupport.notifyListeners(new ChangeSupport.Callback<MenuOptionListener>()
        {
            @Override
            public void notify(MenuOptionListener listener)
            {
                listener.menuOptionSelected(context, command);
            }
        }, ourExecutor);
    }
}
