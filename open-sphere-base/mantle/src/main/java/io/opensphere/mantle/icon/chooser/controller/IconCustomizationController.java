package io.opensphere.mantle.icon.chooser.controller;

import javafx.beans.value.ObservableValue;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.model.CustomizationModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.icon.chooser.view.IconDetail;
import io.opensphere.mantle.icon.chooser.view.IconView;

/**
 * A controller used to modify the customization state of a given icon.
 */
public class IconCustomizationController
{
    /** The model in which customization operations are stored. */
    private final CustomizationModel myCustomizationModel;

    /** The icon model in which the chooser is backed. */
    private final IconModel myModel;

    /** The detail panel on which the selected icon information is displayed. */
    private final IconDetail myDetailPanel;

    /**
     * Creates a new controller with the supplied model and view.
     *
     * @param model the model in which the chooser's state is maintained.
     * @param iconView the view in which all icon operations are managed.
     */
    public IconCustomizationController(final IconModel model, final IconView iconView)
    {
        myModel = model;
        myDetailPanel = iconView.getDetailPanel();
        myCustomizationModel = model.getCustomizationModel();

        myModel.selectedRecordProperty().addListener(this::applySelectedIcon);
        if(myModel.selectedRecordProperty().get() != null)
        {
            applySelectedIcon(null, null, myModel.selectedRecordProperty().get());
        }
    }

    /**
     * Applies the selected icon to the dialog.
     * @param obs The observable value.
     * @param ov The old value.
     * @param nv The new value.
     */
    private void applySelectedIcon(ObservableValue<? extends IconRecord> obs, IconRecord ov, IconRecord nv)
    {
        System.out.println("Event setting selected icon record.");
        if (ov != null)
        {
            myCustomizationModel.nameProperty().unbindBidirectional(ov.nameProperty());
            myCustomizationModel.sourceProperty().unbindBidirectional(ov.collectionNameProperty());
        }
        myCustomizationModel.nameProperty().bindBidirectional(nv.nameProperty());
        myCustomizationModel.sourceProperty().bindBidirectional(nv.collectionNameProperty());
        myCustomizationModel.getTags().setAll(nv.getTags());
        myCustomizationModel.getTransformModel().resetAllToDefault();
        myDetailPanel.redrawPreview(nv);
    }
}
