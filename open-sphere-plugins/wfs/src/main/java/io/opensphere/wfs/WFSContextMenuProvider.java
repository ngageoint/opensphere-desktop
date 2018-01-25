package io.opensphere.wfs;

import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent.RequeryType;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * Content menu provider for WFS data types.
 */
public class WFSContextMenuProvider implements ContextMenuProvider<DataGroupContextKey>
{
    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The event manager. */
    private final EventManager myEventManager;

    /** Supplier for a dialog parent. */
    private final Supplier<? extends JFrame> myDialogParentSupplier;

    /**
     * Constructor.
     *
     * @param mantleToolbox The mantle toolbox.
     * @param eventManager The event manager.
     * @param dialogParentSupplier A supplier for a dialog parent component.
     */
    public WFSContextMenuProvider(MantleToolbox mantleToolbox, EventManager eventManager,
            Supplier<? extends JFrame> dialogParentSupplier)
    {
        myMantleToolbox = mantleToolbox;
        myEventManager = eventManager;
        myDialogParentSupplier = dialogParentSupplier;
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, DataGroupContextKey key)
    {
        if (key.getDataType() instanceof WFSDataType)
        {
            final WFSDataType wfsDataType = (WFSDataType)key.getDataType();

            List<JMenuItem> menuItems = New.list();

            JMenuItem configItem = new JMenuItem("Select Feature Columns");
            configItem.addActionListener(e -> wfsDataType.showConfig(myDialogParentSupplier.get()));
            menuItems.add(configItem);

            if (!Boolean.getBoolean("opensphere.productionMode"))
            {
                JMenuItem printItem = new JMenuItem("Print WFS configuration");
                printItem.addActionListener(e -> wfsDataType.showContent(myDialogParentSupplier.get()));
                menuItems.add(printItem);
            }

            if (wfsDataType.isVisible())
            {
                if (myMantleToolbox.getDataElementCache().getElementCountForType(wfsDataType) > 0)
                {
                    JMenuItem purgeItem = new JMenuItem("Purge Data");
                    purgeItem.addActionListener(e -> myMantleToolbox.getDataTypeController().removeDataType(wfsDataType, this));
                    menuItems.add(purgeItem);
                }
                if (!myMantleToolbox.getQueryRegionManager().getQueryRegions().isEmpty())
                {
                    JMenuItem requeryItem = new JMenuItem("Re-Query Data");
                    requeryItem.addActionListener(
                        e -> myEventManager.publishEvent(new SingleLayerRequeryEvent(wfsDataType, RequeryType.FULL_REQUERY)));
                    menuItems.add(requeryItem);
                }
            }

            return menuItems;
        }
        return null;
    }

    @Override
    public int getPriority()
    {
        return 40;
    }
}
