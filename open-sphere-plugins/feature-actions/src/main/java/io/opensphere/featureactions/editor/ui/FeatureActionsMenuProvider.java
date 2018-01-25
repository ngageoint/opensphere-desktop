package io.opensphere.featureactions.editor.ui;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.crust.DataTypeChecker;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Shows the feature actions menu item when the user right clicks on a feature
 * layer.
 */
public class FeatureActionsMenuProvider implements ContextMenuProvider<DataGroupContextKey>
{
    /** Displays the editor. */
    private final ActionEditorDisplayer myDisplayer;

    /** Used to get the main frame. */
    private Supplier<JFrame> mainFrame;

    /**
     * Constructs a new menu provider.
     *
     * @param displayer Displays the editor.
     * @param uiRegistry Used to get the main frame.
     */
    public FeatureActionsMenuProvider(ActionEditorDisplayer displayer, UIRegistry uiRegistry)
    {
        myDisplayer = displayer;
        if (uiRegistry != null)
        {
            mainFrame = () -> uiRegistry.getMainFrameProvider().get();
        }
        else
        {
            mainFrame = () -> null;
        }
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
    {
        if (!DataTypeChecker.isFeatureType(key.getDataType()))
        {
            return Collections.emptyList();
        }

        return menuItemsForLayer(getDataTypes(key));
    }

    @Override
    public int getPriority()
    {
        return 21;
    }

    /**
     * Construct a menu item for a feature layer, if one is provided.
     *
     * @param layer the layer
     * @return a list of one or fewer menu items
     */
    public Collection<? extends Component> menuItemsForLayer(DataTypeInfo layer)
    {
        List<Component> mergeMenus = New.list();
        if (layer != null)
        {
            JMenuItem featureActionItem = SwingUtilities.newMenuItem("Feature Actions...",
                    e -> myDisplayer.displaySimpleEditor(mainFrame.get(), layer));
            mergeMenus.add(featureActionItem);
        }
        return mergeMenus;
    }

    /**
     * Gets the data types for the context key.
     *
     * @param key the context key
     * @return the data types
     */
    public DataTypeInfo getDataTypes(DataGroupContextKey key)
    {
        Collection<DataTypeInfo> dataTypes = New.list(key.getDataTypes());
        for (DataGroupInfo group : key.getDataGroups())
        {
            dataTypes.addAll(group.getMembers(false));
        }

        for (DataTypeInfo layer : dataTypes)
        {
            if (DataTypeChecker.isFeatureType(layer))
            {
                return layer;
            }
        }

        return null;
    }
}
