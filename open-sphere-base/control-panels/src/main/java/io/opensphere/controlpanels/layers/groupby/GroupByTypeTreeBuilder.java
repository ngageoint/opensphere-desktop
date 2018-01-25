package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupCategorizationUtilities;
import io.opensphere.mantle.data.impl.GroupCategorizer;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByTypeTreeBuilder extends GroupByDefaultTreeBuilder
{
    @Override
    public String getGroupByName()
    {
        return "Type";
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return new GroupCategorizer()
        {
            @Override
            public List<String> getAllCategories()
            {
                Set<String> catSet = New.set(getCategories());
                List<String> allCats = New.list();
                for (GroupCategorizationUtilities.LayerType lt : GroupCategorizationUtilities.LayerType.values())
                {
                    if (catSet.contains(lt.getLabel()))
                    {
                        allCats.add(lt.getLabel());
                    }
                }
                return allCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dataGroup)
            {
                Set<String> cats = GroupCategorizationUtilities.getGroupCategories(dataGroup, isActiveGroupsOnly());
                getCategories().addAll(cats);
                return cats;
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dataType)
            {
                return Collections.<String>emptySet();
            }
        };
    }
}
