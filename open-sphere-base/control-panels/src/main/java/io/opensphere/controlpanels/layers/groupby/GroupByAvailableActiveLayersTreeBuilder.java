package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupCategorizer;

/**
 * The Class GroupByAvailableActiveLayersTreeBuilder.
 */
public class GroupByAvailableActiveLayersTreeBuilder extends GroupByDefaultTreeBuilder
{
    @Override
    public String getGroupByName()
    {
        return "Active";
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
                String category = dgi.activationProperty().isActiveOrActivating() ? "Active" : "Inactive";
                getCategories().add(category);
                return Collections.singleton(category);
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                return Collections.emptySet();
            }
        };
    }
}
