package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.ActiveGroupByTreeBuilder;
import io.opensphere.mantle.data.impl.AvailableGroupByTreeBuilder;
import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility.TreeOptions;
import io.opensphere.mantle.data.impl.GroupCategorizer;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByDefaultTreeBuilder implements ActiveGroupByTreeBuilder, AvailableGroupByTreeBuilder
{
    /** The active groups only. */
    private boolean myActiveGroupsOnly;

    /** The categories. */
    private final Set<String> myCategories = New.set();

    /** The Group comparator. */
    private Comparator<? super DataGroupInfo> myGroupComparator;

    /** The sub nodes for multi member groups. */
    private boolean mySubNodesForMultiMemberGroups;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** The Type comparator. */
    private Comparator<? super DataTypeInfo> myTypeComparator;

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    public Set<String> getCategories()
    {
        return myCategories;
    }

    /**
     * Gets the data group controller.
     *
     * @return the data group controller
     */
    public DataGroupController getDataGroupController()
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController();
    }

    @Override
    public String getGroupByName()
    {
        return "Source";
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return new GroupCategorizer()
        {
            @Override
            public List<String> getAllCategories()
            {
                List<String> allCats = New.list(myCategories);
                Collections.sort(allCats);
                return allCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dgi)
            {
                String cat = dgi.getTopParent().getDisplayName();
                myCategories.add(cat);
                return Collections.singleton(cat);
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                return Collections.<String>emptySet();
            }
        };
    }

    @Override
    public Comparator<? super DataGroupInfo> getGroupComparator()
    {
        return myGroupComparator;
    }

    @Override
    public Predicate<DataGroupInfo> getGroupFilter()
    {
        if (myActiveGroupsOnly)
        {
            return g -> g.activationProperty().isActiveOrActivating() && (g.hasMembers(false) || !g.isFlattenable())
                    && !g.isHidden();
        }
        else
        {
            return g -> g.hasMembers(false) && g.userActivationStateControl() && !g.isHidden();
        }
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public TreeOptions getTreeOptions()
    {
        return new TreeOptions(mySubNodesForMultiMemberGroups);
    }

    @Override
    public Comparator<? super DataTypeInfo> getTypeComparator()
    {
        return myTypeComparator;
    }

    @Override
    public void initializeForActive(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myActiveGroupsOnly = true;
        mySubNodesForMultiMemberGroups = true;
    }

    @Override
    public void initializeForAvailable(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Sets the group comparator.
     *
     * @param comp the new group comparator
     */
    @Override
    public void setGroupComparator(Comparator<? super DataGroupInfo> comp)
    {
        myGroupComparator = comp;
    }

    /**
     * Sets the type comparator.
     *
     * @param comp the new type comparator
     */
    public void setTypeComparator(Comparator<? super DataTypeInfo> comp)
    {
        myTypeComparator = comp;
    }

    /**
     * Gets the active groups only.
     *
     * @return the active groups only
     */
    public boolean isActiveGroupsOnly()
    {
        return myActiveGroupsOnly;
    }

    /**
     * Indicates if it shows the members of a data group.
     *
     * @return True if members of a data group are shown, false otherwise.
     */
    public boolean isSubNodesForMultiMemberGroups()
    {
        return mySubNodesForMultiMemberGroups;
    }
}
