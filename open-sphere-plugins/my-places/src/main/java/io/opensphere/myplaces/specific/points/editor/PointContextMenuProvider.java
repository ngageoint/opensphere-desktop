package io.opensphere.myplaces.specific.points.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Provides context menu items specific for points.
 */
public class PointContextMenuProvider implements ContextMenuProvider<DataGroupInfo.DataGroupContextKey>
{
    /**
     * Allows the user to create a point by manually typing in the location.
     */
    private final ManualPointCreator myManualCreator;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new points context menu provider.
     *
     * @param toolbox The toolbox.
     * @param controller the controller
     * @param editListener The edit listener to launch a point editor.
     * @param model The model.
     */
    public PointContextMenuProvider(Toolbox toolbox, AnnotationEditController controller, MyPlacesEditListener editListener,
            MyPlacesModel model)
    {
        myToolbox = toolbox;
        myManualCreator = new ManualPointCreator(myToolbox, controller, editListener, model);
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, final DataGroupInfo.DataGroupContextKey key)
    {
        List<JMenuItem> menuItems = New.list();

        if (key.getDataGroup() instanceof MyPlacesDataGroupInfo && key.getDataType() == null)
        {
            final MyPlacesDataGroupInfo group = (MyPlacesDataGroupInfo)key.getDataGroup();

            JMenuItem configItem = new JMenuItem("Create map point...");
            configItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    myManualCreator.createManualPoint(group);
                }
            });
            menuItems.add(configItem);
        }

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }
}
