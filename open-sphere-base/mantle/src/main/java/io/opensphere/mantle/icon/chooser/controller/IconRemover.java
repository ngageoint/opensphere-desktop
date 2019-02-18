package io.opensphere.mantle.icon.chooser.controller;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.model.IconModel;

/**
 * Deletes the selected icon(s).
 */
public class IconRemover
{
    /**
     * The icon model.
     */
    private final IconModel myModel;

    /**
     * Constructor.
     *
     * @param model The icon model.
     */
    public IconRemover(IconModel model)
    {
        myModel = model;
    }

    /**
     * Deletes the selected icon(s) from the icon registry.
     */
    public void deleteIcons()
    {
        IconRecord record = myModel.selectedRecordProperty().get();
        if(record != null)
        {
            myModel.getIconRegistry().removeIcon(record, this);
        }
    }
}
