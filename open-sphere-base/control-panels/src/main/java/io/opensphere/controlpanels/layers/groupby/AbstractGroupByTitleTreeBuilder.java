package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupCategorizer;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public abstract class AbstractGroupByTitleTreeBuilder extends GroupByDefaultTreeBuilder
{
    /**
     * Instantiates a new group by title tree builder.
     *
     * @param tb the {@link Toolbox}
     * @param subNodesForMultiMemberGroups the sub nodes for multi member groups
     * @param activeGroupsOnly the active groups only
     */
    public AbstractGroupByTitleTreeBuilder(Toolbox tb, boolean subNodesForMultiMemberGroups, boolean activeGroupsOnly)
    {
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return new GroupCategorizer()
        {
            @Override
            public List<String> getAllCategories()
            {
                List<String> allCategories = New.list(getCategories());
                Collections.sort(allCategories, new Comparator<String>()
                {
                    @Override
                    public int compare(String cat1, String cat2)
                    {
                        return cat1.toUpperCase().compareTo(cat2.toUpperCase());
                    }
                });
                return allCategories;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dgi)
            {
                String category = dgi.getDisplayName();
                getCategories().add(category);
                return Collections.singleton(category);
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                return Collections.<String>emptySet();
            }
        };
    }
}
