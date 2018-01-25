package io.opensphere.controlpanels.animation.view;

import java.awt.Component;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.tree.TreeSelectionModel;

import io.opensphere.controlpanels.layers.SearchAvailableLayersPanel;
import io.opensphere.controlpanels.layers.activedata.controller.AvailableDataDataLayerController;
import io.opensphere.controlpanels.layers.activedata.controller.PredicatedAvailableDataDataLayerController;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.predicate.AndPredicate;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.filter.HasPredicatedMemberPredicate;

/**
 * Layer picker dialog for held layers.
 */
public class PickLayerDialog extends OptionDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The layers panel. */
    private final SearchAvailableLayersPanel myPanel;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param parent the parent component
     * @param selectedGroupIds the selected group IDs
     */
    public PickLayerDialog(Toolbox toolbox, Component parent, final Collection<String> selectedGroupIds)
    {
        super(parent);
        setTitle("Select Layer(s) to Hold");

        AndPredicate<DataGroupInfo> groupPredicate = new AndPredicate<DataGroupInfo>(g -> g.activationProperty().isActive(),
                new HasPredicatedMemberPredicate(t -> t.getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled()));
        AvailableDataDataLayerController controller = new PredicatedAvailableDataDataLayerController(toolbox, groupPredicate);
        myPanel = new SearchAvailableLayersPanel(toolbox, controller, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION, null);
        myPanel.initGuiElements();
        if (CollectionUtilities.hasContent(selectedGroupIds))
        {
            myPanel.setSelectedDataGroups(g -> selectedGroupIds.contains(g.getId()));
        }
        setComponent(myPanel);
    }

    /**
     * Gets the selected group IDs.
     *
     * @return the selected group IDs
     */
    public Collection<String> getSelectedGroupIDs()
    {
        return myPanel.getSelectedDataGroups().stream().map(g -> g.getId()).collect(Collectors.toList());
    }
}
