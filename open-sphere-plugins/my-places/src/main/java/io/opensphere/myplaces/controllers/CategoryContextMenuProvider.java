package io.opensphere.myplaces.controllers;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.CategoryContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Provides the context menu for group modifications.
 *
 */
public class CategoryContextMenuProvider implements ContextMenuProvider<CategoryContextKey>
{
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
    public CategoryContextMenuProvider(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myModel = model;
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, final CategoryContextKey key)
    {
        List<Component> menuItems;
        if (Constants.MY_PLACES_LABEL.equals(key.getCategory()))
        {
            menuItems = New.list(2);
            menuItems.add(new CreateFolderMenuItem(myToolbox, myModel.getDataGroups()));
            menuItems.add(new ExportMenu(myToolbox, Collections.singletonList(myModel.getDataGroups()),
                    Collections.<DataTypeInfo>emptyList()));
        }
        else
        {
            menuItems = Collections.emptyList();
        }
        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 4;
    }
}
