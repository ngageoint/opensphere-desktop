package io.opensphere.core.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import io.opensphere.core.MapManager;
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
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.angle.Coordinates;
import io.opensphere.core.units.angle.DecimalDegrees;
import io.opensphere.core.units.angle.DegDecimalMin;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;
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
     * The location to be displayed by the popup manager. Package visibility to
     * prevent synthetic accessors.
     */
    LatLonAlt myLocation;

    /**
     * A flag to inform the popup that an elevation provider is present. Package
     * visibility to prevent synthetic accessors.
     */
    boolean myHasElevationProvider;

    /**
     * The menu provider for events which occur with no associated geometries.
     */
    private final ContextMenuProvider<ScreenPositionContextKey> myDefaultContextMenuProvider = new ContextMenuProvider<>()
    {
        private int counter;

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

                JMenuItem copyToClipboard = new JMenuItem("Open Coordinate Viewer",
                        new GenericFontIcon(AwesomeIconSolid.MAP_MARKER, Color.WHITE));

                copyToClipboard.addActionListener(e ->
                {
                    JDialog cordMenu = CordMenu(pos,++counter);
                    cordMenu.setVisible(true);
                });
                menuItems.add(copyToClipboard);
            }
            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 11500;
        }
    };

    /**
     * The popup menu for coordinate information.
     * 
     * @param pos the geographic position on the globe.
     * @param counter 
     * @return dialog the popup window.
     */
    private final JDialog CordMenu(GeographicPosition pos, int counter)
    {

        JDialog dialog = new JDialog();
        dialog.setTitle("Mouse Position " + counter);
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DecimalDegrees latitudeDD = Coordinates.create(DecimalDegrees.class, pos.getLat().getMagnitude());
        DecimalDegrees longitudeDD = Coordinates.create(DecimalDegrees.class, pos.getLon().getMagnitude());

        DegDecimalMin latitudeDDM = Coordinates.create(DegDecimalMin.class, pos.getLat().getMagnitude());
        DegDecimalMin longitudeDDM = Coordinates.create(DegDecimalMin.class, pos.getLon().getMagnitude());

        DegreesMinutesSeconds latitudeDMS = Coordinates.create(DegreesMinutesSeconds.class, pos.getLat().getMagnitude());
        DegreesMinutesSeconds longitudeDMS = Coordinates.create(DegreesMinutesSeconds.class, pos.getLon().getMagnitude());

        StringBuilder builder = new StringBuilder("DD:\t");
        builder.append(latitudeDD.toShortLabelString(14, 6, 'N', 'S').trim()).append("\t");
        builder.append(longitudeDD.toShortLabelString(14, 6, 'E', 'W').trim()).append("\n");

        builder.append("DMS:\t");
        builder.append(latitudeDMS.toShortLabelString(14, 6, 'N', 'S').trim()).append("\t");
        builder.append(longitudeDMS.toShortLabelString(14, 6, 'E', 'W').trim()).append("\n");

        builder.append("DDM:\t");
        builder.append(latitudeDDM.toShortLabelString(14, 6, 'N', 'S').trim()).append("\t");
        builder.append(longitudeDDM.toShortLabelString(14, 6, 'E', 'W').trim()).append("\n");

        builder.append("MGRS:\t");
        MGRSConverter converter = new MGRSConverter();
        builder.append(converter.createString(new UTM(pos)));

        if (myHasElevationProvider)
        {
            UnitsProvider<Length> lengthProvider = myUnitsRegistry.getUnitsProvider(Length.class);
            Length alt = Length.create(lengthProvider.getPreferredUnits(), myLocation.getAltitude().getMagnitude());
            builder.append("\nAlt:\t").append(alt.toShortLabelString(10, 0).trim());
        }
        JTextArea area = new JTextArea(builder.toString());
        area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setBackground(detailsPanel.getBackground());
        detailsPanel.add(area);

        dialog.getContentPane().add(detailsPanel, BorderLayout.CENTER);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.pack();
        dialog.setAlwaysOnTop(true);
        return dialog;
    }

    /** The map manager this menu provider serves. */
    private final MapManager myMapManager;

    /**
     * The menu provider for events which occur on multiple polygon geometries.
     */
    private final ContextMenuProvider<MultiGeometryContextKey> myMultiGeometryContextMenuProvider = new ContextMenuProvider<>()
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
