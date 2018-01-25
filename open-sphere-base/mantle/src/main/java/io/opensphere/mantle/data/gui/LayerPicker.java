package io.opensphere.mantle.data.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import javafx.scene.control.ButtonBar.ButtonData;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;

/** Layer picker dialog. */
public class LayerPicker extends JFXDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Shows the picker for active data types matching the filter, and returns
     * the selections. Can be called from any thread.
     *
     * @param toolbox the toolbox
     * @param title the title
     * @param typeFilter the data type filter
     * @return the selected data types
     */
    public static Collection<DataTypeInfo> showActive(Toolbox toolbox, String title, Predicate<? super DataTypeInfo> typeFilter)
    {
        Window owner = toolbox.getUIRegistry().getMainFrameProvider().get();
        Collection<DataTypeInfo> dataTypes = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class)
                .getDataGroupController().findActiveMembers(typeFilter);
        return show(owner, title, dataTypes);
    }

    /**
     * Shows the picker and returns the selections. Can be called from any
     * thread.
     *
     * @param owner the owner
     * @param title the title
     * @param dataTypes the data type options
     * @return the selected data types
     */
    public static Collection<DataTypeInfo> show(Window owner, String title, Collection<? extends DataTypeInfo> dataTypes)
    {
        return EventQueueUtilities.happyOnEdt(() ->
        {
            LayerPicker picker = new LayerPicker(owner, title, dataTypes);
            picker.setVisible(true);
            return picker.getSelections();
        });
    }

    /**
     * Constructor. Must be called from the Swing thread.
     *
     * @param owner the owner
     * @param title the title
     * @param dataTypes the data type options
     */
    public LayerPicker(Window owner, String title, Collection<? extends DataTypeInfo> dataTypes)
    {
        super(owner, title, () -> new LayerListView(dataTypes));
        setMinimumSize(new Dimension(300, 300));
        setSize(300, 400);
        setLocationRelativeTo(owner);
    }

    /**
     * Gets the user's selections.
     *
     * @return the selected data types
     */
    public Collection<DataTypeInfo> getSelections()
    {
        return getResponse() == ButtonData.OK_DONE ? ((LayerListView)getFxNode()).getSelections() : Collections.emptyList();
    }
}
