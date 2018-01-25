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
public abstract class AbstractGroupByServerProviderTreeBuilder extends GroupByDefaultTreeBuilder
{
    /**
     * Instantiates a new group by server/provider tree builder.
     *
     * @param tb the {@link Toolbox}
     * @param subNodesForMultiMemberGroups the sub nodes for multi member groups
     * @param activeGroupsOnly the active groups only
     */
    public AbstractGroupByServerProviderTreeBuilder(Toolbox tb, boolean subNodesForMultiMemberGroups, boolean activeGroupsOnly)
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
                List<String> allCats = New.list(getCategories());
                Collections.sort(allCats, new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        return o1.toUpperCase().compareTo(o2.toUpperCase());
                    }
                });
                return allCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dgi)
            {
                String cat = dgi.getProviderType();
                getCategories().add(cat);
                return Collections.singleton(cat);
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                return Collections.<String>emptySet();
            }
        };
    }
}
