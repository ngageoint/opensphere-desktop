package io.opensphere.myplaces.importer;

import java.awt.EventQueue;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import io.opensphere.core.Plugin;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;

/**
 * Abstract exporter that creates my places from {@link DataElement}s.
 */
public abstract class AbstractDataElementExporter extends AbstractExporter
{
    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && Plugin.class.isAssignableFrom(target)
                && getObjects().stream().allMatch(o -> o instanceof DataElement);
    }

    @Override
    public File export(Object target) throws ExportException
    {
        assert !EventQueue.isDispatchThread();

        Collection<MapDataElement> elements = CollectionUtilities.filterDowncast(getObjects(), MapDataElement.class);
        if (!elements.isEmpty())
        {
            DataTypeInfo dataType = elements.iterator().next().getDataTypeInfo();
            export(dataType, elements);
        }
        else
        {
            throw new ExportException("No data are selected.");
        }
        if (target instanceof File)
        {
            return (File)target;
        }
        return null;
    }

    @Override
    public MimeType getMimeType()
    {
        return null;
    }

    /**
     * Exports the data elements.
     *
     * @param dataType the data type
     * @param elements the data elements
     * @throws ExportException If a problem occurs during export.
     */
    protected abstract void export(DataTypeInfo dataType, Collection<? extends MapDataElement> elements) throws ExportException;

    /**
     * Determines whether the size of the export is acceptable.
     *
     * @param size the number of elements to export
     * @return whether the export is accepted
     */
    protected boolean acceptSize(int size)
    {
        return size <= 20 || JOptionPane.OK_OPTION == EventQueueUtilities.happyOnEdt(() -> Integer.valueOf(
                JOptionPane.showConfirmDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                        "Create " + size + " My Places?", "Confirm Large Copy", JOptionPane.OK_CANCEL_OPTION)));
    }

    /**
     * Gets the parent data group.
     *
     * @return the parent data group, or null if it couldn't be found
     */
    protected MyPlacesDataGroupInfo getParentGroup()
    {
        MyPlacesDataGroupInfo parentDataGroup = null;
        List<DataGroupInfo> groups = New.list(1);
        MantleToolbox mantleToolbox = getToolbox().getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        mantleToolbox.getDataGroupController().findDataGroupInfo(g -> g instanceof MyPlacesDataGroupInfo, groups, true);
        if (!groups.isEmpty())
        {
            parentDataGroup = (MyPlacesDataGroupInfo)groups.get(0);
        }
        return parentDataGroup;
    }
}
