package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.ViewerPositionConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Transformer for Latitude and Longitude lines.
 */
class GridTransformer extends DefaultTransformer
{
    /** Max latitude. */
    private static final double MAX_LAT = 90.;

    /** Max longitude. */
    private static final double MAX_LON = 180.;

    /** The minimum allowable value for lat/lon spacing. */
    private static final double MIN_SPACING = 0.1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GridTransformer.class);

    /** If Denver should be displayed. */
    private boolean myDenver;

    /** Currently published latitude lines. */
    private final List<PolylineGeometry> myLatitudeLines = new ArrayList<>();

    /** The spacing between the latitude lines, in degrees. */
    private double myLatitudeSpacing = 15;

    /** Currently published longitude lines. */
    private final List<PolylineGeometry> myLongitudeLines = new ArrayList<>();

    /** The spacing between the longitude lines, in degrees. */
    private double myLongitudeSpacing = 15;

    /** The parent frame. */
    private final Supplier<? extends Component> myParentFrameProvider;

    /**
     * Constructor.
     *
     * @param parentFrameProvider The parent frame.
     */
    public GridTransformer(Supplier<? extends Component> parentFrameProvider)
    {
        super(null);
        myParentFrameProvider = parentFrameProvider;
    }

    /** Disable the latitude grid lines. */
    public void disableLatitudeGrid()
    {
        if (!myLatitudeLines.isEmpty())
        {
            List<PolylineGeometry> removes = new ArrayList<>(myLatitudeLines);
            publishGeometries(Collections.<PolylineGeometry>emptySet(), removes);
            myLatitudeLines.clear();
        }
    }

    /** Disable the longitude grid lines. */
    public void disableLongitudeGrid()
    {
        if (!myLongitudeLines.isEmpty())
        {
            List<PolylineGeometry> removes = new ArrayList<>(myLongitudeLines);
            publishGeometries(Collections.<PolylineGeometry>emptySet(), removes);
            myLongitudeLines.clear();
        }
    }

    /** Enable the latitude grid lines. */
    public void enableLatitudeGrid()
    {
        publishLatitudeLines();
    }

    /** Enable the longitude grid lines. */
    public void enableLongitudeGrid()
    {
        publishLongitudeLines();
    }

    /**
     * Setup menu items for features provided by this transformer.
     *
     * @param toolbox References to facilities that may be used by the plug-in
     *            to interact with the rest of the system.
     */
    public void initializeMenus(final Toolbox toolbox)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                initializeLatMenu(toolbox);
                initializeLonMenu(toolbox);
            }
        });
    }

    @Override
    public void open()
    {
        super.open();

        if (myDenver)
        {
            publishDenver();
        }
    }

    /**
     * Publish the geometries for the Denver point.
     */
    public void publishDenver()
    {
        final double denverLat = 39.739167;
        final double denverLon = -104.984722;
        final double denverAlt = 1609.344;
        final double labelOffset = 100.;
        final Altitude maxAltitude = Altitude.createFromMeters(200000., Altitude.ReferenceLevel.ELLIPSOID);
        Constraints constraints = new Constraints(null, new ViewerPositionConstraint((Altitude)null, maxAltitude));
        LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<GeographicPosition>();
        labelBuilder.setText(" Denver");
        labelBuilder.setFont(null);
        labelBuilder.setPosition(new GeographicPosition(LatLonAlt.createFromDegreesMeters(denverLat, denverLon,
                denverAlt + labelOffset, Altitude.ReferenceLevel.ELLIPSOID)));
        labelBuilder.setHorizontalAlignment(0f);
        labelBuilder.setVerticalAlignment(0f);
        LabelRenderProperties labelProps = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z - 1, true, true);
        labelProps.setColor(Color.WHITE);
        publishGeometries(Collections.singleton(new LabelGeometry(labelBuilder, labelProps, constraints)),
                Collections.<Geometry>emptySet());

        PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<GeographicPosition>();
        PointRenderProperties pointProps = new DefaultPointRenderProperties(ZOrderRenderProperties.TOP_Z, true, true, true);
        pointProps.setColor(Color.GREEN);
        pointProps.setSize(2f);
        pointBuilder.setPosition(new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(denverLat, denverLon, denverAlt, Altitude.ReferenceLevel.ELLIPSOID)));
        publishGeometries(Collections.singleton(new PointGeometry(pointBuilder, pointProps, constraints)),
                Collections.<Geometry>emptySet());
    }

    /**
     * Set if Denver should be displayed.
     *
     * @param denver <code>true</code> if Denver should be displayed.
     */
    public void setDenver(boolean denver)
    {
        myDenver = denver;
    }

    /**
     * Set the latitude spacing.
     *
     * @param latitudeSpacing The number of degrees between latitude lines.
     */
    public void setLatitudeSpacing(double latitudeSpacing)
    {
        myLatitudeSpacing = latitudeSpacing;
    }

    /**
     * Set the longitude spacing.
     *
     * @param longitudeSpacing The number of degrees between longitude lines.
     */
    public void setLongitudeSpacing(double longitudeSpacing)
    {
        myLongitudeSpacing = longitudeSpacing;
    }

    /**
     * Method that publishes the latitude lines.
     */
    protected void publishLatitudeLines()
    {
        if (myLatitudeSpacing <= 0)
        {
            return;
        }
        disableLatitudeGrid();
        PolylineGeometry.Builder<GeographicPosition> lineBuilder = new PolylineGeometry.Builder<GeographicPosition>();
        final double altM = 1000.;
        PolylineRenderProperties props1 = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z - 2000, true, false);
        props1.setColor(new Color(200, 200, 200, 200));
        for (double lat = -MAX_LAT + myLatitudeSpacing; lat < MAX_LAT; lat += myLatitudeSpacing)
        {
            List<GeographicPosition> vertices1 = new ArrayList<>();
            final double lonSpacing = 5.;
            for (double lon = -MAX_LON; lon <= MAX_LON; lon += lonSpacing)
            {
                vertices1.add(new GeographicPosition(
                        LatLonAlt.createFromDegreesMeters(lat, lon, altM, Altitude.ReferenceLevel.TERRAIN)));
            }
            lineBuilder.setVertices(vertices1);
            myLatitudeLines.add(new PolylineGeometry(lineBuilder, props1, null));
        }
        publishGeometries(myLatitudeLines, Collections.<PolylineGeometry>emptySet());
    }

    /**
     * Method that publishes the longitude lines.
     */
    protected void publishLongitudeLines()
    {
        if (myLongitudeSpacing <= 0)
        {
            return;
        }
        disableLongitudeGrid();
        final double latSpacing = 5.;
        final double topOffset = 10.;
        final double bottomOffset = 10.;

        PolylineGeometry.Builder<GeographicPosition> lineBuilder = new PolylineGeometry.Builder<GeographicPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z - 2000, true, false);
        props.setColor(new Color(200, 200, 200, 200));
        final double altM = 1000.;

        for (double lon = -MAX_LON; lon <= MAX_LON; lon += myLongitudeSpacing)
        {
            List<GeographicPosition> vertices1 = new ArrayList<>();
            for (double lat = -MAX_LAT + bottomOffset; lat <= MAX_LAT - topOffset; lat += latSpacing)
            {
                vertices1.add(new GeographicPosition(
                        LatLonAlt.createFromDegreesMeters(lat, lon, altM, Altitude.ReferenceLevel.TERRAIN)));
            }
            lineBuilder.setVertices(vertices1);
            myLongitudeLines.add(new PolylineGeometry(lineBuilder, props, null));
        }
        publishGeometries(myLongitudeLines, Collections.<PolylineGeometry>emptySet());
    }

    /**
     * Set up the menu items for handling latitude lines.
     *
     * @param toolbox References to facilities that may be used by the plug-in
     *            to interact with the rest of the system.
     */
    private void initializeLatMenu(Toolbox toolbox)
    {
        JMenu overlaysMenu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                MenuBarRegistry.VIEW_MENU, MenuBarRegistry.OVERLAYS_MENU);

        JMenu latMenu = new JMenu("Latitude lines");
        final JCheckBoxMenuItem latEnable = new JCheckBoxMenuItem("Lines Enabled");
        latEnable.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                if (latEnable.isSelected())
                {
                    enableLatitudeGrid();
                }
                else
                {
                    disableLatitudeGrid();
                }
            }
        });
        latMenu.add(latEnable);
        final JMenuItem latSpacing = new JMenuItem("Set Line Spacing...");
        latSpacing.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                String val = JOptionPane.showInputDialog(myParentFrameProvider.get(), "Latitude line spacing in degrees",
                        Double.toString(myLatitudeSpacing));
                if (val != null)
                {
                    StringBuilder errorMsg = new StringBuilder(114);
                    errorMsg.append("Unable to change spacing.  Value must be valid number greater\n than or equal to ");
                    errorMsg.append(MIN_SPACING);
                    errorMsg.append(" and less than or equal to 90.");
                    try
                    {
                        double space = Double.parseDouble(val);
                        if (space < MIN_SPACING || space > 90.)
                        {
                            JOptionPane.showMessageDialog(myParentFrameProvider.get(), errorMsg.toString(),
                                    "Entered value out of range", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        setLatitudeSpacing(space);
                        if (latEnable.isSelected())
                        {
                            publishLatitudeLines();
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        JOptionPane.showMessageDialog(myParentFrameProvider.get(), errorMsg.toString(),
                                "Entered value not properly formatted", JOptionPane.INFORMATION_MESSAGE);
                        LOGGER.warn("Latitude line spacing not set to \"" + val + "\".");
                    }
                }
            }
        });
        latMenu.add(latSpacing);
        overlaysMenu.add(latMenu);
    }

    /**
     * Set up the menu items for handling longitude lines.
     *
     * @param toolbox References to facilities that may be used by the plug-in
     *            to interact with the rest of the system.
     */
    private void initializeLonMenu(Toolbox toolbox)
    {
        JMenu overlaysMenu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                MenuBarRegistry.VIEW_MENU, MenuBarRegistry.OVERLAYS_MENU);

        JMenu lonMenu = new JMenu("Longitude lines");
        final JCheckBoxMenuItem lonEnable = new JCheckBoxMenuItem("Lines Enabled");
        lonEnable.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                if (lonEnable.isSelected())
                {
                    enableLongitudeGrid();
                }
                else
                {
                    disableLongitudeGrid();
                }
            }
        });
        lonMenu.add(lonEnable);
        final JMenuItem lonSpacing = new JMenuItem("Set Line Spacing...");
        lonSpacing.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                String val = JOptionPane.showInputDialog(myParentFrameProvider.get(), "Longitude line spacing in degrees",
                        Double.toString(myLongitudeSpacing));
                if (val != null)
                {
                    StringBuilder errorMsg = new StringBuilder(115);
                    errorMsg.append("Unable to change spacing.  Value must be valid number greater\n than or equal to ");
                    errorMsg.append(MIN_SPACING);
                    errorMsg.append(" and less than or equal to 180.");
                    try
                    {
                        double space = Double.parseDouble(val);
                        if (space < MIN_SPACING || space > 180.)
                        {
                            JOptionPane.showMessageDialog(myParentFrameProvider.get(), errorMsg.toString(),
                                    "Entered value out of range", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        setLongitudeSpacing(space);
                        if (lonEnable.isSelected())
                        {
                            publishLongitudeLines();
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        JOptionPane.showMessageDialog(myParentFrameProvider.get(), errorMsg.toString(),
                                "Entered value not properly formatted", JOptionPane.INFORMATION_MESSAGE);
                        LOGGER.warn("Longitude line spacing not set to \"" + val + "\".");
                    }
                }
            }
        });
        lonMenu.add(lonSpacing);

        overlaysMenu.add(lonMenu);
    }
}
