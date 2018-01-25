package io.opensphere.controlpanels.layers.activedata;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Provides an export menu item for a single selected data type, if there is a
 * valid exporter.
 */
public class ExportersSingleMenuProvider implements ContextMenuProvider<DataGroupContextKey>
{
    /**
     * Used to provide the menu items.
     */
    private final ExportersMenuProvider myMultiProvider;

    /**
     * Constructor.
     *
     * @param multiProvider Used to build the menu items.
     */
    public ExportersSingleMenuProvider(ExportersMenuProvider multiProvider)
    {
        myMultiProvider = multiProvider;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
    {
        List<DataTypeInfo> dataTypes = New.list();
        if (key.getDataType() != null)
        {
            dataTypes.add(key.getDataType());
        }
        return myMultiProvider.getMenuItems(contextId,
                new MultiDataGroupContextKey(New.list(key.getDataGroup()), New.list(key.getDataGroup()), dataTypes));
    }

    @Override
    public int getPriority()
    {
        return myMultiProvider.getPriority();
    }
}
