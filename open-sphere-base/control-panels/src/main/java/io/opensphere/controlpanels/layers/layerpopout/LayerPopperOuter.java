package io.opensphere.controlpanels.layers.layerpopout;

import java.awt.Component;
import java.awt.Container;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.controlpanels.layers.layerpopout.controller.PopoutController;
import io.opensphere.controlpanels.layers.layerpopout.controller.PopoutPreferences;
import io.opensphere.controlpanels.layers.layerpopout.controller.TitlePopulator;
import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModel;
import io.opensphere.controlpanels.layers.layerpopout.view.LayerPopoutView;
import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Popouts specified layers to a new active layer window only showing those
 * layers.
 */
public class LayerPopperOuter
{
    /**
     * Indicates if the saved windows have already been launched.
     */
    private static boolean ourIsSavedLaunched;

    /**
     * Populates the title.
     */
    private final TitlePopulator myTitlePopulator;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Responsible for creating the popout views.
     */
    private final ActiveDataViewCreator myViewCreator;

    /**
     * Constructs a new popper outer and will restore any previously opened
     * popper outer windows.
     *
     * @param toolbox The toolbox.
     * @param viewCreator The acitve data view creator.
     */
    public LayerPopperOuter(Toolbox toolbox, ActiveDataViewCreator viewCreator)
    {
        myToolbox = toolbox;
        myViewCreator = viewCreator;
        myTitlePopulator = new TitlePopulator();
        launchSaved();
    }

    /**
     * Popouts the layers into another layer window.
     *
     * @param node The node to open the popout window for.
     * @param launchingComponent The component popping out the layers.
     */
    public void popoutLayers(TreeTableTreeNode node, Component launchingComponent)
    {
        PopoutModel model = new PopoutModel();
        myTitlePopulator.populateTitle(model, node.toString());

        List<DataGroupInfo> layers = getLayers(node);

        for (DataGroupInfo dataGroup : layers)
        {
            model.getDataGroupInfoKeys().add(dataGroup.getId());
        }

        LayerPopoutView panel = createPanel(model);

        panel.setLocation(launchingComponent.getWidth() + 10, 0);

        buildWindow(panel);

        panel.setVisible(true);
    }

    /**
     * Builds the HUD window for the popout panel.
     *
     * @param panel The popout panel.
     */
    private void buildWindow(LayerPopoutView panel)
    {
        HUDJInternalFrame.Builder builder = new HUDJInternalFrame.Builder();
        builder.setInternalFrame(panel);
        HUDJInternalFrame frame = new HUDJInternalFrame(builder);
        myToolbox.getUIRegistry().getComponentRegistry().addObjectsForSource(this, Collections.singleton(frame));
    }

    /**
     * Creates the popout panel.
     *
     * @param model The model.
     * @return The popout panel.
     */
    private LayerPopoutView createPanel(PopoutModel model)
    {
        PopoutController controller = new PopoutController(myToolbox, model);

        Container view = myViewCreator.createActiveDataView(controller);
        LayerPopoutView panel = new LayerPopoutView(myToolbox, view, controller);

        return panel;
    }

    /**
     * Gets the list of layers that are to be viewed by the popout window.
     *
     * @param node The tree node the user wants to open a new window for.
     * @return The list of data groups to be viewed.
     */
    private List<DataGroupInfo> getLayers(TreeTableTreeNode node)
    {
        List<DataGroupInfo> layers = New.list();

        if (node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            if (uo.isCategoryNode())
            {
                Set<String> parentsTaken = New.set();
                for (int i = 0; i < node.getChildCount(); i++)
                {
                    TreeTableTreeNode child = (TreeTableTreeNode)node.getChildAt(i);
                    if (child.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
                    {
                        GroupByNodeUserObject childUserObject = (GroupByNodeUserObject)child.getPayload().getPayloadData();
                        DataTypeInfo dataType = childUserObject.getDataTypeInfo();
                        DataGroupInfo dataGroup = childUserObject.getDataGroupInfo();
                        if (dataType != null)
                        {
                            if (dataType.getParent() != null && !parentsTaken.contains(dataType.getParent().getId()))
                            {
                                layers.add(dataType.getParent());
                                parentsTaken.add(dataType.getParent().getId());
                            }
                        }
                        else if (dataGroup != null)
                        {
                            if (dataGroup.isFlattenable() && !parentsTaken.contains(dataGroup.getId()))
                            {
                                layers.add(dataGroup);
                                parentsTaken.add(dataGroup.getId());
                            }
                            else if (!dataGroup.isFlattenable() && dataGroup.getParent() != null
                                    && !parentsTaken.contains(dataGroup.getParent().getId()))
                            {
                                layers.add(dataGroup.getParent());
                                parentsTaken.add(dataGroup.getParent().getId());
                            }
                        }
                    }
                }
            }
            else
            {
                layers.add(uo.getDataGroupInfo());
            }
        }

        return layers;
    }

    /**
     * Launches all saved pop outs.
     */
    private void launchSaved()
    {
        if (!ourIsSavedLaunched)
        {
            ourIsSavedLaunched = true;
            Collection<PopoutModel> models = PopoutPreferences.getInstance().getModels(myToolbox);
            for (PopoutModel model : models)
            {
                LayerPopoutView view = createPanel(model);
                buildWindow(view);
                view.setVisible(true);
            }
        }
    }
}
