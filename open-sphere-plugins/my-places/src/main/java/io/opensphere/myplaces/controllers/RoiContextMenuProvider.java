package io.opensphere.myplaces.controllers;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.myplaces.models.MyPlacesModel;

public class RoiContextMenuProvider implements ContextMenuProvider<DataGroupInfo.DataGroupContextKey>
{

    /**
     * Provides the context menu for group modifications.
     *
     */
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The places model.
     */
    private final MyPlacesModel myModel;

    /**
     * Constructs a new points context menu provider.
     *
     * @param toolbox The toolbox.
     * @param model The my places model.
     */
    public RoiContextMenuProvider(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myModel = model;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, DataGroupContextKey key)
    {
        List<Component> menuItems;

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 6;
    }
}
