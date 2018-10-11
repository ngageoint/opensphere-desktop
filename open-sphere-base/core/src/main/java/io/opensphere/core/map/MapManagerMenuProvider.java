package io.opensphere.core.map;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import io.opensphere.core.MapManager;
import io.opensphere.core.Notify;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.MultiGeometryContextKey;
import io.opensphere.core.control.action.context.ScreenPositionContextKey;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.units.angle.Angle;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/** Helper class to provide context menus for a map manager. */
public class MapManagerMenuProvider
{
    /** The control action manager. */
    private final ContextActionManager myControlActionManager;

    /** The Event Manager. **/
    @SuppressWarnings("unused")
    private final EventManager myEventManager;

    /** Whether to show the toast message. */
    @SuppressWarnings("unused")
    private final boolean myShowToast = true;

    /**
     * The menu provider for events which occur with no associated geometries.
     */
    private final ContextMenuProvider<ScreenPositionContextKey> myDefaultContextMenuProvider = new ContextMenuProvider<ScreenPositionContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, ScreenPositionContextKey key)
        {
            List<JMenuItem> menuItems = New.list();

            final GeographicPosition pos = myMapManager.convertToPosition(key.getPosition().asVector2i(), ReferenceLevel.TERRAIN);
            if (pos != null)
            {
                JMenuItem center = new JMenuItem("Center On");
                center.setIcon(new GenericFontIcon(AwesomeIconSolid.BULLSEYE, Color.WHITE));
                center.addActionListener(arg0 -> new ViewerAnimator(myMapManager.getStandardViewer(), pos).start());
                menuItems.add(center);

                JMenuItem copyToClipboard = new JMenuItem("Copy coordinates to clipboard",
                        new GenericFontIcon(AwesomeIconSolid.COPY, Color.WHITE));
                copyToClipboard.addActionListener(arg0 ->
                {
                    if (myUnitsRegistry != null)
                    {
                        Class<? extends Angle> angleUnits = myUnitsRegistry.getPreferredUnits(Angle.class);
                        String label = pos.toDisplayString(angleUnits, (Class<? extends Length>)null);

                        try
                        {
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(label), null);
                            Notify.info("Copied " + label + " to clipboard");
                        }
                        catch (IllegalStateException ex)
                        {
                            JOptionPane.showMessageDialog(null, "Failed to copy to clipboard.");
                        }
                    }
                });
                menuItems.add(copyToClipboard);

                JMenuItem copyMGRSToClipboard = new JMenuItem("Copy MGRS to clipboard",
                        new GenericFontIcon(AwesomeIconSolid.COPY, Color.WHITE));
                copyMGRSToClipboard.addActionListener(arg0 ->
                {
                    String label = new MGRSConverter().createString(new UTM(pos));

                    try
                    {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(label), null);
                    }
                    catch (IllegalStateException ex)
                    {
                        JOptionPane.showMessageDialog(null, "Failed to copy to clipboard.");
                    }
                });
                menuItems.add(copyMGRSToClipboard);
            }

            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 11500;
        }
    };

    /** The map manager this menu provider serves. */
    private final MapManager myMapManager;

    /**
     * The menu provider for events which occur on multiple polygon geometries.
     */
    private final ContextMenuProvider<MultiGeometryContextKey> myMultiGeometryContextMenuProvider = new ContextMenuProvider<MultiGeometryContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, MultiGeometryContextKey key)
        {
            List<JMenuItem> menuItems = New.list();

            JMenuItem zoom = new JMenuItem("Zoom to");
            zoom.setIcon(new GenericFontIcon(AwesomeIconSolid.CROP, Color.WHITE, 12));
            menuItems.add(zoom);
            JMenuItem center = new JMenuItem("Center On");
            center.setIcon(new GenericFontIcon(AwesomeIconSolid.BULLSEYE, Color.WHITE));
            menuItems.add(center);

            if (key.getGeometries().isEmpty())
            {
                center.setEnabled(false);
                center.setToolTipText("No geometry selected for action.");
                zoom.setEnabled(false);
                zoom.setToolTipText("No geometry selected for action.");
            }
            else
            {
                addCenterAction(center, key.getGeometries());
                addZoomAction(zoom, key.getGeometries());
            }

            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 11501;
        }
    };

    /** The units registry. */
    private final UnitsRegistry myUnitsRegistry;

    /**
     * Constructor.
     *
     * @param mapMan The map manager this menu provider serves.
     * @param actionManager The control action manager.
     * @param unitsRegistry The units registry.
     * @param eventManager The event manager.
     */
    public MapManagerMenuProvider(MapManager mapMan, ContextActionManager actionManager, UnitsRegistry unitsRegistry,
            EventManager eventManager)
    {
        myMapManager = mapMan;
        myControlActionManager = actionManager;
        myUnitsRegistry = unitsRegistry;
        myEventManager = eventManager;

        myControlActionManager.registerContextMenuItemProvider(ContextIdentifiers.ROI_CONTEXT, MultiGeometryContextKey.class,
                myMultiGeometryContextMenuProvider);

        myControlActionManager.registerContextMenuItemProvider(ContextIdentifiers.SCREEN_POSITION_CONTEXT,
                ScreenPositionContextKey.class, myDefaultContextMenuProvider);
    }

    /** Perform an required cleanup. */
    public void close()
    {
        myControlActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.ROI_CONTEXT, MultiGeometryContextKey.class,
                myMultiGeometryContextMenuProvider);

        myControlActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.SCREEN_POSITION_CONTEXT,
                ScreenPositionContextKey.class, myDefaultContextMenuProvider);
    }

    /**
     * Add the action listener to the menu item for handling centering.
     *
     * @param menuItem The menu item which causes centering.
     * @param geoms The geometries which are used to determine centered viewer
     *            location.
     */
    private void addCenterAction(JMenuItem menuItem, final Collection<? extends Geometry> geoms)
    {
        menuItem.addActionListener(arg0 ->
        {
            List<Position> positions = New.list();
            for (Geometry geom : geoms)
            {
                if (geom instanceof PolylineGeometry)
                {
                    positions.addAll(((PolylineGeometry)geom).getVertices());
                }
            }
            ViewerAnimator animator = new ViewerAnimator(myMapManager.getStandardViewer(), positions, false);
            animator.start();
        });
    }

    /**
     * Add the action listener to the menu item for handling zooming.
     *
     * @param menuItem The menu item which causes zooming.
     * @param geoms The geometries which are used to determine zoomed viewer
     *            location.
     */
    private void addZoomAction(JMenuItem menuItem, final Collection<? extends Geometry> geoms)
    {
        menuItem.addActionListener(arg0 ->
        {
            List<Position> positions = New.list();
            for (Geometry geom : geoms)
            {
                if (geom instanceof PolylineGeometry)
                {
                    positions.addAll(((PolylineGeometry)geom).getVertices());
                }
            }
            ViewerAnimator animator = new ViewerAnimator(myMapManager.getStandardViewer(), positions, true);
            animator.start();
        });
    }
}