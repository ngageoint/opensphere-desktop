package io.opensphere.merge.controller;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.merge.model.MergeModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Validates the merge model.
 */
public class MergeValidator implements ChangeListener<String>
{
    /**
     * Used to get the merged root group to validate unique name.
     */
    private final DataGroupController myGroupController;

    /**
     * The model to validate.
     */
    private final MergeModel myModel;

    /**
     * The validator support, used to proxy validation messages to the UI.
     */
    private final DefaultValidatorSupport myValidatorSupport;

    /**
     * Constructs a new validator.
     *
     * @param groupController Used to get the merged root group to validate
     *            unique name.
     * @param model The merge model.
     */
    public MergeValidator(DataGroupController groupController, MergeModel model)
    {
        myGroupController = groupController;
        myModel = model;
        myValidatorSupport = new DefaultValidatorSupport(myModel);
        myModel.getNewLayerName().addListener(this);
        changed(null, null, myModel.getNewLayerName().get());
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        String errorMessage = null;
        if (StringUtils.isNotEmpty(newValue))
        {
            DataGroupInfo groupInfo = myGroupController.getDataGroupInfo(newValue);
            if (groupInfo != null)
            {
                errorMessage = "Layer name already exists.";
            }
        }
        else
        {
            errorMessage = "Please type in a new layer name.";
        }

        if (errorMessage == null)
        {
            myValidatorSupport.setValidationResult(ValidationStatus.VALID, null);
        }
        else
        {
            myValidatorSupport.setValidationResult(ValidationStatus.ERROR, errorMessage);
        }
    }

    /**
     * Gets the validator support.
     *
     * @return The validator support to proxy validation messages to the UI.
     */
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }
}
