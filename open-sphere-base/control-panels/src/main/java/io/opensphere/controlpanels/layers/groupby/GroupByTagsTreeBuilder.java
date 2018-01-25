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
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByTagsTreeBuilder extends GroupByDefaultTreeBuilder
{
    /** The Constant NOT_TAGGED category. */
    private static final String NOT_TAGGED = "No Tags";

    @Override
    public String getGroupByName()
    {
        return "Tag";
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
                boolean removedNotTagged = false;
                if (getCategories().contains(NOT_TAGGED))
                {
                    removedNotTagged = true;
                    allCats.remove(NOT_TAGGED);
                }
                Collections.sort(allCats, new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        return o1.toUpperCase().compareTo(o2.toUpperCase());
                    }
                });
                if (removedNotTagged)
                {
                    allCats.add(NOT_TAGGED);
                }
                return allCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dgi)
            {
                Set<String> cats = New.set();
                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    cats.addAll(getTypeCategories(dti));
                }
                getCategories().addAll(cats);
                return cats;
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                Set<String> cats = New.set();
                Set<String> tags = dti.getTags();
                if (tags != null && !tags.isEmpty())
                {
                    for (String tag : tags)
                    {
                        cats.add(tag.toUpperCase());
                    }
                }
                else
                {
                    cats.add(NOT_TAGGED);
                }
                return cats;
            }
        };
    }
}
