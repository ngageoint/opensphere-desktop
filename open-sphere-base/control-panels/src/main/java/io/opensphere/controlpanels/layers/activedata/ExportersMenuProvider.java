package io.opensphere.controlpanels.layers.activedata;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.export.Exporters;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.export.ExportMenuProvider;

/**
 * Provides a right click menu items for exporting layers to files.
 */
public class ExportersMenuProvider implements ContextMenuProvider<MultiDataGroupContextKey>
{
    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new export menu provider to be used for the context menu in
     * the layers window.
     *
     * @param toolbox The system toolbox.
     */
    public ExportersMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, MultiDataGroupContextKey key)
    {
        List<Component> exportMenus = New.list(1);
        Collection<DataTypeInfo> dataTypes = getDataTypes(key);
        if (!dataTypes.isEmpty())
        {
            List<Exporter> exporters = Exporters.getExporters(dataTypes, myToolbox, File.class);
            if (!exporters.isEmpty())
            {
                JMenu exportMenu = new JMenu("Export");
                exportMenu.setIcon(new GenericFontIcon(AwesomeIconSolid.DOWNLOAD, Color.WHITE, 12));
                for (JMenuItem menuItem : new ExportMenuProvider().getMenuItems(myToolbox, "To ", exporters))
                {
                    exportMenu.add(menuItem);
                }
                exportMenus.add(exportMenu);
            }
        }
        return exportMenus;
    }

    @Override
    public int getPriority()
    {
        return 10;
    }

    /**
     * Gets the data types for the context key.
     *
     * @param key the context key
     * @return the data types
     */
    private Collection<DataTypeInfo> getDataTypes(MultiDataGroupContextKey key)
    {
        Collection<DataTypeInfo> dataTypes = key.getActualDataTypes();
        if (dataTypes.isEmpty())
        {
            for (DataGroupInfo group : key.getActualDataGroups())
            {
                dataTypes.addAll(group.getMembers(false));
            }
        }
        return dataTypes;
    }
}
