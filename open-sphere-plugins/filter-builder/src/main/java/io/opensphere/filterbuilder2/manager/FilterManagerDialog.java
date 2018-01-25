package io.opensphere.filterbuilder2.manager;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder.controller.FilterSet;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.filter.DataLayerFilter;

/**
 * The filter manager dialog.
 */
public final class FilterManagerDialog extends OptionDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(FilterManagerDialog.class);

    /**
     * Gets the dialog title for the given data type.
     *
     * @param dataType the data type
     * @return the dialog title
     */
    private static String dialogTitle(DataTypeInfo dataType)
    {
        if (dataType != null)
        {
            return dataType.getDisplayName() + " Features Filters";
        }
        return "Manage Filters";
    }

    /**
     * Get the label to be used in a menu that launches a dialog for the specified type of filter. In case of null argument, it
     * returns the generic "Manage Filters" label.
     *
     * @param dataType the datatype for which the label will be generated (optional, will default to a blank value if supplied
     *            with null).
     * @return the stuff
     */
    public static String menuLabel(DataTypeInfo dataType)
    {
        if (dataType != null)
        {
            return dataType.getDisplayName() + " Features";
        }
        return "Manage Filters";
    }

    /**
     * Shows the filter manager for the given data group.
     *
     * @param toolbox the toolbox
     * @param dataGroupInfo the data group info
     */
    public static void showDataGroup(FilterBuilderToolbox toolbox, DataGroupInfo dataGroupInfo)
    {
        showDataType(toolbox, getDataType(dataGroupInfo));
    }

    /**
     * Shows the filter manager for the given data type.
     *
     * @param tools the toolbox with which the dialog will be configured.
     * @param dataType the data type
     */
    public static void showDataType(FilterBuilderToolbox tools, DataTypeInfo dataType)
    {
        FilterManagerDialog dialog = new FilterManagerDialog(tools, dataType);
        dialog.build();
        dialog.showDialog();
    }

    // For now, assume that this is in no-save mode.
    // Also note that the dialog is modal in this case.
    /**
     * Shows a dialog from which the user can select filters.
     *
     * @param tools the toolbox with which the dialog is configured.
     * @param fs the filter set with which to populate the dialog.
     * @return true if the user clicked okay, false otherwise.
     */
    public static boolean showFilterSet(FilterBuilderToolbox tools, FilterSet fs)
    {
        FilterManagerDialog dialog = new FilterManagerDialog(tools, fs);
        dialog.build();
        dialog.showDialog();
        return dialog.getSelection() == JOptionPane.OK_OPTION;
    }

    /**
     * Gets the data type to filter from the data group.
     *
     * @param dataGroupInfo the data group
     * @return the data type to filter
     */
    private static DataTypeInfo getDataType(DataGroupInfo dataGroupInfo)
    {
        DataTypeInfo dataType = null;
        if (dataGroupInfo != null)
        {
            Collection<DataTypeInfo> filterableDataTypes = DataLayerFilter.getFilterableDataTypes(dataGroupInfo);
            if (filterableDataTypes.size() == 1)
            {
                dataType = filterableDataTypes.iterator().next();
            }
            else
            {
                LOGGER.error(filterableDataTypes.size() + " data types found for " + dataGroupInfo.getLongDisplayName());
            }
        }
        return dataType;
    }

    /**
     * Construct with a FilterSet. For now, we assume that this constructor is only used when the edits apply only to the given
     * filters and are not persisted.
     *
     * @param tools the tools from which the UI registry is extracted.
     * @param fs the filter set with which to populate the dialog.
     */
    private FilterManagerDialog(FilterBuilderToolbox tools, FilterSet fs)
    {
        super(tools.getMainToolBox().getUIRegistry().getMainFrameProvider().get());
        setTitle("Select Filter (Ops Clock)");
        setModal(true);
        setButtonLabels(Arrays.asList(ButtonPanel.OK));

        FilterManagerPanel fmp = new FilterManagerPanel(tools, fs);
        setComponent(fmp);
    }

    /**
     * Constructor.
     *
     * @param tools the toolbox
     * @param dataType the data type
     */
    private FilterManagerDialog(FilterBuilderToolbox tools, DataTypeInfo dataType)
    {
        super(tools.getMainToolBox().getUIRegistry().getMainFrameProvider().get());
        setTitle(dialogTitle(dataType));
        setModal(false);
        setButtonLabels(Arrays.asList(ButtonPanel.OK));

        FilterManagerPanel filterManagerPanel = new FilterManagerPanel(tools, dataType);
        setComponent(filterManagerPanel);
        getContentButtonPanel().add(filterManagerPanel.getExportButton());
        getContentButtonPanel().add(filterManagerPanel.getImportButton());
    }
}
