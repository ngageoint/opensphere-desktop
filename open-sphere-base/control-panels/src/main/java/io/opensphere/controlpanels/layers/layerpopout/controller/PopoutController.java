package io.opensphere.controlpanels.layers.layerpopout.controller;

import java.util.Set;

import io.opensphere.controlpanels.layers.activedata.controller.ActiveDataDataLayerController;
import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.HUDFrameBoundsHandler;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Controller for the Layer popout tool.
 */
public class PopoutController extends ActiveDataDataLayerController implements HUDFrameBoundsHandler
{
    /**
     * The model.
     */
    private final PopoutModel myModel;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs the popout controller.
     *
     * @param pBox The toolbox.
     * @param model The model for the popout tool.
     */
    public PopoutController(Toolbox pBox, PopoutModel model)
    {
        super(pBox, null);
        myModel = model;
        myToolbox = pBox;
        saveModel();
    }

    @Override
    public void boundsSet(int x, int y, int width, int height)
    {
        myModel.setHeight(height);
        myModel.setWidth(width);
        myModel.setY(y);
        myModel.setX(x);
        saveModel();
    }

    /**
     * Called when the view is closed by the user.
     */
    public void closed()
    {
        PopoutPreferences.getInstance().removeModel(myToolbox, myModel);
    }

    /**
     * Gets the popout model.
     *
     * @return The model.
     */
    public PopoutModel getModel()
    {
        return myModel;
    }

    @Override
    protected Set<DataGroupInfo> getDataGroupInfoSet()
    {
        Set<DataGroupInfo> popoutGroups = New.set();
        Set<DataGroupInfo> dataGroups = super.getDataGroupInfoSet();

        for (DataGroupInfo dataGroup : dataGroups)
        {
            Set<DataGroupInfo> viewableGroups = getViewableGroups(dataGroup);
            popoutGroups.addAll(viewableGroups);
        }

        return popoutGroups;
    }

    /**
     * Checks to see if the given data group is viewable by the tool.
     *
     * @param dataGroup The data group that is viewable.
     * @return True if the group is viewable, false otherwise.
     */
    private Set<DataGroupInfo> getViewableGroups(DataGroupInfo dataGroup)
    {
        Set<DataGroupInfo> viewableGroups = New.set();

        if (myModel.getDataGroupInfoKeys().contains(dataGroup.getId()))
        {
            viewableGroups.add(dataGroup);
        }

        for (DataGroupInfo child : dataGroup.getChildren())
        {
            viewableGroups.addAll(getViewableGroups(child));
        }

        return viewableGroups;
    }

    /**
     * Saves the model to the popout preferences.
     */
    private void saveModel()
    {
        PopoutPreferences.getInstance().saveModel(myToolbox, myModel);
    }
}
