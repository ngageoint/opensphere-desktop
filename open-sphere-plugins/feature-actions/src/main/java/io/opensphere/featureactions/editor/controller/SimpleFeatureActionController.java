package io.opensphere.featureactions.editor.controller;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Controller that can duplicate and remove a single
 * {@link SimpleFeatureAction}.
 */
public class SimpleFeatureActionController
{
    /**
     * The action to either copy and/or remove.
     */
    private final SimpleFeatureAction myAction;

    /**
     * Contains all actions for the entire layer.
     */
    private final SimpleFeatureActions myActions;

    /**
     * The group the action belongs to.
     */
    private final SimpleFeatureActionGroup myGroup;

    /**
     * Used to get the available columns for the type.
     */
    private final DataTypeController myTypeController;

    /**
     * Constructs a new controller.
     *
     * @param typeController Used to get the available columns for the type.
     * @param actions Contains all actions for the entire layer.
     * @param group The group the action belongs to.
     * @param action The action to either copy or remove when asked.
     */
    public SimpleFeatureActionController(DataTypeController typeController, SimpleFeatureActions actions,
            SimpleFeatureActionGroup group, SimpleFeatureAction action)
    {
        myTypeController = typeController;
        myActions = actions;
        myGroup = group;
        myAction = action;
        populateAvailableColumns();
    }

    /**
     * Copies the action and adds the copy to the existing group.
     */
    public void copy()
    {
        FeatureAction act = myAction.getFeatureAction();
        FeatureAction copied = XMLUtilities.jaxbClone(act, FeatureAction.class);
        if (Utilities.sameInstance(copied, act))
        {
            return;
        }

        copied.setName(copied.getName() + " Copy");
        SimpleFeatureAction simpleCopy = new SimpleFeatureAction(copied);
        myGroup.getActions().add(simpleCopy);
    }

    /**
     * Removes the action from the group.
     */
    public void remove()
    {
        myGroup.getActions().remove(myAction);
    }

    /**
     * Gets all the available columns for the given layer.
     */
    private void populateAvailableColumns()
    {
        String layerId = myActions.getLayerId();
        DataTypeInfo layer = myTypeController.getDataTypeInfoForType(layerId);
        if (layer != null)
        {
            List<String> columns = New.list(layer.getMetaDataInfo().getKeyNames());
            Collections.sort(columns);

            for (String column : columns)
            {
                if (!myAction.getAvailableColumns().contains(column))
                {
                    myAction.getAvailableColumns().add(column);
                }
            }
        }
    }
}
