package io.opensphere.arcgis2;

import java.awt.Component;
import java.awt.Window;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.TextViewDialog;

/** A context menu provider for ArcGIS layers. */
public class ArcGISContextMenuProvider implements ContextMenuProvider<DataGroupContextKey>
{
    /** The tool box through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The provider used to access the main frame. */
    private final Supplier<? extends JFrame> myDialogParentSupplier;

    /**
     * Creates a new menu provider with the supplied tool box.
     * 
     * @param toolbox the tool box through which application state is accessed.
     */
    public ArcGISContextMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myDialogParentSupplier = myToolbox.getUIRegistry().getMainFrameProvider();
    }

    /**
     * {@inheritDoc}
     * 
     * @see io.opensphere.core.control.action.ContextMenuProvider#getMenuItems(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public Collection<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
    {
        if (!Boolean.getBoolean("opensphere.productionMode") && key.getDataGroup() instanceof ArcGISDataGroupInfo)
        {
            JMenuItem printMenu = new JMenuItem("Print ArcGIS Configuration");
            printMenu.addActionListener(e -> showContent(myDialogParentSupplier.get(), key.getDataType()));
            return Collections.singletonList(printMenu);
        }
        return null;
    }

    /**
     * Creates a dialog for the supplied data type.
     * 
     * @param parent the window in which the dialog will be shown.
     * @param dataType the data type for which to generate the summary.
     */
    private void showContent(Window parent, DataTypeInfo dataType)
    {
        TextViewDialog dialog = new TextViewDialog(parent, "DataTypeInfo Summary: " + dataType.getDisplayName(),
                printConfiguration(dataType), false, myToolbox.getPreferencesRegistry());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Creates a summary configuration for the supplied data type.
     * 
     * @param dataType the data type for which to print the configuration.
     * @return a String containing the summary of the supplied data type.
     */
    private String printConfiguration(DataTypeInfo dataType)
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ArcGIS Datatype:\nGeometry Column: ").append(dataType.getMetaDataInfo().getGeometryColumn());
        sb.append(dataType.toString());
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see io.opensphere.core.control.action.ContextMenuProvider#getPriority()
     */
    @Override
    public int getPriority()
    {
        return 40;
    }
}
