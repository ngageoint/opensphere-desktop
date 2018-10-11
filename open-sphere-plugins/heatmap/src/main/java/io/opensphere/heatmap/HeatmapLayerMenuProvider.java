package io.opensphere.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Heat map menu provider for layers. */
public class HeatmapLayerMenuProvider implements ContextMenuProvider<DataGroupContextKey>
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
            private ContextMenuProvider<DataGroupContextKey> myContextMenuProvider;

            @Override
            public void open()
            {
                ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
                if (manager != null)
                {
                    myContextMenuProvider = new HeatmapLayerMenuProvider(toolbox, heatmapController);
                    manager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                            myContextMenuProvider);
                }
            }

            @Override
            public void close()
            {
                ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
                if (manager != null && myContextMenuProvider != null)
                {
                    manager.deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                            myContextMenuProvider);
                }
            }
        };
    }

    /** The heat map controller. */
    private final HeatmapController myHeatmapController;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param heatmapController the heat map controller
     */
    public HeatmapLayerMenuProvider(Toolbox toolbox, HeatmapController heatmapController)
    {
        myToolbox = toolbox;
        myHeatmapController = heatmapController;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
    {
        List<JMenuItem> menuItems = Collections.emptyList();
        if (HeatmapUtilities.isFeatureLayer(key.getDataType()))
        {
            JMenuItem menuItem = SwingUtilities.newMenuItem(HeatmapController.MENU_TEXT,
                    e -> myHeatmapController.create(key.getDataType()));
            menuItem.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            menuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.FIRE, Color.WHITE));
            menuItem.setEnabled(hasFeatures(key.getDataType()));

            menuItems = Collections.singletonList(menuItem);
        }
        return menuItems;
    }

    /**
     * Tests to determine if the data type has any data loaded.
     *
     * @param dataType that data type for which the test is performed.
     * @return true if data is present for the supplied data type, false
     *         otherwise.
     */
    private boolean hasFeatures(DataTypeInfo dataType)
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getElementCountForType(dataType) > 0;
    }

    @Override
    public int getPriority()
    {
        return 30;
    }
}
