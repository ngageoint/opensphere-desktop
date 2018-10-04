package io.opensphere.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.SwingUtilities;

/** Heat map menu provider for query geometries. */
public class HeatmapGeometryMenuProvider implements ContextMenuProvider<GeometryContextKey>
{
    /**
     * Creates a service that manages the layer context menu.
     *
     * @param toolbox the toolbox
     * @param heatmapController the heat map controller
     * @return the service
     */
    public static Service createService(Toolbox toolbox, HeatmapController heatmapController)
    {
        return new Service()
        {
            private ContextMenuProvider<GeometryContextKey> myContextMenuProvider;

            @Override
            public void open()
            {
                ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
                if (manager != null)
                {
                    myContextMenuProvider = new HeatmapGeometryMenuProvider(toolbox, heatmapController);
                    manager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT,
                            GeometryContextKey.class, myContextMenuProvider);
                    manager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                            GeometryContextKey.class, myContextMenuProvider);
                }
            }

            @Override
            public void close()
            {
                ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
                if (manager != null && myContextMenuProvider != null)
                {
                    manager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT,
                            GeometryContextKey.class, myContextMenuProvider);
                    manager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                            GeometryContextKey.class, myContextMenuProvider);
                }
            }
        };
    }

    /** The heat map controller. */
    private final HeatmapController myHeatmapController;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param heatmapController the heat map controller
     */
    public HeatmapGeometryMenuProvider(Toolbox toolbox, HeatmapController heatmapController)
    {
        myHeatmapController = heatmapController;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, GeometryContextKey key)
    {
        List<JMenuItem> menuItems = Collections.emptyList();

        Geometry geometry = key.getGeometry();
        if (geometry instanceof PolygonGeometry)
        {
            JMenuItem menuItem = SwingUtilities.newMenuItem(HeatmapController.MENU_TEXT,
                e -> myHeatmapController.create(geometry));
            menuItem.setIcon( new GenericFontIcon(AwesomeIconSolid.FIRE, Color.WHITE, 12));
            menuItems = Collections.singletonList(menuItem);
        }

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 11510;
    }
}
