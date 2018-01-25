package io.opensphere.featureactions.editor.ui;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Displays the {@link SimpleFeatureActionEditor} so the user can edit feature
 * actions for a given layer.
 */
public class ActionEditorDisplayerImpl implements ActionEditorDisplayer
{
    /** Stores all feature actions. */
    private final FeatureActionsRegistry myActionRegistry;

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructs a new displayer.
     *
     * @param toolbox The system toolbox.
     * @param actionRegistry Stores all feature actions.
     */
    public ActionEditorDisplayerImpl(Toolbox toolbox, FeatureActionsRegistry actionRegistry)
    {
        myToolbox = toolbox;
        myActionRegistry = actionRegistry;
    }

    @Override
    public void displaySimpleEditor(Window owner, DataTypeInfo layer)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            JFXDialog dialog = new JFXDialog(owner, "Feature Actions for " + layer.getDisplayName());
            dialog.setFxNode(new SimpleFeatureActionEditorUI(myToolbox, myActionRegistry, dialog, layer));
            dialog.setSize(new Dimension(910, 600));
            dialog.setResizable(true);
            dialog.setLocationRelativeTo(owner);
            dialog.setModal(false);
            dialog.setVisible(true);
        });
    }
}
