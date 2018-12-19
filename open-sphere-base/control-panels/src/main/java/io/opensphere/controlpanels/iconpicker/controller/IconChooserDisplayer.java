package io.opensphere.controlpanels.iconpicker.controller;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.view.IconDialog;
import javafx.beans.property.ObjectProperty;

/**
 * Displays the {@link IconDialog} and gets the {@link IconRecord} of the icon
 * the user picked.
 */
public interface IconChooserDisplayer
{
    /**
     * Displays the {@link IconDialog} and gets the {@link IconRecord} of the
     * icon the user picked.
     *
     * @param toolbox The system toolbox.
     * @param selectedIcon This property will be populated with the picked icon,
     *            or null if the user did not pick anything.
     */
    void displayIconChooser(Toolbox toolbox, ObjectProperty<IconRecord> selectedIcon);
}
