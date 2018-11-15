package io.opensphere.controlpanels.iconpicker.ui;

import java.util.function.Supplier;

import javax.swing.JFrame;

import io.opensphere.controlpanels.iconpicker.controller.IconChooserDisplayer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.gui.IconChooserDialog;
import io.opensphere.mantle.iconproject.view.IconDialog;
import javafx.beans.property.ObjectProperty;

/**
 * Displays the {@link IconChooserDialog} and gets the {@link IconRecord} from
 * the user.
 */
public class IconChooserDisplayerImpl implements IconChooserDisplayer
{
    /**
     * The parent UI.
     */
    private final Supplier<? extends JFrame> myParent;

    /**
     * Constructs a new displayer.
     *
     * @param supplier The parent UI.
     */
    public IconChooserDisplayerImpl(Supplier<? extends JFrame> supplier)
    {
        myParent = supplier;
    }

    @Override
    public void displayIconChooser(Toolbox toolbox, ObjectProperty<IconRecord> selectedIcon)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            IconDialog dialog = new IconDialog(toolbox, myParent.get());
            dialog.setInitialValueSupplier(
                    () -> selectedIcon.get() != null ? selectedIcon.get().imageURLProperty().get().toString() : null);
            dialog.setAcceptListener(r -> selectedIcon.set(r));
            dialog.setVisible(true);
        });
    }
}
