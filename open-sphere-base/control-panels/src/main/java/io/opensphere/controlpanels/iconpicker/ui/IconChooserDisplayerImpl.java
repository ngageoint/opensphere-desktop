package io.opensphere.controlpanels.iconpicker.ui;

import java.util.function.Supplier;

import javafx.beans.property.ObjectProperty;
import javafx.stage.Stage;

import javax.swing.JFrame;

import io.opensphere.controlpanels.iconpicker.controller.IconChooserDisplayer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.gui.IconChooserDialog;

/**
 * Displays the {@link IconChooserDialog} and gets the {@link IconRecord} from
 * the user.
 */
public class IconChooserDisplayerImpl implements IconChooserDisplayer
{
    /**
     * The parent UI.
     */
    private final Supplier<? extends Stage> myParent;

    /**
     * Constructs a new displayer.
     *
     * @param parent The parent UI.
     */
    public IconChooserDisplayerImpl(Supplier<? extends Stage> parent)
    {
        myParent = parent;
    }

    @Override
    public void displayIconChooser(Toolbox toolbox, ObjectProperty<IconRecord> selectedIcon)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            IconChooserDialog fileDialog = new IconChooserDialog(myParent.get(), true, toolbox);
            fileDialog.addActionListener(action ->
            {
                if (action.getActionCommand() == IconChooserDialog.ICON_SELECTED)
                {
                    selectedIcon.set(fileDialog.getSelectedIcon());
                }
                else
                {
                    selectedIcon.set(null);
                }
            });
            fileDialog.setVisible(true);
        });
    }
}
