package io.opensphere.subterrain.debug;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Used to debug the xray goggles.
 */
public class XrayGogglesDebug
{
    /**
     * The minimum latitude.
     */
    private static final double MAX_LAT = 39.4248828113;

    /**
     * The maximum longitude.
     */
    private static final double MAX_LON = -104.2043926957;

    /**
     * The minimum latitude.
     */
    private static final double MIN_LAT = 39.4186373637;

    /**
     * The maximum latitude.
     */
    private static final double MIN_LON = -104.2154023278;

    /**
     * The opacity to set for the tile.
     */
    private static final float OPACITY = .4f;

    /**
     * Any tiles within this box will become transparent.
     */
    private static final GeographicBoundingBox TRANS_BOX = new GeographicBoundingBox(
            LatLonAlt.createFromDegrees(MIN_LAT, MIN_LON), LatLonAlt.createFromDegrees(MAX_LAT, MAX_LON));

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new xray goggles debug class that shows a debug menu item.
     *
     * @param toolbox The system toolbox.
     */
    public XrayGogglesDebug(Toolbox toolbox)
    {
        myToolbox = toolbox;
        EventQueueUtilities.runOnEDT(() ->
        {
            myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                    .add(getDebugMenu());
        });
    }

    /**
     * Collects all the tiles we need to make transparent.
     *
     * @param parent The parent tile.
     * @param transparents The list to add the tiles to.
     */
    private void collectTiles(TileGeometry parent, List<TileGeometry> transparents)
    {
        Collection<TileGeometry> children = parent.getChildren(false);
        for (TileGeometry child : children)
        {
            GeographicBoundingBox childBox = getGeographicBox(child);
            if (TRANS_BOX.contains(childBox))
            {
                transparents.add(child);
            }

            if (TRANS_BOX.intersects(childBox))
            {
                collectTiles(child, transparents);
            }
        }
    }

    /**
     * The debug menu item to draw an ellipsoid on globe.
     *
     * @return The menu item.
     */
    private JMenuItem getDebugMenu()
    {
        JMenuItem menu = new JMenuItem("Xray Goggles");
        menu.addActionListener((a) -> makeTransparent());
        return menu;
    }

    /**
     * Gets a geographic bounding box of the tile.
     *
     * @param tile The tile to get a geo box for.
     * @return The geographic bounding box of the tile or null if not a geo
     *         tile.
     */
    private GeographicBoundingBox getGeographicBox(TileGeometry tile)
    {
        List<GeographicPosition> corners = New.list();
        for (Position position : tile.getBounds().getVertices())
        {
            if (position instanceof GeographicPosition)
            {
                corners.add((GeographicPosition)position);
            }
        }

        GeographicBoundingBox box = null;
        if (!corners.isEmpty())
        {
            box = GeographicBoundingBox.getMinimumBoundingBox(corners);
        }

        return box;
    }

    /**
     * Makes any tile within the bbox -104.2154023278, 39.4186373637,
     * -104.2043926957, 39.4248828113 transparent.
     */
    private void makeTransparent()
    {
        GeometryRegistry registry = myToolbox.getGeometryRegistry();
        Collection<Geometry> geometries = registry.getGeometries();
        for (Geometry geometry : geometries)
        {
            if (geometry instanceof TileGeometry)
            {
                TileGeometry tile = (TileGeometry)geometry;
                GeographicBoundingBox box = getGeographicBox(tile);
                if (box != null && box.intersects(TRANS_BOX))
                {
                    List<TileGeometry> transparents = New.list();
                    collectTiles(tile, transparents);

                    for (TileGeometry geom : transparents)
                    {
                        geom.getRenderProperties().setOpacity(OPACITY);
                        geom.getRenderProperties().setObscurant(false);
                    }
                }
            }
        }
    }
}
