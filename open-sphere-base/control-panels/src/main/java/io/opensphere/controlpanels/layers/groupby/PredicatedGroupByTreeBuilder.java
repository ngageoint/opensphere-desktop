package io.opensphere.controlpanels.layers.groupby;

import java.util.Comparator;
import java.util.function.Predicate;

import io.opensphere.core.util.predicate.AndPredicate;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility.TreeOptions;
import io.opensphere.mantle.data.impl.GroupByTreeBuilder;
import io.opensphere.mantle.data.impl.GroupCategorizer;

/**
 * A group-by tree builder that filters the data groups so only the ones that
 * pass a predicate are in the tree.
 */
public class PredicatedGroupByTreeBuilder implements GroupByTreeBuilder
{
    /** The predicate. */
    private final Predicate<? super DataGroupInfo> myPredicate;

    /** The wrapped tree builder. */
    private final GroupByTreeBuilder myTreeBuilder;

    /**
     * Constructor.
     *
     * @param wrappedTreeBuilder The wrapped tree builder.
     * @param filter The data group filter.
     */
    public PredicatedGroupByTreeBuilder(GroupByTreeBuilder wrappedTreeBuilder, Predicate<? super DataGroupInfo> filter)
    {
        myTreeBuilder = wrappedTreeBuilder;
        myPredicate = filter;
    }

    @Override
    public String getGroupByName()
    {
        return myTreeBuilder.getGroupByName();
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return myTreeBuilder.getGroupCategorizer();
    }

    @Override
    public Comparator<? super DataGroupInfo> getGroupComparator()
    {
        return myTreeBuilder.getGroupComparator();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.impl.GroupByTreeBuilder#getDataCategoryFilter()
     */
    @Override
    public Predicate<DataGroupInfo> getDataCategoryFilter()
    {
        // by default, only match things that haven't been categorized:
        return g -> g.getDataCategories().isEmpty();
    }

    @Override
    public Predicate<DataGroupInfo> getGroupFilter()
    {
        return new AndPredicate<>(myTreeBuilder.getGroupFilter(), myPredicate);
    }

    @Override
    public TreeOptions getTreeOptions()
    {
        return myTreeBuilder.getTreeOptions();
    }

    @Override
    public Comparator<? super DataTypeInfo> getTypeComparator()
    {
        return myTreeBuilder.getTypeComparator();
    }

    @Override
    public void setGroupComparator(Comparator<? super DataGroupInfo> comparator)
    {
        myTreeBuilder.setGroupComparator(comparator);
    }
}
