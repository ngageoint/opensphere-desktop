package io.opensphere.myplaces.controllers;

import java.io.File;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.export.Exporters;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.export.ExportMenuProvider;

/**
 * Export menu.
 */
public class ExportMenu extends JMenu
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param dataGroups the selected data group
     * @param dataTypes the selected data type
     */
    public ExportMenu(final Toolbox toolbox, final Collection<? extends DataGroupInfo> dataGroups,
            final Collection<? extends DataTypeInfo> dataTypes)
    {
        super("Export");
        ExportMenuProvider menuProvider = new ExportMenuProvider();
        Collection<Object> objects = CollectionUtilities.concat(dataGroups, dataTypes);
        for (JMenuItem menuItem : menuProvider.getMenuItems(toolbox, "To ", Exporters.getExporters(objects, toolbox, File.class)))
        {
            add(menuItem);
        }
    }
}
